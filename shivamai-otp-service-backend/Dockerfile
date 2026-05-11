# =========================================
# STAGE 1 — BUILD APPLICATION
# =========================================

FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom first for dependency caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# =========================================
# STAGE 2 — RUNTIME IMAGE
# =========================================

FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/shivamai-otp-service-0.0.1-SNAPSHOT.jar app.jar

# Expose application port
EXPOSE 8080

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]