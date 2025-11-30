FROM openjdk:11.0.11-jre-slim
VOLUME /tmp
COPY build/libs/covy-cart-1.0.jar CovyCartService.jar
ENTRYPOINT ["java", "-jar", "CovyCartService.jar"]