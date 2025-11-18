package covy.covygoods.controller;

import covy.covygoods.common.elastic.document.GoodsDocument;
import covy.covygoods.common.excel.service.ExcelUploadService;
import covy.covygoods.dto.GoodsDto;
import covy.covygoods.entity.GoodsEntity;
import covy.covygoods.service.GoodsService;
import covy.covygoods.vo.ResponseCatalog;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/")
public class GoodsController {

  Environment env;
  GoodsService goodsService;
  ExcelUploadService excelUploadService;

  @Autowired
  public GoodsController(Environment env, GoodsService goodsService, ExcelUploadService excelUploadService) {
    this.env = env;
    this.goodsService = goodsService;
    this.excelUploadService = excelUploadService;
  }

  @GetMapping("/heath_check")
  public String status() {
    return String.format("It's Working in User Service On PORT %s", env.getProperty("local.server.port"));
  }

  @GetMapping("/users")
  public ResponseEntity<List<ResponseCatalog>> getUsersAll() {
    Iterable<GoodsEntity> userList = goodsService.getAllGoods();

    List<ResponseCatalog> resut = new ArrayList<>();
    userList.forEach(v -> {
      resut.add(new ModelMapper().map(v, ResponseCatalog.class));
    });

    return ResponseEntity.status(HttpStatus.OK).body(resut);
  }

  @GetMapping("/user/{goodsNm}")
  public ResponseEntity<List<ResponseCatalog>> findGoodsNm(@PathVariable(name = "goodsNm") String goodsNm,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestHeader(value = "X-User-Id", required = false) String id,
      @RequestHeader(value = "X-User-Email", required = false) String email) {

    Pageable pageable = PageRequest.of(page, size);
    Iterable<GoodsDocument> goodsList = goodsService.getgoods(goodsNm, pageable);


    String userId = id;
    String userEmail = email;
    List<ResponseCatalog> resut = new ArrayList<>();
    goodsList.forEach(v -> {
      resut.add(new ModelMapper().map(v, ResponseCatalog.class));
    });

    return ResponseEntity.status(HttpStatus.OK).body(resut);
  }

  @GetMapping("/search-goodsCd/{goodsCd}")
  public ResponseEntity<Optional<GoodsEntity>> findGoodsCd(@PathVariable(name = "goodsCd") String goodsCd) {

    Optional<GoodsEntity> goods = goodsService.getGoodsCd(goodsCd);
    return ResponseEntity.status(HttpStatus.OK).body(goods);
  }

  @GetMapping("/top/keyword")
  public ResponseEntity<List<String>> findTopKeywords() {

    return ResponseEntity.status(HttpStatus.OK).body(goodsService.getTopKeywords(10));
  }

  @PostMapping("/upload")
  public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) {

    if (file.isEmpty()) {
      return ResponseEntity.badRequest().body("엑셀 파일을 선택해주세요.");
    }

    // 파일 확장자 체크
    String filename = StringUtils.cleanPath(file.getOriginalFilename());
    if (!filename.endsWith(".xls") && !filename.endsWith(".xlsx")) {
      return ResponseEntity.badRequest().body("엑셀 파일만 업로드 가능합니다.");
    }

    try {
      // MultipartFile → 임시 파일로 변환
      File tempFile = File.createTempFile("upload-", filename);
      file.transferTo(tempFile);

      // Excel 읽기
      Iterable<GoodsEntity> goodsList = excelUploadService.readExcel(tempFile);

      goodsService.saveGoodsAll(goodsList);

      // 성공 응답
      return ResponseEntity.ok(goodsList);

    } catch (IOException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("파일 처리 중 오류가 발생했습니다.");
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(e.getMessage());
    }
  }

  /* DB를 통한 상품 검색
  @GetMapping("/user/{goodsNm}")
  public ResponseEntity<List<ResponseCatalog>> getFindGoods(@PathVariable(name = "goodsNm") String goodsNm) {
    Iterable<GoodsEntity> goodsList = goodsService.getgoods(goodsNm);

    List<ResponseCatalog> resut = new ArrayList<>();
    goodsList.forEach(v -> {
      resut.add(new ModelMapper().map(v, ResponseCatalog.class));
    });

    return ResponseEntity.status(HttpStatus.OK).body(resut);
  }*/
}