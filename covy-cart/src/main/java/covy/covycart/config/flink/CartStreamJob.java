package covy.covycart.config.flink;

import covy.covycart.config.log.UserActionEvent;
import covy.covycart.config.redis.RedisCartSink;
//import covy.covycart.config.elastic.ElasticsearchCartSink;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.consumer.ConsumerConfig;

public class CartStreamJob {

  public static void main(String[] args) throws Exception {

    // 1️⃣ Flink 실행 환경 설정
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // 2️⃣ KafkaSource 생성 (새 API)
    KafkaSource<String> source = KafkaSource.<String>builder()
        .setBootstrapServers("localhost:9092")
        .setTopics("cart-events")
        .setGroupId("flink-consumer-group")
        .setStartingOffsets(OffsetsInitializer.earliest())
        .setValueOnlyDeserializer(new SimpleStringSchema())
        .build();

    // 3️⃣ Kafka → Flink 데이터 스트림
    System.out.println("값이 들어오나요");
    DataStream<String> stream = env.fromSource(
        source,
        WatermarkStrategy.noWatermarks(),
        "Kafka Source"
    );

    // 4️⃣ JSON 문자열 → UserActionEvent 변환
    ObjectMapper mapper = new ObjectMapper();
    DataStream<UserActionEvent> eventStream = stream.map(json -> {
      System.out.println("📩 Received raw JSON: " + json);
      return mapper.readValue(json, UserActionEvent.class);
    });

    // 5️⃣ Redis Sink & Elasticsearch Sink 추가
    eventStream.print();
    eventStream.addSink(new RedisCartSink());
//    eventStream.addSink(new ElasticsearchCartSink());

    // 6️⃣ 실행
    env.execute("Cart Stream Processing Job (KafkaSource)");
  }
}
