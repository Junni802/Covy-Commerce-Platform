package covy.covymarket;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class CovyMarketApplication {

  public static void main(String[] args) {
    SpringApplication.run(CovyMarketApplication.class, args);
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @LoadBalanced
  public RestTemplate getRestTemplate() {
    return new RestTemplate();
  }

  @Bean
  public Logger.Level feignLoggerLevel() {
    return Level.ALL;
  }

//  @Bean
//  public FeignErrorDecoder getFeignErrorDecoder() {
//    return new FeignErrorDecoder();
//  }

}
