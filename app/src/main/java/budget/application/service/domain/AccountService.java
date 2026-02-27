package budget.application.service.domain;

import budget.application.common.Constants;
import budget.application.common.Exceptions;
import budget.application.db.dao.AccountDao;
import budget.application.db.dao.DaoFactory;
import budget.application.db.util.TransactionManager;
import budget.application.model.dto.AccountRequest;
import budget.application.model.dto.AccountResponse;
import budget.application.model.entity.Account;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.math.BigDecimal;
import java.sql.Connection;
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
          validateAccount(accountRequest);

          Account accountIn =
              new Account(
                  null,
                  accountRequest.name(),
                  accountRequest.accountType(),
                  accountRequest.bankName(),
                  null,
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
                  accountOut.accountBalance(),
                  accountOut.status());

          return new AccountResponse(
              List.of(account), ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public List<Account> readNoEx(List<UUID> ids, Connection connection) {
    AccountDao accountDao = accountDaoFactory.create(connection);
    return accountDao.readNoEx(ids);
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

          List<AccountResponse.Account> accounts =
              accountList.stream()
                  .map(
                      account ->
                          new AccountResponse.Account(
                              account.id(),
                              account.name(),
                              account.accountType(),
                              account.bankName(),
                              account.accountBalance(),
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
          validateAccount(accountRequest);

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
                  null,
                  accountRequest.status(),
                  null,
                  null);
          Account accountOut = accountDao.update(accountIn);
          AccountResponse.Account account =
              new AccountResponse.Account(
                  accountOut.id(),
                  accountOut.name(),
                  accountOut.accountType(),
                  accountOut.bankName(),
                  accountOut.accountBalance(),
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

  public void updateAccountBalances(Map<UUID, BigDecimal> accountBalanceUpdates)
      throws SQLException {
    log.debug("Update account balance: AccountBalanceUpdates={}", accountBalanceUpdates);
    transactionManager.executeVoid(
        transactionContext -> {
          AccountDao accountDao = accountDaoFactory.create(transactionContext.connection());
          int rowsUpdated = accountDao.updateAccountBalances(accountBalanceUpdates);
          log.info("Updated [{}] accounts", rowsUpdated);
        });
  }

  // TODO remove this
  private BigDecimal getCurrentBalance(
      Account account, Map<UUID, AccountResponse.AccountCurrentBalanceCalc> currentBalanceCalcMap) {
    BigDecimal openingBalance = account.accountBalance();
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

  private void validateAccount(AccountRequest accountRequest) {
    if (accountRequest == null) {
      throw new Exceptions.BadRequestException("Account request cannot be null...");
    }
    if (CommonUtilities.isEmpty(accountRequest.name())) {
      throw new Exceptions.BadRequestException("Account name cannot be empty...");
    }
    if (CommonUtilities.isEmpty(accountRequest.accountType())) {
      throw new Exceptions.BadRequestException("Account type cannot be empty...");
    }
    if (!Constants.ACCOUNT_TYPES.contains(accountRequest.accountType())) {
      throw new Exceptions.BadRequestException("Account type is invalid...");
    }
    if (CommonUtilities.isEmpty(accountRequest.bankName())) {
      throw new Exceptions.BadRequestException("Bank name cannot be empty...");
    }
    if (CommonUtilities.isEmpty(accountRequest.status())) {
      throw new Exceptions.BadRequestException("Account status cannot be empty...");
    }
    if (!Constants.ACCOUNT_STATUSES.contains(accountRequest.status())) {
      throw new Exceptions.BadRequestException("Account status is invalid...");
    }
  }
}
