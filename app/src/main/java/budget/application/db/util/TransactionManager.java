package budget.application.db.util;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionManager {

  private final DataSource dataSource;

  public TransactionManager(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /** Execute a function inside a transaction. Allows throwing SQLException inside the lambda. */
  public <T> T execute(String requestId, SqlWork<T> work) throws SQLException {
    try (TransactionContext txnCxt = new TransactionContext(requestId, getConnection())) {
      T result = work.apply(txnCxt);
      txnCxt.commit();
      return result;
    }
  }

  /** Execute a void operation inside a transaction. */
  public void executeVoid(String requestId, SqlVoidWork work) throws SQLException {
    try (TransactionContext txnCxt = new TransactionContext(requestId, getConnection())) {
      work.apply(txnCxt);
      txnCxt.commit();
    }
  }

  // ---- Functional Interfaces ----

  @FunctionalInterface
  public interface SqlWork<T> {
    T apply(TransactionContext txnCxt) throws SQLException;
  }

  @FunctionalInterface
  public interface SqlVoidWork {
    void apply(TransactionContext txnCxt) throws SQLException;
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
