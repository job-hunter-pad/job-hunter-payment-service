FROM openjdk:15-jdk-alpine

RUN apk add wget

RUN wget -qO- "https://github.com/stripe/stripe-cli/releases/download/v1.5.14/stripe_1.5.14_linux_x86_64.tar.gz" | tar -xz

ARG STRIPE_SECRET_KEY
ARG WEB_HOOK

ENV STRIPE_KEY=$STRIPE_SECRET_KEY
ENV WEB_HOOK_KEY=$WEB_HOOK
ENV STRIPE_API_KEY=$STRIPE_SECRET_KEY

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} paymentservice.jar

COPY scripts/run.sh run.sh

RUN ["chmod", "+x", "./run.sh"]

EXPOSE 8080

CMD ./run.sh
