# 1. Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Runtime stage
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/ad-performance-aggregator-1.0-SNAPSHOT.jar ./app.jar

ENTRYPOINT ["java", "-cp", "app.jar", "com.presentation.Main"]