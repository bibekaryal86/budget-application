package budget.application.service.domain;

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
import budget.application.service.util.TransactionManager;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionService {

  private final TransactionManager tx;

  public TransactionService(TransactionManager tx) {
    this.tx = tx;
  }

  public TransactionResponse create(TransactionRequest tr) throws SQLException {
    log.debug("Create transaction: TransactionRequest=[{}]", tr);
    return tx.execute(
        bs -> {
          TransactionRepository txnRepo = new TransactionRepository(bs);
          CategoryRepository categoryRepo = new CategoryRepository(bs);
          TransactionItemRepository itemRepo = new TransactionItemRepository(bs);

          validate(tr, categoryRepo);
          Transaction txnIn =
              Transaction.builder()
                  .txnDate(tr.txnDate())
                  .merchant(tr.merchant())
                  .totalAmount(tr.totalAmount())
                  .notes(tr.notes())
                  .build();

          Transaction txnOut = txnRepo.create(txnIn);
          log.debug("Created transaction: Transaction=[{}]", txnOut);

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
          log.debug("Created transaction items: TransactionItems=[{}]", txnItemsOut);

          return new TransactionResponse(
              List.of(new TransactionWithItems(txnOut, txnItemsOut)),
              ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public TransactionResponse read(List<UUID> ids) throws SQLException {
    log.debug("Read transactions: ids=[{}]", ids);
    return tx.execute(
        bs -> {
          TransactionRepository txnRepo = new TransactionRepository(bs);
          TransactionItemRepository itemRepo = new TransactionItemRepository(bs);
          List<Transaction> txns = txnRepo.read(ids);
          List<UUID> txnIds = txns.stream().map(Transaction::id).toList();
          List<TransactionItem> items = itemRepo.readByTransactionIds(txnIds);
          Map<UUID, List<TransactionItem>> txnItemsMap =
              items.stream().collect(Collectors.groupingBy(TransactionItem::transactionId));
          List<TransactionWithItems> txnWithItems =
              txns.stream()
                  .map(
                      txn ->
                          new TransactionWithItems(
                              txn, txnItemsMap.getOrDefault(txn.id(), List.of())))
                  .toList();
          return new TransactionResponse(txnWithItems, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public TransactionResponse update(UUID id, TransactionRequest tr) throws SQLException {
    log.debug("Update transaction: id=[{}], TransactionRequest=[{}]", id, tr);
    return tx.execute(
        bs -> {
          TransactionRepository txnRepo = new TransactionRepository(bs);
          TransactionItemRepository itemRepo = new TransactionItemRepository(bs);
          CategoryRepository categoryRepo = new CategoryRepository(bs);

          validate(tr, categoryRepo);

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
          log.debug("Updated transaction: Transaction=[{}]", txnOut);

          List<TransactionItem> txnItemsOut = new ArrayList<>();
          if (CommonUtilities.isEmpty(tr.items())) {
            txnItemsOut = itemRepo.readByTransactionIds(List.of(id));
          } else {
            int deleteCount = itemRepo.deleteByTransactionIds(List.of(id));
            log.debug(
                "Deleted transaction items for transaction: txnId=[{}], deleteCount=[{}]",
                id,
                deleteCount);

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
            txnItemsOut = itemRepo.createItems(txnItemsIn);
            log.debug("Recreated transaction items: TransactionItems=[{}]", txnItemsOut);
          }

          return new TransactionResponse(
              List.of(new TransactionWithItems(txnOut, txnItemsOut)),
              ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public TransactionResponse delete(List<UUID> ids) throws SQLException {
    log.info("Delete transactions: ids=[{}]", ids);
    return tx.execute(
        bs -> {
          TransactionRepository txnRepo = new TransactionRepository(bs);
          TransactionItemRepository itemRepo = new TransactionItemRepository(bs);
          int deleteCountTxnItems = itemRepo.deleteByTransactionIds(ids);
          log.info(
              "Deleted transaction items for transactions: ids=[{}], deleteCount=[{}]",
              ids,
              deleteCountTxnItems);

          int deleteCount = txnRepo.delete(ids);
          log.info("Deleted transactions: ids=[{}], deleteCount=[{}]", ids, deleteCount);
          return new TransactionResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }

  public void reconcileAll() throws SQLException {
    tx.executeVoid(
        bs -> {
          TransactionRepository txnRepo = new TransactionRepository(bs);
          TransactionItemRepository itemRepo = new TransactionItemRepository(bs);
          // Read all transactions
          List<Transaction> allTxns = txnRepo.read(List.of());
          for (Transaction txn : allTxns) {
            UUID txnId = txn.id();
            List<TransactionItem> items = itemRepo.readByTransactionIds(List.of(txnId));
            double sum = items.stream().mapToDouble(TransactionItem::amount).sum();
            if (Double.compare(sum, txn.totalAmount()) != 0) {
              log.info(
                  "[Reconciliation] MISMATCH for txn {} | total={} | sum(items)={}",
                  txnId,
                  txn.totalAmount(),
                  sum);
            }
          }
        });
  }

  private void validate(TransactionRequest tr, CategoryRepository categoryRepo) {
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
