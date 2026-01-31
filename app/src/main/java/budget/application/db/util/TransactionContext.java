package budget.application.db.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionContext implements AutoCloseable {
  private static final Logger log = LoggerFactory.getLogger(TransactionContext.class);

  private final Connection connection;
  private final long startTimeMs;
  private final UUID txId;

  private TransactionState txState = TransactionState.ACTIVE;

  private enum TransactionState {
    ACTIVE,
    COMMITTED,
    ROLLED_BACK
  }

  public TransactionContext(final Connection connection) throws SQLException {
    this.connection = connection;
    this.startTimeMs = System.currentTimeMillis();
    this.connection.setAutoCommit(false);
    txId = UUID.randomUUID();

    log.debug("[txId:{}] Transaction started,,,", txId);
  }

  public void commit() throws SQLException {
    if (txState == TransactionState.ACTIVE) {
      long startCommit = System.currentTimeMillis();
      try {
        connection.commit();
        txState = TransactionState.COMMITTED;
        long commitDuration = System.currentTimeMillis() - startCommit;
        long totalDuration = System.currentTimeMillis() - startTimeMs;
        log.debug(
            "[txId:{}] Transaction committed successfully (commit={}ms, total={}ms)",
            txId,
            commitDuration,
            totalDuration);
      } catch (SQLException e) {
        log.error("[txId:{}] Failed to commit transaction: {}", txId, e.getMessage(), e);
        throw e;
      }
    } else {
      String message = "Transaction already " + txState;
      log.warn("[txId:{}] Attempted to commit but transaction is already {}", txId, txState);
      throw new IllegalStateException(message);
    }
  }

  public void rollback() throws SQLException {
    if (txState == TransactionState.ACTIVE) {
      long startRollback = System.currentTimeMillis();
      try {
        connection.rollback();
        txState = TransactionState.ROLLED_BACK;
        long rollbackDuration = System.currentTimeMillis() - startRollback;
        long totalDuration = System.currentTimeMillis() - startTimeMs;
        log.info(
            "[txId:{}]  Transaction rolled back (rollback={}ms, total={}ms)",
            txId,
            rollbackDuration,
            totalDuration);
      } catch (SQLException e) {
        log.error("[txId:{}]  Failed to rollback transaction: {}", txId, e.getMessage(), e);
        throw e;
      }
    } else {
      log.debug("[txId:{}]  Rollback called but transaction already {}", txId, txState);
    }
  }

  @Override
  public void close() throws SQLException {
    try {
      if (txState == TransactionState.ACTIVE) {
        log.warn(
            "[txId:{}]  Transaction not explicitly committed/rolled back, "
                + "auto-rolling back in close()",
            txId);
        rollback();
      }
    } finally {
      try {
        connection.setAutoCommit(true);
        log.trace("[txId:{}] Auto-commit restored", txId);
      } catch (SQLException e) {
        log.error("[txId:{}] Failed to restore auto-commit: {}", txId, e.getMessage(), e);
      } finally {
        try {
          connection.close();
          log.debug("[txId:{}] Connection closed", txId);
        } catch (SQLException e) {
          log.error("[txId:{}] Failed to close connection: {}", txId, e.getMessage(), e);
        }
      }
    }
  }

  public Connection connection() {
    if (txState != TransactionState.ACTIVE) {
      log.error("[txId:{}] Attempted to access connection after transaction {}", txId, txState);
      throw new IllegalStateException("Cannot use connection after transaction " + txState);
    }
    return connection;
  }
}
