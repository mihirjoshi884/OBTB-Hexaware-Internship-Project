# --- STAGE 1: Build Stage ---
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Copy pom, wrapper files, and source
COPY pom.xml .
COPY .mvn ./.mvn
COPY mvnw .
COPY src ./src

# Ensure the wrapper script has execution permissions
RUN chmod +x mvnw

# Run using the wrapper instead of global 'mvn'
RUN ./mvnw clean package -DskipTests
# --- STAGE 2: Runtime Stage ---
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copies the compiled jar (using a wildcard so you don't have to change the artifact name every time)
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]