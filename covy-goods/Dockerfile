FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/covy-goods-1.0.jar CovyGoodsService.jar
ENTRYPOINT ["java", "-jar", "CovyGoodsService.jar"]