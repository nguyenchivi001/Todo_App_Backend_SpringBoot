# ========================
# Stage 1: Build the application
# ========================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy project files
COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

# Build the application (skip tests to build faster)
RUN mvn clean package -DskipTests


# ========================
# Stage 2: Runtime image
# ========================
FROM eclipse-temurin:21-jre

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port your Spring Boot app runs on (default: 8080)
EXPOSE 8081

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
