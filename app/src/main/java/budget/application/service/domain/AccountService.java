package budget.application.service.domain;

import budget.application.common.Constants;
import budget.application.common.Exceptions;
import budget.application.db.dao.AccountBalancesDao;
import budget.application.db.dao.AccountDao;
import budget.application.db.dao.DaoFactory;
import budget.application.db.util.DaoUtils;
import budget.application.db.util.TransactionManager;
import budget.application.model.dto.AccountRequest;
import budget.application.model.dto.AccountResponse;
import budget.application.model.entity.Account;
import budget.application.service.util.ResponseUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
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
  private final DaoFactory<AccountBalancesDao> accountBalancesDaoFactory;

  public AccountService(
      DataSource dataSource,
      DaoFactory<AccountDao> accountDaoFactory,
      DaoFactory<AccountBalancesDao> accountBalancesDaoFactory) {
    this.transactionManager = new TransactionManager(dataSource);
    this.accountDaoFactory = accountDaoFactory;
    this.accountBalancesDaoFactory = accountBalancesDaoFactory;
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
          return ResponseUtils.getAccountResponse(
              List.of(accountOut), Map.of(), ResponseUtils.defaultInsertResponseMetadata());
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

          AccountBalancesDao accountBalancesDao =
              accountBalancesDaoFactory.create(transactionContext.connection());
          // get account balances history
          Map<UUID, List<AccountResponse.AccountBalanceHistory>> accountBalanceHistoryMap =
              accountBalancesDao.readAccountBalancesHistory(ids);
          // get account beginning balances
          Map<UUID, AccountResponse.AccountBalanceHistory> accountBeginningBalancesMap =
              accountBalancesDao.readAccountBeginningBalances(ids);
          // get current account balances
          String currentYearMonth = DaoUtils.getYearMonth(LocalDate.now());

          for (Account account : accountList) {
            List<AccountResponse.AccountBalanceHistory> history =
                accountBalanceHistoryMap.computeIfAbsent(account.id(), _ -> new ArrayList<>());

            // add beginning balance as the first element of the list
            AccountResponse.AccountBalanceHistory beginning =
                accountBeginningBalancesMap.get(account.id());
            if (beginning != null
                && (history.isEmpty()
                    || !history.getFirst().yearMonth().equals(beginning.yearMonth()))) {
              history.addFirst(beginning);
            }

            // add the current month account balance history to the end of the list
            history.removeIf(h -> h.yearMonth().equals(currentYearMonth));
            history.add(
                new AccountResponse.AccountBalanceHistory(
                    currentYearMonth, account.accountBalance()));
          }

          return ResponseUtils.getAccountResponse(
              accountList, accountBalanceHistoryMap, ResponseMetadata.emptyResponseMetadata());
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

          return ResponseUtils.getAccountResponse(
              List.of(accountOut),
              Map.of(),
              ResponseUtils.defaultUpdateResponseMetadata());
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
              List.of(), ResponseUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }

  public void updateAccountBalances(
      String mdcRequestId, Map<UUID, BigDecimal> accountBalanceUpdates) throws SQLException {
    log.info(
        "[{}] Update account balance: AccountBalanceUpdates={}",
        mdcRequestId,
        accountBalanceUpdates);
    transactionManager.executeVoid(
        transactionContext -> {
          AccountDao accountDao = accountDaoFactory.create(transactionContext.connection());
          int rowsUpdated = accountDao.updateAccountBalances(accountBalanceUpdates);
          log.info("[{}] Updated [{}] account balances", mdcRequestId, rowsUpdated);
        });
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
