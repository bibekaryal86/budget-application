package budget.application.service.domain;

import budget.application.db.dao.AccountBalancesDao;
import budget.application.db.dao.DaoFactory;
import budget.application.db.util.TransactionManager;
import budget.application.model.dto.InsightsResponse;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
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

  public int createAccountBalances(UUID accountId, String yearMonth) throws SQLException {
    log.debug("Create Account Balances: AccountId=[{}], YearMonth=[{}]", accountId, yearMonth);
    return 0;
  }

  public InsightsResponse.AccountBalancesSummaries readAccountBalances(
      UUID accountId, String yearMonth) throws SQLException {
    log.debug("Read Account Balances: AccountId=[{}], YearMonth=[{}]", accountId, yearMonth);
    return null;
  }

  public void updateAccountBalances(
      UUID accountId, String yearMonth, BigDecimal accountBalance, String notes)
      throws SQLException {
    log.debug(
        "Update Account Balances: AccountId=[{}], YearMonth=[{}], AccountBalance=[{}], Notes=[{}]",
        accountId,
        yearMonth,
        accountBalance,
        notes);
    transactionManager.executeVoid(
        transactionContext -> {
          AccountBalancesDao accountBalancesDao =
              accountBalancesDaoFactory.create(transactionContext.connection());
          int rowsUpdated =
              accountBalancesDao.updateAccountBalances(accountId, yearMonth, accountBalance, notes);
          log.debug("Updated [{}] Account Balances", rowsUpdated);
        });
  }

  public int deleteAccountBalances(List<UUID> accountIds, Connection connection)
      throws SQLException {
    log.info("Delete Account Balances: AccountIds=[{}]", accountIds);
    if (connection == null) {
      return transactionManager.execute(
          transactionContext -> deleteAccountBalances(accountIds, transactionContext.connection()));
    }
    AccountBalancesDao accountBalancesDao = accountBalancesDaoFactory.create(connection);
    return accountBalancesDao.deleteAccountBalances(accountIds);
  }
}
