package budget.application.service.domain;

import budget.application.db.dao.AccountBalancesDao;
import budget.application.db.dao.DaoFactory;
import budget.application.db.util.TransactionManager;
import budget.application.model.dto.InsightsResponse;
import budget.application.model.dto.RequestParams;
import budget.application.model.entity.AccountBalances;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountBalancesService {
  private static final Logger log = LoggerFactory.getLogger(AccountBalancesService.class);

  private final TransactionManager transactionManager;
  private final DaoFactory<AccountBalancesDao> accountBalancesDaoFactory;

  public AccountBalancesService(
      DataSource dataSource, DaoFactory<AccountBalancesDao> accountBalancesDaoFactory) {
    this.transactionManager = new TransactionManager(dataSource);
    this.accountBalancesDaoFactory = accountBalancesDaoFactory;
  }

  public void createAccountBalances(List<AccountBalances> accountBalances) throws SQLException {
    log.debug("Create Account Balances: AccountBalances={}", accountBalances);
    transactionManager.executeVoid(
        transactionContext -> {
          AccountBalancesDao accountBalancesDao =
              accountBalancesDaoFactory.create(transactionContext.connection());
          accountBalancesDao.createAccountBalances(accountBalances);
          log.debug("Created [{}] Account Balances", accountBalances.size());
        });
  }

  public InsightsResponse.AccountSummaries readAccountBalances(
      RequestParams.AccountSummaryParams requestParams) throws SQLException {
    log.debug("Read Account Balances: RequestParams=[{}]", requestParams);

    return transactionManager.execute(
        transactionContext -> {
          AccountBalancesDao accountBalancesDao =
              accountBalancesDaoFactory.create(transactionContext.connection());
          LocalDate beginDate = requestParams.beginDate();
          LocalDate endDate = requestParams.endDate();
          List<UUID> accountIds = requestParams.accountIds();
          List<InsightsResponse.AccountSummary> accountBalanceSummaries =
              accountBalancesDao.readAccountBalances(beginDate, endDate, accountIds);
          return new InsightsResponse.AccountSummaries(
              accountBalanceSummaries, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public void updateAccountBalances(LocalDate yearMonth, String notes, Map<UUID, BigDecimal> accountBalanceUpdates)
      throws SQLException {
    log.debug(
        "Update Account Balances: YearMonth=[{}], Notes=[{}], AccountBalanceUpdates={}",
        yearMonth,
        notes,
        accountBalanceUpdates);
    transactionManager.executeVoid(
        transactionContext -> {
          AccountBalancesDao accountBalancesDao =
              accountBalancesDaoFactory.create(transactionContext.connection());
          int rowsUpdated =
              accountBalancesDao.updateAccountBalances(yearMonth, notes, accountBalanceUpdates);
          log.debug("Updated [{}] Account Balances", rowsUpdated);
        });
  }
}
