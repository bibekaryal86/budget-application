package budget.application.service.domain;

import budget.application.common.Exceptions;
import budget.application.common.Validations;
import budget.application.db.dao.CategoryTypeDao;
import budget.application.db.util.TransactionManager;
import budget.application.model.dto.CategoryTypeRequest;
import budget.application.model.dto.CategoryTypeResponse;
import budget.application.model.entity.CategoryType;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoryTypeService {

  private static final Logger log = LoggerFactory.getLogger(CategoryTypeService.class);

  private final TransactionManager transactionManager;

  public CategoryTypeService(DataSource dataSource) {
    this.transactionManager = new TransactionManager(dataSource);
  }

  public CategoryTypeResponse create(String requestId, CategoryTypeRequest categoryTypeRequest)
      throws SQLException {
    log.debug(
        "[{}] Create category type: CategoryTypeRequest=[{}]", requestId, categoryTypeRequest);
    return transactionManager.execute(
        requestId,
        transactionContext -> {
          CategoryTypeDao categoryTypeDao =
              new CategoryTypeDao(requestId, transactionContext.connection());
          Validations.validateCategoryType(requestId, categoryTypeRequest);
          CategoryType categoryTypeIn = new CategoryType(null, categoryTypeRequest.name());
          UUID id = categoryTypeDao.create(categoryTypeIn).id();
          log.debug("[{}] Created category type: Id=[{}]", requestId, id);
          CategoryTypeResponse.CategoryType categoryType =
              new CategoryTypeResponse.CategoryType(id, categoryTypeIn.name().toUpperCase());
          return new CategoryTypeResponse(
              List.of(categoryType), ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public CategoryTypeResponse read(String requestId, List<UUID> ids) throws SQLException {
    log.debug("[{}] Read category types: Ids=[{}]", requestId, ids);
    return transactionManager.execute(
        requestId,
        transactionContext -> {
          CategoryTypeDao categoryTypeDao =
              new CategoryTypeDao(requestId, transactionContext.connection());
          List<CategoryType> categoryTypeList = categoryTypeDao.read(ids);

          if (ids.size() == 1 && categoryTypeList.isEmpty()) {
            throw new Exceptions.NotFoundException(
                requestId, "CategoryType", ids.getFirst().toString());
          }

          List<CategoryTypeResponse.CategoryType> categoryTypes =
              categoryTypeList.stream()
                  .map(ct -> new CategoryTypeResponse.CategoryType(ct.id(), ct.name()))
                  .toList();

          return new CategoryTypeResponse(categoryTypes, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public CategoryTypeResponse update(
      String requestId, UUID id, CategoryTypeRequest categoryTypeRequest) throws SQLException {
    log.debug(
        "[{}] Update category type: Id=[{}], CategoryTypeRequest=[{}]",
        requestId,
        id,
        categoryTypeRequest);
    return transactionManager.execute(
        requestId,
        transactionContext -> {
          CategoryTypeDao categoryTypeDao =
              new CategoryTypeDao(requestId, transactionContext.connection());
          Validations.validateCategoryType(requestId, categoryTypeRequest);

          List<CategoryType> categoryTypeList = categoryTypeDao.read(List.of(id));

          if (categoryTypeList.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "CategoryType", id.toString());
          }

          CategoryType categoryTypeIn = new CategoryType(id, categoryTypeRequest.name());
          categoryTypeDao.update(categoryTypeIn);
          CategoryTypeResponse.CategoryType categoryType =
              new CategoryTypeResponse.CategoryType(id, categoryTypeIn.name().toUpperCase());
          return new CategoryTypeResponse(
              List.of(categoryType), ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public CategoryTypeResponse delete(String requestId, List<UUID> ids) throws SQLException {
    log.info("[{}] Delete category types: Ids=[{}]", requestId, ids);
    return transactionManager.execute(
        requestId,
        transactionContext -> {
          CategoryTypeDao categoryTypeDao =
              new CategoryTypeDao(requestId, transactionContext.connection());

          List<CategoryType> categoryTypeList = categoryTypeDao.read(ids);

          if (ids.size() == 1 && categoryTypeList.isEmpty()) {
            throw new Exceptions.NotFoundException(
                requestId, "CategoryType", ids.getFirst().toString());
          }

          int deleteCount = categoryTypeDao.delete(ids);
          return new CategoryTypeResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }
}
