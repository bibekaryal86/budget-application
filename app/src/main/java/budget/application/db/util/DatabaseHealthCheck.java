package budget.application.db.util;

import budget.application.model.dto.response.DbHealthStatus;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

public class DatabaseHealthCheck {

  private final HikariDataSource hikari;

  public DatabaseHealthCheck(DataSource dataSource) {
    this.hikari = (HikariDataSource) dataSource;
  }

  public DbHealthStatus check() {
    boolean dbOk = false;
    String dbMessage = "OK";

    try (Connection conn = hikari.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT 1");
        ResultSet rs = ps.executeQuery()) {

      dbOk = rs.next();

    } catch (SQLException e) {
      dbOk = false;
      dbMessage = "DB_ERROR: " + e.getMessage();
    }

    HikariPoolMXBean pool = hikari.getHikariPoolMXBean();

    int active = pool.getActiveConnections();
    int idle = pool.getIdleConnections();
    int total = pool.getTotalConnections();
    int awaiting = pool.getThreadsAwaitingConnection();

    return new DbHealthStatus(dbOk, dbMessage, active, idle, total, awaiting);
  }
}
