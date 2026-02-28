package budget.application.server.core;

import budget.application.server.handlers.AccountHandler;
import budget.application.server.handlers.AppTestsHandler;
import budget.application.server.handlers.BudgetHandler;
import budget.application.server.handlers.CategoryHandler;
import budget.application.server.handlers.CategoryTypeHandler;
import budget.application.server.handlers.InsightsHandler;
import budget.application.server.handlers.TransactionHandler;

public final class ServerContext {

  private final AppTestsHandler appTestsHandler;
  private final AccountHandler accountHandler;
  private final BudgetHandler budgetHandler;
  private final CategoryTypeHandler categoryTypeHandler;
  private final CategoryHandler categoryHandler;
  private final InsightsHandler insightsHandler;
  private final TransactionHandler transactionHandler;

  public ServerContext(
      AppTestsHandler appTestsHandler,
      AccountHandler accountHandler,
      BudgetHandler budgetHandler,
      CategoryTypeHandler categoryTypeHandler,
      CategoryHandler categoryHandler,
      InsightsHandler insightsHandler,
      TransactionHandler transactionHandler) {
    this.appTestsHandler = appTestsHandler;
    this.accountHandler = accountHandler;
    this.budgetHandler = budgetHandler;
    this.categoryTypeHandler = categoryTypeHandler;
    this.categoryHandler = categoryHandler;
    this.insightsHandler = insightsHandler;
    this.transactionHandler = transactionHandler;
  }

  public AppTestsHandler getAppTestsHandler() {
    return appTestsHandler;
  }

  public AccountHandler getAccountHandler() {
    return accountHandler;
  }

  public BudgetHandler getBudgetHandler() {
    return budgetHandler;
  }

  public CategoryTypeHandler getCategoryTypeHandler() {
    return categoryTypeHandler;
  }

  public CategoryHandler getCategoryHandler() {
    return categoryHandler;
  }

  public InsightsHandler getInsightsHandler() {
    return insightsHandler;
  }

  public TransactionHandler getTransactionHandler() {
    return transactionHandler;
  }
}
