package covy.covycart.config;

import covy.covycart.config.log.UserActionEvent;
import java.sql.DriverManager;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserActionEventSink extends RichSinkFunction<UserActionEvent> {

  private final String url = "jdbc:mariadb://localhost:3306/mydb";
  private final String username = "root";
  private final String password = "test1357";

  @Override
  public void invoke(UserActionEvent event, Context context) {
    try (Connection conn = DriverManager.getConnection(url, username, password);
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO cart_events(user_id, goods_cd, action_type, timestamp) VALUES (?, ?, ?, ?)")) {

      ps.setString(1, event.getUserId());
      ps.setString(2, event.getGoodsCd());
      ps.setString(3, event.getActionType().name());
      ps.setLong(4, event.getTimestamp());
      ps.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
