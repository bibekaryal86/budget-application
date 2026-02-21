package budget.application.service.domain;

import budget.application.common.Exceptions;
import budget.application.common.Validations;
import budget.application.db.dao.AccountDao;
import budget.application.db.dao.CategoryDao;
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
  private final DaoFactory<CategoryDao> categoryDaoFactory;
  private final DaoFactory<AccountDao> accountDaoFactory;

  public TransactionItemService(
      DataSource dataSource,
      DaoFactory<TransactionItemDao> transactionItemDaoFactory,
      DaoFactory<CategoryDao> categoryDaoFactory,
      DaoFactory<AccountDao> accountDaoFactory) {
    this.transactionManager = new TransactionManager(dataSource);
    this.transactionItemDaoFactory = transactionItemDaoFactory;
    this.categoryDaoFactory = categoryDaoFactory;
    this.accountDaoFactory = accountDaoFactory;
  }

  public TransactionItemResponse create(TransactionItemRequest transactionItemRequest)
      throws SQLException {
    log.debug("Create transaction item: TransactionItemRequest=[{}]", transactionItemRequest);
    return transactionManager.execute(
        transactionContext -> {
          TransactionItemDao transactionItemDao =
              transactionItemDaoFactory.create(transactionContext.connection());
          CategoryDao categoryDao = categoryDaoFactory.create(transactionContext.connection());
          AccountDao accountDao = accountDaoFactory.create(transactionContext.connection());

          List<Category> categoryList =
              transactionItemRequest == null || transactionItemRequest.categoryId() == null
                  ? List.of()
                  : categoryDao.readNoEx(List.of(transactionItemRequest.categoryId()));
          List<Account> accountList =
              transactionItemRequest == null || transactionItemRequest.accountId() == null
                  ? List.of()
                  : accountDao.readNoEx(List.of(transactionItemRequest.accountId()));
          Validations.validateTransactionItem(
              transactionItemRequest, Boolean.FALSE, categoryList, accountList);

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
        });
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
    log.debug(
        "Update transaction item: Id=[{}], TransactionItemRequest=[{}]",
        id,
        transactionItemRequest);
    return transactionManager.execute(
        transactionContext -> {
          TransactionItemDao transactionItemDao =
              transactionItemDaoFactory.create(transactionContext.connection());
          CategoryDao categoryDao = categoryDaoFactory.create(transactionContext.connection());
          AccountDao accountDao = accountDaoFactory.create(transactionContext.connection());

          List<Category> categoryList =
              transactionItemRequest == null || transactionItemRequest.categoryId() == null
                  ? List.of()
                  : categoryDao.readNoEx(List.of(transactionItemRequest.categoryId()));
          List<Account> accountList =
              transactionItemRequest == null || transactionItemRequest.accountId() == null
                  ? List.of()
                  : accountDao.readNoEx(List.of(transactionItemRequest.accountId()));
          Validations.validateTransactionItem(
              transactionItemRequest, Boolean.FALSE, categoryList, accountList);

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
        });
  }

  public TransactionItemResponse delete(List<UUID> ids) throws SQLException {
    log.info("Delete transaction items: Ids=[{}]", ids);
    return transactionManager.execute(
        transactionContext -> {
          TransactionItemDao transactionItemDao =
              transactionItemDaoFactory.create(transactionContext.connection());

          List<TransactionItem> transactionItemList = transactionItemDao.read(ids);
          if (ids.size() == 1 && transactionItemList.isEmpty()) {
            throw new Exceptions.NotFoundException("TransactionItem", ids.getFirst().toString());
          }

          int deleteCount = transactionItemDao.delete(ids);
          return new TransactionItemResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }
}
