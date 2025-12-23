package budget.application.utilities;

import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScheduleManager {

  private final ScheduledExecutorService executor;
  private final DailyTxnReconScheduler dailyTxnReconScheduler;

  public ScheduleManager(DataSource dataSource) {
    this.executor =
        Executors.newSingleThreadScheduledExecutor(
            r -> {
              Thread t = new Thread(r, "scheduler-main");
              t.setDaemon(false);
              return t;
            });

    this.dailyTxnReconScheduler = new DailyTxnReconScheduler(dataSource, executor);
  }

  public void start() {
    LocalTime startTime = LocalTime.of(2, 0);
    dailyTxnReconScheduler.start(startTime);
    log.info("DailyTxnReconScheduler scheduled for [{}]", startTime);
  }

  public void shutdown() {
    log.info("Shutting down scheduler...");
    executor.shutdown();
    try {
      if (!executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) {
        log.info("Forcing scheduler shutdown...");
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
