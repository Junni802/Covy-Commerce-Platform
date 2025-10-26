package covy.covycart.config.elastic;

import covy.covycart.config.log.UserActionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestClient;
import org.apache.http.HttpHost;
import org.elasticsearch.xcontent.XContentType;

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
    if (esClient != null) {
      String id = value.getUserId() + "-" + value.getGoodsCd() + "-" + value.getTimestamp();
      IndexRequest request = new IndexRequest("cart-events")
          .id(id)
          .source(mapper.writeValueAsString(value), XContentType.JSON);
      esClient.index(request, RequestOptions.DEFAULT);
    }
  }

  @Override
  public void close() throws Exception {
    if (esClient != null) esClient.close();
  }
}