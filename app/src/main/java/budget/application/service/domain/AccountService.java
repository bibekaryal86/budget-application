package budget.application.service.domain;

import budget.application.common.Constants;
import budget.application.common.Exceptions;
import budget.application.common.Validations;
import budget.application.db.dao.AccountDao;
import budget.application.db.dao.DaoFactory;
import budget.application.db.util.TransactionManager;
import budget.application.model.dto.AccountRequest;
import budget.application.model.dto.AccountResponse;
import budget.application.model.entity.Account;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountService {
  private static final Logger log = LoggerFactory.getLogger(AccountService.class);

  private final TransactionManager transactionManager;
  private final DaoFactory<AccountDao> accountDaoFactory;

  public AccountService(DataSource dataSource, DaoFactory<AccountDao> accountDaoFactory) {
    this.transactionManager = new TransactionManager(dataSource);
    this.accountDaoFactory = accountDaoFactory;
  }

  public AccountResponse create(AccountRequest accountRequest) throws SQLException {
    log.debug("Create account: AccountRequest=[{}]", accountRequest);
    return transactionManager.execute(
        transactionContext -> {
          AccountDao accountDao = accountDaoFactory.create(transactionContext.connection());

          Validations.validateAccount(accountRequest);

          Account accountIn =
              new Account(
                  null,
                  accountRequest.name(),
                  accountRequest.accountType(),
                  accountRequest.bankName(),
                  accountRequest.openingBalance(),
                  accountRequest.status(),
                  null,
                  null);
          Account accountOut = accountDao.create(accountIn);
          log.debug("Created account: Id=[{}]", accountOut.id());
          AccountResponse.Account account =
              new AccountResponse.Account(
                  accountOut.id(),
                  accountOut.name(),
                  accountOut.accountType(),
                  accountOut.bankName(),
                  accountOut.openingBalance(),
                  accountOut.openingBalance(), // balances are same when account is created
                  accountOut.status());

          return new AccountResponse(
              List.of(account), ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public AccountResponse read(List<UUID> ids) throws SQLException {
    log.debug("Read accounts: Ids={}", ids);
    return transactionManager.execute(
        transactionContext -> {
          AccountDao accountDao = accountDaoFactory.create(transactionContext.connection());
          List<Account> accountList = accountDao.read(ids);
          if (ids.size() == 1 && accountList.isEmpty()) {
            throw new Exceptions.NotFoundException("Account", ids.getFirst().toString());
          }

          List<UUID> accountIds = accountList.stream().map(Account::id).toList();
          Map<UUID, AccountResponse.AccountCurrentBalanceCalc> currentBalanceCalcMap =
              accountDao.getTotalBalancesForCurrentBalance(accountIds);

          List<AccountResponse.Account> accounts =
              accountList.stream()
                  .map(
                      account ->
                          new AccountResponse.Account(
                              account.id(),
                              account.name(),
                              account.accountType(),
                              account.bankName(),
                              account.openingBalance(),
                              getCurrentBalance(account, currentBalanceCalcMap),
                              account.status()))
                  .sorted(Comparator.comparing(AccountResponse.Account::bankName))
                  .toList();

          return new AccountResponse(accounts, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public AccountResponse.AccountRefLists readAccountBanks() throws SQLException {
    log.debug("Read account banks");
    return transactionManager.execute(
        transactionContext -> {
          AccountDao accountDao = accountDaoFactory.create(transactionContext.connection());

          List<String> bankNames = accountDao.readAllBanks();
          return new AccountResponse.AccountRefLists(
              bankNames, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public AccountResponse update(UUID id, AccountRequest accountRequest) throws SQLException {
    log.debug("Update account: Id=[{}], AccountRequest=[{}]", id, accountRequest);
    return transactionManager.execute(
        transactionContext -> {
          AccountDao accountDao = accountDaoFactory.create(transactionContext.connection());
          Validations.validateAccount(accountRequest);

          List<Account> accountList = accountDao.read(List.of(id));
          if (accountList.isEmpty()) {
            throw new Exceptions.NotFoundException("Account", id.toString());
          }

          Account accountIn =
              new Account(
                  id,
                  accountRequest.name(),
                  accountRequest.accountType(),
                  accountRequest.bankName(),
                  accountRequest.openingBalance(),
                  accountRequest.status(),
                  null,
                  null);
          Account accountOut = accountDao.update(accountIn);
          Map<UUID, AccountResponse.AccountCurrentBalanceCalc> currentBalanceCalcMap =
              accountDao.getTotalBalancesForCurrentBalance(List.of(id));
          AccountResponse.Account account =
              new AccountResponse.Account(
                  accountOut.id(),
                  accountOut.name(),
                  accountOut.accountType(),
                  accountOut.bankName(),
                  accountOut.openingBalance(),
                  getCurrentBalance(accountOut, currentBalanceCalcMap),
                  accountOut.status());
          return new AccountResponse(
              List.of(account), ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public AccountResponse delete(List<UUID> ids) throws SQLException {
    log.info("Delete accounts: Ids=[{}]", ids);
    return transactionManager.execute(
        transactionContext -> {
          AccountDao accountDao = accountDaoFactory.create(transactionContext.connection());

          List<Account> accountList = accountDao.read(ids);
          if (ids.size() == 1 && accountList.isEmpty()) {
            throw new Exceptions.NotFoundException("Account", ids.getFirst().toString());
          }

          int deleteCount = accountDao.delete(ids);
          return new AccountResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }

  private BigDecimal getCurrentBalance(
      Account account, Map<UUID, AccountResponse.AccountCurrentBalanceCalc> currentBalanceCalcMap) {
    BigDecimal openingBalance = account.openingBalance();
    BigDecimal currentBalance = openingBalance;
    if (currentBalanceCalcMap.containsKey(account.id())) {
      BigDecimal totalIncomes = currentBalanceCalcMap.get(account.id()).totalIncome();
      BigDecimal totalExpenses = currentBalanceCalcMap.get(account.id()).totalExpense();
      BigDecimal totalTransfers = currentBalanceCalcMap.get(account.id()).totalTransfers();

      String accountType;
      if (Constants.ASSET_ACCOUNT_TYPES.contains(account.accountType())
          || Constants.INVEST_ACCOUNT_TYPES.contains(account.accountType())) {
        accountType = "POSITIVE";
      } else if (Constants.DEBT_ACCOUNT_TYPES.contains(account.accountType())) {
        accountType = "NEGATIVE";
      } else {
        throw new Exceptions.NotFoundException("Account", "Type");
      }

      switch (accountType) {
        case "POSITIVE" ->
            currentBalance =
                openingBalance.add(totalIncomes).subtract(totalExpenses).add(totalTransfers);
        case "NEGATIVE" ->
            currentBalance =
                openingBalance.subtract(totalIncomes).add(totalExpenses).subtract(totalTransfers);
      }
    }

    return currentBalance;
  }
}
