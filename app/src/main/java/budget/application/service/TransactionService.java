package budget.application.service;

import budget.application.db.repository.BaseRepository;
import budget.application.db.repository.CategoryRepository;
import budget.application.db.repository.TransactionItemRepository;
import budget.application.db.repository.TransactionRepository;
import budget.application.model.entity.Transaction;
import budget.application.model.entity.TransactionItem;
import budget.application.model.pojo.TransactionWithItems;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class TransactionService {

  private final TransactionRepository txnRepo;
  private final TransactionItemRepository itemRepo;
  private final CategoryRepository categoryRepo;

  public TransactionService(BaseRepository bs) {
    this.txnRepo = new TransactionRepository(bs);
    this.itemRepo = new TransactionItemRepository(bs);
    this.categoryRepo = new CategoryRepository(bs);
  }

  public TransactionWithItems create(Transaction txn, List<TransactionItem> items)
      throws SQLException {
    List<UUID> categoryIds = items.stream().map(TransactionItem::categoryId).toList();

    if (categoryRepo.read(categoryIds).size() != categoryIds.size()) {
      throw new IllegalArgumentException("One or more category IDs do not exist");
    }

    double sum = items.stream().mapToDouble(TransactionItem::amount).sum();
    if (Double.compare(sum, txn.totalAmount()) != 0) {
      throw new IllegalArgumentException("Transaction total does not match sum of items");
    }

    txnRepo.create(txn);
    itemRepo.createItems(items);

    return new TransactionWithItems(txn, items);
  }

  public TransactionWithItems read(UUID id) throws SQLException {
    List<Transaction> txns = txnRepo.read(List.of(id));
    if (txns.isEmpty()) return null;

    Transaction txn = txns.getFirst();
    List<TransactionItem> items = itemRepo.readByTransactionId(id);

    return new TransactionWithItems(txn, items);
  }

  public TransactionWithItems update(Transaction txn, List<TransactionItem> items)
      throws SQLException {
    List<UUID> categoryIds = items.stream().map(TransactionItem::categoryId).toList();

    if (categoryRepo.read(categoryIds).size() != categoryIds.size()) {
      throw new IllegalArgumentException("One or more category IDs do not exist");
    }

    // Validate total matches sum of items
    double sum = items.stream().mapToDouble(TransactionItem::amount).sum();
    if (Double.compare(sum, txn.totalAmount()) != 0) {
      throw new IllegalArgumentException("Transaction total does not match sum of items");
    }

    // Update transaction
    txnRepo.update(txn);

    // Replace items
    List<TransactionItem> existing = itemRepo.readByTransactionId(txn.id());
    itemRepo.delete(existing.stream().map(TransactionItem::id).toList());
    itemRepo.createItems(items);

    return new TransactionWithItems(txn, items);
  }

  public int delete(UUID id) throws SQLException {
    List<TransactionItem> items = itemRepo.readByTransactionId(id);
    itemRepo.delete(items.stream().map(TransactionItem::id).toList());
    return txnRepo.delete(List.of(id));
  }
}
