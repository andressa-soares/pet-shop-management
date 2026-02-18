# syntax=docker/dockerfile:1.7

# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copia só o que muda menos primeiro (melhor cache)
COPY pom.xml ./

# Cache do repositório Maven (muito mais rápido em rebuild)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -DskipTests package

# Runtime stage (menor que debian/jre normal)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Cria usuário sem privilégios
RUN addgroup -S app && adduser -S app -G app
USER app

# Copia o jar gerado
COPY --from=builder /app/target/*.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
