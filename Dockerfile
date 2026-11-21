
# ==========================================
# STAGE 1: Build React + Vite Frontend
# ==========================================
FROM node:20-alpine AS frontend-build

WORKDIR /app/frontend

COPY Finora_Frontend/package*.json ./

RUN npm install

COPY Finora_Frontend/ ./

ENV VITE_API_BASE_URL=/api

RUN npm run build


# ==========================================
# STAGE 2: Build Spring Boot Backend
# ==========================================
FROM maven:3.9.9-eclipse-temurin-21 AS backend-build

WORKDIR /app/backend

COPY Finora_Backend/pom.xml ./

RUN mvn dependency:go-offline -B

COPY Finora_Backend/src ./src

RUN mvn clean package -DskipTests


# ==========================================
# STAGE 3: Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache nginx gettext

# Copy React production build
COPY --from=frontend-build /app/frontend/dist /usr/share/nginx/html

# Copy Spring Boot JAR
COPY --from=backend-build /app/backend/target/*.jar /app/app.jar

# Copy Nginx configuration template
COPY nginx.conf.template /app/nginx.conf.template

# Copy startup script
COPY start.sh /app/start.sh

RUN chmod +x /app/start.sh

EXPOSE 10000

CMD ["/app/start.sh"]
