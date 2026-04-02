# Online Bus Ticket Booking System (OBTB)

A modern, scalable microservices-based application for booking bus tickets online. Built with Spring Boot on the backend and Angular on the frontend, this system demonstrates enterprise-level architecture patterns and best practices.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Backend Services](#backend-services)
- [Frontend](#frontend)
- [Installation & Setup](#installation--setup)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Technologies Used](#technologies-used)
- [Project Structure in Detail](#project-structure-in-detail)

---

## 🎯 Overview

The Online Bus Ticket Booking (OBTB) System is a comprehensive platform that allows users to:
- Register and manage user accounts
- Browse available buses and routes
- Book and manage tickets
- Process payments and transactions
- Receive notifications for bookings and updates
- Secure authentication via OAuth2

This is a **microservices architecture** with independent, scalable services communicating through an API Gateway with centralized configuration management.

---

## 🏗️ Architecture

The system follows a **microservices architecture pattern** with the following key components:

```
┌─────────────────────────────────────────────────────────────┐
│                    Angular Frontend                          │
│              (Port: Development / 80 Production)             │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                   API Gateway (Port: 9090)                   │
│        Routes requests to appropriate microservices          │
└────────────────────────┬────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┬──────────────└──────┐
        │                │                │                     │
┌───────▼──────┐ ┌──────▼──────┐ ┌───────▼──────┐ ┌────────────▼───┐
│   Config     │ │   OAuth     │ │    User      │ │   Notification │
│   Server     │ │   Service   │ │   Service    │ │   Service      │
│ (Port: 8083) │ │ (Port: 8081)│ │ (Port: 8082) │ │  (Port: 8084)  │
└──────────────┘ └─────────────┘ └──────────────┘ └────────────────┘
        ▲                            
        │                            
    ┌───┴──────────────────────────────────────────────────────┬──────┐
    │                                                           │      │
┌───▼──────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────▼────┐
│     Bus      │ │   Booking    │ │ Transaction  │ │ (Future Svcs) │
│   Service    │ │   Service    │ │  Service     │ │               │
└──────────────┘ └──────────────┘ └──────────────┘ └────────────────┘
```

---

## 📁 Project Structure

```
hexaware-project/
├── backend/
│   └── OBTB-HEXAWARE/                    # Main Spring Boot Parent Project
│       ├── CONFIG-SERVER/                # Centralized Configuration Server
│       ├── API-GATEWAY/                  # API Gateway (Spring Cloud Gateway)
│       ├── OauthService/                 # OAuth2 Authentication Service
│       ├── USER-SERVICE/                 # User Management Service
│       ├── BusService/                   # Bus & Route Management Service
│       ├── BookingService/               # Ticket Booking Service
│       ├── TransactionService/           # Payment & Transaction Service
│       ├── NotificationService/          # Email/SMS Notification Service
│       ├── docker-compose.yaml           # Docker compose configuration
│       └── pom.xml                       # Parent Maven POM
├── frontend/
│   └── obtb-app/                         # Angular 21 Frontend Application
│       ├── src/                          # Source code
│       ├── package.json                  # NPM dependencies
│       └── angular.json                  # Angular configuration
├── obtb-certs/                           # Security certificates for SSL/TLS
│   ├── service.cert                      # Service certificate
│   └── [Keystore/Truststore files]
├── scripts/
│   ├── startup.sh                        # Start all services
│   ├── stop.sh                           # Stop all services
│   └── save.sh                           # Backup script
└── README.md                             # This file

```

---

## 🔧 Prerequisites

### System Requirements
- **Java**: JDK 17 or higher
- **Node.js**: v18.x or higher
- **npm**: v9.x or higher
- **Docker**: Latest version (for containerized deployment)
- **Maven**: 3.8.x or higher (included as mvnw wrapper)

### Verification
```bash
# Check Java version
java -version

# Check Node.js and npm
node --version
npm --version

# Check Maven (using wrapper)
./mvnw --version
```

---

## 🚀 Backend Services

| Service | Port | Purpose | Technology |
|---------|------|---------|-----------|
| **Config-Server** | 8083 | Centralized configuration management | Spring Cloud Config |
| **OAuth Service** | 8081 | OAuth2 authentication & authorization | Spring Security OAuth2 |
| **API Gateway** | 9090 | Request routing & API composition | Spring Cloud Gateway |
| **User Service** | 8082 | User registration, profiles, management | Spring Data JPA |
| **Bus Service** | - | Bus routes, schedules, availability | Spring Boot, JPA |
| **Booking Service** | - | Ticket booking logic, seat management | Spring Boot, JPA |
| **Transaction Service** | - | Payment processing, transactions | Spring Boot, JPA |
| **Notification Service** | 8084 | Email/SMS notifications | Spring Boot, Kafka |

### Service Descriptions

**Config-Server**
- Centralized configuration server
- All microservices fetch their configuration from this server
- Profiles: dev, stage, prod

**OAuth Service**
- Provides OAuth2 authentication
- Manages user login and token generation
- Integrates with User Service for user validation

**API Gateway**
- Entry point for all client requests
- Routes requests to appropriate services
- Handles cross-cutting concerns (CORS, authentication, rate limiting)
- Centralized security layer

**User Service**
- User registration and profile management
- User information persistence
- Role-based access control (RBAC)

**Bus Service**
- Bus fleet management
- Route management and scheduling
- Real-time seat availability

**Booking Service**
- Handles ticket booking logic
- Seat selection and reservation
- Booking history and management

**Transaction Service**
- Payment processing
- Transaction logging
- Refund management

**Notification Service**
- Email notifications
- SMS notifications (optional)
- Kafka integration for event-driven notifications

---

## 🎨 Frontend

**Technology Stack:**
- **Framework**: Angular 21.1.0
- **Styling**: Tailwind CSS 4.0.0
- **HTTP Client**: RxJS with Angular HttpClient
- **Authentication**: angular-oauth2-oidc
- **PDF Viewer**: ng2-pdf-viewer
- **Notifications**: ngx-toastr, SweetAlert2

**Location**: `/frontend/obtb-app`

**Key Features:**
- Responsive design with Tailwind CSS
- OAuth2 integration for secure authentication
- PDF ticket generation and viewing
- Real-time notifications
- Reactive forms and state management
- Type-safe development with TypeScript 5.9

---

## 📦 Installation & Setup

### 1. Clone the Repository
```bash
cd /Users/mihirjoshi/Desktop/internship-project/hexaware-project
```

### 2. Backend Setup

#### Option A: Using Docker Compose (Recommended)
```bash
cd backend/OBTB-HEXAWARE

# Build all services and start with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

#### Option B: Local Maven Build
```bash
cd backend/OBTB-HEXAWARE

# Build all modules
./mvnw clean install -DskipTests

# Build specific service
./mvnw clean install -pl <SERVICE_NAME> -DskipTests
```

### 3. Frontend Setup
```bash
cd frontend/obtb-app

# Install dependencies
npm install

# Clear cache (if needed)
npm run clear-cache

# Update dependencies
npm update
```

---

## 🏃 Running the Application

### Option 1: Using Startup Script (Recommended for Local Development)
```bash
cd scripts

# Make script executable
chmod +x startup.sh

# Run the startup script (starts services in order)
./startup.sh

# Logs will be saved in logs/startup_logs/ directory
```

### Option 2: Docker Compose (For Containerized Deployment)
```bash
cd backend/OBTB-HEXAWARE

# Start all services
docker-compose up -d

# Verify services are running
docker-compose ps

# View logs for specific service
docker-compose logs <service-name>

# Stop all services
docker-compose down
```

### Option 3: Manual Local Startup (Individual Services)

**Terminal 1 - Config Server:**
```bash
cd backend/OBTB-HEXAWARE/Config-Server
../mvnw spring-boot:run -Dspring-boot.run.mainClass="org.hexaware.configserver.ConfigServerApplication"
```

**Terminal 2 - OAuth Service:**
```bash
cd backend/OBTB-HEXAWARE/OauthService
../mvnw spring-boot:run -Dspring-boot.run.mainClass="org.hexaware.oauthservice.OauthServiceApplication"
```

**Terminal 3 - API Gateway:**
```bash
cd backend/OBTB-HEXAWARE/API-GATEWAY
../mvnw spring-boot:run -Dspring-boot.run.mainClass="org.hexaware.apigateway.ApiGatewayApplication"
```

**Terminal 4 - User Service:**
```bash
cd backend/OBTB-HEXAWARE/USER-SERVICE
../mvnw spring-boot:run -Dspring-boot.run.mainClass="org.hexaware.userservice.UserServiceApplication"
```

**Terminal 5 - Other Services:**
Repeat for BusService, BookingService, TransactionService, NotificationService

**Terminal 6 - Frontend:**
```bash
cd frontend/obtb-app
npm start

# Application will be available at http://localhost:4200
```

### Option 4: Simple Manual Build and Run
```bash
# Build entire project
./mvnw clean install -pl . -DskipTests

# Start services individually in separate terminals (as shown above)
```

---

## 🔌 API Endpoints

### API Gateway (Port: 9090)

All requests go through the API Gateway which routes to respective services:

```
GET    /gateway/users/**           → User Service
POST   /gateway/auth/**            → OAuth Service
POST   /gateway/buses/**           → Bus Service
POST   /gateway/bookings/**        → Booking Service
POST   /gateway/transactions/**    → Transaction Service
POST   /gateway/notifications/**   → Notification Service
GET    /gateway/config/**          → Config Server
```

### Service-Specific Endpoints (For Direct Access)

| Service | Base URL | Example Routes |
|---------|----------|----------------|
| Config Server | http://localhost:8083 | `/actuator/health` |
| OAuth Service | http://localhost:8081 | `/oauth/token`, `/oauth/validate` |
| User Service | http://localhost:8082 | `/api/users`, `/api/users/{id}` |
| Notification Service | http://localhost:8084 | `/api/notifications/email` |

---

## 💻 Technologies Used

### Backend
- **Framework**: Spring Boot 4.0.0
- **JDK**: Java 17
- **Build Tool**: Apache Maven 3.8.x
- **Database**: (Configured in application properties)
- **Authentication**: OAuth2 / Spring Security
- **Message Queue**: Apache Kafka
- **Configuration**: Spring Cloud Config
- **API Gateway**: Spring Cloud Gateway
- **ORM**: Spring Data JPA/Hibernate
- **Actuator**: Spring Boot Actuator (health checks)

### Frontend
- **Framework**: Angular 21.1.0
- **Language**: TypeScript 5.9.2
- **Styling**: Tailwind CSS 4.0.0
- **HTTP Client**: RxJS 7.8.2
- **Package Manager**: npm
- **Build Tool**: Angular CLI 21.2.0
- **Testing**: Vitest 4.0.18
- **Build Bundler**: Rollup/Webpack (via Angular Build)

### DevOps & Deployment
- **Containerization**: Docker
- **Orchestration**: Docker Compose
- **Security**: SSL/TLS certificates (in obtb-certs/)

---

## 📊 Project Structure in Detail

### Backend Module Structure

Each backend service follows this structure:
```
SERVICE_NAME/
├── src/
│   ├── main/
│   │   ├── java/org/hexaware/[servicename]/
│   │   │   ├── Application.java          # Entry point
│   │   │   ├── config/                   # Configuration classes
│   │   │   ├── controller/               # REST controllers
│   │   │   ├── service/                  # Business logic
│   │   │   ├── repository/               # Data access layer
│   │   │   ├── entity/                   # JPA entities
│   │   │   ├── dto/                      # Data transfer objects
│   │   │   ├── exception/                # Exception handling
│   │   │   └── util/                     # Utility classes
│   │   └── resources/
│   │       ├── application.properties    # Default config
│   │       ├── application-dev.properties
│   │       ├── application-stage.properties
│   │       └── application-prod.properties
│   └── test/
│       └── java/                         # Unit tests
├── pom.xml                               # Maven configuration
├── mvnw / mvnw.cmd                       # Maven wrapper
├── Dockerfile                            # Docker image config
└── target/                               # Compiled output

```

### Frontend Structure

```
obtb-app/
├── src/
│   ├── app/
│   │   ├── auth/                         # Authentication module
│   │   ├── components/                   # Reusable components
│   │   ├── pages/                        # Page components
│   │   ├── services/                     # HTTP services
│   │   ├── models/                       # TypeScript interfaces
│   │   ├── interceptors/                 # HTTP interceptors
│   │   └── app.component.ts              # Root component
│   ├── environments/                     # Environment configs
│   ├── styles.css                        # Global styles
│   ├── main.ts                           # Entry point
│   ├── index.html                        # HTML template
│   └── silent-refresh.html               # OAuth silent refresh
├── package.json                          # NPM configuration
├── angular.json                          # Angular CLI config
├── tsconfig.json                         # TypeScript config
├── tailwind.config.js                    # Tailwind CSS config
├── postcss.config.js                     # PostCSS config
└── public/                               # Static assets

```

---

## 📝 Configuration

### Backend Configuration Files

Each service has environment-specific configuration files:

- **application.properties** - Default configuration
- **application-dev.properties** - Development environment
- **application-stage.properties** - Staging environment  
- **application-prod.properties** - Production environment

### Docker Compose Configuration

The `docker-compose.yaml` file includes:
- Service definitions with ports and environment variables
- Network configuration (obtb-network)
- Volume mounts for SSL certificates
- Health checks for service readiness
- Dependency management (service startup order)

---

## 🧪 Testing

### Backend Tests
```bash
# Run all tests
./mvnw test

# Run tests for specific module
./mvnw test -pl <MODULE_NAME>

# Skip tests during build
./mvnw install -DskipTests
```

### Frontend Tests
```bash
cd frontend/obtb-app

# Run unit tests
npm test

# Run tests in watch mode
npm test -- --watch
```

---

## 🐛 Troubleshooting

### Common Issues

**Issue: Services fail to start**
- Check if required ports (8081-8084, 9090, 4200) are available
- Verify Java 17+ is installed: `java -version`

**Issue: Config Server connection error**
- Ensure Config-Server starts first
- Check network connectivity: `docker-compose logs config-service`

**Issue: SSL Certificate errors**
- Verify certificates exist in `obtb-certs/`
- Check Kafka SSL configuration in environment variables

**Issue: Frontend build errors**
- Clear cache: `npm run clear-cache`
- Reinstall dependencies: `rm -rf node_modules && npm install`

---

## 📞 Support & Documentation

### References
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [Spring Cloud Config](https://docs.spring.io/spring-cloud-config/)
- [Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/)
- [Angular Documentation](https://angular.io/docs)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)

### Logs & Debugging

**Backend Logs:**
```bash
# Docker logs
docker-compose logs <service-name> -f

# Local startup logs
cat logs/startup_logs/<service-name>.log
```

**Frontend Logs:**
- Open browser DevTools (F12)
- Check Console tab for errors
- Check Network tab for API call failures

---

## 📄 License

This project is part of the Hexaware internship program.

---

## 👥 Contributors

This is a collaborative internship project under the Hexaware Technologies internship program.

---

## 🔐 Security Notes

- OAuth2 is configured for authentication
- SSL/TLS certificates are in `obtb-certs/`
- All services communicate through the API Gateway
- Never commit sensitive credentials to version control
- Use environment variables for production configurations

---

**Last Updated**: April 2026
**Project Status**: In Development
