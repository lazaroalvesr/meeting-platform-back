
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia os arquivos de dependência primeiro para aproveitar o cache do Docker
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Baixa as dependências (ajuda a acelerar os builds futuros)
RUN ./mvnw dependency:go-offline

# Copia o código fonte e faz o build do pacote (.jar) sem rodar os testes
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ----------------------------------------------------
# Etapa 2: Execução da aplicação (Imagem leve)
# ----------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Cria um usuário não-root por questões de segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia apenas o arquivo .jar gerado na Etapa 1
COPY --from=builder /app/target/*.jar app.jar

# Expõe a porta padrão que sua API Java escuta (ajuste se usar outra porta)
EXPOSE 8080

# Define a variável de ambiente para produção
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Comando para rodar a aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]