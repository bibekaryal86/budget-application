package budget.application.scheduler;

import budget.application.service.domain.TransactionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleManager {
  private static final Logger log = LoggerFactory.getLogger(ScheduleManager.class);

  private final ScheduledExecutorService scheduledExecutorService;
  private final DailyTransactionReconScheduler dailyTransactionReconScheduler;
  private final DatabaseHealthCheckScheduler databaseHealthCheckScheduler;

  public ScheduleManager(DataSource dataSource, TransactionService transactionService) {
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
  }

  public void start() {
    dailyTransactionReconScheduler.start();
    databaseHealthCheckScheduler.start();
  }

  public void shutdown() {
    log.info("Shutting down scheduler...");
    scheduledExecutorService.shutdown();
    try {
      if (!scheduledExecutorService.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) {
        log.info("Forcing scheduler shutdown...");
        scheduledExecutorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduledExecutorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
