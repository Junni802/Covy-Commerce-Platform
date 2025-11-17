package covy.covygoods.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BooleanToYNConverter implements AttributeConverter<Boolean, String> {

  @Override
  public String convertToDatabaseColumn(Boolean value) {
    return (value != null && value) ? "Y" : "N";
  }

  @Override
  public Boolean convertToEntityAttribute(String value) {
    return "Y".equalsIgnoreCase(value);
  }
}
