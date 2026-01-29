package budget.application.scheduler;

import budget.application.db.util.DatabaseHealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DatabaseHealthCheckScheduler {
  private static final Logger log = LoggerFactory.getLogger(DatabaseHealthCheckScheduler.class);

  private final ScheduledExecutorService scheduledExecutorService;
  private final DatabaseHealthCheck databaseHealthCheck;

  public DatabaseHealthCheckScheduler(DataSource dataSource, ScheduledExecutorService scheduledExecutorService) {
    this.scheduledExecutorService = scheduledExecutorService;
    this.databaseHealthCheck = new DatabaseHealthCheck(dataSource);
  }

  public void start() {
    log.info("Starting database health check scheduler");
    long initialDelayMillis = 0;
    long periodMillis = Duration.ofMinutes(15).toMillis();

    scheduledExecutorService.scheduleAtFixedRate(
        this::runSafe, initialDelayMillis, periodMillis, TimeUnit.MILLISECONDS);
  }

  private void runSafe() {
    try {
      run();
    } catch (Exception ex) {
      log.error("DatabaseHealthCheck failed...", ex);
    }
  }

  private void run() {
    log.info("Running database health check...");
    databaseHealthCheck.check();
  }
}
