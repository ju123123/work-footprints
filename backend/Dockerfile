FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY maven-settings.xml /app/maven-settings.xml
COPY pom.xml /app/pom.xml
RUN mvn -s /app/maven-settings.xml -q -DskipTests dependency:go-offline
COPY src /app/src
RUN mvn -s /app/maven-settings.xml -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/workfootprint-backend-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

