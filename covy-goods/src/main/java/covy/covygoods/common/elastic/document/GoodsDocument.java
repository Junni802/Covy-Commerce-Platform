package covy.covygoods.common.elastic.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "goods")
public class GoodsDocument {
  @Id
  private String goodsCd;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String goodsNm;

  private String category;
  private Integer price;
  private boolean deleted;
}