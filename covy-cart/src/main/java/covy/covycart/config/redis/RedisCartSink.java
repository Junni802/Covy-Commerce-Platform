package covy.covycart.config.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import covy.covycart.config.log.UserActionEvent;
import java.util.HashMap;
import java.util.Map;
import redis.clients.jedis.Jedis;

import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.configuration.Configuration;

public class RedisCartSink extends RichSinkFunction<UserActionEvent> {

  private transient Jedis jedis;

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

    String actionLogKey = "user:" + event.getUserId() + ":actions";

    Map<String, Object> logData = new HashMap<>();
    logData.put("goodsId", event.getGoodsCd());
    logData.put("action", event.getActionType().name());
    logData.put("timestamp", System.currentTimeMillis());

// ObjectMapper로 JSON 변환
    String jsonLog = new ObjectMapper().writeValueAsString(logData);

// 최근 행동부터 LPUSH (시간 순)
    jedis.lpush(actionLogKey, jsonLog);

// 로그가 너무 길어지지 않게 최근 100개까지만 유지
    jedis.ltrim(actionLogKey, 0, 99);

    System.out.println("rkqt dkds s");
    String key = "user:" + event.getUserId() + ":cart";
    String field = event.getGoodsCd();

    switch (event.getActionType()) {
      case ADD_TO_CATRT:
        // 이미 존재하면 +1, 없으면 새로 추가
        jedis.hincrBy(key, field, 1);
        break;

      case REMOVE_FROM_CART:
        jedis.hdel(key, field);
        break;

      case CLEAR_CART:
        jedis.del(key);
        break;
    }
  }

  @Override
  public void close() throws Exception {
    if (jedis != null) jedis.close();
    super.close();
  }
}