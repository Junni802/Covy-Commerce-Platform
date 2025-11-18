package covy.covygoods.common.messagequeue.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import covy.covygoods.repository.GoodsRepository;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumer {

  private final GoodsRepository repository;
  private final ObjectMapper mapper = new ObjectMapper();

  @Autowired
  public KafkaConsumer(GoodsRepository repository) {
    this.repository = repository;
  }

  @KafkaListener(topics = "example-catalog-topic")
  public void updateGoods(String kafkaMessage) {
    log.info("Kafka Message: {}", kafkaMessage);

    try {
      // JSON -> Map 변환
      Map<String, Object> map = mapper.readValue(kafkaMessage, new TypeReference<>() {});

      String goodsCd = (String) map.get("goodsCd");
      if (goodsCd == null) {
        log.warn("Kafka 메시지에 goodsCd 없음, 건너뜀");
        return;
      }

      repository.findByGoodsCd(goodsCd).ifPresent(entity -> {
        // DB 구조 기준으로 업데이트 가능한 필드만 처리
        String goodsNm = (String) map.get("goodsNm");
        String category = (String) map.get("category");
        Number priceNumber = (Number) map.get("price");
        Boolean delYn = map.get("delYn") != null ? Boolean.parseBoolean(map.get("delYn").toString()) : false;

        if (goodsNm != null) entity.setGoodsNm(goodsNm);
        if (category != null) entity.setCategory(category);
        if (priceNumber != null) entity.setPrice(priceNumber.intValue());
        entity.setDeleted(delYn); // boolean 필드

        repository.save(entity);
        log.info("상품 {} 정보 업데이트 완료", goodsCd);
      });

    } catch (JsonProcessingException e) {
      log.error("Kafka 메시지 파싱 실패: {}", e.getMessage(), e);
    } catch (Exception e) {
      log.error("Kafka 처리 중 오류 발생: {}", e.getMessage(), e);
    }
  }
}