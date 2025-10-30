package covy.covycart.config.flink;

import covy.covycart.config.log.UserActionEvent;
import covy.covycart.config.redis.RedisCartSink;
import covy.covycart.config.elastic.ElasticsearchCartSink;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import java.util.Properties;

public class CartStreamJob {

  public static void main(String[] args) throws Exception {
    // 1️⃣ Flink 실행 환경 설정
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // 2️⃣ Kafka 설정
    Properties properties = new Properties();
    properties.setProperty("bootstrap.servers", "localhost:9092");
    properties.setProperty("group.id", "cart-flink-group");

    // 3️⃣ Kafka Consumer 생성
    FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>(
        "cart-events",
        new SimpleStringSchema(),
        properties
    );

    // 4️⃣ Kafka → Flink 데이터 스트림 생성
    DataStream<String> stream = env.addSource(consumer);

    // 5️⃣ JSON 문자열 → UserActionEvent 변환
    ObjectMapper mapper = new ObjectMapper();
    DataStream<UserActionEvent> eventStream = stream.map(json -> mapper.readValue(json, UserActionEvent.class));

    // 6️⃣ Redis와 Elasticsearch Sink 연결
    eventStream.addSink(new RedisCartSink());
    eventStream.addSink(new ElasticsearchCartSink());

    // 7️⃣ 실행
    env.execute("Cart Stream Processing Job");
  }
}