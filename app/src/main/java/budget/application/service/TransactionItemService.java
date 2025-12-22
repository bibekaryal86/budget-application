package budget.application.service;

import budget.application.db.repository.BaseRepository;
import budget.application.db.repository.CategoryRepository;
import budget.application.db.repository.TransactionItemRepository;
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

  public TransactionItem create(TransactionItem ti) throws SQLException {
    validate(ti);
    return repo.create(ti);
  }

  public List<TransactionItem> read(List<UUID> ids) throws SQLException {
    return repo.read(ids);
  }

  public TransactionItem update(TransactionItem ti) throws SQLException {
    return repo.update(ti);
  }

  public int delete(List<UUID> ids) throws SQLException {
    return repo.delete(ids);
  }

  private void validate(TransactionItem ti) {
    if (ti == null) {
      throw new IllegalArgumentException("Transaction item cannot be null...");
    }
    if (ti.amount() <= 0) {
      throw new IllegalArgumentException("Transaction item amount cannot be negative...");
    }
  }
}
