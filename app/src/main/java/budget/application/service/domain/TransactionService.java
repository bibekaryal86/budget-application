package budget.application.service.domain;

import budget.application.common.Constants;
import budget.application.common.Exceptions;
import budget.application.common.Validations;
import budget.application.db.dao.CategoryDao;
import budget.application.db.dao.CategoryTypeDao;
import budget.application.db.dao.TransactionDao;
import budget.application.db.dao.TransactionItemDao;
import budget.application.db.util.TransactionManager;
import budget.application.model.dto.PaginationRequest;
import budget.application.model.dto.PaginationResponse;
import budget.application.model.dto.RequestParams;
import budget.application.model.dto.TransactionRequest;
import budget.application.model.dto.TransactionResponse;
import budget.application.model.entity.Transaction;
import budget.application.model.entity.TransactionItem;
import budget.application.service.util.ResponseMetadataUtils;
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

  private final TransactionManager transactionManager;
  private final Email email;

  public TransactionService(DataSource dataSource, Email email) {
    this.transactionManager = new TransactionManager(dataSource);
    this.email = email;
  }

  public TransactionResponse create(TransactionRequest transactionRequest) throws SQLException {
    log.debug("Create transaction: TransactionRequest=[{}]", transactionRequest);
    return transactionManager.execute(
        transactionContext -> {
          TransactionDao transactionDao = new TransactionDao(transactionContext.connection());
          CategoryDao categoryDao = new CategoryDao(transactionContext.connection());
          CategoryTypeDao categoryTypeDao = new CategoryTypeDao(transactionContext.connection());
          TransactionItemDao transactionItemDao =
              new TransactionItemDao(transactionContext.connection());

          Validations.validateTransaction(transactionRequest, categoryDao, categoryTypeDao);
          Transaction transactionIn =
              new Transaction(
                  null,
                  transactionRequest.txnDate(),
                  transactionRequest.merchant(),
                  transactionRequest.accountId(),
                  transactionRequest.totalAmount(),
                  null,
                  null);

          UUID transactionId = transactionDao.create(transactionIn).id();
          log.debug("Created transaction: Id=[{}]", transactionId);

          List<TransactionItem> transactionItemsIn =
              transactionRequest.items().stream()
                  .map(
                      item ->
                          new TransactionItem(
                              null,
                              transactionId,
                              item.categoryId(),
                              item.amount(),
                              item.tags(),
                              item.notes()))
                  .toList();
          List<UUID> transactionItemIds =
              transactionItemDao.createItems(transactionItemsIn).stream()
                  .map(TransactionItem::id)
                  .toList();
          log.debug("Created transaction items: TransactionItems=[{}]", transactionItemIds);

          List<TransactionResponse.Transaction> transactions =
              transactionDao.readTransactions(List.of(transactionId), null, null).items();
          return new TransactionResponse(
              transactions, ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public TransactionResponse read(
      List<UUID> transactionIds,
      RequestParams.TransactionParams requestParams,
      PaginationRequest paginationRequest)
      throws SQLException {
    log.debug(
        "Read transactions: TransactionIds=[{}], RequestParams=[{}]",
        transactionIds,
        requestParams);
    return transactionManager.execute(
        transactionContext -> {
          TransactionDao transactionDao = new TransactionDao(transactionContext.connection());

          PaginationResponse<TransactionResponse.Transaction> transactionPaginationResponse =
              transactionDao.readTransactions(transactionIds, requestParams, paginationRequest);

          if (transactionIds.size() == 1
              && (transactionPaginationResponse == null
                  || CommonUtilities.isEmpty(transactionPaginationResponse.items()))) {
            throw new Exceptions.NotFoundException(
                "Transaction", transactionIds.getFirst().toString());
          }

          ResponseMetadata responseMetadata =
              new ResponseMetadata(
                  ResponseMetadata.emptyResponseStatusInfo(),
                  ResponseMetadata.emptyResponseCrudInfo(),
                  transactionPaginationResponse.pageInfo());

          return new TransactionResponse(transactionPaginationResponse.items(), responseMetadata);
        });
  }

  public TransactionResponse.TransactionMerchants readTransactionMerchants() throws SQLException {
    log.debug("Read transaction merchants");
    return transactionManager.execute(
        transactionContext -> {
          TransactionDao transactionDao = new TransactionDao(transactionContext.connection());
          List<String> merchants = transactionDao.readAllMerchants();
          return new TransactionResponse.TransactionMerchants(
              merchants, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public TransactionResponse update(UUID id, TransactionRequest transactionRequest)
      throws SQLException {
    log.debug("Update transaction: Id=[{}], TransactionRequest=[{}]", id, transactionRequest);

    return transactionManager.execute(
        transactionContext -> {
          TransactionDao transactionDao = new TransactionDao(transactionContext.connection());
          TransactionItemDao transactionItemDao =
              new TransactionItemDao(transactionContext.connection());
          CategoryDao categoryDao = new CategoryDao(transactionContext.connection());
          CategoryTypeDao categoryTypeDao = new CategoryTypeDao(transactionContext.connection());

          Validations.validateTransaction(transactionRequest, categoryDao, categoryTypeDao);

          List<Transaction> transactionList = transactionDao.read(List.of(id));
          if (transactionList.isEmpty()) {
            throw new Exceptions.NotFoundException("Transaction", id.toString());
          }

          Transaction transactionIn =
              new Transaction(
                  id,
                  transactionRequest.txnDate(),
                  transactionRequest.merchant(),
                  transactionRequest.accountId(),
                  transactionRequest.totalAmount(),
                  null,
                  null);

          // Update transaction
          Transaction transactionOut = transactionDao.update(transactionIn);
          log.debug("Updated transaction: Transaction=[{}]", transactionOut);

          int deleteCount = transactionItemDao.deleteByTransactionIds(List.of(id));
          log.debug(
              "Deleted transaction items for transaction: TxnId=[{}], DeleteCount=[{}]",
              id,
              deleteCount);

          List<TransactionItem> transactionItemsList =
              transactionRequest.items().stream()
                  .map(
                      item ->
                          new TransactionItem(
                              null,
                              id,
                              item.categoryId(),
                              item.amount(),
                              item.tags(),
                              item.notes()))
                  .toList();
          List<TransactionItem> transactionItemList =
              transactionItemDao.createItems(transactionItemsList);
          log.debug("Recreated transaction items: TransactionItems=[{}]", transactionItemList);

          List<TransactionResponse.Transaction> transactions =
              transactionDao.readTransactions(List.of(id), null, null).items();
          return new TransactionResponse(
              transactions, ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public TransactionResponse delete(List<UUID> ids) throws SQLException {
    log.info("Delete transactions: Ids=[{}]", ids);
    return transactionManager.execute(
        transactionContext -> {
          TransactionDao transactionDao = new TransactionDao(transactionContext.connection());
          TransactionItemDao transactionItemDao =
              new TransactionItemDao(transactionContext.connection());

          List<Transaction> transactionList = transactionDao.read(ids);
          if (ids.size() == 1 && transactionList.isEmpty()) {
            throw new Exceptions.NotFoundException("Transaction", ids.getFirst().toString());
          }

          int deleteCountTransactionItems = transactionItemDao.deleteByTransactionIds(ids);
          log.info(
              "Deleted transaction items for transactions: Ids=[{}], DeleteCount=[{}]",
              ids,
              deleteCountTransactionItems);

          int deleteCount = transactionDao.delete(ids);
          log.info("Deleted transactions: Ids=[{}], DeleteCount=[{}]", ids, deleteCount);
          return new TransactionResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }

  public void reconcileAll() throws SQLException {
    log.debug("Reconciling all transactions...");
    List<TransactionResponse.Transaction> mismatchTransactions = new ArrayList<>();
    transactionManager.executeVoid(
        transactionContext -> {
          TransactionDao transactionDao = new TransactionDao(transactionContext.connection());
          TransactionItemDao transactionItemDao =
              new TransactionItemDao(transactionContext.connection());
          // Read all transactions
          int pageNumber = 1;
          int pageSize = 1000;

          while (true) {
            PaginationRequest paginationRequest = new PaginationRequest(pageNumber, pageSize);
            PaginationResponse<TransactionResponse.Transaction> transactionPaginationResponse =
                transactionDao.readTransactions(List.of(), null, paginationRequest);

            List<TransactionResponse.Transaction> transactions =
                transactionPaginationResponse.items();
            if (transactions.isEmpty()) {
              break;
            }

            for (TransactionResponse.Transaction transaction : transactions) {
              UUID id = transaction.id();
              List<TransactionItem> transactionItemList =
                  transactionItemDao.readByTransactionIds(List.of(id));
              BigDecimal sumItems =
                  transactionItemList.stream()
                      .map(TransactionItem::amount)
                      .reduce(BigDecimal.ZERO, BigDecimal::add);
              if (sumItems.compareTo(transaction.totalAmount()) != 0) {
                mismatchTransactions.add(transaction);
                log.debug(
                    "MISMATCH for Txn=[{}] | Total=[{}] | SUM(Items)=[{}]",
                    id,
                    transaction.totalAmount(),
                    sumItems);
              }
            }
            pageNumber++;
          }
        });
    if (!mismatchTransactions.isEmpty()) {
      List<UUID> mismatchTransactionIds =
          mismatchTransactions.stream().map(TransactionResponse.Transaction::id).toList();
      log.info("Mismatched transactions found: TxnIds={}", mismatchTransactionIds);
      sendReconciliationEmail(mismatchTransactions);
    }
  }

  private void sendReconciliationEmail(List<TransactionResponse.Transaction> mismatchTransactions) {
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

    for (TransactionResponse.Transaction transaction : mismatchTransactions) {
      emailBody
          .append("<tr>")
          .append("<td>")
          .append(transaction.id())
          .append("</td>")
          .append("<td>")
          .append(transaction.txnDate())
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
