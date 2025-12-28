package budget.application.service.domain;

import budget.application.common.Exceptions;
import budget.application.common.Validations;
import budget.application.db.repository.CategoryRepository;
import budget.application.db.repository.CategoryTypeRepository;
import budget.application.model.dto.request.CategoryRequest;
import budget.application.model.dto.response.CategoryResponse;
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
          CategoryRepository repo = new CategoryRepository(requestId, bs);
          CategoryTypeRepository typeRepo = new CategoryTypeRepository(requestId, bs);

          Validations.validateCategory(requestId, cr, typeRepo);

          Category cIn = new Category(null, cr.categoryTypeId(), cr.name(), null, null);
          Category cOut = repo.create(cIn);
          return new CategoryResponse(
              List.of(cOut), ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public CategoryResponse read(String requestId, List<UUID> ids) throws SQLException {
    log.debug("[{}] Read categories: Ids=[{}]", requestId, ids);
    return tx.execute(
        bs -> {
          CategoryRepository repo = new CategoryRepository(requestId, bs);
          List<Category> cList = repo.read(ids);

          if (ids.size() == 1 && cList.isEmpty()) {
            throw new Exceptions.NotFoundException(
                requestId, "Category", ids.getFirst().toString());
          }

          return new CategoryResponse(cList, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public CategoryResponse update(String requestId, UUID id, CategoryRequest cr)
      throws SQLException {
    log.debug("[{}] Update category: Id=[{}], CategoryRequest=[{}]", requestId, id, cr);
    return tx.execute(
        bs -> {
          CategoryRepository repo = new CategoryRepository(requestId, bs);
          CategoryTypeRepository typeRepo = new CategoryTypeRepository(requestId, bs);
          Validations.validateCategory(requestId, cr, typeRepo);

          List<Category> cList = repo.read(List.of(id));
          if (cList.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "Category", id.toString());
          }

          Category cIn = new Category(id, cr.categoryTypeId(), cr.name(), null, null);
          Category cOut = repo.update(cIn);
          return new CategoryResponse(
              List.of(cOut), ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public CategoryResponse delete(String requestId, List<UUID> ids) throws SQLException {
    log.info("[{}] Delete categories: Ids=[{}]", requestId, ids);
    return tx.execute(
        bs -> {
          CategoryRepository repo = new CategoryRepository(requestId, bs);

          List<Category> cList = repo.read(ids);
          if (ids.size() == 1 && cList.isEmpty()) {
            throw new Exceptions.NotFoundException(
                requestId, "Category", ids.getFirst().toString());
          }

          int deleteCount = repo.delete(ids);
          return new CategoryResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }
}
