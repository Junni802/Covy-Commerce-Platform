package covy.covycoupon.common.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
public class KafkaConsumerConfig {
  // Consumer 인스턴스를 생성하는데 필요한 설정값들을 정의

  // 컨슈머 설정 정보를 세팅하는 ConsumerFactory 빈 등록
  @Bean
  public ConsumerFactory<String, Long> consumerFactory() {
    // 컨슈머 설정 값들을 담을 Map 선언
    Map<String, Object> config = new HashMap<>();

    // 컨슈머 설정 정보 세팅
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); 				// 서버 정보
    config.put(ConsumerConfig.GROUP_ID_CONFIG, "group_1"); 								// 컨슈머 그룹 ID
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // key 역직렬화 클래스
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class); // value 역직렬화 클래스

    return new DefaultKafkaConsumerFactory<>(config);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Long> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, Long> factory = new ConcurrentKafkaListenerContainerFactory<>();

    // 앞서 세팅한 설정 값을 담는 consuemerFactory를 설정해준다.
    factory.setConsumerFactory(consumerFactory());

    return factory;
  }

}
