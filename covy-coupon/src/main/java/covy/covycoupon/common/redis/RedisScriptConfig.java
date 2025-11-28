package covy.covycoupon.common.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisScriptConfig {

  @Bean(name = "limitedCouponScript")
  public RedisScript limitedCouponScript() {
    DefaultRedisScript script = new DefaultRedisScript();
    script.setLocation(new ClassPathResource("limitedCoupon.lua"));
    script.setResultType(Long.class);
    return script;
  }

}
