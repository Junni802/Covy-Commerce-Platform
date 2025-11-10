package covy.covycart;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CovyCartApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(CovyCartApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    // Flink 스트림 환경 생성
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // KafkaSource 설정
    KafkaSource<String> source = KafkaSource.<String>builder()
        .setBootstrapServers("localhost:9092")
        .setTopics("cart-events")
        .setGroupId("flink-consumer-group")
        .setStartingOffsets(OffsetsInitializer.earliest())
        .setValueOnlyDeserializer(new SimpleStringSchema())
        .build();

    // Kafka 스트림 데이터 가져오기
    DataStream<String> stream = env.fromSource(
        source,
        WatermarkStrategy.noWatermarks(),
        "Kafka Source"
    );

    // 데이터 처리 (예: 콘솔 출력)
    stream.print();

    // Flink 실행
    env.execute("Flink Kafka Consumer Job");
  }

}
