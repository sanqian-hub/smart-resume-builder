# 阶段1：构建阶段
FROM maven:3.5-jdk-8-alpine AS builder
WORKDIR /app

# 先拷贝 pom.xml 并下载依赖（利用缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 再拷贝源码并打包
COPY src ./src
# mv 对target目录下的jar包重命名为app.jar
RUN mvn package -DskipTests && mv target/*.jar target/app.jar

# 阶段2：运行阶段（轻量 JRE）
FROM openjdk:8-jre-alpine
WORKDIR /app

# 从构建阶段拷贝 JAR 包
COPY --from=builder /app/target/app.jar app.jar

# 暴露端口（和 SpringBoot 配置的 server.port 一致）
EXPOSE 8080

# 启动命令
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]