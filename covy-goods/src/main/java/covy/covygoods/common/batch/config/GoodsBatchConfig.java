package covy.covygoods.common.batch.config;

import covy.covygoods.common.elastic.document.GoodsDocument;
import covy.covygoods.entity.GoodsEntity;
import covy.covygoods.repository.GoodsSearchRepository;
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

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class GoodsBatchConfig {

  private final EntityManagerFactory entityManagerFactory;
  private final GoodsSearchRepository goodsSearchRepository;
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  /** 1. Reader (DB → GoodsEntity) */
  @Bean
  public JpaPagingItemReader<GoodsEntity> goodsReader() {
    JpaPagingItemReader<GoodsEntity> reader = new JpaPagingItemReader<>();
    reader.setEntityManagerFactory(entityManagerFactory);
    reader.setQueryString("SELECT g FROM GoodsEntity g");
    reader.setPageSize(100);
    return reader;
  }

  /** 2. Processor (Entity → Document 변환) */
  @Bean
  public ItemProcessor<GoodsEntity, GoodsDocument> goodsProcessor() {
    return goods -> {
      GoodsDocument doc = new GoodsDocument();
      doc.setGoodsCd(goods.getGoodsCd()); // Elasticsearch의 PK
      doc.setGoodsNm(goods.getGoodsNm());
      doc.setCategory(goods.getCategory());
      doc.setPrice(goods.getPrice());
      doc.setDeleted(goods.isDeleted());
      return doc;
    };
  }

  /** 3. Writer (Elasticsearch 저장) */
  @Bean
  public RepositoryItemWriter<GoodsDocument> goodsWriter() {
    RepositoryItemWriter<GoodsDocument> writer = new RepositoryItemWriter<>();
    writer.setRepository(goodsSearchRepository);
    writer.setMethodName("save");
    return writer;
  }

  /** 4. Step */
  @Bean
  public Step goodsStep() {
    return new StepBuilder("goodsStep", jobRepository)
        .<GoodsEntity, GoodsDocument>chunk(100, transactionManager)
        .reader(goodsReader())
        .processor(goodsProcessor())
        .writer(goodsWriter())
        .build();
  }

  /** 5. Job */
  @Bean
  public Job goodsJob(Step goodsStep) {
    return new JobBuilder("goodsJob", jobRepository)
        .start(goodsStep)
        .build();
  }
}
