FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Копируем ВСЕ POM файлы из родительского проекта
COPY pom.xml .
COPY ewm-stats-dto/pom.xml ewm-stats-dto/
COPY ewm-stats-client/pom.xml ewm-stats-client/
COPY ewm-stats-service/pom.xml ewm-stats-service/
COPY ewm-main-service/pom.xml ewm-main-service/

# Загружаем зависимости
RUN mvn dependency:go-offline -B

# Копируем исходный код только для нужных модулей
COPY ewm-stats-dto ewm-stats-dto/
COPY ewm-stats-client ewm-stats-client/
COPY ewm-stats-service ewm-stats-service/

# Собираем stats-service
RUN mvn clean package -pl ewm-stats-service -am -DskipTests

FROM eclipse-temurin:21-jre-jammy

RUN addgroup --system --gid 1000 appuser && \
    adduser --system --uid 1000 --gid 1000 appuser

WORKDIR /app

COPY --from=builder /app/ewm-stats-service/target/*.jar app.jar

RUN chown -R appuser:appuser /app && \
    chmod 755 /app

USER appuser

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]