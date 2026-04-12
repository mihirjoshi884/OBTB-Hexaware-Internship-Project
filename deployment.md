# OBTB Deployment Guide

## 1. Summary

This file documents the complete deployment strategy for the OBTB microservices ecosystem using a low-cost stack.

- Frontend: Angular deployed on Vercel
- Backend: 8 Spring Boot microservices deployed as Docker containers on Oracle Cloud Infrastructure Always Free ARM instance
- Database: Managed Neon.tech PostgreSQL
- Broker: Managed Upstash Kafka
- Monitoring: Uptime Kuma running as a Docker container on the OCI instance
- Config: external GitHub repository via Spring Cloud Config Server
- CI/CD: GitHub Actions building ARM64 images with `docker/setup-buildx-action`, pushing to a registry, and deploying to OCI via SSH

> The external config repository `https://github.com/mihirjoshi884/config-server-repository` is the single source of truth. Spring Cloud Config Server loads runtime configuration and secrets from it.

---

## 2. Phase-by-Phase Execution

### Phase 1: Provision managed services

1. Neon.tech (PostgreSQL)
   - Create a managed PostgreSQL database instance.
   - Create a dedicated database for OBTB.
   - Create a database user with a strong password.
   - Use SSL mode `prefer` or `require`.
   - Save the following values:
     - `NEON_DB_URL`
     - `NEON_DB_USERNAME`
     - `NEON_DB_PASSWORD`

2. Upstash Kafka
   - Create a managed Upstash Kafka cluster.
   - Create the Kafka topics required by your services.
   - Save these values:
     - `UPSTASH_KAFKA_BOOTSTRAP_SERVERS`
     - `UPSTASH_KAFKA_USERNAME`
     - `UPSTASH_KAFKA_PASSWORD`

3. Vercel Frontend
   - Create a Vercel project for the Angular frontend.
   - Connect it to your GitHub repository.
   - Add environment variables for API base URLs and any auth endpoints.

### Phase 2: Launch OCI Always Free ARM instance

1. Create an Oracle Cloud Free Tier Ampere A1 instance with:
   - 4 OCPUs
   - 24 GB RAM
   - Oracle Linux 9 or Ubuntu 24.04

2. Open required ports:
   - `22` (SSH)
   - `80`, `443` (HTTP/HTTPS)
   - `8080-8090` (service access)
   - `3001` (Uptime Kuma)

3. Install on the instance:
   - Docker
   - Docker Compose v2
   - Git
   - SSH client

4. Create a deployment directory:
   - `/home/opc/obtb-deployment`

### Phase 3: Configure GitHub external config repo access

1. Create an SSH deploy key on the OCI host:
   - `ssh-keygen -t ed25519 -f ~/.ssh/config-server-deploy-key -N ""`

2. Add the deploy key to GitHub:
   - Add the public key to `config-server-repository` as a read-only deploy key.

3. Add GitHub to known hosts:
   - `ssh-keyscan github.com >> ~/.ssh/known_hosts`
   - `chmod 600 ~/.ssh/known_hosts ~/.ssh/config-server-deploy-key`

4. Configure Config Server to use SSH access:
   - `spring.cloud.config.server.git.uri=git@github.com:mihirjoshi884/config-server-repository.git`
   - `spring.cloud.config.server.git.clone-on-start=true`
   - `spring.cloud.config.server.git.force-pull=true`

### Phase 4: Launch Docker services

1. Place `docker-compose.yml` and `.env` in `/home/opc/obtb-deployment`.
2. Ensure the SSH deploy key is mounted into the Config Server container.
3. Run:
   - `docker compose up -d`
4. Validate:
   - `docker compose ps`
   - `docker compose logs config-server`
   - `curl -f http://localhost:8083/actuator/health`

---

## 3. Production Dockerfile Template

Use this ARM64-friendly, multi-stage Dockerfile template for each Spring Boot service.

```dockerfile
# Stage 1: Build
FROM maven:3.9.8-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN mvn -B dependency:go-offline -DskipTests

COPY src ./src
RUN mvn -B package -DskipTests -Pprod

# Stage 2: Runtime
FROM amazoncorretto:17-alpine
RUN addgroup -S app && adduser -S app -G app

WORKDIR /app
COPY --from=builder /workspace/target/*.jar app.jar

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=70.0 \
  -XX:+UseG1GC \
  -XX:MetaspaceSize=128m \
  -XX:MaxMetaspaceSize=256m \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=prod"

EXPOSE 8080
USER app

ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS -jar /app/app.jar"]
```

### Notes

- Use `--platform=linux/arm64` when building with Buildx.
- `amazoncorretto:17-alpine` is compatible with Ampere A1.
- The Build stage caches dependencies and produces a small runtime image.
- For any service needing more memory, use `-Xmx` explicitly in `JAVA_TOOL_OPTIONS`.

---

## 4. Docker Compose Orchestration

This `docker-compose.yml` defines all backend services plus Uptime Kuma.

```yaml
version: "3.8"

services:
  config-server:
    image: ghcr.io/${REGISTRY_OWNER}/obtb-config-server:${IMAGE_TAG}
    container_name: obtb-config-server
    ports:
      - "8083:8083"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_CLOUD_CONFIG_SERVER_GIT_URI: git@github.com:mihirjoshi884/config-server-repository.git
      SPRING_CLOUD_CONFIG_SERVER_GIT_CLONE_ON_START: "true"
      SPRING_CLOUD_CONFIG_SERVER_GIT_FORCE_PULL: "true"
      SPRING_CLOUD_CONFIG_SERVER_GIT_DEFAULT_LABEL: main
    volumes:
      - ~/.ssh/config-server-deploy-key:/root/.ssh/config-server-deploy-key:ro
      - ~/.ssh/known_hosts:/root/.ssh/known_hosts:ro
    restart: unless-stopped
    mem_limit: 1536m
    healthcheck:
      test: ["CMD-SHELL", "curl -fs http://localhost:8083/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3

  api-gateway:
    image: ghcr.io/${REGISTRY_OWNER}/obtb-api-gateway:${IMAGE_TAG}
    container_name: obtb-api-gateway
    depends_on:
      config-server:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_CONFIG_IMPORT: optional:configserver:${CONFIG_SERVER_URL:http://config-server:8083/}
      CONFIG_SERVER_URL: http://config-server:8083/
      SPRING_CLOUD_CONFIG_NAME: baseurls,Common,API-GATEWAY
      JAVA_TOOL_OPTIONS: "-Xmx1024m -Xms512m -XX:+UseG1GC"
    ports:
      - "8080:8080"
    restart: unless-stopped
    mem_limit: 1536m
    healthcheck:
      test: ["CMD-SHELL", "curl -fs http://localhost:8080/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3

  booking-service:
    image: ghcr.io/${REGISTRY_OWNER}/obtb-booking-service:${IMAGE_TAG}
    container_name: obtb-booking-service
    depends_on:
      config-server:
        condition: service_started
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_CONFIG_IMPORT: optional:configserver:${CONFIG_SERVER_URL:http://config-server:8083/}
      CONFIG_SERVER_URL: http://config-server:8083/
      SPRING_CLOUD_CONFIG_NAME: baseurls,Common,BookingService
      JAVA_TOOL_OPTIONS: "-Xmx1024m -Xms512m -XX:+UseG1GC"
    ports:
      - "8087:8087"
    restart: unless-stopped
    mem_limit: 1536m
    healthcheck:
      test: ["CMD-SHELL", "curl -fs http://localhost:8087/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3

  bus-service:
    image: ghcr.io/${REGISTRY_OWNER}/obtb-bus-service:${IMAGE_TAG}
    container_name: obtb-bus-service
    depends_on:
      config-server:
        condition: service_started
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_CONFIG_IMPORT: optional:configserver:${CONFIG_SERVER_URL:http://config-server:8083/}
      CONFIG_SERVER_URL: http://config-server:8083/
      SPRING_CLOUD_CONFIG_NAME: baseurls,Common,BusService
      JAVA_TOOL_OPTIONS: "-Xmx1024m -Xms512m -XX:+UseG1GC"
    ports:
      - "8084:8084"
    restart: unless-stopped
    mem_limit: 1536m
    healthcheck:
      test: ["CMD-SHELL", "curl -fs http://localhost:8084/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3

  notification-service:
    image: ghcr.io/${REGISTRY_OWNER}/obtb-notification-service:${IMAGE_TAG}
    container_name: obtb-notification-service
    depends_on:
      config-server:
        condition: service_started
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_CONFIG_IMPORT: optional:configserver:${CONFIG_SERVER_URL:http://config-server:8083/}
      CONFIG_SERVER_URL: http://config-server:8083/
      SPRING_CLOUD_CONFIG_NAME: baseurls,Common,NotificationService
      JAVA_TOOL_OPTIONS: "-Xmx1024m -Xms512m -XX:+UseG1GC"
    ports:
      - "8085:8085"
    restart: unless-stopped
    mem_limit: 1536m
    healthcheck:
      test: ["CMD-SHELL", "curl -fs http://localhost:8085/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3

  oauth-service:
    image: ghcr.io/${REGISTRY_OWNER}/obtb-oauth-service:${IMAGE_TAG}
    container_name: obtb-oauth-service
    depends_on:
      config-server:
        condition: service_started
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_CONFIG_IMPORT: optional:configserver:${CONFIG_SERVER_URL:http://config-server:8083/}
      CONFIG_SERVER_URL: http://config-server:8083/
      SPRING_CLOUD_CONFIG_NAME: baseurls,Common,AuthService
      JAVA_TOOL_OPTIONS: "-Xmx1024m -Xms512m -XX:+UseG1GC"
    ports:
      - "8081:8081"
    restart: unless-stopped
    mem_limit: 1536m
    healthcheck:
      test: ["CMD-SHELL", "curl -fs http://localhost:8081/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3

  transaction-service:
    image: ghcr.io/${REGISTRY_OWNER}/obtb-transaction-service:${IMAGE_TAG}
    container_name: obtb-transaction-service
    depends_on:
      config-server:
        condition: service_started
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_CONFIG_IMPORT: optional:configserver:${CONFIG_SERVER_URL:http://config-server:8083/}
      CONFIG_SERVER_URL: http://config-server:8083/
      SPRING_CLOUD_CONFIG_NAME: baseurls,Common,TransactionService
      JAVA_TOOL_OPTIONS: "-Xmx1024m -Xms512m -XX:+UseG1GC"
    ports:
      - "8086:8086"
    restart: unless-stopped
    mem_limit: 1536m
    healthcheck:
      test: ["CMD-SHELL", "curl -fs http://localhost:8086/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3

  user-service:
    image: ghcr.io/${REGISTRY_OWNER}/obtb-user-service:${IMAGE_TAG}
    container_name: obtb-user-service
    depends_on:
      config-server:
        condition: service_started
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_CONFIG_IMPORT: optional:configserver:${CONFIG_SERVER_URL:http://config-server:8083/}
      CONFIG_SERVER_URL: http://config-server:8083/
      SPRING_CLOUD_CONFIG_NAME: baseurls,Common,UserService
      JAVA_TOOL_OPTIONS: "-Xmx1024m -Xms512m -XX:+UseG1GC"
    ports:
      - "8082:8082"
    restart: unless-stopped
    mem_limit: 1536m
    healthcheck:
      test: ["CMD-SHELL", "curl -fs http://localhost:8082/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3

  ai-service:
    image: ghcr.io/${REGISTRY_OWNER}/obtb-ai-service:${IMAGE_TAG}
    container_name: obtb-ai-service
    depends_on:
      config-server:
        condition: service_started
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_CONFIG_IMPORT: optional:configserver:${CONFIG_SERVER_URL:http://config-server:8083/}
      CONFIG_SERVER_URL: http://config-server:8083/
      SPRING_CLOUD_CONFIG_NAME: baseurls,Common,ai_service
      JAVA_TOOL_OPTIONS: "-Xmx1024m -Xms512m -XX:+UseG1GC"
    ports:
      - "8090:8090"
    restart: unless-stopped
    mem_limit: 1536m
    healthcheck:
      test: ["CMD-SHELL", "curl -fs http://localhost:8090/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3

  uptime-kuma:
    image: louislam/uptime-kuma:1
    container_name: uptime-kuma
    ports:
      - "3001:3001"
    volumes:
      - ./uptime-kuma:/app/data
    restart: unless-stopped
    mem_limit: 256m
    healthcheck:
      test: ["CMD-SHELL", "curl -fs http://localhost:3001 || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3

networks:
  default:
    name: obtb-network
```

### Notes

- `ghcr.io/${REGISTRY_OWNER}` images are built by CI.
- `config-server` is required because all backends import config from it.
- Use the same config import pattern in each service.
- `mem_limit` keeps the 24 GB host stable.

---

## 5. CI/CD Pipeline

### GitHub Actions workflow

```yaml
name: OBTB ARM64 Build and Deploy

on:
  push:
    branches: [main]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup QEMU
        uses: docker/setup-qemu-action@v3

      - name: Setup Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Login to GitHub Container Registry
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GHCR_PAT }}

      - name: Build and push ARM64 images
        run: |
          services=(
            api-gateway
            booking-service
            bus-service
            notification-service
            oauth-service
            transaction-service
            user-service
            ai-service
            config-server
          )
          for service in "${services[@]}"; do
            docker buildx build \
              --platform linux/arm64 \
              --tag ghcr.io/${{ github.repository_owner }}/obtb-${service}:${{ github.sha }} \
              --tag ghcr.io/${{ github.repository_owner }}/obtb-${service}:latest \
              --push \
              -f backend/OBTB-HEXAWARE/${service}/Dockerfile .
          done

  deploy:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to OCI instance
        uses: appleboy/ssh-action@v0.1.8
        with:
          host: ${{ secrets.OCI_SSH_HOST }}
          username: ${{ secrets.OCI_SSH_USER }}
          key: ${{ secrets.OCI_SSH_PRIVATE_KEY }}
          port: ${{ secrets.OCI_SSH_PORT }}
          script: |
            cd /home/opc/obtb-deployment
            echo "${{ secrets.GHCR_PAT }}" | docker login ghcr.io -u "${{ secrets.GHCR_USERNAME }}" --password-stdin
            docker compose pull
            docker compose up -d --remove-orphans
            docker image prune -f
            docker system prune -f
```

### Notes

- Builds are cross-compiled for `linux/arm64`.
- Image tags include both the commit SHA and `latest`.
- The deploy job logs into GHCR on the remote host and updates the compose services.
- The remote host must already have Docker Compose files and SSH access configured.

---

## 6. External Configuration Management

### Config Server as the single source of truth

All backend services must load configuration from Spring Cloud Config Server, not from embedded properties files.

Each backend service should include these settings in its local `application.properties`:

```properties
spring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}
spring.config.import=optional:configserver:${CONFIG_SERVER_URL:http://localhost:8083/}
spring.cloud.config.name=baseurls,Common,<ServiceName>
```

### Private GitHub repo access via SSH deploy key

- Use an SSH deploy key for `config-server` to access `git@github.com:mihirjoshi884/config-server-repository.git`.
- Mount the key into the container as `/root/.ssh/config-server-deploy-key`.
- Use `known_hosts` for strict host verification.

### Runtime config reload strategy

Enable refresh support in all services and Config Server:

```properties
management.endpoints.web.exposure.include=health,info,refresh
management.endpoint.health.show-details=always
```

After config updates, refresh a service manually:

```bash
curl -X POST http://localhost:8081/actuator/refresh
```

For automation, you can add a webhook or orchestration step to call `/actuator/refresh` after repo updates.

### Profile naming conventions

Follow the config repo naming pattern:

- `baseurls-prod.properties`
- `Common-prod.properties`
- `AuthService-prod.properties`
- `BookingService-prod.properties`
- `BusService-prod.properties`
- `NotificationService-prod.properties`
- `TransactionService-prod.properties`
- `UserService-prod.properties`
- `ai_service-prod.properties`

Secrets and environment-specific values should be kept in the external repo profiles.

---

## 7. Environment Variables and Secrets

### GitHub Secrets required

- `GHCR_PAT`
- `GHCR_USERNAME`
- `OCI_SSH_HOST`
- `OCI_SSH_PORT`
- `OCI_SSH_USER`
- `OCI_SSH_PRIVATE_KEY`

### `.env` values for localhost / compose

```env
REGISTRY_OWNER=mihirjoshi884
IMAGE_TAG=latest
CONFIG_SERVER_URL=http://config-server:8083/
SPRING_PROFILES_ACTIVE=prod
```

### Config Server runtime variables

- `SPRING_CLOUD_CONFIG_SERVER_GIT_URI=git@github.com:mihirjoshi884/config-server-repository.git`
- `SPRING_CLOUD_CONFIG_SERVER_GIT_CLONE_ON_START=true`
- `SPRING_CLOUD_CONFIG_SERVER_GIT_FORCE_PULL=true`

### Service runtime variables

For each backend service:

- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_CONFIG_IMPORT=optional:configserver:${CONFIG_SERVER_URL:http://config-server:8083/}`
- `CONFIG_SERVER_URL=http://config-server:8083/`
- `SPRING_CLOUD_CONFIG_NAME=baseurls,Common,<ServiceName>`
- `JAVA_TOOL_OPTIONS="-Xmx1024m -Xms512m -XX:+UseG1GC"`

---

## 8. JVM and Memory Optimization

### Host memory budget

On the 24 GB host, allocate conservatively:

- 1.5 GB per backend service
- 1.5 GB for Config Server
- 256 MB for Uptime Kuma
- Remainder for OS and Docker overhead

### Recommended JVM settings

Use container-aware JVM tuning:

```bash
JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=70.0 \
  -XX:+UseG1GC \
  -XX:MetaspaceSize=128m \
  -XX:MaxMetaspaceSize=256m \
  -Djava.security.egd=file:/dev/./urandom"
```

For production services, also set:

```bash
-Xms512m -Xmx1024m
```

This preserves headroom while keeping each JVM efficient.

---

## 9. Deployment Checklist

1. Provision Neon PostgreSQL and Upstash Kafka.
2. Provision the OCI Ampere A1 instance.
3. Install Docker, Docker Compose, Git, and SSH utilities.
4. Create the SSH deploy key and add it to the GitHub config repo deploy keys.
5. Configure Config Server to use SSH access to the private repo.
6. Add `docker-compose.yml` and `.env` to `/home/opc/obtb-deployment`.
7. Build and push ARM64 images via GitHub Actions.
8. Deploy the compose stack on the OCI instance.
9. Validate health endpoints and config refresh functionality.
10. Configure Vercel frontend with the correct backend API URLs.

---

## 10. Security Notes

- Keep the GitHub config repo private.
- Use SSH deploy key access rather than HTTPS PAT for Config Server.
- Do not store secrets in public repo branches.
- Keep secrets in profile files within the private config repo.
- Use strict SSH host verification for GitHub.

---

## 11. Service Mapping

Deploy these services through Docker Compose:

- `config-server`
- `api-gateway`
- `booking-service`
- `bus-service`
- `notification-service`
- `oauth-service`
- `transaction-service`
- `user-service`
- `ai-service`
- `uptime-kuma`

---

## 12. Final Notes

This guide is tailored to your requirement that the external repo is the single source of truth and that runtime configuration is loaded through Spring Cloud Config Server. The architecture supports ARM64 on Oracle Cloud with a production-grade Docker build strategy, runtime config refresh, and automated deployment via GitHub Actions.
