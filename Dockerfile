# ── Vaihe 1: Käännös ──
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Kopioidaan Maven wrapper ja pom.xml ensin (Docker cache)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Kopioidaan lähdekoodi ja rakennetaan JAR
COPY src/ src/
RUN ./mvnw package -DskipTests -B

# ── Vaihe 2: Ajoympäristö ──
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
