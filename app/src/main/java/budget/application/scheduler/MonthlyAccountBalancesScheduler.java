package budget.application.scheduler;

import budget.application.common.Constants;
import budget.application.service.domain.AccountBalancesService;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
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

public class MonthlyAccountBalancesScheduler {

  private static final Logger log = LoggerFactory.getLogger(MonthlyAccountBalancesScheduler.class);

  private final ScheduledExecutorService scheduledExecutorService;
  private final AccountBalancesService accountBalancesService;
  private ScheduledFuture<?> scheduledFuture;

  public MonthlyAccountBalancesScheduler(
      AccountBalancesService accountBalancesService,
      ScheduledExecutorService scheduledExecutorService) {
    this.accountBalancesService = accountBalancesService;
    this.scheduledExecutorService = scheduledExecutorService;
  }

  public void start() {
    scheduleNextRun();
  }

  private void scheduleNextRun() {
    ZonedDateTime now =
        ZonedDateTime.now(
            ZoneId.of(
                CommonUtilities.getSystemEnvProperty(
                    Constants.ENV_TIME_ZONE, Constants.ENV_TIME_ZONE_DEFAULT)));
    long delayMillis = computeDelayToNextEndOfMonth(now, LocalTime.of(23, 0));
    log.info("Scheduling monthly account balances scheduler in {} ms", delayMillis);

    scheduledFuture =
        scheduledExecutorService.schedule(
            () -> {
              runSafe();
              scheduleNextRun();
            },
            delayMillis,
            TimeUnit.MILLISECONDS);
  }

  private void runSafe() {
    try {
      run();
    } catch (Exception ex) {
      log.error("Monthly Account Balances Scheduler failed", ex);
    }
  }

  private void run() throws SQLException {
    log.info("Running Monthly Account Balances job...");
    accountBalancesService.createAccountBalances();
  }

  private long computeDelayToNextEndOfMonth(ZonedDateTime now, LocalTime runAt) {
    LocalDate today = now.toLocalDate();
    LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
    LocalDateTime nextRun = endOfMonth.atTime(runAt);

    // If we've already passed this month's run time, schedule next month
    if (nextRun.isBefore(now.toLocalDateTime())) {
      LocalDate nextMonth = today.plusMonths(1);
      endOfMonth = nextMonth.withDayOfMonth(nextMonth.lengthOfMonth());
      nextRun = endOfMonth.atTime(runAt);
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
