package budget.application.service.domain;

import budget.application.common.Constants;
import budget.application.common.Exceptions;
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
import io.github.bibekaryal86.shdsvc.Email;
import io.github.bibekaryal86.shdsvc.dtos.EmailRequest;
import io.github.bibekaryal86.shdsvc.dtos.EmailResponse;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionService {

  private final TransactionManager tx;
  private final Email email;

  public TransactionService(DataSource dataSource, Email email) {
    this.tx = new TransactionManager(dataSource);
    this.email = email;
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

          if (ids.size() == 1 && txns.isEmpty()) {
            throw new Exceptions.NotFoundException(
                requestId, "Transaction", ids.getFirst().toString());
          }

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

          List<Transaction> txns = txnRepo.read(List.of(id));
          if (txns.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "Transaction", id.toString());
          }

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

          List<Transaction> txns = txnRepo.read(ids);
          if (ids.size() == 1 && txns.isEmpty()) {
            throw new Exceptions.NotFoundException(
                requestId, "Transaction", ids.getFirst().toString());
          }

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
    log.debug("[{}] Reconciling all transactions...", requestId);
    List<Transaction> mmTxns = new ArrayList<>();
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
                mmTxns.add(txn);
                log.debug(
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
    if (!mmTxns.isEmpty()) {
      log.info("[{}] Mismatched transactions found: [{}]", requestId, mmTxns.size());
      sendReconciliationEmail(mmTxns);
    }
  }

  private void validate(String requestId, TransactionRequest tr, CategoryRepository categoryRepo) {
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
    Set<UUID> tiIds =
        tr.items().stream().map(TransactionItemRequest::categoryId).collect(Collectors.toSet());
    if (categoryRepo.readByIdsNoEx(tiIds.stream().toList()).size() != tiIds.size()) {
      throw new Exceptions.BadRequestException(
          String.format("[%s] One or more category IDs do not exist...", requestId));
    }
  }

  private void sendReconciliationEmail(List<Transaction> mmTxns) {
    String subject = "PETS Txn Mismatch Report";
    String emailTo = CommonUtilities.getSystemEnvProperty(Constants.ENV_RECON_EMAIL_TO);
    StringBuilder emailBody = new StringBuilder();

    emailBody
        .append("<html>")
        .append("<body style='font-family: Arial, sans-serif; font-size: 14px;'>")
        .append("<h2>PETS Transaction Mismatch Report</h2>")
        .append("<p>The following transactions were identified as mismatches:</p>");

    emailBody
        .append(
            "<table border='1' cellpadding='6' cellspacing='0' style='border-collapse: collapse;'>")
        .append("<tr style='background-color: #f2f2f2;'>")
        .append("<th>Transaction ID</th>")
        .append("<th>Transaction Date</th>")
        .append("</tr>");

    for (Transaction txn : mmTxns) {
      emailBody
          .append("<tr>")
          .append("<td>")
          .append(txn.id())
          .append("</td>")
          .append("<td>")
          .append(txn.txnDate())
          .append("</td>")
          .append("</tr>");
    }

    emailBody
        .append("</table>")
        .append("<br/><p>Generated automatically by PETS service.</p>")
        .append("</body></html>");

    EmailResponse emailResponse =
        email.sendEmail(
            new EmailRequest(
                new EmailRequest.EmailContact("PETS Service", "PETS Service"),
                List.of(new EmailRequest.EmailContact(emailTo, emailTo)),
                List.of(),
                new EmailRequest.EmailContent(subject, null, emailBody.toString()),
                List.of()));
    log.info("Txn Recon Email Sent: {}", emailResponse);
  }
}
