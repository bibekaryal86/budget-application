package budget.application.scheduler;

import budget.application.service.domain.AccountBalancesService;
import budget.application.service.domain.TransactionService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleManager {
  private static final Logger log = LoggerFactory.getLogger(ScheduleManager.class);

  private final ScheduledExecutorService scheduledExecutorService;
  private final DailyTransactionReconScheduler dailyTransactionReconScheduler;
  private final DatabaseHealthCheckScheduler databaseHealthCheckScheduler;
  private final MonthlyAccountBalancesScheduler monthlyAccountBalancesScheduler;

  public ScheduleManager(
      DataSource dataSource,
      TransactionService transactionService,
      AccountBalancesService accountBalancesService) {
    this.scheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor(
            r -> {
              Thread t = new Thread(r, "scheduler-main");
              t.setDaemon(false);
              return t;
            });

    this.dailyTransactionReconScheduler =
        new DailyTransactionReconScheduler(transactionService, scheduledExecutorService);
    this.databaseHealthCheckScheduler =
        new DatabaseHealthCheckScheduler(dataSource, scheduledExecutorService);
    this.monthlyAccountBalancesScheduler =
        new MonthlyAccountBalancesScheduler(accountBalancesService, scheduledExecutorService);
  }

  public void start() {
    dailyTransactionReconScheduler.start();
    databaseHealthCheckScheduler.start();
    monthlyAccountBalancesScheduler.start();
  }

  public void shutdown() {
    log.info("Shutting down scheduler...");
    scheduledExecutorService.shutdown();
    try {
      if (!scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
        log.info("Forcing scheduler shutdown...");
        scheduledExecutorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduledExecutorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  public Map<String, Object> getSchedulerStatus() {
    Map<String, Object> status = new HashMap<>();
    status.put("SchedulerStatusIsShutdown", scheduledExecutorService.isShutdown());
    status.put("SchedulerStatusIsTerminated", scheduledExecutorService.isTerminated());
    status.put("DailyTransactionReconNextRunTime", dailyTransactionReconScheduler.getNextRunTime());
    status.put(
        "MonthlyAccountBalancesNextRunTime", monthlyAccountBalancesScheduler.getNextRunTime());
    return status;
  }
}
