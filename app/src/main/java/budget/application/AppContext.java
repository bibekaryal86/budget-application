package budget.application;

import budget.application.cache.AccountCache;
import budget.application.cache.CategoryCache;
import budget.application.cache.CategoryTypeCache;
import budget.application.db.dao.AccountDao;
import budget.application.db.dao.BudgetDao;
import budget.application.db.dao.CategoryDao;
import budget.application.db.dao.CategoryTypeDao;
import budget.application.db.dao.DaoFactory;
import budget.application.db.dao.InsightsDao;
import budget.application.db.dao.TransactionDao;
import budget.application.db.dao.TransactionItemDao;
import budget.application.db.util.DataSourceFactory;
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
import java.sql.SQLException;
import javax.sql.DataSource;

public final class AppContext {
  private final ScheduleManager scheduleManager;
  private final ServerContext serverContext;

  public AppContext() throws SQLException {
    DataSource dataSource = DataSourceFactory.create();
    Email email = new Email();

    AccountCache accountCache = new AccountCache();
    CategoryTypeCache categoryTypeCache = new CategoryTypeCache();
    CategoryCache categoryCache = new CategoryCache();

    DaoFactory<AccountDao> accountDaoFactory =
        connection -> new AccountDao(connection, accountCache);
    DaoFactory<BudgetDao> budgetDaoFactory = BudgetDao::new;
    DaoFactory<CategoryDao> categoryDaoFactory =
        connection -> new CategoryDao(connection, categoryCache);
    DaoFactory<CategoryTypeDao> categoryTypeDaoFactory =
        connection -> new CategoryTypeDao(connection, categoryTypeCache);
    DaoFactory<InsightsDao> insightsDaoFactory = InsightsDao::new;
    DaoFactory<TransactionDao> transactionDaoFactory = TransactionDao::new;
    DaoFactory<TransactionItemDao> transactionItemDaoFactory = TransactionItemDao::new;

    AccountService accountService = new AccountService(dataSource, accountDaoFactory);
    BudgetService budgetService =
        new BudgetService(dataSource, budgetDaoFactory, categoryDaoFactory);
    InsightsService insightsService = new InsightsService(dataSource, insightsDaoFactory);
    CategoryTypeService categoryTypeService =
        new CategoryTypeService(dataSource, categoryTypeDaoFactory);
    CategoryService categoryService =
        new CategoryService(dataSource, categoryDaoFactory, categoryTypeDaoFactory);
    TransactionItemService transactionItemService =
        new TransactionItemService(dataSource, transactionItemDaoFactory, categoryDaoFactory);
    TransactionService transactionService =
        new TransactionService(
            dataSource,
            email,
            transactionDaoFactory,
            transactionItemDaoFactory,
            categoryDaoFactory,
            categoryTypeDaoFactory);

    AccountHandler accountHandler = new AccountHandler(accountService);
    AppTestsHandler appTestsHandler = new AppTestsHandler();
    BudgetHandler budgetHandler = new BudgetHandler(budgetService);
    CategoryHandler categoryHandler = new CategoryHandler(categoryService);
    CategoryTypeHandler categoryTypeHandler = new CategoryTypeHandler(categoryTypeService);
    InsightsHandler insightsHandler = new InsightsHandler(insightsService);
    TransactionHandler transactionHandler = new TransactionHandler(transactionService);
    TransactionItemHandler transactionItemHandler =
        new TransactionItemHandler(transactionItemService);

    scheduleManager = new ScheduleManager(dataSource, transactionService);
    serverContext =
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

  public ScheduleManager getScheduleManager() {
    return scheduleManager;
  }

  public ServerContext getServerContext() {
    return serverContext;
  }
}
