package budget.application.service.util;

import budget.application.db.repository.BaseRepository;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

  private final Connection connection;

  public TransactionManager(Connection connection) {
    this.connection = connection;
  }

  /** Execute a function inside a transaction. Allows throwing SQLException inside the lambda. */
  public <T> T execute(SqlWork<T> work) throws SQLException {
    try (BaseRepository bs = new BaseRepository(connection)) {
      T result = work.apply(bs);
      bs.commit();
      return result;
    }
  }

  /** Execute a void operation inside a transaction. */
  public void executeVoid(SqlVoidWork work) throws SQLException {
    try (BaseRepository bs = new BaseRepository(connection)) {
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
}
