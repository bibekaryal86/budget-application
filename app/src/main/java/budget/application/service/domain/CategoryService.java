package budget.application.service.domain;

import budget.application.common.Exceptions;
import budget.application.common.Validations;
import budget.application.db.dao.CategoryDao;
import budget.application.db.dao.CategoryTypeDao;
import budget.application.db.util.TransactionManager;
import budget.application.model.dto.CategoryRequest;
import budget.application.model.dto.CategoryResponse;
import budget.application.model.entity.Category;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoryService {
  private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

  private final TransactionManager transactionManager;

  public CategoryService(DataSource dataSource) {
    this.transactionManager = new TransactionManager(dataSource);
  }

  public CategoryResponse create(CategoryRequest categoryRequest) throws SQLException {
    log.debug("Create category: CategoryRequest=[{}]", categoryRequest);
    return transactionManager.execute(
        transactionContext -> {
          CategoryDao categoryDao = new CategoryDao(transactionContext.connection());
          CategoryTypeDao categoryTypeDao = new CategoryTypeDao(transactionContext.connection());

          Validations.validateCategory(categoryRequest, categoryTypeDao);

          Category categoryIn =
              new Category(null, categoryRequest.categoryTypeId(), categoryRequest.name());
          UUID id = categoryDao.create(categoryIn).id();
          log.debug("Created category: Id=[{}]", id);
          CategoryResponse.Category category =
              categoryDao.readCategories(List.of(id), List.of()).getFirst();

          return new CategoryResponse(
              List.of(category), ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public CategoryResponse read(List<UUID> categoryIds, List<UUID> categoryTypeIds)
      throws SQLException {
    log.debug(
        "Read categories: CategoryIds=[{}], CategoryTypeIds=[{}]", categoryIds, categoryTypeIds);

    return transactionManager.execute(
        transactionContext -> {
          CategoryDao categoryDao = new CategoryDao(transactionContext.connection());
          List<CategoryResponse.Category> categories =
              categoryDao.readCategories(categoryIds, categoryTypeIds);

          if (categoryIds.size() == 1 && categories.isEmpty()) {
            throw new Exceptions.NotFoundException("Category", categoryIds.getFirst().toString());
          }

          return new CategoryResponse(categories, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public CategoryResponse update(UUID id, CategoryRequest categoryRequest) throws SQLException {
    log.debug("Update category: Id=[{}], CategoryRequest=[{}]", id, categoryRequest);
    return transactionManager.execute(
        transactionContext -> {
          CategoryDao categoryDao = new CategoryDao(transactionContext.connection());
          CategoryTypeDao categoryTypeDao = new CategoryTypeDao(transactionContext.connection());
          Validations.validateCategory(categoryRequest, categoryTypeDao);

          List<Category> categoryList = categoryDao.read(List.of(id));
          if (categoryList.isEmpty()) {
            throw new Exceptions.NotFoundException("Category", id.toString());
          }

          Category categoryIn =
              new Category(id, categoryRequest.categoryTypeId(), categoryRequest.name());
          categoryDao.update(categoryIn);
          CategoryResponse.Category category =
              categoryDao.readCategories(List.of(id), List.of()).getFirst();
          return new CategoryResponse(
              List.of(category), ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public CategoryResponse delete(List<UUID> ids) throws SQLException {
    log.info("Delete categories: Ids=[{}]", ids);
    return transactionManager.execute(
        transactionContext -> {
          CategoryDao categoryDao = new CategoryDao(transactionContext.connection());

          List<Category> categoryList = categoryDao.read(ids);
          if (ids.size() == 1 && categoryList.isEmpty()) {
            throw new Exceptions.NotFoundException("Category", ids.getFirst().toString());
          }

          int deleteCount = categoryDao.delete(ids);
          return new CategoryResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }
}
