package budget.application.db.util;

import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionContext implements AutoCloseable {
  private static final Logger log = LoggerFactory.getLogger(TransactionContext.class);

  private final Connection connection;
  private final String requestId;
  private final long startTimeMs;
  private TransactionState state = TransactionState.ACTIVE;

  private enum TransactionState {
    ACTIVE,
    COMMITTED,
    ROLLED_BACK
  }

  public TransactionContext(final String requestId, final Connection connection)
      throws SQLException {
    this.connection = connection;
    this.requestId = requestId;
    this.startTimeMs = System.currentTimeMillis();
    this.connection.setAutoCommit(false);

    log.debug("[{}] Transaction started", requestId);
  }

  public void commit() throws SQLException {
    if (state == TransactionState.ACTIVE) {
      long startCommit = System.currentTimeMillis();
      try {
        connection.commit();
        state = TransactionState.COMMITTED;
        long commitDuration = System.currentTimeMillis() - startCommit;
        long totalDuration = System.currentTimeMillis() - startTimeMs;
        log.debug(
            "[{}] Transaction committed successfully (commit={}ms, total={}ms)",
            requestId,
            commitDuration,
            totalDuration);
      } catch (SQLException e) {
        log.error("[{}] Failed to commit transaction: {}", requestId, e.getMessage(), e);
        throw e;
      }
    } else {
      String message = "Transaction already " + state;
      log.warn("[{}] Attempted to commit but transaction is already {}", requestId, state);
      throw new IllegalStateException(message);
    }
  }

  public void rollback() throws SQLException {
    if (state == TransactionState.ACTIVE) {
      long startRollback = System.currentTimeMillis();
      try {
        connection.rollback();
        state = TransactionState.ROLLED_BACK;
        long rollbackDuration = System.currentTimeMillis() - startRollback;
        long totalDuration = System.currentTimeMillis() - startTimeMs;
        log.info(
            "[{}] Transaction rolled back (rollback={}ms, total={}ms)",
            requestId,
            rollbackDuration,
            totalDuration);
      } catch (SQLException e) {
        log.error("[{}] Failed to rollback transaction: {}", requestId, e.getMessage(), e);
        throw e;
      }
    } else {
      log.debug("[{}] Rollback called but transaction already {}", requestId, state);
    }
  }

  @Override
  public void close() throws SQLException {
    try {
      if (state == TransactionState.ACTIVE) {
        log.warn(
            "[{}] Transaction not explicitly committed/rolled back, "
                + "auto-rolling back in close()",
            requestId);
        rollback();
      }
    } finally {
      try {
        connection.setAutoCommit(true);
        log.trace("[{}] Auto-commit restored", requestId);
      } catch (SQLException e) {
        log.error("[{}] Failed to restore auto-commit: {}", requestId, e.getMessage(), e);
      } finally {
        try {
          connection.close();
          log.debug("[{}] Connection closed", requestId);
        } catch (SQLException e) {
          log.error("[{}] Failed to close connection: {}", requestId, e.getMessage(), e);
        }
      }
    }
  }

  public Connection connection() {
    if (state != TransactionState.ACTIVE) {
      log.error("[{}] Attempted to access connection after transaction {}", requestId, state);
      throw new IllegalStateException("Cannot use connection after transaction " + state);
    }
    return connection;
  }
}
