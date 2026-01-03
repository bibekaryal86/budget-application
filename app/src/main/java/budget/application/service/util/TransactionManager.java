package budget.application.service.util;

import budget.application.db.dao.BaseRepository;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionManager {

  private final DataSource dataSource;

  public TransactionManager(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /** Execute a function inside a transaction. Allows throwing SQLException inside the lambda. */
  public <T> T execute(SqlWork<T> work) throws SQLException {
    try (BaseRepository bs = new BaseRepository(getConnection())) {
      T result = work.apply(bs);
      bs.commit();
      return result;
    }
  }

  /** Execute a void operation inside a transaction. */
  public void executeVoid(SqlVoidWork work) throws SQLException {
    try (BaseRepository bs = new BaseRepository(getConnection())) {
      work.apply(bs);
      bs.commit();
    }
  }

  // ---- Functional Interfaces ----

  @FunctionalInterface
  public interface SqlWork<T> {
    T apply(BaseRepository bs) throws SQLException;
  }

  @FunctionalInterface
  public interface SqlVoidWork {
    void apply(BaseRepository bs) throws SQLException;
  }

  // ---- Utilities ----
  private Connection getConnection() {
    try {
      return this.dataSource.getConnection();
    } catch (SQLException e) {
      throw new RuntimeException("Unable to get DB connection", e);
    }
  }
}
