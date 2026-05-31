FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/aquaculture-system-1.0.0.jar app.jar
COPY ../application.yml application.yml
RUN mkdir -p /app/data/export
ENV TZ=Asia/Shanghai
EXPOSE 8080
ENTRYPOINT [" java\, \-jar\, \app.jar\]
