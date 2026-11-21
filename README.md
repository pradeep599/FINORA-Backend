# FINORA – AI-Powered Personal Finance & Expense Intelligence Platform

## Overview

Finora is an AI-powered personal finance management platform designed to help users manage their income, expenses, accounts, and monthly budgets efficiently.

The backend is developed using **Java 21 and Spring Boot 3.4**. It provides secure REST APIs, transaction management, financial analytics, AI-powered expense categorization, intelligent budget recommendations, and personalized financial insights.

It uses Spring Security with JWT authentication, BCrypt password encryption, Spring Data JPA, and TiDB Cloud Serverless for persistent data storage.

## Key Features

- **User Authentication:** Secure registration, login, JWT authentication, and social login integration.
- **Password Management:** Forgot-password and reset-password functionality with email verification codes.
- **Account Management:** Manage bank accounts, credit accounts, wallets, and cash accounts.
- **Transaction Management:** Create, retrieve, and delete income and expense transactions.
- **Automatic Balance Updates:** Transactional balance synchronization to maintain accurate account balances.
- **Financial Analytics:** Calculate net balance, monthly income, monthly expenses, savings rate, and cashflow trends.
- **AI Expense Categorization:** Automatically categorize expenses using merchant descriptions.
- **Spending Pattern Analysis:** Identify weekend spending spikes, category concentration, and potential savings.
- **Smart Budget Recommendations:** Generate personalized monthly budgets using historical spending and 50/30/20 principles.
- **Financial Health Score:** Generate a financial score from 0–100 with actionable recommendations.
- **CSV Reports:** Export transaction history as CSV.
- **Community Feedback:** Submit and retrieve user feedback and reviews.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot 3.4 | Backend framework |
| Spring Security | Application security |
| Spring Data JPA | Database operations |
| Hibernate | ORM |
| MySQL / TiDB Cloud | Relational database |
| JWT | Authentication |
| BCrypt | Password encryption |
| Google Gemini API | AI-powered intelligence |
| Spring Mail | Email notifications |
| Maven | Dependency and build management |
| Docker | Containerization |
| Render | Cloud deployment |

## Project Architecture

The backend follows a layered architecture.

```text
Finora_Backend/
│
├── src/main/java/com/finpulse/
│   │
│   ├── controller/
│   │   ├── AuthController
│   │   ├── FinanceController
│   │   └── AiController
│   │
│   ├── service/
│   │
│   ├── service/impl/
│   │   ├── AuthServiceImpl
│   │   ├── FinanceServiceImpl
│   │   └── AiServiceImpl
│   │
│   ├── repository/
│   │
│   ├── entity/
│   │
│   ├── dto/
│   │
│   ├── security/
│   │
│   └── exception/
│
├── src/main/resources/
│   └── application.properties
│
├── pom.xml
└── README.md
```

*The above represents the documented package architecture; individual class files may vary.*

## Database Design

Finora uses six relational entities.

| Table | Description |
|---|---|
| users | Stores user information and authentication details |
| accounts | Stores bank, credit, wallet, and cash accounts |
| categories | Stores income and expense categories |
| transactions | Stores financial transactions |
| budgets | Stores monthly category budgets |
| feedbacks | Stores community feedback and reviews |

Hibernate automatically manages the database schema using the configured JPA settings.

## REST API Endpoints

### Authentication APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Authenticate user |
| POST | `/api/auth/oauth` | Google / Apple authentication |
| POST | `/api/auth/forgot-password` | Generate password reset code |
| POST | `/api/auth/reset-password` | Reset account password |

### Profile APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/profile` | Retrieve user profile |
| PUT | `/api/profile` | Update user profile |

### Account APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/accounts` | Retrieve accounts |
| POST | `/api/accounts` | Create account |
| DELETE | `/api/accounts` | Delete account |

### Transaction APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/transactions` | Retrieve transactions |
| POST | `/api/transactions` | Create transaction |
| DELETE | `/api/transactions` | Delete transaction |

### Budget APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/budgets` | Retrieve budgets |
| POST | `/api/budgets` | Create budget |
| DELETE | `/api/budgets` | Delete budget |

### Analytics APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/analytics/dashboard` | Retrieve financial dashboard analytics |

### AI APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/ai/categorize` | AI expense categorization |
| GET | `/api/ai/spending-patterns` | Analyze spending behavior |
| GET | `/api/ai/budget-recommendations` | Generate smart budgets |
| GET | `/api/ai/monthly-summary` | Generate financial health summary |

### Reports and Feedback

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/reports/transactions/csv` | Download transaction report |
| GET | `/api/feedback` | Retrieve feedback |
| POST | `/api/feedback` | Submit feedback |
| GET | `/api/health` | Check backend health |

**Authentication:** All protected endpoints require a valid JWT token in the Authorization header.

```http
Authorization: Bearer YOUR_JWT_TOKEN
```

## Installation and Setup

### Prerequisites

- Java JDK 21
- Apache Maven
- MySQL or TiDB Cloud account
- Eclipse IDE or IntelliJ IDEA
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/pradeep599/FINORA-Backend.git
```

### 2. Navigate to Backend Directory

```bash
cd FINORA-Backend
```

### 3. Configure Database

Update `src/main/resources/application.properties` with your database configuration.

Example:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/finora}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:your_password}

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

Configure the required JWT secret and optional AI and SMTP credentials through environment variables.

### 4. Run the Application

Using Maven:

```bash
mvn clean install
mvn spring-boot:run
```

Or run the main Spring Boot application class from Eclipse.

### 5. Verify Backend

```text
http://localhost:8080/api/health
```

## AI Intelligence Engine

Finora uses a hybrid AI architecture.

**Google Gemini Integration**

When a valid Gemini API key is configured, the application can use the Gemini 2.0 Flash model for AI-powered financial intelligence.

**Deterministic Smart Engine**

When Gemini credentials are unavailable, the application automatically uses its built-in rule-based and statistical intelligence engine over database records.

This supports:

- Expense categorization.
- Spending pattern detection.
- Budget recommendations.
- Financial health analysis.
- Personalized financial insights.

## Security

- Stateless JWT authentication.
- BCrypt password hashing.
- Spring Security authorization.
- Protected REST API endpoints.
- Secure password reset functionality.
- Environment-based configuration for sensitive credentials.
- TLS-secured TiDB Cloud database connections.

## Cloud Deployment

The backend is designed to work with a combined Docker deployment on Render.

The production architecture includes:

- Java 21 runtime.
- Spring Boot REST API.
- Nginx reverse proxy.
- TiDB Cloud Serverless database.
- Environment-based production configuration.

The backend runs internally on port `8080`, while Nginx handles incoming requests on Render's assigned port.

### Important Environment Variables

| Variable | Description |
|---|---|
| `DB_URL` | TiDB Cloud JDBC connection URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | JWT signing secret |
| `GEMINI_API_KEY` | Optional Gemini API key |
| `GEMINI_MODEL` | Gemini model name |
| `MAIL_HOST` | SMTP server |
| `MAIL_PORT` | SMTP port |
| `MAIL_USERNAME` | Email account |
| `MAIL_PASSWORD` | Gmail application password |

Never commit database credentials, JWT secrets, or API keys to GitHub.

## Future Enhancements

- Advanced financial forecasting.
- More personalized financial recommendations.
- Additional financial institution integrations.
- Advanced fraud and anomaly detection.
- Enhanced financial reporting.

## Author

**Finora Development Team**

## License

This project is developed for educational and hackathon purposes.
