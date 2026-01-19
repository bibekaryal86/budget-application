package budget.application.service.domain;

import budget.application.common.Constants;
import budget.application.common.Exceptions;
import budget.application.common.Validations;
import budget.application.db.dao.CategoryDao;
import budget.application.db.dao.TransactionDao;
import budget.application.db.dao.TransactionItemDao;
import budget.application.model.dto.PaginationRequest;
import budget.application.model.dto.PaginationResponse;
import budget.application.model.dto.RequestParams;
import budget.application.model.dto.TransactionRequest;
import budget.application.model.dto.TransactionResponse;
import budget.application.model.entity.Transaction;
import budget.application.model.entity.TransactionItem;
import budget.application.service.util.ResponseMetadataUtils;
import budget.application.service.util.TransactionManager;
import io.github.bibekaryal86.shdsvc.Email;
import io.github.bibekaryal86.shdsvc.dtos.EmailRequest;
import io.github.bibekaryal86.shdsvc.dtos.EmailResponse;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionService {
  private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

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
          TransactionDao txnDao = new TransactionDao(requestId, bs.connection());
          CategoryDao categoryDao = new CategoryDao(requestId, bs.connection());
          TransactionItemDao itemDao = new TransactionItemDao(requestId, bs.connection());

          Validations.validateTransaction(requestId, tr, categoryDao);
          Transaction txnIn =
              new Transaction(
                  null,
                  tr.txnDate(),
                  tr.merchant(),
                  tr.accountId(),
                  tr.totalAmount(),
                  tr.notes(),
                  null,
                  null);

          UUID txnId = txnDao.create(txnIn).id();
          log.debug("[{}] Created transaction: Id=[{}]", requestId, txnId);

          List<TransactionItem> txnItemsIn =
              tr.items().stream()
                  .map(
                      item ->
                          new TransactionItem(
                              null,
                              txnId,
                              item.categoryId(),
                              item.label(),
                              item.amount(),
                              item.tags()))
                  .toList();
          List<UUID> txnItemsIds =
              itemDao.createItems(txnItemsIn).stream().map(TransactionItem::id).toList();
          log.debug(
              "[{}] Created transaction items: TransactionItems=[{}]", requestId, txnItemsIds);

          List<TransactionResponse.Transaction> txns =
              txnDao.readTransactions(List.of(txnId), null);
          return new TransactionResponse(
              txns, ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public TransactionResponse read(
      String requestId, List<UUID> txnIds, RequestParams.TransactionParams requestParams)
      throws SQLException {
    log.debug(
        "[{}] Read transactions: TxnIds=[{}], RequestParams=[{}]",
        requestId,
        txnIds,
        requestParams);
    return tx.execute(
        bs -> {
          TransactionDao txnDao = new TransactionDao(requestId, bs.connection());
          TransactionItemDao itemDao = new TransactionItemDao(requestId, bs.connection());

          List<TransactionResponse.Transaction> txns =
              txnDao.readTransactions(txnIds, requestParams);

          if (txnIds.size() == 1 && txns.isEmpty()) {
            throw new Exceptions.NotFoundException(
                requestId, "Transaction", txnIds.getFirst().toString());
          }

          return new TransactionResponse(txns, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public TransactionResponse.TransactionMerchants readTransactionMerchants(String requestId)
      throws SQLException {
    log.debug("[{}] Read transaction merchants", requestId);
    return tx.execute(
        bs -> {
          TransactionDao txnDao = new TransactionDao(requestId, bs.connection());
          List<String> txnMerchants = txnDao.readAllMerchants();
          return new TransactionResponse.TransactionMerchants(
              txnMerchants, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public TransactionResponse update(String requestId, UUID id, TransactionRequest tr)
      throws SQLException {
    log.debug("[{}] Update transaction: Id=[{}], TransactionRequest=[{}]", requestId, id, tr);
    return tx.execute(
        bs -> {
          TransactionDao txnDao = new TransactionDao(requestId, bs.connection());
          TransactionItemDao itemDao = new TransactionItemDao(requestId, bs.connection());
          CategoryDao categoryDao = new CategoryDao(requestId, bs.connection());

          Validations.validateTransaction(requestId, tr, categoryDao);

          List<Transaction> txns = txnDao.read(List.of(id));
          if (txns.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "Transaction", id.toString());
          }

          Transaction txnIn =
              new Transaction(
                  id,
                  tr.txnDate(),
                  tr.merchant(),
                  tr.accountId(),
                  tr.totalAmount(),
                  tr.notes(),
                  null,
                  null);

          // Update transaction
          Transaction txnOut = txnDao.update(txnIn);
          log.debug("[{}] Updated transaction: Transaction=[{}]", requestId, txnOut);

          List<TransactionItem> txnItemsOut = new ArrayList<>();
          if (CommonUtilities.isEmpty(tr.items())) {
            txnItemsOut = itemDao.readByTransactionIds(List.of(id));
          } else {
            int deleteCount = itemDao.deleteByTransactionIds(List.of(id));
            log.debug(
                "[{}] Deleted transaction items for transaction: TxnId=[{}], DeleteCount=[{}]",
                requestId,
                id,
                deleteCount);

            List<TransactionItem> txnItemsIn =
                tr.items().stream()
                    .map(
                        item ->
                            new TransactionItem(
                                null,
                                id,
                                item.categoryId(),
                                item.label(),
                                item.amount(),
                                item.tags()))
                    .toList();
            txnItemsOut = itemDao.createItems(txnItemsIn);
            log.debug(
                "[{}] Recreated transaction items: TransactionItems=[{}]", requestId, txnItemsOut);
          }

          List<TransactionResponse.Transaction> txnsOut =
              txnDao.readTransactions(List.of(id), null);
          return new TransactionResponse(
              txnsOut, ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public TransactionResponse delete(String requestId, List<UUID> ids) throws SQLException {
    log.info("[{}] Delete transactions: Ids=[{}]", requestId, ids);
    return tx.execute(
        bs -> {
          TransactionDao txnDao = new TransactionDao(requestId, bs.connection());
          TransactionItemDao itemDao = new TransactionItemDao(requestId, bs.connection());

          List<Transaction> txns = txnDao.read(ids);
          if (ids.size() == 1 && txns.isEmpty()) {
            throw new Exceptions.NotFoundException(
                requestId, "Transaction", ids.getFirst().toString());
          }

          int deleteCountTxnItems = itemDao.deleteByTransactionIds(ids);
          log.info(
              "[{}] Deleted transaction items for transactions: Ids=[{}], DeleteCount=[{}]",
              requestId,
              ids,
              deleteCountTxnItems);

          int deleteCount = txnDao.delete(ids);
          log.info(
              "[{}] Deleted transactions: Ids=[{}], DeleteCount=[{}]", requestId, ids, deleteCount);
          return new TransactionResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }

  public void reconcileAll(String requestId) throws SQLException {
    log.debug("[{}] Reconciling all transactions...", requestId);
    List<Transaction> mmTxns = new ArrayList<>();
    tx.executeVoid(
        bs -> {
          TransactionDao txnDao = new TransactionDao(requestId, bs.connection());
          TransactionItemDao itemDao = new TransactionItemDao(requestId, bs.connection());
          // Read all transactions
          int pageNumber = 1;
          int pageSize = 1000;

          while (true) {
            PaginationRequest pageReq = new PaginationRequest(pageNumber, pageSize);
            PaginationResponse<Transaction> pagedTxns = txnDao.readAll(pageReq);

            List<Transaction> txns = pagedTxns.items();
            if (txns.isEmpty()) {
              break;
            }

            for (Transaction txn : txns) {
              UUID txnId = txn.id();
              List<TransactionItem> items = itemDao.readByTransactionIds(List.of(txnId));
              BigDecimal sumItems =
                  items.stream()
                      .map(TransactionItem::amount)
                      .reduce(BigDecimal.ZERO, BigDecimal::add);
              if (sumItems.compareTo(txn.totalAmount()) != 0) {
                mmTxns.add(txn);
                log.debug(
                    "[{}] MISMATCH for Txn=[{}] | Total=[{}] | SUM(Items)=[{}]",
                    requestId,
                    txnId,
                    txn.totalAmount(),
                    sumItems);
              }
            }
            pageNumber++;
          }
        });
    if (!mmTxns.isEmpty()) {
      List<UUID> mmTxnIds = mmTxns.stream().map(Transaction::id).toList();
      log.info("[{}] Mismatched transactions found: TxnIds={}", requestId, mmTxnIds);
      sendReconciliationEmail(mmTxns);
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
    log.info(
        "Txn Recon Email Sent: {}", emailResponse == null ? "Failed" : emailResponse.toString());
  }
}
