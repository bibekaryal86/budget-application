package budget.application.service.domain;

import budget.application.common.Exceptions;
import budget.application.common.Validations;
import budget.application.db.repository.CategoryRepository;
import budget.application.db.repository.TransactionItemRepository;
import budget.application.model.dto.TransactionItemRequest;
import budget.application.model.dto.TransactionItemResponse;
import budget.application.model.entity.TransactionItem;
import budget.application.service.util.ResponseMetadataUtils;
import budget.application.service.util.TransactionManager;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionItemService {
  private static final Logger log = LoggerFactory.getLogger(TransactionItemService.class);

  private final TransactionManager tx;

  public TransactionItemService(DataSource dataSource) {
    this.tx = new TransactionManager(dataSource);
  }

  public TransactionItemResponse create(String requestId, TransactionItemRequest tir)
      throws SQLException {
    log.debug("[{}] Create transaction item: TransactionItemRequest=[{}]", requestId, tir);
    return tx.execute(
        bs -> {
          TransactionItemRepository repo = new TransactionItemRepository(requestId, bs);
          CategoryRepository categoryRepo = new CategoryRepository(requestId, bs);

          Validations.validateTransactionItem(requestId, tir, categoryRepo, Boolean.FALSE);

          TransactionItem tiIn =
              new TransactionItem(
                  null,
                  tir.transactionId(),
                  tir.categoryId(),
                  tir.label(),
                  tir.amount(),
                  tir.txnType());
          TransactionItem tiOut = repo.create(tiIn);
          return new TransactionItemResponse(
              List.of(tiOut), ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public TransactionItemResponse read(String requestId, List<UUID> ids) throws SQLException {
    log.debug("[{}] Read transaction items: Ids=[{}]", requestId, ids);
    return tx.execute(
        bs -> {
          TransactionItemRepository repo = new TransactionItemRepository(requestId, bs);
          List<TransactionItem> tiList = repo.read(ids);

          if (ids.size() == 1 && tiList.isEmpty()) {
            throw new Exceptions.NotFoundException(
                requestId, "TransactionItem", ids.getFirst().toString());
          }

          return new TransactionItemResponse(tiList, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public TransactionItemResponse update(String requestId, UUID id, TransactionItemRequest tir)
      throws SQLException {
    log.debug(
        "[{}] Update transaction item: Id=[{}], TransactionItemRequest=[{}]", requestId, id, tir);
    return tx.execute(
        bs -> {
          TransactionItemRepository repo = new TransactionItemRepository(requestId, bs);
          CategoryRepository categoryRepo = new CategoryRepository(requestId, bs);
          Validations.validateTransactionItem(requestId, tir, categoryRepo, Boolean.FALSE);

          List<TransactionItem> tiList = repo.read(List.of(id));
          if (tiList.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "TransactionItem", id.toString());
          }

          TransactionItem tiIn =
              new TransactionItem(
                  id,
                  tir.transactionId(),
                  tir.categoryId(),
                  tir.label(),
                  tir.amount(),
                  tir.txnType());
          TransactionItem tiOut = repo.update(tiIn);
          return new TransactionItemResponse(
              List.of(tiOut), ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public TransactionItemResponse delete(String requestId, List<UUID> ids) throws SQLException {
    log.info("[{}] Delete transaction items: Ids=[{}]", requestId, ids);
    return tx.execute(
        bs -> {
          TransactionItemRepository repo = new TransactionItemRepository(requestId, bs);

          List<TransactionItem> tiList = repo.read(ids);
          if (ids.size() == 1 && tiList.isEmpty()) {
            throw new Exceptions.NotFoundException(
                requestId, "TransactionItem", ids.getFirst().toString());
          }

          int deleteCount = repo.delete(ids);
          return new TransactionItemResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }
}
