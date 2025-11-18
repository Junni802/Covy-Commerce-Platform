package covy.covygoods.common.excel.config;

import covy.covygoods.common.excel.service.ExcelUploadService;
import covy.covygoods.common.excel.service.ExcelUploadServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExcelConfig {

  @Bean
  public ExcelUploadService writeExcel() {
    return new ExcelUploadServiceImpl();
  }


}