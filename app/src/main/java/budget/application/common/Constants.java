package budget.application.common;

import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import io.netty.util.AttributeKey;
import java.util.List;
import java.util.Objects;

public class Constants {
  private Constants() {}

  public static final boolean IS_PRODUCTION;

  public static final String ENV_SERVER_PORT = "PORT";
  public static final String SPRING_PROFILES_ACTIVE = "SPRING_PROFILES_ACTIVE";
  public static final String ENV_SELF_USERNAME = "SELF_USERNAME";
  public static final String ENV_SELF_PASSWORD = "SELF_PASSWORD";
  public static final String ENV_DB_HOST_PROD = "DB_HOST_PROD";
  public static final String ENV_DB_NAME_PROD = "DB_NAME_PROD";
  public static final String ENV_DB_USERNAME_PROD = "DB_USERNAME_PROD";
  public static final String ENV_DB_PASSWORD_PROD = "DB_PASSWORD_PROD";
  public static final String ENV_DB_HOST_SANDBOX = "DB_HOST_SANDBOX";
  public static final String ENV_DB_NAME_SANDBOX = "DB_NAME_SANDBOX";
  public static final String ENV_DB_USERNAME_SANDBOX = "DB_USERNAME_SANDBOX";
  public static final String ENV_DB_PASSWORD_SANDBOX = "DB_PASSWORD_SANDBOX";
  public static final String ENV_ENVSVC_BASE_URL = "ENVSVC_BASE_URL";
  public static final String ENV_ENVSVC_USERNAME = "ENVSVC_USR";
  public static final String ENV_ENVSVC_PASSWORD = "ENVSVC_PWD";
  public static final String ENV_EMAIL_API_URL = "EMAIL_API_URL";
  public static final String ENV_EMAIL_API_USERNAME = "EMAIL_API_USR";
  public static final String ENV_EMAIL_API_PASSWORD = "EMAIL_API_PWD";
  public static final String ENV_RECON_EMAIL_TO = "RECON_EMAIL_TO";
  public static final List<String> ENV_KEY_NAMES =
      List.of(
          ENV_SERVER_PORT,
          SPRING_PROFILES_ACTIVE,
          ENV_SELF_USERNAME,
          ENV_SELF_PASSWORD,
          ENV_EMAIL_API_URL,
          ENV_EMAIL_API_USERNAME,
          ENV_EMAIL_API_PASSWORD,
          ENV_ENVSVC_BASE_URL,
          ENV_ENVSVC_USERNAME,
          ENV_ENVSVC_PASSWORD,
          ENV_RECON_EMAIL_TO);
  public static final List<String> ENV_KEY_NAMES_PROD =
      List.of(ENV_DB_HOST_PROD, ENV_DB_NAME_PROD, ENV_DB_USERNAME_PROD, ENV_DB_PASSWORD_PROD);
  public static final List<String> ENV_KEY_NAMES_SANDBOX =
      List.of(
          ENV_DB_HOST_SANDBOX,
          ENV_DB_NAME_SANDBOX,
          ENV_DB_USERNAME_SANDBOX,
          ENV_DB_PASSWORD_SANDBOX);

  public static final String THIS_APP_NAME = "petssvc";
  public static final String THIS_APP_NAME_ENV_DETAILS = "petsservice";
  public static final String ENV_PORT_DEFAULT = "8101";
  public static final String PRODUCTION_ENV = "production";
  public static final String TESTING_ENV = "test_mode";

  public static final AttributeKey<String> REQUEST_ID = AttributeKey.valueOf("REQUEST_ID");
  public static final AttributeKey<AuthToken> AUTH_TOKEN = AttributeKey.valueOf("AUTH_TOKEN");

  public static final int BOSS_GROUP_THREADS = 1;
  public static final int WORKER_GROUP_THREADS = 8;
  public static final int CONNECT_TIMEOUT_MILLIS = 5000; // 5 seconds
  public static final int MAX_CONTENT_LENGTH = 1048576; // 1MB
  public static final String CONTENT_LENGTH_DEFAULT = "0";

  public static final List<String> TRANSACTION_TYPES = List.of("NEEDS", "WANTS");
  public static final List<String> ACCOUNT_TYPES =
      List.of("CASH", "CREDIT", "LOAN", "CHECKING", "SAVINGS", "INVESTMENT", "OTHER");
  public static final List<String> ACCOUNT_STATUSES = List.of("ACTIVE", "INACTIVE");

  public static final String CATEGORY_TYPE_INCOME_NAME = "INCOME";
  public static final String CATEGORY_TYPE_SAVINGS_NAME = "SAVINGS";
  public static final String CATEGORY_TYPE_TRANSFER_NAME = "TRANSFER";
  public static final List<String> NO_EXPENSE_CATEGORY_TYPES =
      List.of(CATEGORY_TYPE_INCOME_NAME, CATEGORY_TYPE_SAVINGS_NAME, CATEGORY_TYPE_TRANSFER_NAME);

  static {
    IS_PRODUCTION =
        Objects.equals(
            CommonUtilities.getSystemEnvProperty(SPRING_PROFILES_ACTIVE), PRODUCTION_ENV);
  }
}
