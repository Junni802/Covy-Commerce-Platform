package covy.covycart;

import covy.covycart.config.flink.CartStreamJob;
import javax.sql.DataSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "covy.covycart.feign") // FeignClient 패키지 명 맞춰주기
public class CovyCartApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(CovyCartApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    // Flink 환경 생성
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // Flink Job 실행
    CartStreamJob job = new CartStreamJob();
    job.startJob(env);

    // Flink 실행
    env.execute("Cart Stream Processing Job (Spring Boot Integration)");
  }
}
