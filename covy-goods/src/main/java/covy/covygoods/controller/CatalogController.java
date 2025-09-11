package covy.covygoods.controller;

import covy.covygoods.entity.CatalogEntity;
import covy.covygoods.service.CatalogService;
import covy.covygoods.vo.ResponseCatalog;
import java.util.ArrayList;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/catalog-service")
public class CatalogController {

  Environment env;
  CatalogService catalogService;

  @Autowired
  public CatalogController(Environment env, CatalogService catalogService) {
    this.env = env;
    this.catalogService = catalogService;
  }

  @GetMapping("/heath_check")
  public String status() {
    return String.format("It's Working in User Service On PORT %s", env.getProperty("local.server.port"));
  }

  @GetMapping("/users")
  public ResponseEntity<List<ResponseCatalog>> getUsersAll() {
    Iterable<CatalogEntity> userList = catalogService.getAllCatalogs();

    List<ResponseCatalog> resut = new ArrayList<>();
    userList.forEach(v -> {
      resut.add(new ModelMapper().map(v, ResponseCatalog.class));
    });

    return ResponseEntity.status(HttpStatus.OK).body(resut);
  }
}