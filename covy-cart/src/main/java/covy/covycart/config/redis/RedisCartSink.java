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
    jedis = new Jedis("localhost", 6379);
  }

  @Override
  public void invoke(UserActionEvent value, Context context) {
    if (jedis != null) {
      String key = "user:" + value.getUserId() + ":cart";
      String field = value.getGoodsCd();
      jedis.hincrBy(key, field, 1);
    }
  }

  @Override
  public void close() throws Exception {
    if (jedis != null) jedis.close();
    super.close();
  }
}