# ----------------------------------------------------
# Etapa 1: Build da aplicação (Compilação)
# ----------------------------------------------------
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copia arquivos de configuração e do wrapper do Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Copia o código fonte e gera o pacote .jar (pulando testes)
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ----------------------------------------------------
# Etapa 2: Execução da aplicação (Imagem leve)
# ----------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Usuário não-root para segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia o .jar gerado na Etapa 1
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]