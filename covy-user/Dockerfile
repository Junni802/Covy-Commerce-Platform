FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/covy-user-1.0.jar CovyUser.jar
ENTRYPOINT ["java", "-jar", "CovyUser.jar"]