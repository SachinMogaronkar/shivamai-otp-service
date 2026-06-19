# =========================================
# STAGE 1 — BUILD APPLICATION
# =========================================

FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# =========================================
# COPY MAVEN FILES
# =========================================

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# =========================================
# DOWNLOAD DEPENDENCIES
# =========================================

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# =========================================
# COPY SOURCE CODE
# =========================================

COPY src ./src

# =========================================
# BUILD APPLICATION
# =========================================

RUN ./mvnw clean package -DskipTests

# =========================================
# STAGE 2 — RUNTIME IMAGE
# =========================================

FROM eclipse-temurin:17-jre

WORKDIR /app

# =========================================
# COPY BUILT JAR
# =========================================

COPY --from=build /app/target/shivamai-otp-service-0.0.1-SNAPSHOT.jar app.jar

# =========================================
# EXPOSE APPLICATION PORT
# =========================================

EXPOSE 8080

# =========================================
# START APPLICATION
# =========================================

ENTRYPOINT ["java", "-jar", "app.jar"]