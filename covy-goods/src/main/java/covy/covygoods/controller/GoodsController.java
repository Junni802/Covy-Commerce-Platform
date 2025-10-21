package covy.covygoods.controller;

import covy.covygoods.common.elastic.document.GoodsDocument;
import covy.covygoods.entity.GoodsEntity;
import covy.covygoods.service.GoodsService;
import covy.covygoods.vo.ResponseCatalog;
import java.util.ArrayList;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/")
public class GoodsController {

  Environment env;
  GoodsService goodsService;

  @Autowired
  public GoodsController(Environment env, GoodsService goodsService) {
    this.env = env;
    this.goodsService = goodsService;
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
  public ResponseEntity<List<ResponseCatalog>> getFindGoods(@PathVariable(name = "goodsNm") String goodsNm,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestHeader(value = "X-User-Id", required = false) String userId) {

    Pageable pageable = PageRequest.of(page, size);
    Iterable<GoodsDocument> goodsList = goodsService.getgoods(goodsNm, pageable);


    String id = userId;
    List<ResponseCatalog> resut = new ArrayList<>();
    goodsList.forEach(v -> {
      resut.add(new ModelMapper().map(v, ResponseCatalog.class));
    });

    return ResponseEntity.status(HttpStatus.OK).body(resut);
  }

  @GetMapping("/top/keyword")
  public ResponseEntity<List<String>> findTopKeywords() {

    return ResponseEntity.status(HttpStatus.OK).body(goodsService.getTopKeywords(10));
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