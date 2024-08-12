##############################################
# Planner Multi-Stage Build with Caching #
##############################################

# Stage 1: Build the Planner with Maven (the image Maven version should be 3.9.6)
FROM maven:3.9.6-eclipse-temurin-21 as planner_builder
# Build the WindService-Interface with Maven (because Planner depends on it)
WORKDIR /windservice-interface
COPY /windservice-interface/src /windservice-interface/src
COPY /windservice-interface/pom.xml /windservice-interface/
RUN mvn clean install -DskipTests
# Build the Planner with Maven
WORKDIR /planner
COPY /planner/src /planner/src
COPY /planner/pom.xml /planner/
RUN mvn -B package -DskipTests

# Stage 2: Create a minimal Docker image with the Planner
FROM eclipse-temurin:21
WORKDIR /usr/src/app/
RUN curl -LJO \
  https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh \
  && chmod +x /usr/src/app/wait-for-it.sh
COPY --from=planner_builder /planner/target/*.jar /usr/src/app/
EXPOSE 8008 9090 5672 15672
CMD [ "java", "-jar", "planner-0.2.0.jar" ]
