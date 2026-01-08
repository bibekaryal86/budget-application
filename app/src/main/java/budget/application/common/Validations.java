package budget.application.common;

import budget.application.db.dao.CategoryDao;
import budget.application.db.dao.CategoryTypeDao;
import budget.application.model.dto.AccountRequest;
import budget.application.model.dto.BudgetRequest;
import budget.application.model.dto.CategoryRequest;
import budget.application.model.dto.CategoryResponse;
import budget.application.model.dto.CategoryTypeRequest;
import budget.application.model.dto.TransactionItemRequest;
import budget.application.model.dto.TransactionRequest;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.math.BigDecimal;

public class Validations {
  private Validations() {}

  public static void validateAccount(String requestId, AccountRequest ar) {
    if (ar == null) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Account request cannot be null...", requestId));
    }
    if (CommonUtilities.isEmpty(ar.name())) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Account name cannot be empty...", requestId));
    }
    if (CommonUtilities.isEmpty(ar.accountType())) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Account type cannot be empty...", requestId));
    }
    if (!Constants.ACCOUNT_TYPES.contains(ar.accountType())) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Account type is invalid...", requestId));
    }
    if (CommonUtilities.isEmpty(ar.bankName())) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Bank name cannot be empty...", requestId));
    }
    if (ar.openingBalance() == null || ar.openingBalance().intValue() < 0) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Opening balance cannot be null or negative...", requestId));
    }
    if (CommonUtilities.isEmpty(ar.status())) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Account status cannot be empty...", requestId));
    }
    if (!Constants.ACCOUNT_STATUSES.contains(ar.status())) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Account status is invalid...", requestId));
    }
  }

  public static void validateBudget(String requestId, BudgetRequest br, CategoryDao categoryDao) {
    if (br == null) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Budget request cannot be null...", requestId));
    }
    if (br.categoryId() == null) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Budget category cannot be null...", requestId));
    }
    if (br.budgetMonth() < 1 || br.budgetMonth() > 12) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Budget month should be between 1 and 12...", requestId));
    }
    if (br.budgetYear() < 2025 || br.budgetYear() > 2100) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Budget year should be between 2025 and 2100...", requestId));
    }

    if (br.amount() == null || br.amount().intValue() < 1) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Budget amount cannot be zero or negative...", requestId));
    }

    CategoryResponse.Category category = categoryDao.readByIdNoEx(br.categoryId()).orElse(null);
    if (category == null) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Category does not exist...", requestId));
    }
  }

  public static void validateCategory(
      String requestId, CategoryRequest cr, CategoryTypeDao categoryTypeDao) {
    if (cr == null) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Category request cannot be null...", requestId));
    }
    if (cr.categoryTypeId() == null) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Category type cannot be null...", requestId));
    }
    if (CommonUtilities.isEmpty(cr.name())) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Category name cannot be empty...", requestId));
    }
    if (categoryTypeDao.readByIdNoEx(cr.categoryTypeId()).isEmpty()) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Category type does not exist...", requestId));
    }
  }

  public static void validateCategoryType(String requestId, CategoryTypeRequest ctr) {
    if (ctr == null) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Category type request cannot be null...", requestId));
    }
    if (CommonUtilities.isEmpty(ctr.name())) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Category type name cannot be empty...", requestId));
    }
  }

  public static void validateTransactionItem(
      String requestId, TransactionItemRequest tir, CategoryDao categoryDao, Boolean isCreateTxn) {
    if (tir == null) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Transaction item request cannot be null...", requestId));
    }
    if (!isCreateTxn && tir.transactionId() == null) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Transaction item transaction cannot be null...", requestId));
    }
    if (tir.categoryId() == null) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Transaction item category cannot be null...", requestId));
    }
    if (CommonUtilities.isEmpty(tir.label())) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Transaction item label cannot be empty...", requestId));
    }
    if (tir.amount() == null || tir.amount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Transaction item amount cannot be null or negative...", requestId));
    }

    CategoryResponse.Category category = categoryDao.readByIdNoEx(tir.categoryId()).orElse(null);
    if (category == null) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Category does not exist...", requestId));
    }

    if (!Constants.NO_EXPENSE_CATEGORY_TYPES.contains(category.categoryType().name())) {
      if (CommonUtilities.isEmpty(tir.expType())) {
        throw new Exceptions.BadRequestException(
            String.format("[%s] Transaction item type cannot be empty...", requestId));
      }
      if (!Constants.TRANSACTION_TYPES.contains(tir.expType())) {
        throw new Exceptions.BadRequestException(
            String.format("[%s] Transaction item type is invalid...", requestId));
      }
    }
  }

  public static void validateTransaction(
      String requestId, TransactionRequest tr, CategoryDao categoryDao) {
    if (tr == null) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Transaction request cannot be null...", requestId));
    }
    if (CommonUtilities.isEmpty(tr.merchant())) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Transaction merchant cannot be empty...", requestId));
    }
    if (tr.accountId() == null) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Transaction account cannot be null...", requestId));
    }
    if (tr.totalAmount() == null || tr.totalAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Transaction total cannot be null or negative...", requestId));
    }
    if (CommonUtilities.isEmpty(tr.items())) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Transaction must have at least one item...", requestId));
    }
    BigDecimal sumItems =
        tr.items().stream()
            .map(TransactionItemRequest::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (sumItems.compareTo(tr.totalAmount()) != 0) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Total amount does not match sum of items...", requestId));
    }
    for (TransactionItemRequest tir : tr.items()) {
      validateTransactionItem(requestId, tir, categoryDao, Boolean.TRUE);
    }
  }
}
