package covy.covycart.config;

import covy.covycart.config.log.UserActionEvent;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.bson.Document;

public class UserActionEventSink extends RichSinkFunction<UserActionEvent> {

  // ✅ MongoDB 연결 설정
  private final String uri = "mongodb://localhost:27017";
  private transient MongoClient mongoClient;
  private transient MongoCollection<Document> collection;

  @Override
  public void open(org.apache.flink.configuration.Configuration parameters) {
    // ✅ MongoClient 초기화 (Flink에서 매번 연결 생성 방지)
    mongoClient = MongoClients.create(uri);

    MongoDatabase database = mongoClient.getDatabase("mydb");       // DB 이름
    collection = database.getCollection("cart_events");             // 컬렉션 이름
  }

  @Override
  public void invoke(UserActionEvent event, Context context) {
    try {
      // ✅ Document로 MongoDB에 insert
      Document doc = new Document()
          .append("user_id", event.getUserId())
          .append("goods_cd", event.getGoodsCd())
          .append("goods_nm", event.getGoodsNm())
          .append("category", event.getCategory())
          .append("action_type", event.getActionType().name())
          .append("timestamp", event.getTimestamp());

      collection.insertOne(doc);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() {
    if (mongoClient != null) {
      mongoClient.close();
    }
  }
}
