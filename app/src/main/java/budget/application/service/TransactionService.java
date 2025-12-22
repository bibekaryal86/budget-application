package budget.application.service;

import budget.application.db.repository.BaseRepository;
import budget.application.db.repository.CategoryRepository;
import budget.application.db.repository.TransactionItemRepository;
import budget.application.db.repository.TransactionRepository;
import budget.application.model.dto.request.TransactionItemRequest;
import budget.application.model.dto.request.TransactionRequest;
import budget.application.model.entity.Transaction;
import budget.application.model.entity.TransactionItem;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
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

  public Transaction create(TransactionRequest tr) throws SQLException {
    validate(tr);
    Transaction txnIn =
        Transaction.builder()
            .txnDate(tr.txnDate())
            .merchant(tr.merchant())
            .totalAmount(tr.totalAmount())
            .notes(tr.notes())
            .build();

    Transaction txnOut = txnRepo.create(txnIn);

    List<TransactionItem> txnItems =
        tr.items().stream()
            .map(
                item ->
                    TransactionItem.builder()
                        .transactionId(txnOut.id())
                        .categoryId(item.categoryId())
                        .label(item.label())
                        .amount(item.amount())
                        .build())
            .toList();
    itemRepo.createItems(txnItems);
    return txnOut;
  }

  public List<Transaction> read(List<UUID> ids) throws SQLException {
    return txnRepo.read(ids);
  }

  public Transaction update(UUID id, TransactionRequest tr) throws SQLException {
    validate(tr);

    Transaction txnIn =
        Transaction.builder()
            .id(id)
            .txnDate(tr.txnDate())
            .merchant(tr.merchant())
            .totalAmount(tr.totalAmount())
            .notes(tr.notes())
            .build();

    // Update transaction
    Transaction txnOut = txnRepo.update(txnIn);

    // Replace items
    List<TransactionItem> existing = itemRepo.readByTransactionIds(List.of(id));
    itemRepo.delete(existing.stream().map(TransactionItem::id).toList());

    List<TransactionItem> txnItems =
        tr.items().stream()
            .map(
                item ->
                    TransactionItem.builder()
                        .transactionId(id)
                        .categoryId(item.categoryId())
                        .label(item.label())
                        .amount(item.amount())
                        .build())
            .toList();
    itemRepo.createItems(txnItems);

    return txnOut;
  }

  public int delete(List<UUID> ids) throws SQLException {
    List<TransactionItem> items = itemRepo.readByTransactionIds(ids);
    itemRepo.delete(items.stream().map(TransactionItem::id).toList());
    return txnRepo.delete(ids);
  }

  private void validate(TransactionRequest tr) {
    if (tr == null) {
      throw new IllegalArgumentException("Transaction request cannot be null...");
    }
    if (CommonUtilities.isEmpty(tr.merchant())) {
      throw new IllegalArgumentException("Transaction merchant cannot be empty...");
    }
    if (tr.totalAmount() <= 0) {
      throw new IllegalArgumentException("Transaction total cannot be negative...");
    }
    if (CommonUtilities.isEmpty(tr.items())) {
      throw new IllegalArgumentException("Transaction must have at least one item...");
    }
    double sum = tr.items().stream().mapToDouble(TransactionItemRequest::amount).sum();
    if (Double.compare(sum, tr.totalAmount()) != 0) {
      throw new IllegalArgumentException("Total amount does not match sum of items");
    }
    List<UUID> tiIds = tr.items().stream().map(TransactionItemRequest::categoryId).toList();
    if (categoryRepo.readByIdsNoEx(tiIds).size() != tiIds.size()) {
      throw new IllegalArgumentException("One or more category IDs do not exist");
    }
  }
}
