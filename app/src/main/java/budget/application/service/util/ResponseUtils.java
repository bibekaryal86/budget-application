package budget.application.service.util;

import budget.application.model.dto.AccountResponse;
import budget.application.model.entity.Account;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ResponseUtils {
  private ResponseUtils() {}

  public static ResponseMetadata defaultInsertResponseMetadata() {
    return new ResponseMetadata(
        ResponseMetadata.emptyResponseStatusInfo(),
        new ResponseMetadata.ResponseCrudInfo(1, 0, 0, 0),
        ResponseMetadata.emptyResponsePageInfo());
  }

  public static ResponseMetadata defaultUpdateResponseMetadata() {
    return new ResponseMetadata(
        ResponseMetadata.emptyResponseStatusInfo(),
        new ResponseMetadata.ResponseCrudInfo(0, 1, 0, 0),
        ResponseMetadata.emptyResponsePageInfo());
  }

  public static ResponseMetadata defaultDeleteResponseMetadata(int deletedCount) {
    return new ResponseMetadata(
        ResponseMetadata.emptyResponseStatusInfo(),
        new ResponseMetadata.ResponseCrudInfo(0, 0, deletedCount, 0),
        ResponseMetadata.emptyResponsePageInfo());
  }

  public static ResponseMetadata defaultStatusInfoResponseMetadata(String errMsg, Exception ex) {
    if (!CommonUtilities.isEmpty(errMsg)) {
      return new ResponseMetadata(
          new ResponseMetadata.ResponseStatusInfo(errMsg),
          ResponseMetadata.emptyResponseCrudInfo(),
          ResponseMetadata.emptyResponsePageInfo());
    } else if (ex != null && !CommonUtilities.isEmpty(ex.getMessage())) {
      return new ResponseMetadata(
          new ResponseMetadata.ResponseStatusInfo(ex.getMessage()),
          ResponseMetadata.emptyResponseCrudInfo(),
          ResponseMetadata.emptyResponsePageInfo());
    }
    return ResponseMetadata.emptyResponseMetadata();
  }

  public static AccountResponse getAccountResponse(
      List<Account> accountList,
      Map<UUID, List<AccountResponse.AccountBalanceHistory>> accountBalanceHistory,
      ResponseMetadata responseMetadata) {
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
                        account.status(),
                        accountBalanceHistory.getOrDefault(account.id(), List.of())))
            .sorted(Comparator.comparing(AccountResponse.Account::bankName))
            .toList();
    return new AccountResponse(accounts, responseMetadata);
  }
}
