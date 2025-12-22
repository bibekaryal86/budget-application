package budget.application.service.domain;

import budget.application.db.repository.BaseRepository;
import budget.application.db.repository.CategoryRepository;
import budget.application.db.repository.TransactionItemRepository;
import budget.application.db.repository.TransactionRepository;
import budget.application.model.dto.composite.TransactionWithItems;
import budget.application.model.dto.request.TransactionItemRequest;
import budget.application.model.dto.request.TransactionRequest;
import budget.application.model.dto.response.TransactionResponse;
import budget.application.model.entity.Transaction;
import budget.application.model.entity.TransactionItem;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TransactionService {

  private final TransactionRepository txnRepo;
  private final TransactionItemRepository itemRepo;
  private final CategoryRepository categoryRepo;

  public TransactionService(BaseRepository bs) {
    this.txnRepo = new TransactionRepository(bs);
    this.itemRepo = new TransactionItemRepository(bs);
    this.categoryRepo = new CategoryRepository(bs);
  }

  public TransactionResponse create(TransactionRequest tr) throws SQLException {
    validate(tr);
    Transaction txnIn =
        Transaction.builder()
            .txnDate(tr.txnDate())
            .merchant(tr.merchant())
            .totalAmount(tr.totalAmount())
            .notes(tr.notes())
            .build();

    Transaction txnOut = txnRepo.create(txnIn);

    List<TransactionItem> txnItemsIn =
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
    List<TransactionItem> txnItemsOut = itemRepo.createItems(txnItemsIn);
    return new TransactionResponse(
        List.of(new TransactionWithItems(txnOut, txnItemsOut)),
        ResponseMetadataUtils.defaultInsertResponseMetadata());
  }

  public TransactionResponse read(List<UUID> ids) throws SQLException {
    List<Transaction> txnsIn = txnRepo.read(ids);
    List<UUID> txnIds = txnsIn.stream().map(Transaction::id).toList();
    List<TransactionItem> items = itemRepo.readByTransactionIds(txnIds);
    Map<UUID, List<TransactionItem>> txnItemsMap =
        items.stream().collect(Collectors.groupingBy(TransactionItem::transactionId));
    List<TransactionWithItems> txnWithItems =
        txnsIn.stream()
            .map(
                txn -> new TransactionWithItems(txn, txnItemsMap.getOrDefault(txn.id(), List.of())))
            .toList();
    return new TransactionResponse(txnWithItems, ResponseMetadata.emptyResponseMetadata());
  }

  public TransactionResponse update(UUID id, TransactionRequest tr) throws SQLException {
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
    itemRepo.deleteByTransactionIds(List.of(id));

    List<TransactionItem> txnItemsIn =
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
    List<TransactionItem> txnItemsOut = itemRepo.createItems(txnItemsIn);

    return new TransactionResponse(
        List.of(new TransactionWithItems(txnOut, txnItemsOut)),
        ResponseMetadataUtils.defaultUpdateResponseMetadata());
  }

  public TransactionResponse delete(List<UUID> ids) throws SQLException {
    itemRepo.deleteByTransactionIds(ids);
    int deleteCount = txnRepo.delete(ids);
    return new TransactionResponse(
        List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
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
