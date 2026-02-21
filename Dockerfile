#
#FROM eclipse-temurin:21-jdk-alpine
#
#WORKDIR /app
#COPY target/*.jar app.jar
#ENTRYPOINT ["java","-jar","/app/app.jar"]
# Stage 1: Build the JAR
FROM maven:3.9.3-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
# Tutaj kopiujemy JAR z etapu build
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]