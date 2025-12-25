package budget.application;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.IOException;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

public final class TestDataSource {

  private static EmbeddedPostgres dataSource;

  public static void start() throws IOException {
    dataSource = EmbeddedPostgres.builder().setPort(0).start();
  }

  public static DataSource getDataSource() {
    return dataSource.getPostgresDatabase();
  }

  public static void stop() throws IOException {
    dataSource.close();
  }

  public static void flywayMigrate(DataSource ds) {
    Flyway flyway =
        Flyway.configure().dataSource(ds).locations("classpath:test_db/test_migration").load();
    flyway.migrate();
  }
}
