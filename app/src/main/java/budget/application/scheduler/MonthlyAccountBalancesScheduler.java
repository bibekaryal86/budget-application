package budget.application.scheduler;

import budget.application.service.domain.AccountBalancesService;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonthlyAccountBalancesScheduler {

  private static final Logger log = LoggerFactory.getLogger(MonthlyAccountBalancesScheduler.class);

  private final ScheduledExecutorService scheduledExecutorService;
  private final AccountBalancesService accountBalancesService;

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
    long delayMillis = computeDelayToNextEndOfMonth(LocalTime.of(23, 0));

    log.info("Scheduling end-of-month reconciliation in {} ms", delayMillis);

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
      log.error("End-of-month account balance failed", ex);
    }
  }

  private void run() throws SQLException {
    log.info("Running end-of-month account balances job...");
    accountBalancesService.createAccountBalances();
  }

  private long computeDelayToNextEndOfMonth(LocalTime runAt) {
    LocalDateTime now = LocalDateTime.now();
    LocalDate today = now.toLocalDate();
    LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
    LocalDateTime nextRun = endOfMonth.atTime(runAt);

    // If we've already passed this month's run time, schedule next month
    if (nextRun.isBefore(now)) {
      LocalDate nextMonth = today.plusMonths(1);
      endOfMonth = nextMonth.withDayOfMonth(nextMonth.lengthOfMonth());
      nextRun = endOfMonth.atTime(runAt);
    }

    return Duration.between(now, nextRun).toMillis();
  }
}
