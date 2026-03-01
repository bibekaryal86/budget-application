package budget.application.scheduler;

import budget.application.common.Constants;
import budget.application.service.domain.TransactionService;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DailyTransactionReconScheduler {
  private static final Logger log = LoggerFactory.getLogger(DailyTransactionReconScheduler.class);

  private final ScheduledExecutorService scheduledExecutorService;
  private final TransactionService transactionService;
  private ScheduledFuture<?> scheduledFuture;

  public DailyTransactionReconScheduler(
      TransactionService transactionService, ScheduledExecutorService scheduledExecutorService) {
    this.transactionService = transactionService;
    this.scheduledExecutorService = scheduledExecutorService;
  }

  public void start() {
    ZonedDateTime now =
        ZonedDateTime.now(
            ZoneId.of(
                CommonUtilities.getSystemEnvProperty(
                    Constants.ENV_TIME_ZONE, Constants.ENV_TIME_ZONE_DEFAULT)));
    LocalTime runAt = LocalTime.of(2, 0);
    log.info("Starting daily transaction recon scheduler at [{}]", runAt);
    long initialDelayMillis = computeInitialDelayMillis(now, runAt);
    long periodMillis = Duration.ofDays(1).toMillis();

    scheduledFuture =
        scheduledExecutorService.scheduleAtFixedRate(
            this::runSafe, initialDelayMillis, periodMillis, TimeUnit.MILLISECONDS);
  }

  private void runSafe() {
    try {
      run();
    } catch (Exception ex) {
      log.error("Daily Transaction Recon Scheduler failed...", ex);
    }
  }

  private void run() throws SQLException {
    log.info("Running daily transaction reconciliation...");
    transactionService.reconcileAll();
  }

  private long computeInitialDelayMillis(ZonedDateTime now, LocalTime runAt) {
    LocalDateTime nextRun =
        now.toLocalDateTime()
            .withHour(runAt.getHour())
            .withMinute(runAt.getMinute())
            .withSecond(0)
            .withNano(0);
    if (nextRun.isBefore(now.toLocalDateTime())) {
      nextRun = nextRun.plusDays(1);
    }
    return Duration.between(now, nextRun).toMillis();
  }

  public LocalDateTime getNextRunTime() {
    if (scheduledFuture == null || scheduledFuture.isCancelled()) {
      return null;
    }

    long delayMillis = scheduledFuture.getDelay(TimeUnit.MILLISECONDS);

    if (delayMillis < 0) {
      return LocalDateTime.now();
    }

    return LocalDateTime.now().plus(delayMillis, ChronoUnit.MILLIS);
  }
}
