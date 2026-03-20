# STAGE 1: Build the Maven Project
FROM maven:3.8.4-openjdk-17-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

# STAGE 2: Deploy to Tomcat
FROM tomcat:9.0-jdk17-openjdk-slim
RUN rm -rf /usr/local/tomcat/webapps/*
# This pulls the WAR file from the 'build' stage above
COPY --from=build /home/app/target/CWKMAVEN-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 9090
CMD ["catalina.sh", "run"]