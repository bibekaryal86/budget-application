package budget.application.common;

import budget.application.db.dao.AccountDao;
import budget.application.db.dao.CategoryDao;
import budget.application.db.dao.CategoryTypeDao;
import budget.application.model.dto.AccountRequest;
import budget.application.model.dto.BudgetRequest;
import budget.application.model.dto.CategoryRequest;
import budget.application.model.dto.CategoryTypeRequest;
import budget.application.model.dto.TransactionItemRequest;
import budget.application.model.dto.TransactionRequest;
import budget.application.model.entity.Account;
import budget.application.model.entity.Category;
import budget.application.model.entity.CategoryType;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Validations {
  private Validations() {}

  public static void validateAccount(AccountRequest accountRequest) {
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
    if (accountRequest.openingBalance() == null || accountRequest.openingBalance().intValue() < 0) {
      throw new Exceptions.BadRequestException("Opening balance cannot be null or negative...");
    }
    if (CommonUtilities.isEmpty(accountRequest.status())) {
      throw new Exceptions.BadRequestException("Account status cannot be empty...");
    }
    if (!Constants.ACCOUNT_STATUSES.contains(accountRequest.status())) {
      throw new Exceptions.BadRequestException("Account status is invalid...");
    }
  }

  public static void validateBudget(BudgetRequest budgetRequest, CategoryDao categoryDao) {
    if (budgetRequest == null) {
      throw new Exceptions.BadRequestException("Budget request cannot be null...");
    }
    if (budgetRequest.categoryId() == null) {
      throw new Exceptions.BadRequestException("Budget category cannot be null...");
    }
    if (budgetRequest.budgetMonth() < 1 || budgetRequest.budgetMonth() > 12) {
      throw new Exceptions.BadRequestException("Budget month should be between 1 and 12...");
    }
    if (budgetRequest.budgetYear() < 2025 || budgetRequest.budgetYear() > 2100) {
      throw new Exceptions.BadRequestException("Budget year should be between 2025 and 2100...");
    }

    if (budgetRequest.amount() == null || budgetRequest.amount().intValue() < 1) {
      throw new Exceptions.BadRequestException("Budget amount cannot be zero or negative...");
    }

    List<Category> categories = categoryDao.readNoEx(List.of(budgetRequest.categoryId()));
    if (CommonUtilities.isEmpty(categories)) {
      throw new Exceptions.BadRequestException("Category does not exist...");
    }
  }

  public static void validateCategory(
      CategoryRequest categoryRequest, CategoryTypeDao categoryTypeDao) {
    if (categoryRequest == null) {
      throw new Exceptions.BadRequestException("Category request cannot be null...");
    }
    if (categoryRequest.categoryTypeId() == null) {
      throw new Exceptions.BadRequestException("Category type cannot be null...");
    }
    if (CommonUtilities.isEmpty(categoryRequest.name())) {
      throw new Exceptions.BadRequestException("Category name cannot be empty...");
    }
    List<CategoryType> categoryTypes =
        categoryTypeDao.readNoEx(List.of(categoryRequest.categoryTypeId()));
    if (CommonUtilities.isEmpty(categoryTypes)) {
      throw new Exceptions.BadRequestException("Category type does not exist...");
    }
  }

  public static void validateCategoryType(CategoryTypeRequest categoryTypeRequest) {
    if (categoryTypeRequest == null) {
      throw new Exceptions.BadRequestException("Category type request cannot be null...");
    }
    if (CommonUtilities.isEmpty(categoryTypeRequest.name())) {
      throw new Exceptions.BadRequestException("Category type name cannot be empty...");
    }
  }

  public static void validateTransactionItem(
      TransactionItemRequest transactionItemRequest,
      Boolean isCreateTransaction,
      CategoryDao categoryDao,
      List<Category> categories,
      AccountDao accountDao,
      List<Account> accounts) {
    if (transactionItemRequest == null) {
      throw new Exceptions.BadRequestException("Transaction item request cannot be null...");
    }
    if (!isCreateTransaction && transactionItemRequest.transactionId() == null) {
      throw new Exceptions.BadRequestException("Transaction item transaction cannot be null...");
    }
    if (transactionItemRequest.categoryId() == null) {
      throw new Exceptions.BadRequestException("Transaction item category cannot be null...");
    }
    if (transactionItemRequest.accountId() == null) {
      throw new Exceptions.BadRequestException("Transaction item account cannot be null...");
    }
    if (transactionItemRequest.amount() == null
        || transactionItemRequest.amount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new Exceptions.BadRequestException(
          "Transaction item amount cannot be null or negative...");
    }

    if (CommonUtilities.isEmpty(categories)) {
      categories = categoryDao.readNoEx(List.of(transactionItemRequest.categoryId()));
    }

    Category category =
        categories.stream()
            .filter(cat -> cat.id().equals(transactionItemRequest.categoryId()))
            .findFirst()
            .orElse(null);

    if (category == null) {
      throw new Exceptions.BadRequestException("Category does not exist...");
    }

    if (CommonUtilities.isEmpty(accounts)) {
      accounts = accountDao.readNoEx(List.of(transactionItemRequest.accountId()));
    }

    Account account =
        accounts.stream()
            .filter(acc -> acc.id().equals(transactionItemRequest.accountId()))
            .findFirst()
            .orElse(null);

    if (account == null) {
      throw new Exceptions.BadRequestException("Account does not exist...");
    }
  }

  public static void validateTransaction(
      TransactionRequest transactionRequest,
      CategoryDao categoryDao,
      CategoryTypeDao categoryTypeDao,
      AccountDao accountDao) {
    if (transactionRequest == null) {
      throw new Exceptions.BadRequestException("Transaction request cannot be null...");
    }
    if (CommonUtilities.isEmpty(transactionRequest.merchant())) {
      throw new Exceptions.BadRequestException("Transaction merchant cannot be empty...");
    }
    if (transactionRequest.totalAmount() == null
        || transactionRequest.totalAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new Exceptions.BadRequestException("Transaction total cannot be null or negative...");
    }
    if (CommonUtilities.isEmpty(transactionRequest.items())) {
      throw new Exceptions.BadRequestException("Transaction must have at least one item...");
    }
    BigDecimal sumItems =
        transactionRequest.items().stream()
            .map(TransactionItemRequest::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (sumItems.compareTo(transactionRequest.totalAmount()) != 0) {
      throw new Exceptions.BadRequestException("Total amount does not match sum of items...");
    }

    List<UUID> categoryIds =
        transactionRequest.items().stream()
            .map(TransactionItemRequest::categoryId)
            .collect(Collectors.toSet())
            .stream()
            .toList();
    List<Category> categories = categoryDao.readNoEx(categoryIds);
    if (CommonUtilities.isEmpty(categories) || (categories.size() != categoryIds.size())) {
      throw new Exceptions.BadRequestException("Category does not exist...");
    }

    List<UUID> categoryTypeIds =
        categories.stream().map(Category::categoryTypeId).collect(Collectors.toSet()).stream()
            .toList();
    List<CategoryType> categoryTypes = categoryTypeDao.readNoEx(categoryTypeIds);
    if (CommonUtilities.isEmpty(categoryTypes)
        || (categoryTypes.size() != categoryTypeIds.size())) {
      throw new Exceptions.BadRequestException("Category type does not exist...");
    }

    Set<String> categoryTypeNames =
        categoryTypes.stream().map(CategoryType::name).collect(Collectors.toSet());
    for (String categoryTypeName : Constants.NO_EXPENSE_CATEGORY_TYPES) {
      if (categoryTypeNames.contains(categoryTypeName) && categoryTypeNames.size() > 1) {
        throw new Exceptions.BadRequestException(
            String.format(
                "Category type [%s] cannot be mixed with other category types...",
                categoryTypeName));
      }
      if (categoryTypeName.equals(Constants.CATEGORY_TYPE_TRANSFER_NAME) && transactionRequest.items().size() != 2) {
        throw new Exceptions.BadRequestException("Transfer transaction must have exactly 2 items...");
      }
    }

    for (TransactionItemRequest transactionItemRequest : transactionRequest.items()) {
      validateTransactionItem(
          transactionItemRequest, Boolean.TRUE, categoryDao, categories, accountDao, List.of());
    }
  }
}
