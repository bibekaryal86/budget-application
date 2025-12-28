package budget.application.service.domain;

import budget.application.common.Exceptions;
import budget.application.common.Validations;
import budget.application.db.repository.CategoryTypeRepository;
import budget.application.model.dto.request.CategoryTypeRequest;
import budget.application.model.dto.response.CategoryTypeResponse;
import budget.application.model.entity.CategoryType;
import budget.application.service.util.ResponseMetadataUtils;
import budget.application.service.util.TransactionManager;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoryTypeService {

  private static final Logger log = LoggerFactory.getLogger(CategoryTypeService.class);

  private final TransactionManager tx;

  public CategoryTypeService(DataSource dataSource) {
    this.tx = new TransactionManager(dataSource);
  }

  public CategoryTypeResponse create(String requestId, CategoryTypeRequest ctr)
      throws SQLException {
    log.debug("[{}] Create category type: CategoryTypeRequest=[{}]", requestId, ctr);
    return tx.execute(
        bs -> {
          CategoryTypeRepository repo = new CategoryTypeRepository(requestId, bs);
          Validations.validateCategoryType(requestId, ctr);
          CategoryType ctIn = new CategoryType(null, ctr.name());
          CategoryType ctOut = repo.create(ctIn);
          return new CategoryTypeResponse(
              List.of(ctOut), ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public CategoryTypeResponse read(String requestId, List<UUID> ids) throws SQLException {
    log.debug("[{}] Read category types: Ids=[{}]", requestId, ids);
    return tx.execute(
        bs -> {
          CategoryTypeRepository repo = new CategoryTypeRepository(requestId, bs);
          List<CategoryType> ctList = repo.read(ids);

          if (ids.size() == 1 && ctList.isEmpty()) {
            throw new Exceptions.NotFoundException(
                requestId, "CategoryType", ids.getFirst().toString());
          }

          return new CategoryTypeResponse(ctList, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public CategoryTypeResponse update(String requestId, UUID id, CategoryTypeRequest ctr)
      throws SQLException {
    log.debug("[{}] Update category type: Id=[{}], CategoryTypeRequest=[{}]", requestId, id, ctr);
    return tx.execute(
        bs -> {
          CategoryTypeRepository repo = new CategoryTypeRepository(requestId, bs);
          Validations.validateCategoryType(requestId, ctr);

          List<CategoryType> ctList = repo.read(List.of(id));

          if (ctList.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "CategoryType", id.toString());
          }

          CategoryType ctIn = new CategoryType(id, ctr.name());
          CategoryType ctOut = repo.update(ctIn);
          return new CategoryTypeResponse(
              List.of(ctOut), ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public CategoryTypeResponse delete(String requestId, List<UUID> ids) throws SQLException {
    log.info("[{}] Delete category types: Ids=[{}]", requestId, ids);
    return tx.execute(
        bs -> {
          CategoryTypeRepository repo = new CategoryTypeRepository(requestId, bs);

          List<CategoryType> ctList = repo.read(ids);

          if (ids.size() == 1 && ctList.isEmpty()) {
            throw new Exceptions.NotFoundException(
                requestId, "CategoryType", ids.getFirst().toString());
          }

          int deleteCount = repo.delete(ids);
          return new CategoryTypeResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }
}
