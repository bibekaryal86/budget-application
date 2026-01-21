package budget.application.server.util;

public class ApiPaths {
  private ApiPaths() {}

  public static final String BASE_V1 = "/petssvc/api/v1";

  public static final String APP_TESTS = "/petssvc/tests";
  public static final String APP_TESTS_PING = APP_TESTS + "/ping";

  public static final String ACCOUNTS_V1 = BASE_V1 + "/accounts";
  public static final String ACCOUNTS_V1_WITH_ID = ACCOUNTS_V1 + "/";

  public static final String BUDGETS_V1 = BASE_V1 + "/budgets";
  public static final String BUDGETS_V1_WITH_ID = BUDGETS_V1 + "/";

  public static final String CATEGORIES_V1 = BASE_V1 + "/categories";
  public static final String CATEGORIES_V1_WITH_ID = CATEGORIES_V1 + "/";

  public static final String CATEGORY_TYPES_V1 = BASE_V1 + "/category-types";
  public static final String CATEGORY_TYPES_V1_WITH_ID = CATEGORY_TYPES_V1 + "/";

  public static final String TRANSACTIONS_V1 = BASE_V1 + "/transactions";
  public static final String TRANSACTIONS_V1_WITH_ID = TRANSACTIONS_V1 + "/";
  public static final String TRANSACTIONS_V1_WITH_MERCHANTS = TRANSACTIONS_V1 + "/merchants";

  public static final String TRANSACTION_ITEMS_V1 = BASE_V1 + "/transaction-items";
  public static final String TRANSACTION_ITEMS_V1_WITH_ID = TRANSACTION_ITEMS_V1 + "/";
  public static final String TRANSACTION_ITEMS_V1_WITH_TAGS = TRANSACTION_ITEMS_V1 + "/tags";

  public static final String REPORTS_V1 = BASE_V1 + "/reports";
  public static final String REPORTS_V1_TXN_SUMMARIES = REPORTS_V1 + "/txn-summaries";
}
