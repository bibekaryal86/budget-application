package budget.application.utilities;

import budget.application.service.domain.TransactionService;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    TransactionService svc = new TransactionService(dataSource);
    svc.reconcileAll();
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
