./stripe listen --skip-verify --forward-to localhost:8080/webhook &
java -jar /paymentservice.jar
