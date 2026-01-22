# -------------------------------------------------------------------------
# Stage 1: Build the Application
# -------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# 1. Copy only the build configuration files first
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .



RUN chmod +x mvnw
# 2. Download dependencies (This layer is cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# 3. Now copy the source code and build
COPY src ./src
RUN ./mvnw package -DskipTests

# -------------------------------------------------------------------------
# Stage 2: Create the Final Runtime Image
# -------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Non-root user for better security (Recommended)
RUN addgroup --system spring && adduser --system spring -ingroup spring
USER spring:spring

# Copy the built JAR from the build stage
# Replace 'user-service.jar' with the appropriate name for each service
COPY --from=build /app/target/*.jar app.jar

EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]