package covy.covycart.config.flink;

import covy.covycart.config.log.UserActionEvent;
//import covy.covycart.config.redis.RedisCartSink;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.consumer.ConsumerConfig;

public class CartStreamJob1 {

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
        .setBootstrapServers("localhost:9092")
        .setTopics("cart-events")
        .setGroupId("cart-flink-group-v2")
        .setValueOnlyDeserializer(new SimpleStringSchema())
        .setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
        .build();

    DataStream<String> source = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "kafka-source");
    source.print();
    env.execute();
  }
}
