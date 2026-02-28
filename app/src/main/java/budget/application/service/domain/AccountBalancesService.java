package budget.application.service.domain;

import budget.application.db.dao.AccountBalancesDao;
import budget.application.db.dao.DaoFactory;
import budget.application.db.util.DaoUtils;
import budget.application.db.util.TransactionManager;
import budget.application.model.dto.AccountResponse;
import budget.application.model.dto.InsightsResponse;
import budget.application.model.dto.RequestParams;
import budget.application.model.entity.Account;
import budget.application.model.entity.AccountBalances;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.math.BigDecimal;
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
  private final AccountService accountService;

  public AccountBalancesService(
      DataSource dataSource,
      DaoFactory<AccountBalancesDao> accountBalancesDaoFactory,
      AccountService accountService) {
    this.transactionManager = new TransactionManager(dataSource);
    this.accountBalancesDaoFactory = accountBalancesDaoFactory;
    this.accountService = accountService;
  }

  public void createAccountBalances() throws SQLException {
    log.debug("Create Account Balances: YearMonth={}", LocalDate.now());
    transactionManager.executeVoid(
        transactionContext -> {
          List<Account> accounts =
              accountService.readNoEx(List.of(), transactionContext.connection());
          List<AccountBalances> accountBalances =
              accounts.stream()
                  .map(
                      account ->
                          new AccountBalances(
                              null,
                              account.id(),
                              LocalDate.now().plusDays(1),
                              account.accountBalance(),
                              "",
                              null,
                              null))
                  .toList();

          AccountBalancesDao accountBalancesDao =
              accountBalancesDaoFactory.create(transactionContext.connection());
          accountBalancesDao.createAccountBalances(accountBalances);
          log.debug("Created [{}] Account Balances", accountBalances.size());
        });
  }

  public InsightsResponse.AccountSummaries readAccountBalances(
      RequestParams.AccountSummaryParams requestParams) throws SQLException {
    log.debug("Read Account Balances: RequestParams=[{}]", requestParams);
    LocalDate beginDate = requestParams.beginDate();
    LocalDate endDate = requestParams.endDate();
    List<UUID> accountIds = requestParams.accountIds();

    AccountResponse accountResponse = accountService.read(accountIds);
    InsightsResponse.AccountSummary currentMonth =
        new InsightsResponse.AccountSummary(
            DaoUtils.getYearMonth(LocalDate.now()), accountResponse.data());

    List<InsightsResponse.AccountSummary> accountBalanceSummaries =
        transactionManager.execute(
            transactionContext -> {
              AccountBalancesDao accountBalancesDao =
                  accountBalancesDaoFactory.create(transactionContext.connection());

              return accountBalancesDao.readAccountBalances(beginDate, endDate, accountIds);
            });

    accountBalanceSummaries.addFirst(currentMonth);
    return new InsightsResponse.AccountSummaries(
        accountBalanceSummaries, ResponseMetadata.emptyResponseMetadata());
  }

  public void updateAccountBalances(
      LocalDate yearMonth, String notes, Map<UUID, BigDecimal> accountBalanceUpdates)
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
