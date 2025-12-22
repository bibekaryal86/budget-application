package budget.application.service;

import budget.application.db.repository.BaseRepository;
import budget.application.db.repository.CategoryRepository;
import budget.application.db.repository.TransactionItemRepository;
import budget.application.model.dto.request.TransactionItemRequest;
import budget.application.model.entity.TransactionItem;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class TransactionItemService {

  private final TransactionItemRepository repo;
  private final CategoryRepository categoryRepo;

  public TransactionItemService(BaseRepository bs) {
    this.repo = new TransactionItemRepository(bs);
    this.categoryRepo = new CategoryRepository(bs);
  }

  public TransactionItem create(TransactionItemRequest tir) throws SQLException {
    validate(tir);
    TransactionItem ti =
        TransactionItem.builder()
            .transactionId(tir.transactionId())
            .categoryId(tir.categoryId())
            .label(tir.label())
            .amount(tir.amount())
            .build();
    return repo.create(ti);
  }

  public List<TransactionItem> read(List<UUID> ids) throws SQLException {
    return repo.read(ids);
  }

  public TransactionItem update(UUID id, TransactionItemRequest tir) throws SQLException {
    validate(tir);
    TransactionItem ti =
        TransactionItem.builder()
            .id(id)
            .transactionId(tir.transactionId())
            .categoryId(tir.categoryId())
            .label(tir.label())
            .amount(tir.amount())
            .build();
    return repo.update(ti);
  }

  public int delete(List<UUID> ids) throws SQLException {
    return repo.delete(ids);
  }

  private void validate(TransactionItemRequest tir) {
    if (tir == null) {
      throw new IllegalArgumentException("Transaction item cannot be null...");
    }
    if (tir.transactionId() == null) {
      throw new IllegalArgumentException("Transaction item transaction cannot be null...");
    }
    if (tir.categoryId() == null) {
      throw new IllegalArgumentException("Transaction item category cannot be null...");
    }
    if (tir.amount() <= 0) {
      throw new IllegalArgumentException("Transaction item amount cannot be negative...");
    }
    if (categoryRepo.readByIdNoEx(tir.categoryId()).isEmpty()) {
      throw new IllegalArgumentException("Category type does not exist...");
    }
  }
}
