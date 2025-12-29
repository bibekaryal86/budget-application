package budget.application.db.dao;

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
    try {
      if (!completed) {
        rollback();
      }
    } finally {
      try {
        connection.setAutoCommit(true);
      } finally {
        connection.close();
      }
    }
  }

  public Connection connection() {
    return connection;
  }
}
