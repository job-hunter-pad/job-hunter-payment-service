FROM openjdk:15-jdk-alpine

ARG JAR_FILE=build/libs/*.jar

ARG STRIPE_SECRET_KEY

ENV STRIPE_KEY=$STRIPE_SECRET_KEY

COPY ${JAR_FILE} paymentservice.jar

EXPOSE 8080

ENTRYPOINT java -jar /paymentservice.jar ${STRIPE_KEY}