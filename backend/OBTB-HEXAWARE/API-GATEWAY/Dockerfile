# --- STAGE 1: Build Stage ---
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy only what's needed for a build
COPY pom.xml .
COPY src ./src

# Build with maven available in the builder image
RUN mvn -B clean package -DskipTests

# --- STAGE 2: Runtime Stage ---
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copies the compiled jar (using a wildcard so you don't have to change the artifact name every time)
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]