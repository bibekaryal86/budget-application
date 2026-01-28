package budget.application.service.domain;

import budget.application.common.Exceptions;
import budget.application.common.Validations;
import budget.application.db.dao.AccountDao;
import budget.application.db.util.TransactionManager;
import budget.application.model.dto.AccountRequest;
import budget.application.model.dto.AccountResponse;
import budget.application.model.entity.Account;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountService {
  private static final Logger log = LoggerFactory.getLogger(AccountService.class);

  private final TransactionManager transactionManager;

  public AccountService(DataSource dataSource) {
    this.transactionManager = new TransactionManager(dataSource);
  }

  public AccountResponse create(String requestId, AccountRequest accountRequest)
      throws SQLException {
    log.debug("[{}] Create account: AccountRequest=[{}]", requestId, accountRequest);
    return transactionManager.execute(
        requestId,
        transactionContext -> {
          AccountDao accountDao = new AccountDao(requestId, transactionContext.connection());

          Validations.validateAccount(requestId, accountRequest);

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
          log.debug("[{}] Created account: Id=[{}]", requestId, accountOut.id());
          AccountResponse.Account account =
              new AccountResponse.Account(
                  accountOut.id(),
                  accountOut.name(),
                  accountOut.accountType(),
                  accountOut.bankName(),
                  accountOut.openingBalance(),
                  accountOut.status());

          return new AccountResponse(
              List.of(account), ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public AccountResponse read(String requestId, List<UUID> ids) throws SQLException {
    log.debug("[{}] Read accounts: Ids={}", requestId, ids);
    return transactionManager.execute(
        requestId,
        transactionContext -> {
          AccountDao accountDao = new AccountDao(requestId, transactionContext.connection());
          List<Account> accountList = accountDao.read(ids);
          if (ids.size() == 1 && accountList.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "Account", ids.getFirst().toString());
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
                              account.openingBalance(),
                              account.status()))
                  .toList();

          return new AccountResponse(accounts, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public AccountResponse update(String requestId, UUID id, AccountRequest accountRequest)
      throws SQLException {
    log.debug("[{}] Update account: Id=[{}], AccountRequest=[{}]", requestId, id, accountRequest);
    return transactionManager.execute(
        requestId,
        transactionContext -> {
          AccountDao accountDao = new AccountDao(requestId, transactionContext.connection());
          Validations.validateAccount(requestId, accountRequest);

          List<Account> accountList = accountDao.read(List.of(id));
          if (accountList.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "Account", id.toString());
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
          AccountResponse.Account account =
              new AccountResponse.Account(
                  accountOut.id(),
                  accountOut.name(),
                  accountOut.accountType(),
                  accountOut.bankName(),
                  accountOut.openingBalance(),
                  accountOut.status());
          return new AccountResponse(
              List.of(account), ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public AccountResponse delete(String requestId, List<UUID> ids) throws SQLException {
    log.info("[{}] Delete accounts: Ids=[{}]", requestId, ids);
    return transactionManager.execute(
        requestId,
        transactionContext -> {
          AccountDao accountDao = new AccountDao(requestId, transactionContext.connection());

          List<Account> accountList = accountDao.read(ids);
          if (ids.size() == 1 && accountList.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "Account", ids.getFirst().toString());
          }

          int deleteCount = accountDao.delete(ids);
          return new AccountResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }
}
