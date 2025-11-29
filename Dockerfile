FROM maven:3.9.11-amazoncorretto-21 as builder
WORKDIR /opt/app
COPY . .
RUN mvn clean install

FROM amazoncorretto:21
WORKDIR /opt/app
EXPOSE 8080
COPY --from=builder /opt/app/target/*.jar /opt/app/*.jar
ENTRYPOINT ["java", "-jar", "/opt/app/*.jar"]