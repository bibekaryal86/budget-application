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

  public CategoryTypeResponse create(CategoryTypeRequest categoryTypeRequest) throws SQLException {
    log.debug("Create category type: CategoryTypeRequest=[{}]", categoryTypeRequest);
    return transactionManager.execute(
        transactionContext -> {
          CategoryTypeDao categoryTypeDao = new CategoryTypeDao(transactionContext.connection());
          Validations.validateCategoryType(categoryTypeRequest);
          CategoryType categoryTypeIn = new CategoryType(null, categoryTypeRequest.name());
          UUID id = categoryTypeDao.create(categoryTypeIn).id();
          log.debug("Created category type: Id=[{}]", id);
          CategoryTypeResponse.CategoryType categoryType =
              new CategoryTypeResponse.CategoryType(id, categoryTypeIn.name().toUpperCase());

          return new CategoryTypeResponse(
              List.of(categoryType), ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public CategoryTypeResponse read(List<UUID> ids) throws SQLException {
    log.debug("Read category types: Ids=[{}]", ids);

    return transactionManager.execute(
        transactionContext -> {
          CategoryTypeDao categoryTypeDao = new CategoryTypeDao(transactionContext.connection());
          List<CategoryType> categoryTypeList = categoryTypeDao.read(ids);

          if (ids.size() == 1 && categoryTypeList.isEmpty()) {
            throw new Exceptions.NotFoundException("CategoryType", ids.getFirst().toString());
          }

          List<CategoryTypeResponse.CategoryType> categoryTypes =
              categoryTypeList.stream()
                  .map(ct -> new CategoryTypeResponse.CategoryType(ct.id(), ct.name()))
                  .toList();

          return new CategoryTypeResponse(categoryTypes, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public CategoryTypeResponse update(UUID id, CategoryTypeRequest categoryTypeRequest)
      throws SQLException {
    log.debug("Update category type: Id=[{}], CategoryTypeRequest=[{}]", id, categoryTypeRequest);

    return transactionManager.execute(
        transactionContext -> {
          CategoryTypeDao categoryTypeDao = new CategoryTypeDao(transactionContext.connection());
          Validations.validateCategoryType(categoryTypeRequest);

          List<CategoryType> categoryTypeList = categoryTypeDao.read(List.of(id));

          if (categoryTypeList.isEmpty()) {
            throw new Exceptions.NotFoundException("CategoryType", id.toString());
          }

          CategoryType categoryTypeIn = new CategoryType(id, categoryTypeRequest.name());
          categoryTypeDao.update(categoryTypeIn);
          CategoryTypeResponse.CategoryType categoryType =
              new CategoryTypeResponse.CategoryType(id, categoryTypeIn.name().toUpperCase());

          return new CategoryTypeResponse(
              List.of(categoryType), ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public CategoryTypeResponse delete(List<UUID> ids) throws SQLException {
    log.info("Delete category types: Ids=[{}]", ids);
    return transactionManager.execute(
        transactionContext -> {
          CategoryTypeDao categoryTypeDao = new CategoryTypeDao(transactionContext.connection());

          List<CategoryType> categoryTypeList = categoryTypeDao.read(ids);

          if (ids.size() == 1 && categoryTypeList.isEmpty()) {
            throw new Exceptions.NotFoundException("CategoryType", ids.getFirst().toString());
          }

          int deleteCount = categoryTypeDao.delete(ids);

          return new CategoryTypeResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }
}
