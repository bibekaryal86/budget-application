package budget.application.service.domain;

import budget.application.db.repository.BaseRepository;
import budget.application.db.repository.CategoryRepository;
import budget.application.db.repository.CategoryTypeRepository;
import budget.application.model.dto.request.CategoryRequest;
import budget.application.model.dto.response.CategoryResponse;
import budget.application.model.entity.Category;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class CategoryService {

  private final CategoryRepository repo;
  private final CategoryTypeRepository typeRepo;

  public CategoryService(BaseRepository bs) {
    this.repo = new CategoryRepository(bs);
    this.typeRepo = new CategoryTypeRepository(bs);
  }

  public CategoryResponse create(CategoryRequest cr) throws SQLException {
    validate(cr);
    Category cIn = Category.builder().name(cr.name()).categoryTypeId(cr.categoryTypeId()).build();
    Category cOut = repo.create(cIn);
    return new CategoryResponse(
        List.of(cOut), ResponseMetadataUtils.defaultInsertResponseMetadata());
  }

  public CategoryResponse read(List<UUID> ids) throws SQLException {
    List<Category> cList = repo.read(ids);
    return new CategoryResponse(cList, ResponseMetadata.emptyResponseMetadata());
  }

  public CategoryResponse update(UUID id, CategoryRequest cr) throws SQLException {
    validate(cr);
    Category cIn =
        Category.builder().id(id).name(cr.name()).categoryTypeId(cr.categoryTypeId()).build();
    Category cOut = repo.update(cIn);
    return new CategoryResponse(
        List.of(cOut), ResponseMetadataUtils.defaultUpdateResponseMetadata());
  }

  public CategoryResponse delete(List<UUID> ids) throws SQLException {
    int deleteCount = repo.delete(ids);
    return new CategoryResponse(
        List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
  }

  private void validate(CategoryRequest cr) {
    if (cr == null) {
      throw new IllegalArgumentException("Category request cannot be null...");
    }
    if (CommonUtilities.isEmpty(cr.name())) {
      throw new IllegalArgumentException("Category name cannot be empty...");
    }
    if (cr.categoryTypeId() == null) {
      throw new IllegalArgumentException("Category type cannot be null...");
    }
    if (typeRepo.readByIdNoEx(cr.categoryTypeId()).isEmpty()) {
      throw new IllegalArgumentException("Category type does not exist...");
    }
  }
}
