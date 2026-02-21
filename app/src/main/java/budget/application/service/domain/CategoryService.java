package budget.application.service.domain;

import budget.application.common.Exceptions;
import budget.application.common.Validations;
import budget.application.db.dao.CategoryDao;
import budget.application.db.dao.DaoFactory;
import budget.application.db.util.TransactionManager;
import budget.application.model.dto.CategoryRequest;
import budget.application.model.dto.CategoryResponse;
import budget.application.model.entity.Category;
import budget.application.model.entity.CategoryType;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoryService {
  private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

  private final TransactionManager transactionManager;
  private final DaoFactory<CategoryDao> categoryDaoFactory;
  private final CategoryTypeService categoryTypeService;

  public CategoryService(
      DataSource dataSource,
      DaoFactory<CategoryDao> categoryDaoFactory,
      CategoryTypeService categoryTypeService) {
    this.transactionManager = new TransactionManager(dataSource);
    this.categoryDaoFactory = categoryDaoFactory;
    this.categoryTypeService = categoryTypeService;
  }

  public CategoryResponse create(CategoryRequest categoryRequest) throws SQLException {
    log.debug("Create category: CategoryRequest=[{}]", categoryRequest);
    return transactionManager.execute(
        transactionContext -> {
          CategoryDao categoryDao = categoryDaoFactory.create(transactionContext.connection());

          List<CategoryType> categoryTypeList =
              categoryRequest == null || categoryRequest.categoryTypeId() == null
                  ? List.of()
                  : categoryTypeService.readNoEx(
                      List.of(categoryRequest.categoryTypeId()), transactionContext.connection());
          Validations.validateCategory(categoryRequest, categoryTypeList);

          Category categoryIn =
              new Category(null, categoryRequest.categoryTypeId(), categoryRequest.name());
          UUID id = categoryDao.create(categoryIn).id();
          log.debug("Created category: Id=[{}]", id);
          CategoryResponse.Category category = categoryDao.readCategories(List.of(id)).getFirst();

          return new CategoryResponse(
              List.of(category), ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public List<Category> readNoEx(List<UUID> ids, Connection connection) throws SQLException {
    CategoryDao categoryDao = categoryDaoFactory.create(connection);
    return categoryDao.readNoEx(ids);
  }

  public CategoryResponse read(List<UUID> categoryIds) throws SQLException {
    log.debug("Read categories: CategoryIds=[{}]", categoryIds);

    return transactionManager.execute(
        transactionContext -> {
          CategoryDao categoryDao = categoryDaoFactory.create(transactionContext.connection());
          List<CategoryResponse.Category> categories = categoryDao.readCategories(categoryIds);

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
          CategoryDao categoryDao = categoryDaoFactory.create(transactionContext.connection());

          List<CategoryType> categoryTypeList =
              categoryRequest == null || categoryRequest.categoryTypeId() == null
                  ? List.of()
                  : categoryTypeService.readNoEx(
                      List.of(categoryRequest.categoryTypeId()), transactionContext.connection());
          Validations.validateCategory(categoryRequest, categoryTypeList);

          List<Category> categoryList = categoryDao.read(List.of(id));
          if (categoryList.isEmpty()) {
            throw new Exceptions.NotFoundException("Category", id.toString());
          }

          Category categoryIn =
              new Category(id, categoryRequest.categoryTypeId(), categoryRequest.name());
          categoryDao.update(categoryIn);
          CategoryResponse.Category category = categoryDao.readCategories(List.of(id)).getFirst();
          return new CategoryResponse(
              List.of(category), ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public CategoryResponse delete(List<UUID> ids) throws SQLException {
    log.info("Delete categories: Ids=[{}]", ids);
    return transactionManager.execute(
        transactionContext -> {
          CategoryDao categoryDao = categoryDaoFactory.create(transactionContext.connection());

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
