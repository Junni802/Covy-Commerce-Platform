package covy.covycart.config.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import covy.covycart.config.log.UserActionEvent;
import java.util.HashMap;
import java.util.Map;
import redis.clients.jedis.Jedis;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.configuration.Configuration;

/**
 * Flink Sink Function to persist UserActionEvent to Redis.
 * <p>
 * - LPUSH: 최근 행동 로그 저장 (최대 100개)
 * - Hash: 사용자 장바구니 상태 저장
 */
public class RedisCartSink extends RichSinkFunction<UserActionEvent> {

  private transient Jedis jedis;
  private static final int MAX_LOG_SIZE = 100;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);
    try {
      jedis = new Jedis("127.0.0.1", 6379);
      System.out.println("Redis ping: " + jedis.ping());
    } catch (Exception e) {
      System.err.println("Redis 연결 실패: " + e.getMessage());
      throw e;
    }
  }

  @Override
  public void invoke(UserActionEvent event, Context context) throws JsonProcessingException {

    // --- 최근 사용자 행동 로그 ---
    String actionLogKey = "user:" + event.getUserId() + ":actions";
    Map<String, Object> logData = new HashMap<>();
    logData.put("goodsId", event.getGoodsCd());
    logData.put("action", event.getActionType().name());
    logData.put("timestamp", System.currentTimeMillis());

    String jsonLog = OBJECT_MAPPER.writeValueAsString(logData);
    jedis.lpush(actionLogKey, jsonLog);
    jedis.ltrim(actionLogKey, 0, MAX_LOG_SIZE - 1);

    // --- 장바구니 상태 업데이트 ---
    String cartKey = "user:" + event.getUserId() + ":cart";
    String field = event.getGoodsCd();

    switch (event.getActionType()) {
      case ADD_TO_CATRT:
        // 이미 존재하면 +1, 없으면 새로 추가
        jedis.hincrBy(cartKey, field, 1);
        break;

      case REMOVE_FROM_CART:
        jedis.hdel(cartKey, field);
        break;

      case CLEAR_CART:
        jedis.del(cartKey);
        break;

      default:
        // 예외 처리: 정의되지 않은 ActionType
        System.err.println("Unknown ActionType: " + event.getActionType());
    }
  }

  @Override
  public void close() throws Exception {
    if (jedis != null) {
      jedis.close();
    }
    super.close();
  }
}
