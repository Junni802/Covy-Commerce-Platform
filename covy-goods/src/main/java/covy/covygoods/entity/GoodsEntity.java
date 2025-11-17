package covy.covygoods.entity;

import covy.covygoods.common.converter.BooleanToYNConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Entity
@Table(name = "covy_goods")
public class GoodsEntity implements Serializable {

  @Id
  @Column(name = "goods_cd", length = 50, nullable = false)
  @Field(type = FieldType.Keyword)
  private String goodsCd;

  @Column(name = "goods_nm", length = 255, nullable = false)
  private String goodsNm;

  @Column(name = "category", length = 100)
  private String category;

  @Column(name = "price")
  private Integer price;

  @Column(name = "del_yn", length = 1)
  @Convert(converter = BooleanToYNConverter.class)
  @ColumnDefault("'N'")
  private boolean deleted = false;

}
