package budget.application.db.dao;

import budget.application.db.mapper.CategoryRowMapper;
import budget.application.model.entity.Category;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

public class CategoryDao extends BaseDao<Category> {

  public CategoryDao(String requestId, Connection connection) {
    super(requestId, connection, new CategoryRowMapper());
  }

  @Override
  protected String tableName() {
    return "category";
  }

  @Override
  protected List<String> insertColumns() {
    return List.of("category_type_id", "name");
  }

  @Override
  protected List<Object> insertValues(Category c) {
    return List.of(c.categoryTypeId(), c.name().toUpperCase());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("category_type_id", "name");
  }

  @Override
  protected List<Object> updateValues(Category c) {
    return List.of(c.categoryTypeId(), c.name().toUpperCase());
  }

  @Override
  protected UUID getId(Category c) {
    return c.id();
  }

  @Override
  protected String orderByClause() {
    return "name ASC";
  }
}
