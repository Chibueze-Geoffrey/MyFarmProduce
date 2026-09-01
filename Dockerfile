# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

# Copy POMs first so dependency resolution is cached across source-only changes.
COPY pom.xml .
COPY common/pom.xml common/
COPY domain/pom.xml domain/
COPY application/pom.xml application/
COPY infrastructure/pom.xml infrastructure/
COPY web/pom.xml web/
RUN mvn -B -q -pl web -am dependency:go-offline

COPY common/src common/src
COPY domain/src domain/src
COPY application/src application/src
COPY infrastructure/src infrastructure/src
COPY web/src web/src

RUN mvn -B -pl web -am package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre AS final
WORKDIR /app
COPY --from=build /src/web/target/app.jar app.jar

ENV SPRING_PROFILES_ACTIVE=production
EXPOSE 8080

# Render (and most PaaS hosts) inject PORT at container start; Spring reads it via
# server.port=${PORT:8080} in application.properties, so a plain `java -jar` works
# without any shell-form entrypoint trickery.
ENTRYPOINT ["java", "-jar", "app.jar"]
