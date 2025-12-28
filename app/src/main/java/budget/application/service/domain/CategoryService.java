package budget.application.service.domain;

import budget.application.common.Exceptions;
import budget.application.common.Validations;
import budget.application.db.dao.CategoryDao;
import budget.application.db.dao.CategoryTypeDao;
import budget.application.model.dto.CategoryRequest;
import budget.application.model.dto.CategoryResponse;
import budget.application.model.entity.Category;
import budget.application.service.util.ResponseMetadataUtils;
import budget.application.service.util.TransactionManager;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoryService {
  private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

  private final TransactionManager tx;

  public CategoryService(DataSource dataSource) {
    this.tx = new TransactionManager(dataSource);
  }

  public CategoryResponse create(String requestId, CategoryRequest cr) throws SQLException {
    log.debug("[{}] Create category: CategoryRequest=[{}]", requestId, cr);
    return tx.execute(
        bs -> {
          CategoryDao dao = new CategoryDao(requestId, bs.connection());
          CategoryTypeDao typeDao = new CategoryTypeDao(requestId, bs.connection());

          Validations.validateCategory(requestId, cr, typeDao);

          Category cIn = new Category(null, cr.categoryTypeId(), cr.name());
          UUID id = dao.create(cIn).id();
          CategoryResponse.Category cOut = dao.readCategories(List.of(id), List.of()).getFirst();

          return new CategoryResponse(
              List.of(cOut), ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public CategoryResponse read(String requestId, List<UUID> catIds, List<UUID> catTypeIds)
      throws SQLException {
    log.debug("[{}] Read categories: CatIds=[{}], CatTypeIds=[{}}", requestId, catIds, catTypeIds);
    return tx.execute(
        bs -> {
          CategoryDao dao = new CategoryDao(requestId, bs.connection());
          List<CategoryResponse.Category> cList = dao.readCategories(catIds, catTypeIds);

          if (catIds.size() == 1 && cList.isEmpty()) {
            throw new Exceptions.NotFoundException(
                requestId, "Category", catIds.getFirst().toString());
          }

          return new CategoryResponse(cList, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public CategoryResponse update(String requestId, UUID id, CategoryRequest cr)
      throws SQLException {
    log.debug("[{}] Update category: Id=[{}], CategoryRequest=[{}]", requestId, id, cr);
    return tx.execute(
        bs -> {
          CategoryDao dao = new CategoryDao(requestId, bs.connection());
          CategoryTypeDao typeDao = new CategoryTypeDao(requestId, bs.connection());
          Validations.validateCategory(requestId, cr, typeDao);

          List<Category> cList = dao.read(List.of(id));
          if (cList.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "Category", id.toString());
          }

          Category cIn = new Category(id, cr.categoryTypeId(), cr.name());
          dao.update(cIn);
          CategoryResponse.Category cOut = dao.readCategories(List.of(id), List.of()).getFirst();
          return new CategoryResponse(
              List.of(cOut), ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public CategoryResponse delete(String requestId, List<UUID> ids) throws SQLException {
    log.info("[{}] Delete categories: Ids=[{}]", requestId, ids);
    return tx.execute(
        bs -> {
          CategoryDao dao = new CategoryDao(requestId, bs.connection());

          List<Category> cList = dao.read(ids);
          if (ids.size() == 1 && cList.isEmpty()) {
            throw new Exceptions.NotFoundException(
                requestId, "Category", ids.getFirst().toString());
          }

          int deleteCount = dao.delete(ids);
          return new CategoryResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }
}
