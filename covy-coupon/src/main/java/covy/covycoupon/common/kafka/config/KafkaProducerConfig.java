package covy.covycoupon.common.kafka.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.protocol.types.Field.Str;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {
  @Bean
  public ProducerFactory<String, Long> producerFactory() {
    // ProducerFactory<String, Long> : key는 Sring, value는 Long으로 지정한다.
    // 설정 값을 담아줄 map을 변수로 선언한다.
    Map<String, Object> config = new HashMap<>();

    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

    // 메시지 키로 “user123” 과 같은 문자열을 사용한다.
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

    // 메시지 값으로 1L, 2L, 3L과 같은 Long 타입을 사용한다.
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LongSerializer.class);

    return new DefaultKafkaProducerFactory<>(config);
  }

  @Bean
  public KafkaTemplate<String, Long> kafkaTemplate() {
    // 카프카 템플릿을 작성할때 위에서 작성한 설정값을 담아준다.
    // 프로듀서(Producer)는 이 카프카 템플릿을 사용해서 토픽에 메시지를 발행한다.
    return new KafkaTemplate<>(producerFactory());
  }
}