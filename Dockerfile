FROM openjdk:15-jdk-alpine

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} paymentservice.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/paymentservice.jar"]