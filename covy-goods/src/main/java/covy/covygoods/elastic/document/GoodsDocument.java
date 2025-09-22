package covy.covygoods.elastic.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "goods") // 인덱스명
public class GoodsDocument {

  @Id
  private String id;

  private String goodsCd;
  private String goodsNm;
  private Integer stock;
  private Integer unitPrice;
  private String createdAt;
}