package budget.application.service.domain;

import budget.application.db.repository.CategoryRepository;
import budget.application.db.repository.TransactionItemRepository;
import budget.application.model.dto.request.TransactionItemRequest;
import budget.application.model.dto.response.TransactionItemResponse;
import budget.application.model.entity.TransactionItem;
import budget.application.service.util.ResponseMetadataUtils;
import budget.application.service.util.TransactionManager;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionItemService {

  private final TransactionManager tx;

  public TransactionItemService(DataSource dataSource) {
    this.tx = new TransactionManager(dataSource);
  }

  public TransactionItemResponse create(String requestId, TransactionItemRequest tir)
      throws SQLException {
    log.debug("[{}] Create transaction item: TransactionItemRequest=[{}]", requestId, tir);
    return tx.execute(
        bs -> {
          TransactionItemRepository repo = new TransactionItemRepository(bs);
          CategoryRepository categoryRepo = new CategoryRepository(bs);

          validate(requestId, tir, categoryRepo);

          TransactionItem tiIn =
              TransactionItem.builder()
                  .transactionId(tir.transactionId())
                  .categoryId(tir.categoryId())
                  .label(tir.label())
                  .amount(tir.amount())
                  .build();
          TransactionItem tiOut = repo.create(tiIn);
          return new TransactionItemResponse(
              List.of(tiOut), ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public TransactionItemResponse read(String requestId, List<UUID> ids) throws SQLException {
    log.debug("[{}] Read transaction items: ids=[{}]", requestId, ids);
    return tx.execute(
        bs -> {
          TransactionItemRepository repo = new TransactionItemRepository(bs);
          List<TransactionItem> tiList = repo.read(ids);
          return new TransactionItemResponse(tiList, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public TransactionItemResponse update(String requestId, UUID id, TransactionItemRequest tir)
      throws SQLException {
    log.debug(
        "[{}] Update transaction item: id=[{}], TransactionItemRequest=[{}]", requestId, id, tir);
    return tx.execute(
        bs -> {
          TransactionItemRepository repo = new TransactionItemRepository(bs);
          CategoryRepository categoryRepo = new CategoryRepository(bs);
          validate(requestId, tir, categoryRepo);
          TransactionItem tiIn =
              TransactionItem.builder()
                  .id(id)
                  .transactionId(tir.transactionId())
                  .categoryId(tir.categoryId())
                  .label(tir.label())
                  .amount(tir.amount())
                  .build();
          TransactionItem tiOut = repo.update(tiIn);
          return new TransactionItemResponse(
              List.of(tiOut), ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public TransactionItemResponse delete(String requestId, List<UUID> ids) throws SQLException {
    log.info("[{}] Delete transaction items: ids=[{}]", requestId, ids);
    return tx.execute(
        bs -> {
          TransactionItemRepository repo = new TransactionItemRepository(bs);

          int deleteCount = repo.delete(ids);
          return new TransactionItemResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }

  private void validate(
      String requestId, TransactionItemRequest tir, CategoryRepository categoryRepo) {
    if (tir == null) {
      throw new IllegalArgumentException(
          String.format("[%s] Transaction item cannot be null...", requestId));
    }
    if (tir.transactionId() == null) {
      throw new IllegalArgumentException(
          String.format("[%s] Transaction item transaction cannot be null...", requestId));
    }
    if (tir.categoryId() == null) {
      throw new IllegalArgumentException(
          String.format("[%s] Transaction item category cannot be null...", requestId));
    }
    if (tir.amount() <= 0) {
      throw new IllegalArgumentException(
          String.format("[%s] Transaction item amount cannot be negative...", requestId));
    }
    if (categoryRepo.readByIdNoEx(tir.categoryId()).isEmpty()) {
      throw new IllegalArgumentException(
          String.format("[%s] Category type does not exist...", requestId));
    }
  }
}
