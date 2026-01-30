package budget.application;

import budget.application.cache.AccountCache;
import budget.application.cache.CategoryCache;
import budget.application.cache.CategoryTypeCache;
import budget.application.db.dao.AccountDao;
import budget.application.db.dao.BudgetDao;
import budget.application.db.dao.CategoryDao;
import budget.application.db.dao.CategoryTypeDao;
import budget.application.db.dao.InsightsDao;
import budget.application.db.dao.TransactionDao;
import budget.application.db.dao.TransactionItemDao;
import budget.application.scheduler.ScheduleManager;
import budget.application.server.core.ServerContext;
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
import java.io.IOException;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.mockito.Mock;

public final class TestAppContext {
  private final ServerContext testServerContext;

  @Mock private Email testEmail;

  public TestAppContext(DataSource testDatasource) throws SQLException, IOException {
    TestDataSource.start();

    AccountCache accountCache = new AccountCache();
    CategoryTypeCache categoryTypeCache = new CategoryTypeCache();
    CategoryCache categoryCache = new CategoryCache();

    AccountDao accountDao = new AccountDao(testDatasource.getConnection());
    BudgetDao budgetDao = new BudgetDao(testDatasource.getConnection());
    CategoryDao categoryDao = new CategoryDao(testDatasource.getConnection());
    CategoryTypeDao categoryTypeDao = new CategoryTypeDao(testDatasource.getConnection());
    InsightsDao insightsDao = new InsightsDao(testDatasource.getConnection());
    TransactionDao transactionDao = new TransactionDao(testDatasource.getConnection());
    TransactionItemDao transactionItemDao = new TransactionItemDao(testDatasource.getConnection());

    AccountService accountService = new AccountService(testDatasource);
    BudgetService budgetService = new BudgetService(testDatasource);
    InsightsService insightsService = new InsightsService(testDatasource);
    CategoryTypeService categoryTypeService = new CategoryTypeService(testDatasource);
    CategoryService categoryService = new CategoryService(testDatasource);
    TransactionItemService transactionItemService = new TransactionItemService(testDatasource);
    TransactionService transactionService = new TransactionService(testDatasource, testEmail);

    AccountHandler accountHandler = new AccountHandler(testDatasource);
    AppTestsHandler appTestsHandler = new AppTestsHandler();
    BudgetHandler budgetHandler = new BudgetHandler(testDatasource);
    CategoryHandler categoryHandler = new CategoryHandler(testDatasource);
    CategoryTypeHandler categoryTypeHandler = new CategoryTypeHandler(testDatasource);
    InsightsHandler insightsHandler = new InsightsHandler(testDatasource);
    TransactionHandler transactionHandler = new TransactionHandler(testDatasource, testEmail);
    TransactionItemHandler transactionItemHandler = new TransactionItemHandler(testDatasource);

    testServerContext =
        new ServerContext(
            appTestsHandler,
            accountHandler,
            budgetHandler,
            categoryTypeHandler,
            categoryHandler,
            insightsHandler,
            transactionItemHandler,
            transactionHandler);
  }

  public ServerContext getTestServletContext() {
    return testServerContext;
  }
}
