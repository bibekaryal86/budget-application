package budget.application;

import budget.application.cache.CategoryCache;
import budget.application.cache.CategoryTypeCache;
import budget.application.db.dao.AccountBalancesDao;
import budget.application.db.dao.AccountDao;
import budget.application.db.dao.BudgetDao;
import budget.application.db.dao.CategoryDao;
import budget.application.db.dao.CategoryTypeDao;
import budget.application.db.dao.DaoFactory;
import budget.application.db.dao.InsightsDao;
import budget.application.db.dao.TransactionDao;
import budget.application.db.dao.TransactionItemDao;
import budget.application.event.AccountBalanceSubscriber;
import budget.application.event.TransactionEventBus;
import budget.application.scheduler.ScheduleManager;
import budget.application.server.core.ServerContext;
import budget.application.server.handlers.AccountHandler;
import budget.application.server.handlers.AppTestsHandler;
import budget.application.server.handlers.BudgetHandler;
import budget.application.server.handlers.CategoryHandler;
import budget.application.server.handlers.CategoryTypeHandler;
import budget.application.server.handlers.InsightsHandler;
import budget.application.server.handlers.TransactionHandler;
import budget.application.service.domain.AccountBalancesService;
import budget.application.service.domain.AccountService;
import budget.application.service.domain.BudgetService;
import budget.application.service.domain.CategoryService;
import budget.application.service.domain.CategoryTypeService;
import budget.application.service.domain.InsightsService;
import budget.application.service.domain.TransactionItemService;
import budget.application.service.domain.TransactionService;
import io.github.bibekaryal86.shdsvc.Email;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

public final class AppContext {
  private final ScheduleManager scheduleManager;
  private final ServerContext serverContext;

  public AppContext(DataSource dataSource, Email email) throws SQLException {
    CategoryTypeCache categoryTypeCache = new CategoryTypeCache();
    CategoryCache categoryCache = new CategoryCache();

    DaoFactory<AccountDao> accountDaoFactory = AccountDao::new;
    DaoFactory<AccountBalancesDao> accountBalancesDaoFactory = AccountBalancesDao::new;
    DaoFactory<BudgetDao> budgetDaoFactory = BudgetDao::new;
    DaoFactory<CategoryDao> categoryDaoFactory =
        connection -> new CategoryDao(connection, categoryCache);
    DaoFactory<CategoryTypeDao> categoryTypeDaoFactory =
        connection -> new CategoryTypeDao(connection, categoryTypeCache);
    DaoFactory<InsightsDao> insightsDaoFactory = InsightsDao::new;
    DaoFactory<TransactionDao> transactionDaoFactory = TransactionDao::new;
    DaoFactory<TransactionItemDao> transactionItemDaoFactory = TransactionItemDao::new;

    AccountService accountService = new AccountService(dataSource, accountDaoFactory);
    AccountBalancesService accountBalancesService =
        new AccountBalancesService(dataSource, accountBalancesDaoFactory);
    InsightsService insightsService = new InsightsService(dataSource, insightsDaoFactory);
    CategoryTypeService categoryTypeService =
        new CategoryTypeService(dataSource, categoryTypeDaoFactory);
    CategoryService categoryService =
        new CategoryService(dataSource, categoryDaoFactory, categoryTypeService);
    BudgetService budgetService = new BudgetService(dataSource, budgetDaoFactory, categoryService);
    TransactionItemService transactionItemService =
        new TransactionItemService(
            dataSource, transactionItemDaoFactory, categoryService, accountService);

    TransactionEventBus transactionEventBus = new TransactionEventBus();
    transactionEventBus.subscribe(
        new AccountBalanceSubscriber(accountService, accountBalancesService));

    TransactionService transactionService =
        new TransactionService(
            dataSource,
            email,
            transactionDaoFactory,
            transactionItemService,
            categoryService,
            categoryTypeService,
            accountService,
            transactionEventBus);

    AccountHandler accountHandler = new AccountHandler(accountService);
    AppTestsHandler appTestsHandler = new AppTestsHandler();
    BudgetHandler budgetHandler = new BudgetHandler(budgetService);
    CategoryHandler categoryHandler = new CategoryHandler(categoryService);
    CategoryTypeHandler categoryTypeHandler = new CategoryTypeHandler(categoryTypeService);
    InsightsHandler insightsHandler = new InsightsHandler(insightsService);
    TransactionHandler transactionHandler = new TransactionHandler(transactionService);

    scheduleManager = new ScheduleManager(dataSource, transactionService);
    serverContext =
        new ServerContext(
            appTestsHandler,
            accountHandler,
            budgetHandler,
            categoryTypeHandler,
            categoryHandler,
            insightsHandler,
            transactionHandler);

    // seed caches
    try (Connection connection = dataSource.getConnection()) {
      accountDaoFactory.create(connection).read(List.of());
      categoryDaoFactory.create(connection).read(List.of());
      categoryTypeDaoFactory.create(connection).read(List.of());
    }
  }

  public ScheduleManager getScheduleManager() {
    return scheduleManager;
  }

  public ServerContext getServerContext() {
    return serverContext;
  }
}
