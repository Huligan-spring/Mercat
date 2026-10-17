# Paso 1: Compilar la aplicación usando Maven y Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Paso 2: Crear la imagen final de ejecución
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
#COPY --from=build /app/target/mercat-api-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-Xmx350m", "-Xms256m", "-jar", "app.jar"]