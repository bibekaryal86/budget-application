package budget.service.db.repository;

import budget.service.db.dao.CategoryTypeDao;
import budget.service.model.entities.CategoryType;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class CategoryTypeRepository {

  private final CategoryTypeDao dao;

  public CategoryTypeRepository(BaseRepository uow) {
    this.dao = new CategoryTypeDao(uow.connection());
  }

  public CategoryType create(CategoryType ct) throws SQLException {
    return dao.create(ct);
  }

  public List<CategoryType> read(List<UUID> ids) throws SQLException {
    return dao.read(ids);
  }

  public CategoryType update(CategoryType ct) throws SQLException {
    return dao.update(ct);
  }

  public int delete(List<UUID> ids) throws SQLException {
    return dao.delete(ids);
  }
}
