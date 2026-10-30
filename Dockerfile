# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy dependency configuration first
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Ensure the Maven Wrapper is executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code and build the executable JAR
COPY src/ src/

RUN ./mvnw clean package -DskipTests


# Stage 2: Run the application
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

# Run the application as a non-root user
RUN useradd --system --uid 10001 appuser

# Copy only the executable JAR from the build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Switch to the non-root user
USER 10001

# Document the application port
EXPOSE 8080

# Start Spring Boot
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
