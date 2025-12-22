package budget.application.db.dao;

import budget.application.db.mapper.CategoryRowMapper;
import budget.application.model.entities.Category;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CategoryDao extends BaseDao<Category> {

  public CategoryDao(Connection connection) {
    super(connection, new CategoryRowMapper());
  }

  @Override
  protected String tableName() {
    return "category";
  }

  @Override
  protected List<String> insertColumns() {
    return List.of("id", "category_type_id", "name", "created_at", "updated_at");
  }

  @Override
  protected List<Object> insertValues(Category c) {
    return List.of(c.id(), c.categoryTypeId(), c.name(), LocalDateTime.now(), LocalDateTime.now());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("category_type_id", "name", "updated_at");
  }

  @Override
  protected List<Object> updateValues(Category c) {
    return List.of(c.categoryTypeId(), c.name(), LocalDateTime.now());
  }

  @Override
  protected UUID getId(Category c) {
    return c.id();
  }
}
