package budget.application.service.domain;

import budget.application.common.Exceptions;
import budget.application.db.dao.DaoFactory;
import budget.application.db.dao.TransactionItemDao;
import budget.application.db.util.TransactionManager;
import budget.application.model.dto.TransactionItemRequest;
import budget.application.model.dto.TransactionItemResponse;
import budget.application.model.entity.Account;
import budget.application.model.entity.Category;
import budget.application.model.entity.TransactionItem;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionItemService {
  private static final Logger log = LoggerFactory.getLogger(TransactionItemService.class);

  private final TransactionManager transactionManager;
  private final DaoFactory<TransactionItemDao> transactionItemDaoFactory;
  private final CategoryService categoryService;
  private final AccountService accountService;

  public TransactionItemService(
      DataSource dataSource,
      DaoFactory<TransactionItemDao> transactionItemDaoFactory,
      CategoryService categoryService,
      AccountService accountService) {
    this.transactionManager = new TransactionManager(dataSource);
    this.transactionItemDaoFactory = transactionItemDaoFactory;
    this.categoryService = categoryService;
    this.accountService = accountService;
  }

  public TransactionItemResponse create(TransactionItemRequest transactionItemRequest)
      throws SQLException {
    return create(transactionItemRequest, null);
  }

  public TransactionItemResponse create(
      TransactionItemRequest transactionItemRequest, Connection connection) throws SQLException {
    log.debug("Create transaction item: TransactionItemRequest=[{}]", transactionItemRequest);

    if (connection == null) {
      return transactionManager.execute(
          transactionContext -> create(transactionItemRequest, transactionContext.connection()));
    }

    TransactionItemDao transactionItemDao = transactionItemDaoFactory.create(connection);
    validateTransactionItem(transactionItemRequest, Boolean.FALSE, connection);

    TransactionItem transactionItemIn =
        new TransactionItem(
            null,
            transactionItemRequest.transactionId(),
            transactionItemRequest.categoryId(),
            transactionItemRequest.accountId(),
            transactionItemRequest.amount(),
            transactionItemRequest.tags(),
            transactionItemRequest.notes());
    UUID id = transactionItemDao.create(transactionItemIn).id();
    log.debug("Created transaction item: Id=[{}]", id);

    List<TransactionItemResponse.TransactionItem> transactionItems =
        transactionItemDao.readTransactionItems(List.of(id));
    return new TransactionItemResponse(
        transactionItems, ResponseMetadataUtils.defaultInsertResponseMetadata());
  }

  public List<TransactionItem> createItems(
      UUID transactionId,
      List<TransactionItemRequest> transactionItemRequests,
      Connection connection)
      throws SQLException {
    log.debug(
        "Create transaction items: TransactionId=[{}], TransactionItemRequests=[{}]",
        transactionId,
        transactionItemRequests);
    transactionItemRequests.forEach(
        transactionRequest ->
            validateTransactionItem(transactionRequest, Boolean.TRUE, connection));
    TransactionItemDao transactionItemDao = transactionItemDaoFactory.create(connection);
    List<TransactionItem> transactionItemsIn =
        transactionItemRequests.stream()
            .map(
                item ->
                    new TransactionItem(
                        null,
                        transactionId,
                        item.categoryId(),
                        item.accountId(),
                        item.amount(),
                        item.tags(),
                        item.notes()))
            .toList();
    return transactionItemDao.createItems(transactionItemsIn);
  }

  public TransactionItemResponse read(List<UUID> transactionItemIds) throws SQLException {
    log.debug("Read transaction items: Ids={}", transactionItemIds);
    return transactionManager.execute(
        transactionContext -> {
          TransactionItemDao transactionItemDao =
              transactionItemDaoFactory.create(transactionContext.connection());
          List<TransactionItemResponse.TransactionItem> transactionItems =
              transactionItemDao.readTransactionItems(transactionItemIds);

          if (transactionItemIds.size() == 1 && transactionItems.isEmpty()) {
            throw new Exceptions.NotFoundException(
                "TransactionItem", transactionItemIds.getFirst().toString());
          }

          return new TransactionItemResponse(
              transactionItems, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public List<TransactionItem> readByTransactionIds(
      List<UUID> transactionIds, Connection connection) throws SQLException {
    log.debug("Read transaction items by transaction ids: TransactionIds={}", transactionIds);
    TransactionItemDao transactionItemDao = transactionItemDaoFactory.create(connection);
    return transactionItemDao.readByTransactionIds(transactionIds);
  }

  public TransactionItemResponse.TransactionItemTags readTransactionItemTags() throws SQLException {
    log.debug("Read transaction item tags...");
    return transactionManager.execute(
        transactionContext -> {
          TransactionItemDao transactionItemDao =
              transactionItemDaoFactory.create(transactionContext.connection());
          List<String> transactionItemTags = transactionItemDao.readAllTags();
          return new TransactionItemResponse.TransactionItemTags(
              transactionItemTags, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public TransactionItemResponse update(UUID id, TransactionItemRequest transactionItemRequest)
      throws SQLException {
    return update(id, transactionItemRequest, null);
  }

  public TransactionItemResponse update(
      UUID id, TransactionItemRequest transactionItemRequest, Connection connection)
      throws SQLException {
    log.debug(
        "Update transaction item: Id=[{}], TransactionItemRequest=[{}]",
        id,
        transactionItemRequest);

    if (connection == null) {
      return transactionManager.execute(
          transactionContext ->
              update(id, transactionItemRequest, transactionContext.connection()));
    }

    TransactionItemDao transactionItemDao = transactionItemDaoFactory.create(connection);
    validateTransactionItem(transactionItemRequest, Boolean.FALSE, connection);

    List<TransactionItem> transactionItemList = transactionItemDao.read(List.of(id));
    if (transactionItemList.isEmpty()) {
      throw new Exceptions.NotFoundException("TransactionItem", id.toString());
    }

    TransactionItem transactionItemIn =
        new TransactionItem(
            id,
            transactionItemRequest.transactionId(),
            transactionItemRequest.categoryId(),
            transactionItemRequest.accountId(),
            transactionItemRequest.amount(),
            transactionItemRequest.tags(),
            transactionItemRequest.notes());
    transactionItemDao.update(transactionItemIn);
    TransactionItemResponse.TransactionItem transactionItem =
        transactionItemDao.readTransactionItems(List.of(id)).getFirst();
    return new TransactionItemResponse(
        List.of(transactionItem), ResponseMetadataUtils.defaultUpdateResponseMetadata());
  }

  public TransactionItemResponse delete(List<UUID> ids) throws SQLException {
    return delete(ids, null);
  }

  public TransactionItemResponse delete(List<UUID> ids, Connection connection) throws SQLException {
    log.info("Delete transaction items: Ids=[{}]", ids);

    if (connection == null) {
      return transactionManager.execute(
          transactionContext -> delete(ids, transactionContext.connection()));
    }

    TransactionItemDao transactionItemDao = transactionItemDaoFactory.create(connection);
    List<TransactionItem> transactionItemList = transactionItemDao.read(ids);
    if (ids.size() == 1 && transactionItemList.isEmpty()) {
      throw new Exceptions.NotFoundException("TransactionItem", ids.getFirst().toString());
    }

    int deleteCount = transactionItemDao.delete(ids);
    return new TransactionItemResponse(
        List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
  }

  public int deleteByTransactionIds(List<UUID> transactionIds, Connection connection)
      throws SQLException {
    log.info("Delete transaction items by Transaction Ids: TransactionIds=[{}]", transactionIds);
    TransactionItemDao transactionItemDao = transactionItemDaoFactory.create(connection);
    return transactionItemDao.deleteByTransactionIds(transactionIds);
  }

  private void validateTransactionItem(
      TransactionItemRequest transactionItemRequest,
      boolean isCreateTransaction,
      Connection connection) {
    if (transactionItemRequest == null) {
      throw new Exceptions.BadRequestException("Transaction item request cannot be null...");
    }
    if (!isCreateTransaction && transactionItemRequest.transactionId() == null) {
      throw new Exceptions.BadRequestException("Transaction item transaction cannot be null...");
    }
    if (transactionItemRequest.categoryId() == null) {
      throw new Exceptions.BadRequestException("Transaction item category cannot be null...");
    }
    if (transactionItemRequest.accountId() == null) {
      throw new Exceptions.BadRequestException("Transaction item account cannot be null...");
    }
    if (transactionItemRequest.amount() == null
        || transactionItemRequest.amount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new Exceptions.BadRequestException(
          "Transaction item amount cannot be null or negative...");
    }

    List<Category> categoryList =
        categoryService.readNoEx(List.of(transactionItemRequest.categoryId()), connection);
    List<Account> accountList =
        accountService.readNoEx(List.of(transactionItemRequest.accountId()), connection);

    Category category =
        categoryList.stream()
            .filter(cat -> cat.id().equals(transactionItemRequest.categoryId()))
            .findFirst()
            .orElse(null);

    if (category == null) {
      throw new Exceptions.BadRequestException("Category does not exist...");
    }

    Account account =
        accountList.stream()
            .filter(acc -> acc.id().equals(transactionItemRequest.accountId()))
            .findFirst()
            .orElse(null);

    if (account == null) {
      throw new Exceptions.BadRequestException("Account does not exist...");
    }
  }
}
