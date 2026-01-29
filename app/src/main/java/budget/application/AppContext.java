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
import budget.application.server.core.ServerNetty;
import budget.application.server.handlers.AccountHandler;
import budget.application.server.handlers.AppTestsHandler;
import budget.application.server.handlers.BudgetHandler;
import budget.application.server.handlers.CategoryHandler;
import budget.application.server.handlers.CategoryTypeHandler;
import budget.application.server.handlers.InsightsHandler;
import budget.application.server.handlers.NotFoundHandler;
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
  public final ScheduleManager scheduleManager;

  public final CategoryTypeCache categoryTypeCache;
  public final CategoryCache categoryCache;

  public final AccountDao accountDao;
  public final BudgetDao budgetDao;
  public final CategoryDao categoryDao;
  public final CategoryTypeDao categoryTypeDao;
  public final InsightsDao insightsDao;
  public final TransactionDao transactionDao;
  public final TransactionItemDao transactionItemDao;

  public final AccountService accountService;
  public final BudgetService budgetService;
  public final InsightsService insightsService;
  public final CategoryTypeService categoryTypeService;
  public final CategoryService categoryService;
  public final TransactionItemService transactionItemService;
  public final TransactionService transactionService;

  public final AccountHandler accountHandler;
  public final AppTestsHandler appTestsHandler;
  public final BudgetHandler budgetHandler;
  public final CategoryHandler categoryHandler;
  public final CategoryTypeHandler categoryTypeHandler;
  public final InsightsHandler insightsHandler;
  public final NotFoundHandler notFoundHandler;
  public final TransactionHandler transactionHandler;
  public final TransactionItemHandler transactionItemHandler;

  public final ServerNetty serverNetty;

  public AppContext() throws SQLException {
    DataSource dataSource = DataSourceFactory.create();
    Email email = new Email();

    this.scheduleManager = new ScheduleManager(dataSource, email);

    this.categoryTypeCache = new CategoryTypeCache();
    this.categoryCache = new CategoryCache();

    this.accountDao = new AccountDao(dataSource.getConnection());
    this.budgetDao = new BudgetDao(dataSource.getConnection());
    this.categoryDao = new CategoryDao(dataSource.getConnection());
    this.categoryTypeDao = new CategoryTypeDao(dataSource.getConnection());
    this.insightsDao = new InsightsDao(dataSource.getConnection());
    this.transactionDao = new TransactionDao(dataSource.getConnection());
    this.transactionItemDao = new TransactionItemDao(dataSource.getConnection());

    this.accountService = new AccountService(dataSource);
    this.budgetService = new BudgetService(dataSource);
    this.insightsService = new InsightsService(dataSource);
    this.categoryTypeService = new CategoryTypeService(dataSource);
    this.categoryService = new CategoryService(dataSource);
    this.transactionItemService = new TransactionItemService(dataSource);
    this.transactionService = new TransactionService(dataSource, email);

    this.accountHandler = new AccountHandler(dataSource);
    this.appTestsHandler = new AppTestsHandler();
    this.budgetHandler = new BudgetHandler(dataSource);
    this.categoryHandler = new CategoryHandler(dataSource);
    this.categoryTypeHandler = new CategoryTypeHandler(dataSource);
    this.insightsHandler = new InsightsHandler(dataSource);
    this.notFoundHandler = new NotFoundHandler();
    this.transactionHandler = new TransactionHandler(dataSource, email);
    this.transactionItemHandler = new TransactionItemHandler(dataSource);

    this.serverNetty = new ServerNetty(dataSource, email);
  }
}
