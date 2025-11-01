package covy.covycart.config.elastic;

import covy.covycart.config.log.UserActionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestClient;
import org.apache.http.HttpHost;

public class ElasticsearchCartSink extends RichSinkFunction<UserActionEvent> {

  private transient RestHighLevelClient esClient;
  private transient ObjectMapper mapper;

  @Override
  public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
    esClient = new RestHighLevelClient(
        RestClient.builder(
            new HttpHost("localhost", 9200, "http")
        )
    );
    mapper = new ObjectMapper();
  }

  @Override
  public void invoke(UserActionEvent value, Context context) throws Exception {
    try {
      Map<String, Object> document = new HashMap<>();
      document.put("userId", value.getUserId());
      document.put("goodsCd", value.getGoodsCd());
      document.put("actionType", value.getActionType().name());
      document.put("timestamp", value.getTimestamp());

      // 장바구니의 상태를 그대로 기록하기보다, 행동 로그로 남긴다.
      IndexRequest request = new IndexRequest("user_actions")
          .id(value.getUserId() + "_" + value.getGoodsCd() + "_" + value.getTimestamp())
          .source(document);

      esClient.index(request, RequestOptions.DEFAULT);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() throws Exception {
    if (esClient != null) esClient.close();
  }
}