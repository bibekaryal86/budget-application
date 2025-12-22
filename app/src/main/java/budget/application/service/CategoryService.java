package budget.application.service;

import budget.application.db.repository.BaseRepository;
import budget.application.db.repository.CategoryRepository;
import budget.application.db.repository.CategoryTypeRepository;
import budget.application.model.dto.request.CategoryRequest;
import budget.application.model.entity.Category;
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

  public Category create(CategoryRequest cr) throws SQLException {
    validate(cr);
    Category c = Category.builder().name(cr.name()).categoryTypeId(cr.categoryTypeId()).build();
    return repo.create(c);
  }

  public List<Category> read(List<UUID> ids) throws SQLException {
    return repo.read(ids);
  }

  public Category update(UUID id, CategoryRequest cr) throws SQLException {
    validate(cr);
    Category c =
        Category.builder().id(id).name(cr.name()).categoryTypeId(cr.categoryTypeId()).build();
    return repo.update(c);
  }

  public int delete(List<UUID> ids) throws SQLException {
    return repo.delete(ids);
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
