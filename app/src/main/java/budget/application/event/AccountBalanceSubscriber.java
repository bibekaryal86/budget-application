package budget.application.event;

import budget.application.common.Constants;
import budget.application.common.Exceptions;
import budget.application.model.dto.AccountResponse;
import budget.application.model.dto.CategoryResponse;
import budget.application.model.dto.TransactionItemResponse;
import budget.application.model.dto.TransactionResponse;
import budget.application.service.domain.AccountService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AccountBalanceSubscriber implements TransactionEventSubscriber {
  private static final Logger log = LoggerFactory.getLogger(AccountBalanceSubscriber.class);

  public enum AccountType {
    POSITIVE,
    NEGATIVE
  }

  private final AccountService accountService;

  public AccountBalanceSubscriber(AccountService accountService) {
    this.accountService = accountService;
  }

  @Override
  public void onEvent(TransactionEvent event) {
    switch (event.eventType()) {
      case TransactionEvent.Type.CREATE -> updateAccountBalanceOnCreate(event);
      case TransactionEvent.Type.UPDATE -> updateAccountBalanceOnUpdate(event);
      case TransactionEvent.Type.DELETE -> updateAccountBalanceOnDelete(event);
    }
  }

  private AccountType getPositiveNegativeAccountType(String accountType) {
    if (Constants.ASSET_ACCOUNT_TYPES.contains(accountType)
        || Constants.INVEST_ACCOUNT_TYPES.contains(accountType)) {
      return AccountType.POSITIVE;
    } else if (Constants.DEBT_ACCOUNT_TYPES.contains(accountType)) {
      return AccountType.NEGATIVE;
    } else {
      throw new Exceptions.NotFoundException("Account", "Type");
    }
  }

  private boolean isIncomeTransaction(CategoryResponse.Category category) {
    return Constants.CATEGORY_TYPE_TRANSFER_NAME.equals(category.categoryType().name());
  }

  private boolean isTransferInTransaction(CategoryResponse.Category category) {
    return Constants.CATEGORY_TYPE_TRANSFER_NAME.equals(category.categoryType().name())
        && Constants.CATEGORY_TRANSFER_IN.equalsIgnoreCase(category.name());
  }

  private boolean isTransferOutTransaction(CategoryResponse.Category category) {
    return Constants.CATEGORY_TYPE_TRANSFER_NAME.equals(category.categoryType().name())
        && Constants.CATEGORY_TRANSFER_OUT.equalsIgnoreCase(category.name());
  }

  private boolean isExpenseTransaction(CategoryResponse.Category category) {
    return !isIncomeTransaction(category)
        && !isTransferInTransaction(category)
        && !isTransferOutTransaction(category);
  }

  public BigDecimal calculateNewAccountBalance(
      TransactionEvent.Type eventType,
      CategoryResponse.Category category,
      AccountType accountType,
      BigDecimal accountBalance,
      BigDecimal transactionAmount) {
    boolean isIncomeTransaction = isIncomeTransaction(category);
    boolean isTransferInTransaction = isTransferInTransaction(category);
    boolean isTransferOutTransaction = isTransferOutTransaction(category);
    boolean isExpenseTransaction = isExpenseTransaction(category);

    BigDecimal delta = BigDecimal.ZERO;

    switch (accountType) {
      case AccountType.POSITIVE:
        if (isIncomeTransaction || isTransferInTransaction) {
          delta = transactionAmount;
        } else if (isExpenseTransaction || isTransferOutTransaction) {
          delta = transactionAmount.negate();
        }
        break;
      case AccountType.NEGATIVE:
        if (isIncomeTransaction || isTransferInTransaction) {
          delta = transactionAmount.negate();
        } else if (isExpenseTransaction || isTransferOutTransaction) {
          delta = transactionAmount;
        }
        break;
    }

    if (eventType == TransactionEvent.Type.DELETE) {
      delta = delta.negate();
    }

    return accountBalance.add(delta);
  }

  private void processAccountBalanceUpdates(
      List<TransactionItemResponse.TransactionItem> transactionItems,
      TransactionEvent.Type eventType,
      Map<UUID, BigDecimal> accountBalanceUpdates) {
    for (TransactionItemResponse.TransactionItem transactionItem : transactionItems) {
      AccountResponse.Account account = transactionItem.account();
      CategoryResponse.Category category = transactionItem.category();
      BigDecimal transactionAmount = transactionItem.amount();
      AccountType accountType = getPositiveNegativeAccountType(account.accountType());

      BigDecimal currentAccountBalance = accountBalanceUpdates.getOrDefault(account.id(), null);
      if (currentAccountBalance == null) {
        currentAccountBalance = account.accountBalance();
      }
      BigDecimal newAccountBalance =
          calculateNewAccountBalance(
              eventType, category, accountType, currentAccountBalance, transactionAmount);

      accountBalanceUpdates.put(account.id(), newAccountBalance);
    }
  }

  public void updateAccountBalanceOnCreate(TransactionEvent event) {
    try {
      Map<UUID, BigDecimal> accountBalanceUpdates = new HashMap<>();

      List<TransactionItemResponse.TransactionItem> transactionItems =
          event.transactionResponse().getFirst().items();
      processAccountBalanceUpdates(transactionItems, event.eventType(), accountBalanceUpdates);

      accountService.updateAccountBalances(accountBalanceUpdates);
    } catch (Exception e) {
      log.error("Error updating account balance for transaction create: [{}]", event, e);
    }
  }

  public void updateAccountBalanceOnUpdate(TransactionEvent event) {
    try {
      Map<UUID, BigDecimal> accountBalanceUpdates = new HashMap<>();

      // update account balance from before update transaction
      List<TransactionItemResponse.TransactionItem> transactionItems =
          event.transactionResponseBeforeUpdate().stream()
              .map(TransactionResponse.Transaction::items)
              .flatMap(List::stream)
              .toList();
      processAccountBalanceUpdates(
          transactionItems, TransactionEvent.Type.DELETE, accountBalanceUpdates);

      // update account balance from after update transaction
      transactionItems =
          event.transactionResponse().stream()
              .map(TransactionResponse.Transaction::items)
              .flatMap(List::stream)
              .toList();
      processAccountBalanceUpdates(
          transactionItems, TransactionEvent.Type.CREATE, accountBalanceUpdates);

      accountService.updateAccountBalances(accountBalanceUpdates);
    } catch (Exception e) {
      log.error("Error updating account balance for transaction update: [{}]", event, e);
    }
  }

  public void updateAccountBalanceOnDelete(TransactionEvent event) {
    try {
      Map<UUID, BigDecimal> accountBalanceUpdates = new HashMap<>();

      List<TransactionItemResponse.TransactionItem> transactionItems =
          event.transactionResponseBeforeUpdate().stream()
              .map(TransactionResponse.Transaction::items)
              .flatMap(List::stream)
              .toList();
      processAccountBalanceUpdates(transactionItems, event.eventType(), accountBalanceUpdates);

      accountService.updateAccountBalances(accountBalanceUpdates);
    } catch (Exception e) {
      log.error("Error updating account balance for transaction delete: [{}]", event, e);
    }
  }
}
