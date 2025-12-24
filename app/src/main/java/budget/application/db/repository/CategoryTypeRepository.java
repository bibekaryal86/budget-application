package budget.application.db.repository;

import budget.application.db.dao.CategoryTypeDao;
import budget.application.model.entity.CategoryType;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryTypeRepository {

  private final CategoryTypeDao dao;

  public CategoryTypeRepository(String requestId, BaseRepository bs) {
    this.dao = new CategoryTypeDao(requestId, bs.connection());
  }

  public CategoryType create(CategoryType ct) throws SQLException {
    return dao.create(ct);
  }

  public List<CategoryType> read(List<UUID> ids) throws SQLException {
    return dao.read(ids);
  }

  public Optional<CategoryType> readByIdNoEx(UUID id) {
    try {
      return read(List.of(id)).stream().findFirst();
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public CategoryType update(CategoryType ct) throws SQLException {
    return dao.update(ct);
  }

  public int delete(List<UUID> ids) throws SQLException {
    return dao.delete(ids);
  }
}
