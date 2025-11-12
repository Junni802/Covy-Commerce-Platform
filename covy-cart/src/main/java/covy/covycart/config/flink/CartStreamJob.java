package covy.covycart.config.flink;

import covy.covycart.config.log.ActionType;
import covy.covycart.config.log.UserActionEvent;
import covy.covycart.config.redis.RedisCartSink;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class CartStreamJob {

  public void startJob(StreamExecutionEnvironment env) throws Exception {

    // 1️⃣ KafkaSource 설정
    KafkaSource<String> source = KafkaSource.<String>builder()
        .setBootstrapServers("localhost:9092")
        .setTopics("cart-events")
        .setGroupId("flink-consumer-group")
        .setStartingOffsets(OffsetsInitializer.earliest())
        .setValueOnlyDeserializer(new SimpleStringSchema())
        .build();

    // 2️⃣ Kafka → Flink 스트림
    DataStream<String> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

    // 3️⃣ JSON 문자열 -> DTO 변환
    ObjectMapper mapper = new ObjectMapper();
    DataStream<UserActionEvent> eventStream = stream.map(json -> mapper.readValue(json, UserActionEvent.class));

    // ================================
    // 4️⃣ Redis Sink (실시간 장바구니 상태)
    // ================================
    eventStream.addSink(new RedisCartSink());

    // ================================
    // 5️⃣ PostgreSQL Sink (AI 학습용 Raw 이벤트 저장)
    // ================================
    eventStream.addSink(new SinkFunction<UserActionEvent>() {
      @Override
      public void invoke(UserActionEvent event, Context context) {
        try (Connection conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/cart_db", "username", "password")) {

          String sql = "INSERT INTO cart_events(user_id, goods_cd, action_type, timestamp) VALUES (?, ?, ?, ?)";
          try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getUserId());
            ps.setString(2, event.getGoodsCd());
            ps.setString(3, event.getActionType().name());
            ps.setLong(4, event.getTimestamp());
            ps.executeUpdate();
          }

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    // ================================
    // 6️⃣ 실시간 선호도 계산 (Window + KeyBy)
    // ================================
    KeyedStream<UserActionEvent, String> keyedStream = eventStream
        .filter(event -> event.getActionType() != ActionType.VIEW)
        .keyBy(UserActionEvent::getUserId);

    keyedStream
        .window(TumblingEventTimeWindows.of(Time.minutes(1)))
        .process(new ProcessWindowFunction<UserActionEvent, String, String, org.apache.flink.streaming.api.windowing.windows.TimeWindow>() {
          @Override
          public void process(String key,
              Context context,
              Iterable<UserActionEvent> elements,
              Collector<String> out) {

            int addCount = 0;
            for (UserActionEvent e : elements) {
              if (e.getActionType() == ActionType.ADD_TO_CATRT) addCount++;
            }

            out.collect("User " + key + " added " + addCount + " items in last minute");
          }
        })
        .print(); // 콘솔 출력. 필요하면 Redis/ElasticSearch Sink로 바꾸기

  }
}
