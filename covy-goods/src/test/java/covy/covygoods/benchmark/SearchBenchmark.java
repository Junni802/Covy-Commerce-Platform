package covy.covygoods.benchmark;

import covy.covygoods.CovyGoodsApplication;
import covy.covygoods.elastic.document.GoodsDocument;
import covy.covygoods.entity.GoodsEntity;
import covy.covygoods.repository.GoodsRepository;
import covy.covygoods.repository.GoodsSearchRepository;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class SearchBenchmark {  // ✅ public class 이어야 함

  private GoodsRepository goodsRepository;
  private GoodsSearchRepository goodsSearchRepository;

  private String searchKeyword = "초코";

  @Setup(Level.Trial)
  public void setup() {
    AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext(CovyGoodsApplication.class); // main application class
    goodsRepository = context.getBean(GoodsRepository.class);
    goodsSearchRepository = context.getBean(GoodsSearchRepository.class);
  }

  @Benchmark
  public void dbSearch() {
    List<GoodsEntity> result = goodsRepository.findByGoodsNmContaining(searchKeyword);
    result.size();
  }

  @Benchmark
  public void esSearch() {
    List<GoodsDocument> result = goodsSearchRepository.findByGoodsNmContaining(searchKeyword);
    result.size();
  }
}