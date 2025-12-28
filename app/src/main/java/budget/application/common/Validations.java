package budget.application.common;

import budget.application.db.repository.CategoryRepository;
import budget.application.db.repository.CategoryTypeRepository;
import budget.application.model.dto.request.CategoryRequest;
import budget.application.model.dto.request.CategoryTypeRequest;
import budget.application.model.dto.request.TransactionItemRequest;
import budget.application.model.dto.request.TransactionRequest;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;

public class Validations {
  private Validations() {}

  public static void validateCategory(
      String requestId, CategoryRequest cr, CategoryTypeRepository typeRepo) {
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
    if (typeRepo.readByIdNoEx(cr.categoryTypeId()).isEmpty()) {
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
      String requestId,
      TransactionItemRequest tir,
      CategoryRepository categoryRepo,
      Boolean isCreateTxn) {
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
    if (tir.amount() <= 0) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Transaction item amount cannot be zero or negative...", requestId));
    }
    if (CommonUtilities.isEmpty(tir.txnType())) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Transaction item type cannot be empty...", requestId));
    }
    if (!Constants.TRANSACTION_TYPES.contains(tir.txnType())) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Transaction item type is invalid...", requestId));
    }
    if (categoryRepo.readByIdNoEx(tir.categoryId()).isEmpty()) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Category type does not exist...", requestId));
    }
  }

  public static void validateTransaction(
      String requestId, TransactionRequest tr, CategoryRepository categoryRepo) {
    if (tr == null) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Transaction request cannot be null...", requestId));
    }
    if (CommonUtilities.isEmpty(tr.merchant())) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Transaction merchant cannot be empty...", requestId));
    }
    if (tr.totalAmount() <= 0) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Transaction total cannot be negative...", requestId));
    }
    if (CommonUtilities.isEmpty(tr.items())) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Transaction must have at least one item...", requestId));
    }
    double sum = tr.items().stream().mapToDouble(TransactionItemRequest::amount).sum();
    if (Double.compare(sum, tr.totalAmount()) != 0) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] Total amount does not match sum of items...", requestId));
    }
    for (TransactionItemRequest tir : tr.items()) {
      validateTransactionItem(requestId, tir, categoryRepo, Boolean.TRUE);
    }
  }
}
