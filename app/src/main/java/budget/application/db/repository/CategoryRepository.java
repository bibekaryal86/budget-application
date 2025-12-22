package budget.application.db.repository;

import budget.application.db.dao.CategoryDao;
import budget.application.model.entity.Category;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryRepository {

  private final CategoryDao dao;

  public CategoryRepository(BaseRepository bs) {
    this.dao = new CategoryDao(bs.connection());
  }

  public Category create(Category c) throws SQLException {
    return dao.create(c);
  }

  public List<Category> read(List<UUID> ids) throws SQLException {
    return dao.read(ids);
  }

  public List<Category> readByIdsNoEx(List<UUID> ids) {
    try {
      return read(ids);
    } catch (Exception e) {
      return List.of();
    }
  }

  public Optional<Category> readByIdNoEx(UUID id) {
    try {
      return read(List.of(id)).stream().findFirst();
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public Category update(Category c) throws SQLException {
    return dao.update(c);
  }

  public int delete(List<UUID> ids) throws SQLException {
    return dao.delete(ids);
  }
}
