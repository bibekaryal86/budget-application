package budget.application.service.domain;

import budget.application.db.repository.CategoryRepository;
import budget.application.db.repository.TransactionItemRepository;
import budget.application.db.repository.TransactionRepository;
import budget.application.model.dto.composite.PaginationResponse;
import budget.application.model.dto.composite.TransactionWithItems;
import budget.application.model.dto.request.PaginationRequest;
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
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionService {

  private final TransactionManager tx;

  public TransactionService(DataSource dataSource) {
    this.tx = new TransactionManager(dataSource);
  }

  public TransactionResponse create(String requestId, TransactionRequest tr) throws SQLException {
    log.debug("[{}] Create transaction: TransactionRequest=[{}]", requestId, tr);
    return tx.execute(
        bs -> {
          TransactionRepository txnRepo = new TransactionRepository(requestId, bs);
          CategoryRepository categoryRepo = new CategoryRepository(requestId, bs);
          TransactionItemRepository itemRepo = new TransactionItemRepository(requestId, bs);

          validate(requestId, tr, categoryRepo);
          Transaction txnIn =
              Transaction.builder()
                  .txnDate(tr.txnDate())
                  .merchant(tr.merchant())
                  .totalAmount(tr.totalAmount())
                  .notes(tr.notes())
                  .build();

          Transaction txnOut = txnRepo.create(txnIn);
          log.debug("[{}] Created transaction: Transaction=[{}]", requestId, txnOut);

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
          log.debug(
              "[{}] Created transaction items: TransactionItems=[{}]", requestId, txnItemsOut);

          return new TransactionResponse(
              List.of(new TransactionWithItems(txnOut, txnItemsOut)),
              ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public TransactionResponse read(String requestId, List<UUID> ids) throws SQLException {
    log.debug("[{}] Read transactions: Ids=[{}]", requestId, ids);
    return tx.execute(
        bs -> {
          TransactionRepository txnRepo = new TransactionRepository(requestId, bs);
          TransactionItemRepository itemRepo = new TransactionItemRepository(requestId, bs);
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

  public TransactionResponse update(String requestId, UUID id, TransactionRequest tr)
      throws SQLException {
    log.debug("[{}] Update transaction: Id=[{}], TransactionRequest=[{}]", requestId, id, tr);
    return tx.execute(
        bs -> {
          TransactionRepository txnRepo = new TransactionRepository(requestId, bs);
          TransactionItemRepository itemRepo = new TransactionItemRepository(requestId, bs);
          CategoryRepository categoryRepo = new CategoryRepository(requestId, bs);

          validate(requestId, tr, categoryRepo);

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
          log.debug("[{}] Updated transaction: Transaction=[{}]", requestId, txnOut);

          List<TransactionItem> txnItemsOut = new ArrayList<>();
          if (CommonUtilities.isEmpty(tr.items())) {
            txnItemsOut = itemRepo.readByTransactionIds(List.of(id));
          } else {
            int deleteCount = itemRepo.deleteByTransactionIds(List.of(id));
            log.debug(
                "[{}] Deleted transaction items for transaction: txnId=[{}], deleteCount=[{}]",
                requestId,
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
            log.debug(
                "[{}] Recreated transaction items: TransactionItems=[{}]", requestId, txnItemsOut);
          }

          return new TransactionResponse(
              List.of(new TransactionWithItems(txnOut, txnItemsOut)),
              ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public TransactionResponse delete(String requestId, List<UUID> ids) throws SQLException {
    log.info("[{}] Delete transactions: Ids=[{}]", requestId, ids);
    return tx.execute(
        bs -> {
          TransactionRepository txnRepo = new TransactionRepository(requestId, bs);
          TransactionItemRepository itemRepo = new TransactionItemRepository(requestId, bs);
          int deleteCountTxnItems = itemRepo.deleteByTransactionIds(ids);
          log.info(
              "[{}] Deleted transaction items for transactions: Ids=[{}], deleteCount=[{}]",
              requestId,
              ids,
              deleteCountTxnItems);

          int deleteCount = txnRepo.delete(ids);
          log.info(
              "[{}] Deleted transactions: Ids=[{}], deleteCount=[{}]", requestId, ids, deleteCount);
          return new TransactionResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }

  public void reconcileAll(String requestId) throws SQLException {
    log.info("[{}] Reconciling all transactions...", requestId);
    tx.executeVoid(
        bs -> {
          TransactionRepository txnRepo = new TransactionRepository(requestId, bs);
          TransactionItemRepository itemRepo = new TransactionItemRepository(requestId, bs);
          // Read all transactions
          int pageNumber = 1;
          int pageSize = 1000;

          while (true) {
            PaginationRequest pageReq = new PaginationRequest(pageNumber, pageSize);
            PaginationResponse<Transaction> pagedTxns = txnRepo.readAll(pageReq);

            List<Transaction> txns = pagedTxns.items();
            if (txns.isEmpty()) {
              break;
            }

            for (Transaction txn : txns) {
              UUID txnId = txn.id();
              List<TransactionItem> items = itemRepo.readByTransactionIds(List.of(txnId));
              double sum = items.stream().mapToDouble(TransactionItem::amount).sum();
              if (Double.compare(sum, txn.totalAmount()) != 0) {
                log.info(
                    "[{}] MISMATCH for txn=[{}] | total=[{}] | sum(items)=[{}]",
                    requestId,
                    txnId,
                    txn.totalAmount(),
                    sum);
              }
            }
            pageNumber++;
          }
        });
  }

  private void validate(String requestId, TransactionRequest tr, CategoryRepository categoryRepo) {
    if (tr == null) {
      throw new IllegalArgumentException(
          String.format("[%s] Transaction request cannot be null...", requestId));
    }
    if (CommonUtilities.isEmpty(tr.merchant())) {
      throw new IllegalArgumentException(
          String.format("[%s] Transaction merchant cannot be empty...", requestId));
    }
    if (tr.totalAmount() <= 0) {
      throw new IllegalArgumentException(
          String.format("[%s] Transaction total cannot be negative...", requestId));
    }
    if (CommonUtilities.isEmpty(tr.items())) {
      throw new IllegalArgumentException(
          String.format("[%s] Transaction must have at least one item...", requestId));
    }
    double sum = tr.items().stream().mapToDouble(TransactionItemRequest::amount).sum();
    if (Double.compare(sum, tr.totalAmount()) != 0) {
      throw new IllegalArgumentException(
          String.format("[%s] Total amount does not match sum of items", requestId));
    }
    List<UUID> tiIds = tr.items().stream().map(TransactionItemRequest::categoryId).toList();
    if (categoryRepo.readByIdsNoEx(tiIds).size() != tiIds.size()) {
      throw new IllegalArgumentException(
          String.format("[%s] One or more category IDs do not exist", requestId));
    }
  }
}
