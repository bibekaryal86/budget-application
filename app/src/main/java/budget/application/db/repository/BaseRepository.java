package budget.application.db.repository;

import java.sql.Connection;
import java.sql.SQLException;

public class BaseRepository implements AutoCloseable {

  private final Connection connection;
  private boolean completed = false;

  public BaseRepository(final Connection connection) throws SQLException {
    this.connection = connection;
    this.connection.setAutoCommit(false);
  }

  public void commit() throws SQLException {
    if (!completed) {
      connection.commit();
      completed = true;
    }
  }

  public void rollback() throws SQLException {
    if (!completed) {
      connection.rollback();
      completed = true;
    }
  }

  @Override
  public void close() throws SQLException {
    if (!completed) {
      rollback();
    }
    connection.setAutoCommit(true);
  }

  public Connection connection() {
    return connection;
  }
}
