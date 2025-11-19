package covy.covygoods.common.elastic.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Elasticsearch에서 상품 정보를 저장하는 Document 클래스
 * - indexName: goods
 * - 상품 코드(goodsCd)를 ID로 사용
 * - 상품명(goodsNm)은 검색을 위해 Text 타입, standard analyzer 적용
 * - 카테고리, 가격, 삭제 여부 저장
 */
@Data
@Document(indexName = "goods")
public class GoodsDocument {

  /** 상품 코드 (Primary Key, Elasticsearch Document ID) */
  @Id
  private String goodsCd;

  /** 상품명 (검색을 위해 Text 타입, analyzer 적용) */
  @Field(type = FieldType.Text, analyzer = "standard")
  private String goodsNm;

  /** 상품 카테고리 (예: 가공식품>라면류>봉지라면) */
  private String category;

  /** 상품 가격 */
  private Integer price;

  /** 삭제 여부 (true: 삭제, false: 정상) */
  private boolean deleted;
}
