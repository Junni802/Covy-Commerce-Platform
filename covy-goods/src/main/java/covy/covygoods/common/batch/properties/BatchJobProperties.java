package covy.covygoods.common.batch.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "batch")
public class BatchJobProperties {
  private Map<String, String> jobs;

  @PostConstruct
  public void init() {
    System.out.println("Loaded batch.jobs: " + jobs);
  }
}