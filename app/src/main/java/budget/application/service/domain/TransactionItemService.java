package budget.application.service.domain;

import budget.application.common.Exceptions;
import budget.application.common.Validations;
import budget.application.db.dao.CategoryDao;
import budget.application.db.dao.TransactionItemDao;
import budget.application.db.util.TransactionManager;
import budget.application.model.dto.TransactionItemRequest;
import budget.application.model.dto.TransactionItemResponse;
import budget.application.model.entity.TransactionItem;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionItemService {
  private static final Logger log = LoggerFactory.getLogger(TransactionItemService.class);

  private final TransactionManager transactionManager;

  public TransactionItemService(DataSource dataSource) {
    this.transactionManager = new TransactionManager(dataSource);
  }

  public TransactionItemResponse create(
      String requestId, TransactionItemRequest transactionItemRequest) throws SQLException {
    log.debug(
        "[{}] Create transaction item: TransactionItemRequest=[{}]",
        requestId,
        transactionItemRequest);
    return transactionManager.execute(
        requestId,
        transactionContext -> {
          TransactionItemDao transactionItemDao =
              new TransactionItemDao(requestId, transactionContext.connection());
          CategoryDao categoryDao = new CategoryDao(requestId, transactionContext.connection());

          Validations.validateTransactionItem(
              requestId, transactionItemRequest, categoryDao, Boolean.FALSE, List.of());

          TransactionItem transactionItemIn =
              new TransactionItem(
                  null,
                  transactionItemRequest.transactionId(),
                  transactionItemRequest.categoryId(),
                  transactionItemRequest.amount(),
                  transactionItemRequest.tags(),
                  transactionItemRequest.notes());
          UUID id = transactionItemDao.create(transactionItemIn).id();
          log.debug("[{}] Created transaction item: Id=[{}]", requestId, id);

          List<TransactionItemResponse.TransactionItem> transactionItems =
              transactionItemDao.readTransactionItems(List.of(id));
          return new TransactionItemResponse(
              transactionItems, ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public TransactionItemResponse read(String requestId, List<UUID> transactionItemIds)
      throws SQLException {
    log.debug("[{}] Read transaction items: Ids={}", requestId, transactionItemIds);
    return transactionManager.execute(
        requestId,
        transactionContext -> {
          TransactionItemDao transactionItemDao =
              new TransactionItemDao(requestId, transactionContext.connection());
          List<TransactionItemResponse.TransactionItem> transactionItems =
              transactionItemDao.readTransactionItems(transactionItemIds);

          if (transactionItemIds.size() == 1 && transactionItems.isEmpty()) {
            throw new Exceptions.NotFoundException(
                requestId, "TransactionItem", transactionItemIds.getFirst().toString());
          }

          return new TransactionItemResponse(
              transactionItems, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public TransactionItemResponse.TransactionItemTags readTransactionItemTags(String requestId)
      throws SQLException {
    log.debug("[{}] Read transaction item tags", requestId);
    return transactionManager.execute(
        requestId,
        transactionContext -> {
          TransactionItemDao transactionItemDao =
              new TransactionItemDao(requestId, transactionContext.connection());
          List<String> transactionItemTags = transactionItemDao.readAllTags();
          return new TransactionItemResponse.TransactionItemTags(
              transactionItemTags, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public TransactionItemResponse update(
      String requestId, UUID id, TransactionItemRequest transactionItemRequest)
      throws SQLException {
    log.debug(
        "[{}] Update transaction item: Id=[{}], TransactionItemRequest=[{}]",
        requestId,
        id,
        transactionItemRequest);
    return transactionManager.execute(
        requestId,
        transactionContext -> {
          TransactionItemDao transactionItemDao =
              new TransactionItemDao(requestId, transactionContext.connection());
          CategoryDao categoryDao = new CategoryDao(requestId, transactionContext.connection());
          Validations.validateTransactionItem(
              requestId, transactionItemRequest, categoryDao, Boolean.FALSE, List.of());

          List<TransactionItem> transactionItemList = transactionItemDao.read(List.of(id));
          if (transactionItemList.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "TransactionItem", id.toString());
          }

          TransactionItem transactionItemIn =
              new TransactionItem(
                  id,
                  transactionItemRequest.transactionId(),
                  transactionItemRequest.categoryId(),
                  transactionItemRequest.amount(),
                  transactionItemRequest.tags(),
                  transactionItemRequest.notes());
          transactionItemDao.update(transactionItemIn);
          TransactionItemResponse.TransactionItem transactionItem =
              transactionItemDao.readTransactionItems(List.of(id)).getFirst();
          return new TransactionItemResponse(
              List.of(transactionItem), ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public TransactionItemResponse delete(String requestId, List<UUID> ids) throws SQLException {
    log.info("[{}] Delete transaction items: Ids=[{}]", requestId, ids);
    return transactionManager.execute(
        requestId,
        transactionContext -> {
          TransactionItemDao transactionItemDao =
              new TransactionItemDao(requestId, transactionContext.connection());

          List<TransactionItem> transactionItemList = transactionItemDao.read(ids);
          if (ids.size() == 1 && transactionItemList.isEmpty()) {
            throw new Exceptions.NotFoundException(
                requestId, "TransactionItem", ids.getFirst().toString());
          }

          int deleteCount = transactionItemDao.delete(ids);
          return new TransactionItemResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }
}
