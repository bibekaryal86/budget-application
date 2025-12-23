package budget.application.db.util;

import budget.application.utilities.Constants;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import javax.sql.DataSource;

public final class DataSourceFactory {

  private static final String dbHost;
  private static final String jdbcUrl;
  private static final String dbName;
  private static final String dbUser;
  private static final String dbPassword;

  static {
    if (Constants.IS_PRODUCTION) {
      dbHost = CommonUtilities.getSystemEnvProperty(Constants.ENV_DB_HOST_PROD);
      dbName = CommonUtilities.getSystemEnvProperty(Constants.ENV_DB_NAME_PROD);
      dbUser = CommonUtilities.getSystemEnvProperty(Constants.ENV_DB_USERNAME_PROD);
      dbPassword = CommonUtilities.getSystemEnvProperty(Constants.ENV_DB_PASSWORD_PROD);
    } else {
      dbHost = CommonUtilities.getSystemEnvProperty(Constants.ENV_DB_HOST_SANDBOX);
      dbName = CommonUtilities.getSystemEnvProperty(Constants.ENV_DB_NAME_SANDBOX);
      dbUser = CommonUtilities.getSystemEnvProperty(Constants.ENV_DB_USERNAME_SANDBOX);
      dbPassword = CommonUtilities.getSystemEnvProperty(Constants.ENV_DB_PASSWORD_SANDBOX);
    }

    jdbcUrl = String.format("jdbc:postgresql://%s:5432/%s", dbHost, dbName);
  }

  private DataSourceFactory() {}

  public static DataSource create() {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(jdbcUrl);
    hikariConfig.setUsername(dbUser);
    hikariConfig.setPassword(dbPassword);
    hikariConfig.setDriverClassName("org.postgresql.Driver");

    hikariConfig.setMaximumPoolSize(10);
    hikariConfig.setMinimumIdle(5);

    hikariConfig.setIdleTimeout(30000);
    hikariConfig.setConnectionTimeout(20000);
    hikariConfig.setMaxLifetime(1800000);

    hikariConfig.setKeepaliveTime(300000);
    hikariConfig.setValidationTimeout(3000);
    hikariConfig.setLeakDetectionThreshold(30000);

    hikariConfig.setConnectionTestQuery("SELECT 1");
    hikariConfig.setConnectionInitSql("SELECT 1");

    return new HikariDataSource(hikariConfig);
  }
}
