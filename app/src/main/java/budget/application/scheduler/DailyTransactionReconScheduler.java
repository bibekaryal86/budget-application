package budget.application.scheduler;

import budget.application.service.domain.TransactionService;
import io.github.bibekaryal86.shdsvc.Email;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DailyTransactionReconScheduler {
  private static final Logger log = LoggerFactory.getLogger(DailyTransactionReconScheduler.class);

  private final ScheduledExecutorService scheduledExecutorService;
  private final TransactionService transactionService;

  public DailyTransactionReconScheduler(
      DataSource dataSource, ScheduledExecutorService scheduledExecutorService, Email email) {
    this.transactionService = new TransactionService(dataSource, email);
    this.scheduledExecutorService = scheduledExecutorService;
  }

  public void start(LocalTime runAt) {
    log.info("Starting daily txn recon scheduler at [{}]", runAt);
    long initialDelayMillis = computeInitialDelayMillis(runAt);
    long periodMillis = Duration.ofDays(1).toMillis();

    scheduledExecutorService.scheduleAtFixedRate(
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
    String requestId = UUID.randomUUID().toString();
    log.info("[{}] Running daily transaction reconciliation...", requestId);
    transactionService.reconcileAll(requestId);
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
