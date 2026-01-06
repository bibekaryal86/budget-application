package budget.application.service.domain;

import budget.application.common.Exceptions;
import budget.application.common.Validations;
import budget.application.db.dao.AccountDao;
import budget.application.model.dto.AccountRequest;
import budget.application.model.dto.AccountResponse;
import budget.application.model.entity.Account;
import budget.application.service.util.ResponseMetadataUtils;
import budget.application.service.util.TransactionManager;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountService {
  private static final Logger log = LoggerFactory.getLogger(AccountService.class);

  private final TransactionManager tx;

  public AccountService(DataSource dataSource) {
    this.tx = new TransactionManager(dataSource);
  }

  public AccountResponse create(String requestId, AccountRequest ar) throws SQLException {
    log.debug("[{}] Create account: AccountRequest=[{}]", requestId, ar);
    return tx.execute(
        bs -> {
          AccountDao dao = new AccountDao(requestId, bs.connection());

          Validations.validateAccount(requestId, ar);

          Account aIn =
              new Account(
                  null,
                  ar.name(),
                  ar.accountType(),
                  ar.bankName(),
                  ar.openingBalance(),
                  ar.status(),
                  null,
                  null);
          Account aOut = dao.create(aIn);
          log.debug("[{}] Created account: Id=[{}]", requestId, aOut.id());
          AccountResponse.Account account =
              new AccountResponse.Account(
                  aOut.id(),
                  aOut.name(),
                  aOut.accountType(),
                  aOut.bankName(),
                  aOut.openingBalance(),
                  aOut.status());

          return new AccountResponse(
              List.of(account), ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public AccountResponse read(String requestId, List<UUID> ids) throws SQLException {
    log.debug("[{}] Read accounts: Ids={}", requestId, ids);
    return tx.execute(
        bs -> {
          AccountDao dao = new AccountDao(requestId, bs.connection());
          List<Account> aList = dao.read(ids);
          if (ids.size() == 1 && aList.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "Account", ids.getFirst().toString());
          }

          List<AccountResponse.Account> accounts =
              aList.stream()
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

  public AccountResponse update(String requestId, UUID id, AccountRequest ar) throws SQLException {
    log.debug("[{}] Update account: Id=[{}], AccountRequest=[{}]", requestId, id, ar);
    return tx.execute(
        bs -> {
          AccountDao dao = new AccountDao(requestId, bs.connection());
          Validations.validateAccount(requestId, ar);

          List<Account> aList = dao.read(List.of(id));
          if (aList.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "Account", id.toString());
          }

          Account aIn =
              new Account(
                  id,
                  ar.name(),
                  ar.accountType(),
                  ar.bankName(),
                  ar.openingBalance(),
                  ar.status(),
                  null,
                  null);
          Account aOut = dao.update(aIn);
          AccountResponse.Account account =
              new AccountResponse.Account(
                  aOut.id(),
                  aOut.name(),
                  aOut.accountType(),
                  aOut.bankName(),
                  aOut.openingBalance(),
                  aOut.status());
          return new AccountResponse(
              List.of(account), ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public AccountResponse delete(String requestId, List<UUID> ids) throws SQLException {
    log.info("[{}] Delete accounts: Ids=[{}]", requestId, ids);
    return tx.execute(
        bs -> {
          AccountDao dao = new AccountDao(requestId, bs.connection());

          List<Account> aList = dao.read(ids);
          if (ids.size() == 1 && aList.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "Account", ids.getFirst().toString());
          }

          int deleteCount = dao.delete(ids);
          return new AccountResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }
}
