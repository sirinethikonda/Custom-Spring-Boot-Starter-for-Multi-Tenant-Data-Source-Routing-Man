# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the parent pom and all modules
COPY pom.xml .
COPY multitenancy-spring-boot-starter ./multitenancy-spring-boot-starter
COPY demo-application ./demo-application

# Build the entire project from the root
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# The jar will be in the demo-application/target folder
COPY --from=build /app/demo-application/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
