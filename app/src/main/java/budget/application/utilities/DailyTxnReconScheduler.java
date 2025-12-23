package budget.application.utilities;

import budget.application.db.repository.TransactionItemRepository;
import budget.application.db.repository.TransactionRepository;
import budget.application.model.entity.Transaction;
import budget.application.model.entity.TransactionItem;
import budget.application.service.util.TransactionManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DailyTxnReconScheduler {

  private final DataSource dataSource;
  private final Timer timer = new Timer("daily-txn-recon", true);

  public DailyTxnReconScheduler(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void start(LocalTime runAt) {
    log.info("Starting daily txn recon scheduler at [{}]", runAt);
    long delay = computeInitialDelay(runAt);
    long period = 24 * 60 * 60 * 1000L;

    timer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            runReconciliation();
          }
        },
        delay,
        period);
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

  private long computeInitialDelay(LocalTime runAt) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime nextRun =
        now.withHour(runAt.getHour()).withMinute(runAt.getMinute()).withSecond(0).withNano(0);

    if (nextRun.isBefore(now)) {
      nextRun = nextRun.plusDays(1);
    }

    return Duration.between(now, nextRun).toMillis();
  }
}
