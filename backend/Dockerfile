# 后端Dockerfile - 多阶段构建
# 第一阶段：Maven构建
FROM maven:3.8-openjdk-11 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 第二阶段：运行JAR
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /app/target/library-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
