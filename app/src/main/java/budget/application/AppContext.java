package budget.application;

import budget.application.cache.CategoryCache;
import budget.application.cache.CategoryTypeCache;
import budget.application.db.dao.AccountDao;
import budget.application.db.dao.BudgetDao;
import budget.application.db.dao.CategoryDao;
import budget.application.db.dao.CategoryTypeDao;
import budget.application.db.dao.InsightsDao;
import budget.application.db.dao.TransactionDao;
import budget.application.db.dao.TransactionItemDao;
import budget.application.db.util.DataSourceFactory;
import budget.application.scheduler.ScheduleManager;
import budget.application.server.core.ServerManager;
import budget.application.server.handlers.AccountHandler;
import budget.application.server.handlers.AppTestsHandler;
import budget.application.server.handlers.BudgetHandler;
import budget.application.server.handlers.CategoryHandler;
import budget.application.server.handlers.CategoryTypeHandler;
import budget.application.server.handlers.InsightsHandler;
import budget.application.server.handlers.TransactionHandler;
import budget.application.server.handlers.TransactionItemHandler;
import budget.application.service.domain.AccountService;
import budget.application.service.domain.BudgetService;
import budget.application.service.domain.CategoryService;
import budget.application.service.domain.CategoryTypeService;
import budget.application.service.domain.InsightsService;
import budget.application.service.domain.TransactionItemService;
import budget.application.service.domain.TransactionService;
import io.github.bibekaryal86.shdsvc.Email;
import java.sql.SQLException;
import javax.sql.DataSource;

public final class AppContext {
  private final ScheduleManager scheduleManager;
  private final ServerManager serverManager;

  public AppContext() throws SQLException {
    DataSource dataSource = DataSourceFactory.create();
    Email email = new Email();

    CategoryTypeCache categoryTypeCache = new CategoryTypeCache();
    CategoryCache categoryCache = new CategoryCache();

    AccountDao accountDao = new AccountDao(dataSource.getConnection());
    BudgetDao budgetDao = new BudgetDao(dataSource.getConnection());
    CategoryDao categoryDao = new CategoryDao(dataSource.getConnection());
    CategoryTypeDao categoryTypeDao = new CategoryTypeDao(dataSource.getConnection());
    InsightsDao insightsDao = new InsightsDao(dataSource.getConnection());
    TransactionDao transactionDao = new TransactionDao(dataSource.getConnection());
    TransactionItemDao transactionItemDao = new TransactionItemDao(dataSource.getConnection());

    AccountService accountService = new AccountService(dataSource);
    BudgetService budgetService = new BudgetService(dataSource);
    InsightsService insightsService = new InsightsService(dataSource);
    CategoryTypeService categoryTypeService = new CategoryTypeService(dataSource);
    CategoryService categoryService = new CategoryService(dataSource);
    TransactionItemService transactionItemService = new TransactionItemService(dataSource);
    TransactionService transactionService = new TransactionService(dataSource, email);

    AccountHandler accountHandler = new AccountHandler(dataSource);
    AppTestsHandler appTestsHandler = new AppTestsHandler();
    BudgetHandler budgetHandler = new BudgetHandler(dataSource);
    CategoryHandler categoryHandler = new CategoryHandler(dataSource);
    CategoryTypeHandler categoryTypeHandler = new CategoryTypeHandler(dataSource);
    InsightsHandler insightsHandler = new InsightsHandler(dataSource);
    TransactionHandler transactionHandler = new TransactionHandler(dataSource, email);
    TransactionItemHandler transactionItemHandler = new TransactionItemHandler(dataSource);

    scheduleManager = new ScheduleManager(dataSource, email);
    serverManager =
        new ServerManager(
            appTestsHandler,
            accountHandler,
            budgetHandler,
            categoryTypeHandler,
            categoryHandler,
            insightsHandler,
            transactionItemHandler,
            transactionHandler);
  }

  public ScheduleManager getScheduleManager() {
    return scheduleManager;
  }

  public ServerManager getServerManager() {
    return serverManager;
  }
}
