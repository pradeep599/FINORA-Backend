# Stage 1: Build React + Vite Frontend
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY Finora_Frontend/package.json ./
RUN npm install
COPY Finora_Frontend/ ./
ENV VITE_API_BASE_URL=/api
RUN npm run build

# Stage 2: Build Spring Boot Backend
FROM maven:3.9.9-eclipse-temurin-21 AS backend-build
WORKDIR /app/backend
COPY Finora_Backend/pom.xml ./
RUN mvn dependency:go-offline -B
COPY Finora_Backend/src ./src
RUN mvn clean package -DskipTests

# Stage 3: Runtime (Java 21 + Nginx)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache nginx

COPY --from=frontend-build /app/frontend/dist /usr/share/nginx/html
COPY --from=backend-build /app/backend/target/*.jar /app/app.jar

RUN printf '#!/bin/sh\n\
PORT=${PORT:-10000}\n\
cat <<EOF > /etc/nginx/http.d/default.conf\n\
server {\n\
    listen ${PORT};\n\
    root /usr/share/nginx/html;\n\
    index index.html;\n\
    location /api/ {\n\
        proxy_pass http://127.0.0.1:8080/api/;\n\
        proxy_set_header Host \$host;\n\
        proxy_set_header X-Real-IP \$remote_addr;\n\
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;\n\
        proxy_set_header X-Forwarded-Proto \$scheme;\n\
    }\n\
    location / {\n\
        try_files \$uri \$uri/ /index.html;\n\
    }\n\
}\n\
EOF\n\
java -Dserver.port=8080 -jar /app/app.jar &\n\
nginx -g "daemon off;"\n' > /app/start.sh && chmod +x /app/start.sh

EXPOSE 10000
CMD ["/app/start.sh"]