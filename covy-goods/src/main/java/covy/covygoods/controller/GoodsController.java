package covy.covygoods.controller;

import covy.covygoods.common.elastic.document.GoodsDocument;
import covy.covygoods.common.excel.service.ExcelUploadService;
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

/**
 * GoodsController
 * ----------------
 * 상품 조회, 검색, 업로드 관련 API를 제공하는 컨트롤러
 * Excel 업로드 및 Elasticsearch, DB 조회 기능 포함
 */
@RestController
@RequestMapping("/")
public class GoodsController {

  private final Environment env;
  private final GoodsService goodsService;
  private final ExcelUploadService excelUploadService;
  private final ModelMapper modelMapper;

  @Autowired
  public GoodsController(Environment env, GoodsService goodsService, ExcelUploadService excelUploadService) {
    this.env = env;
    this.goodsService = goodsService;
    this.excelUploadService = excelUploadService;
    this.modelMapper = new ModelMapper();
  }

  /**
   * 헬스 체크 API
   * ----------------
   * 서버 상태 및 포트 정보 반환
   *
   * @return 서버 상태 문자열
   */
  @GetMapping("/heath_check")
  public String status() {
    return String.format("It's Working in User Service On PORT %s", env.getProperty("local.server.port"));
  }

  /**
   * 모든 상품 조회
   * ----------------
   * DB에 저장된 모든 상품 정보를 ResponseCatalog DTO 리스트로 반환
   *
   * @return ResponseEntity<List<ResponseCatalog>> - 모든 상품 리스트
   */
  @GetMapping("/users")
  public ResponseEntity<List<ResponseCatalog>> getUsersAll() {
    Iterable<GoodsEntity> goodsList = goodsService.getAllGoods();
    List<ResponseCatalog> result = new ArrayList<>();
    goodsList.forEach(v -> result.add(modelMapper.map(v, ResponseCatalog.class)));
    return ResponseEntity.status(HttpStatus.OK).body(result);
  }

  /**
   * 상품명 검색
   * ----------------
   * Elasticsearch 기반 상품 검색
   *
   * @param goodsNm 검색할 상품명
   * @param page 페이지 번호 (기본 0)
   * @param size 페이지 크기 (기본 20)
   * @param id 요청 헤더 X-User-Id (선택)
   * @param email 요청 헤더 X-User-Email (선택)
   * @return ResponseEntity<List<ResponseCatalog>> - 검색된 상품 리스트
   */
  @GetMapping("/user/{goodsNm}")
  public ResponseEntity<List<ResponseCatalog>> findGoodsNm(
      @PathVariable(name = "goodsNm") String goodsNm,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestHeader(value = "X-User-Id", required = false) String id,
      @RequestHeader(value = "X-User-Email", required = false) String email) {

    Pageable pageable = PageRequest.of(page, size);
    Iterable<GoodsDocument> goodsList = goodsService.getgoods(goodsNm, pageable);

    List<ResponseCatalog> result = new ArrayList<>();
    goodsList.forEach(v -> result.add(modelMapper.map(v, ResponseCatalog.class)));

    return ResponseEntity.status(HttpStatus.OK).body(result);
  }

  /**
   * 상품 코드로 단일 상품 조회
   * ----------------
   *
   * @param goodsCd 검색할 상품 코드
   * @return ResponseEntity<Optional<GoodsEntity>> - 단일 상품 정보
   */
  @GetMapping("/search-goodsCd/{goodsCd}")
  public ResponseEntity<Optional<GoodsEntity>> findGoodsCd(@PathVariable(name = "goodsCd") String goodsCd) {
    Optional<GoodsEntity> goods = goodsService.getGoodsCd(goodsCd);
    return ResponseEntity.status(HttpStatus.OK).body(goods);
  }

  /**
   * 상위 N 키워드 조회
   * ----------------
   *
   * @return ResponseEntity<List<String>> - 인기 검색 키워드 리스트
   */
  @GetMapping("/top/keyword")
  public ResponseEntity<List<String>> findTopKeywords() {
    return ResponseEntity.status(HttpStatus.OK).body(goodsService.getTopKeywords(10));
  }

  /**
   * 엑셀 업로드 후 상품 등록
   * ----------------
   * MultipartFile로 업로드된 Excel 파일을 읽어 DB에 저장
   *
   * @param file 업로드된 Excel 파일
   * @return ResponseEntity<?> - 성공 시 저장된 상품 리스트, 실패 시 오류 메시지
   */
  @PostMapping("/upload")
  public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) {

    // 1. 파일 비어 있는지 체크
    if (file.isEmpty()) {
      return ResponseEntity.badRequest().body("엑셀 파일을 선택해주세요.");
    }

    // 2. 파일 확장자 체크
    String filename = StringUtils.cleanPath(file.getOriginalFilename());
    if (!filename.endsWith(".xls") && !filename.endsWith(".xlsx")) {
      return ResponseEntity.badRequest().body("엑셀 파일만 업로드 가능합니다.");
    }

    try {
      // 3. MultipartFile → 임시 파일 변환
      File tempFile = File.createTempFile("upload-", filename);
      file.transferTo(tempFile);

      // 4. Excel 읽기 및 DB 저장
      Iterable<GoodsEntity> goodsList = excelUploadService.readExcel(tempFile);
      goodsService.saveGoodsAll(goodsList);

      // 5. 성공 응답
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

}
