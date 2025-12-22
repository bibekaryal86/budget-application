package budget.application.service;

import budget.application.db.repository.BaseRepository;
import budget.application.db.repository.CategoryTypeRepository;
import budget.application.model.entity.CategoryType;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class CategoryTypeService {

  private final CategoryTypeRepository repo;

  public CategoryTypeService(BaseRepository bs) {
    this.repo = new CategoryTypeRepository(bs);
  }

  public CategoryType create(CategoryType ct) throws SQLException {
    validate(ct);
    return repo.create(ct);
  }

  public List<CategoryType> read(List<UUID> ids) throws SQLException {
    return repo.read(ids);
  }

  public CategoryType update(CategoryType ct) throws SQLException {
    return repo.update(ct);
  }

  public int delete(List<UUID> ids) throws SQLException {
    return repo.delete(ids);
  }

  private void validate(CategoryType ct) {
    if (ct == null) {
      throw new IllegalArgumentException("Category type cannot be null...");
    }
    if (CommonUtilities.isEmpty(ct.name())) {
      throw new IllegalArgumentException("Category type name cannot be empty...");
    }
  }
}
