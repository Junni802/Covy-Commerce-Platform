package covy.covygoods;

import covy.covygoods.batch.properties.BatchJobProperties;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(BatchJobProperties.class)
@EnableBatchProcessing
@EnableScheduling
public class CovyGoodsApplication {

  public static void main(String[] args) {
    SpringApplication.run(CovyGoodsApplication.class, args);
  }

}
