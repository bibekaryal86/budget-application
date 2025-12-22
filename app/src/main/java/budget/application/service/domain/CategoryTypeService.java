package budget.application.service.domain;

import budget.application.db.repository.CategoryTypeRepository;
import budget.application.model.dto.request.CategoryTypeRequest;
import budget.application.model.dto.response.CategoryTypeResponse;
import budget.application.model.entity.CategoryType;
import budget.application.service.util.ResponseMetadataUtils;
import budget.application.service.util.TransactionManager;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class CategoryTypeService {

  private final TransactionManager tx;

  public CategoryTypeService(TransactionManager tx) {
    this.tx = tx;
  }

  public CategoryTypeResponse create(CategoryTypeRequest ctr) throws SQLException {
    return tx.execute(
        bs -> {
          CategoryTypeRepository repo = new CategoryTypeRepository(bs);
          validate(ctr);
          CategoryType ctIn = CategoryType.builder().name(ctr.name()).build();
          CategoryType ctOut = repo.create(ctIn);
          return new CategoryTypeResponse(
              List.of(ctOut), ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public CategoryTypeResponse read(List<UUID> ids) throws SQLException {
    return tx.execute(
        bs -> {
          CategoryTypeRepository repo = new CategoryTypeRepository(bs);
          List<CategoryType> ctList = repo.read(ids);
          return new CategoryTypeResponse(ctList, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public CategoryTypeResponse update(UUID id, CategoryTypeRequest ctr) throws SQLException {
    return tx.execute(
        bs -> {
          CategoryTypeRepository repo = new CategoryTypeRepository(bs);
          validate(ctr);
          CategoryType ctIn = CategoryType.builder().id(id).name(ctr.name()).build();
          CategoryType ctOut = repo.update(ctIn);
          return new CategoryTypeResponse(
              List.of(ctOut), ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public CategoryTypeResponse delete(List<UUID> ids) throws SQLException {
    return tx.execute(
        bs -> {
          CategoryTypeRepository repo = new CategoryTypeRepository(bs);
          int deleteCount = repo.delete(ids);
          return new CategoryTypeResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }

  private void validate(CategoryTypeRequest ctr) {
    if (ctr == null) {
      throw new IllegalArgumentException("Category type request cannot be null...");
    }
    if (CommonUtilities.isEmpty(ctr.name())) {
      throw new IllegalArgumentException("Category type name cannot be empty...");
    }
  }
}
