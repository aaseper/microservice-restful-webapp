##############################################
# WindService Multi-Stage Build with Caching #
##############################################

# Maven is used to build the WindService and WindService-Interface, and the image Maven version should be 3.9.6
FROM maven:3.9.6-eclipse-temurin-21 as builder
# Build the WindService-Interface with Maven, which is a dependency of the WindService
WORKDIR /windservice-interface
COPY /windservice-interface/src /windservice-interface/src
COPY /windservice-interface/pom.xml /windservice-interface/
RUN mvn clean install -DskipTests
# Build the WindService with Maven
WORKDIR /windservice
COPY /windservice/src /windservice/src
COPY /windservice/pom.xml /windservice/
RUN mvn -B package -DskipTests

# Copy the WindService JAR file to a the final image, with the lightest Alpine OpenJDK 21 base image available
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /usr/src/app/
COPY --from=builder /windservice/target/*.jar /usr/src/app/
EXPOSE 9090
CMD [ "java", "-jar", "windservice-0.2.0.jar" ]