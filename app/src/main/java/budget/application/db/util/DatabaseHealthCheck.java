package budget.application.db.util;

import budget.application.model.utils.DbHealthStatus;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

public class DatabaseHealthCheck {

  private final DataSource dataSource;

  public DatabaseHealthCheck(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public DbHealthStatus check() {
    boolean dbOk = false;
    String dbMessage = "OK";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT 1");
        ResultSet rs = ps.executeQuery()) {

      dbOk = rs.next();

    } catch (SQLException e) {
      dbOk = false;
      dbMessage = "DB_ERROR: " + e.getMessage();
    }

    int active = -1, idle = -1, total = -1, awaiting = -1;

    if (dataSource instanceof HikariDataSource hikari) {
      HikariPoolMXBean pool = hikari.getHikariPoolMXBean();
      active = pool.getActiveConnections();
      idle = pool.getIdleConnections();
      total = pool.getTotalConnections();
      awaiting = pool.getThreadsAwaitingConnection();
    }

    return new DbHealthStatus(dbOk, dbMessage, active, idle, total, awaiting);
  }
}
