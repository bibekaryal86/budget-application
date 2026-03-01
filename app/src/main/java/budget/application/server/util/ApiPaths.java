package budget.application.server.util;

public class ApiPaths {
  private ApiPaths() {}

  public static final String BASE_V1 = "/petssvc/api/v1";

  public static final String APP_TESTS = "/petssvc/tests";
  public static final String APP_TESTS_PING = APP_TESTS + "/ping";
  public static final String APP_TESTS_SCHEDULERS = APP_TESTS + "/schedulers";

  public static final String ACCOUNTS_V1 = BASE_V1 + "/accounts";
  public static final String ACCOUNTS_V1_WITH_ID = ACCOUNTS_V1 + "/";
  public static final String ACCOUNTS_V1_TYPES = ACCOUNTS_V1 + "/types";
  public static final String ACCOUNTS_V1_STATUSES = ACCOUNTS_V1 + "/statuses";
  public static final String ACCOUNTS_V1_BANKS = ACCOUNTS_V1 + "/banks";

  public static final String BUDGETS_V1 = BASE_V1 + "/budgets";
  public static final String BUDGETS_V1_WITH_ID = BUDGETS_V1 + "/";

  public static final String CATEGORIES_V1 = BASE_V1 + "/categories";
  public static final String CATEGORIES_V1_WITH_ID = CATEGORIES_V1 + "/";

  public static final String CATEGORY_TYPES_V1 = BASE_V1 + "/category-types";
  public static final String CATEGORY_TYPES_V1_WITH_ID = CATEGORY_TYPES_V1 + "/";

  public static final String TRANSACTIONS_V1 = BASE_V1 + "/transactions";
  public static final String TRANSACTIONS_V1_WITH_ID = TRANSACTIONS_V1 + "/";
  public static final String TRANSACTIONS_V1_WITH_MERCHANTS = TRANSACTIONS_V1 + "/merchants";

  public static final String INSIGHTS_V1 = BASE_V1 + "/insights";
  public static final String INSIGHTS_V1_CF_SUMMARIES = INSIGHTS_V1 + "/cf-summaries";
  public static final String INSIGHTS_V1_CAT_SUMMARIES = INSIGHTS_V1 + "/cat-summaries";
  public static final String INSIGHTS_V1_ACC_SUMMARIES = INSIGHTS_V1 + "/acc-summaries";
}
