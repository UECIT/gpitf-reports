FROM maven:3-jdk-11 as deps
WORKDIR /app

COPY pom.xml .
RUN mvn -B -Dmaven.repo.local=/app/.m2 dependency:go-offline

FROM deps as build

COPY src src
RUN mvn -B -Dmaven.repo.local=/app/.m2 package

FROM openjdk:11-jre-slim
WORKDIR /app
VOLUME /tmp
COPY start-gpitf-reports-service.sh /app
RUN chmod +x start-gpitf-reports-service.sh
ENTRYPOINT [ "/app/start-gpitf-reports-service.sh" ]
EXPOSE 8086

COPY --from=build /app/target/gpitf-reports-service.war /app