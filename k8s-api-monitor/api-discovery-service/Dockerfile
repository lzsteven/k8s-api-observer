FROM openjdk:11-jre-slim

WORKDIR /app

# 添加应用JAR包
COPY target/api-discovery-service-1.0.0.jar app.jar

# 设置时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

# 设置环境变量
ENV JAVA_OPTS=""

# 暴露端口
EXPOSE 8081

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]