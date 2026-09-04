FROM gradle:8.14-jdk21 AS build
WORKDIR /app

COPY build.gradle settings.gradle /app/

RUN gradle dependencies --no-daemon

COPY src /app/src

RUN gradle build --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine-3.20
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]