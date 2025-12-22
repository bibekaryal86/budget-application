package budget.application.db.repository;

import budget.application.db.dao.CategoryDao;
import budget.application.model.entities.Category;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class CategoryRepository {

  private final CategoryDao dao;

  public CategoryRepository(BaseRepository uow) {
    this.dao = new CategoryDao(uow.connection());
  }

  public Category create(Category c) throws SQLException {
    return dao.create(c);
  }

  public List<Category> read(List<UUID> ids) throws SQLException {
    return dao.read(ids);
  }

  public Category update(Category c) throws SQLException {
    return dao.update(c);
  }

  public int delete(List<UUID> ids) throws SQLException {
    return dao.delete(ids);
  }
}
