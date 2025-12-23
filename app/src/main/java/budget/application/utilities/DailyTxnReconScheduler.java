package budget.application.utilities;

import budget.application.db.repository.TransactionItemRepository;
import budget.application.db.repository.TransactionRepository;
import budget.application.model.entity.Transaction;
import budget.application.model.entity.TransactionItem;
import budget.application.service.domain.TransactionService;
import budget.application.service.util.TransactionManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DailyTxnReconScheduler {

  private final DataSource dataSource;
  private final ScheduledExecutorService executor;

  public DailyTxnReconScheduler(DataSource dataSource, ScheduledExecutorService executor) {
    this.dataSource = dataSource;
    this.executor = executor;
  }

  public void start(LocalTime runAt) {
    log.info("Starting daily txn recon scheduler at [{}]", runAt);
    long initialDelayMillis = computeInitialDelayMillis(runAt);
    long periodMillis = Duration.ofDays(1).toMillis();

    executor.scheduleAtFixedRate(
        this::runSafe, initialDelayMillis, periodMillis, TimeUnit.MILLISECONDS);
  }

  private void runSafe() {
    try {
      run();
    } catch (Exception ex) {
      log.error("DailyTxnReconScheduler failed...", ex);
    }
  }

  private void run() throws SQLException {
    log.info("Running daily transaction reconciliation...");
    TransactionManager txManager = new TransactionManager(getConnection());
    TransactionService svc = new TransactionService(txManager);
    svc.reconcileAll();
  }

  private void runReconciliation() {
    log.info("Running daily txn recon...");
    TransactionManager tx = new TransactionManager(getConnection());

    try {
      tx.execute(
          (bs) -> {
            TransactionRepository txnRepo = new TransactionRepository(bs);
            TransactionItemRepository itemRepo = new TransactionItemRepository(bs);

            List<Transaction> allTxns = txnRepo.read(List.of());

            for (Transaction txn : allTxns) {
              UUID txnId = txn.id();

              List<TransactionItem> items = itemRepo.readByTransactionIds(List.of(txnId));

              double sum = items.stream().mapToDouble(TransactionItem::amount).sum();

              if (Double.compare(sum, txn.totalAmount()) != 0) {
                System.err.println(
                    "[Reconciliation] MISMATCH for txn "
                        + txnId
                        + " | total="
                        + txn.totalAmount()
                        + " | sum(items)="
                        + sum);
              }
            }

            return null;
          });

    } catch (SQLException e) {
      log.error("Reconciliation failed: {}", e.getMessage());
    }
  }

  private Connection getConnection() {
    try {
      return dataSource.getConnection();
    } catch (SQLException e) {
      throw new RuntimeException("Unable to get DB connection", e);
    }
  }

  private long computeInitialDelayMillis(LocalTime runAt) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime nextRun =
        now.withHour(runAt.getHour()).withMinute(runAt.getMinute()).withSecond(0).withNano(0);
    if (nextRun.isBefore(now)) {
      nextRun = nextRun.plusDays(1);
    }
    return Duration.between(now, nextRun).toMillis();
  }
}
