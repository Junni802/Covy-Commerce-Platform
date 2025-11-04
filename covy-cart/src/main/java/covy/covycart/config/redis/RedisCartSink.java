package covy.covycart.config.redis;

import covy.covycart.config.log.UserActionEvent;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
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
  public void invoke(UserActionEvent event, Context context) {
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