package covy.covygoods.common.batch.config;

import covy.covygoods.entity.UserClickLog;
import covy.covygoods.entity.UserFeature;
import covy.covygoods.repository.UserFeatureRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class FeatureBatchConfig {

  private final EntityManagerFactory entityManagerFactory;
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final UserFeatureRepository userFeatureRepository;

  /** 1. Reader: MySQL에서 raw 로그 읽기 */
  @Bean
  public JpaPagingItemReader<UserClickLog> userClickLogReader() {
    JpaPagingItemReader<UserClickLog> reader = new JpaPagingItemReader<>();
    reader.setEntityManagerFactory(entityManagerFactory);
    reader.setQueryString("SELECT u FROM UserClickLog u");
    reader.setPageSize(100);
    return reader;
  }

  /** 2. Processor: raw 로그 → Feature 변환 */
  @Bean
  public ItemProcessor<UserClickLog, UserFeature> userFeatureProcessor() {
    return log -> {
      UserFeature feature = new UserFeature();
      feature.setUserId(log.getUserId());
      feature.setRecentClickedProducts(List.of(log.getProductId()));
      feature.setClickRateFashion("fashion".equals(log.getCategory()) ? 1.0 : 0.0);
      feature.setCartCount(0); // Cart 로그 없으므로 0으로 초기화
      feature.setPurchaseCount(0); // 구매 로그도 없으므로 0
      return feature;
    };
  }

  /** 3. Writer: Feature DB에 저장 */
  @Bean
  public RepositoryItemWriter<UserFeature> userFeatureWriter() {
    RepositoryItemWriter<UserFeature> writer = new RepositoryItemWriter<>();
    writer.setRepository(userFeatureRepository);
    writer.setMethodName("saveAll");
    return writer;
  }

  /** 4. Step 정의 */
  @Bean
  public Step featureStep() {
    return new StepBuilder("featureStep", jobRepository)
        .<UserClickLog, UserFeature>chunk(100, transactionManager)
        .reader(userClickLogReader())
        .processor(userFeatureProcessor())
        .writer(userFeatureWriter())
        .build();
  }

  /** 5. Job 정의 */
  @Bean
  public Job featureJob(Step featureStep) {
    return new JobBuilder("featureJob", jobRepository)
        .start(featureStep)
        .build();
  }
}