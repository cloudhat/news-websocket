FROM eclipse-temurin:17-jdk AS build

WORKDIR /app
COPY . .
RUN ./gradlew clean build

FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
