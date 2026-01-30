package budget.application.db.dao;

import budget.application.db.mapper.CategoryTypeRowMapper;
import budget.application.model.entity.CategoryType;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

public class CategoryTypeDao extends BaseDao<CategoryType> {

  public CategoryTypeDao(Connection connection) {
    super(connection, new CategoryTypeRowMapper(), null);
  }

  @Override
  protected String tableName() {
    return "category_type";
  }

  @Override
  protected List<String> insertColumns() {
    return List.of("name");
  }

  @Override
  protected List<Object> insertValues(CategoryType categoryType) {
    return List.of(categoryType.name().toUpperCase());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("name");
  }

  @Override
  protected List<Object> updateValues(CategoryType categoryType) {
    return List.of(categoryType.name().toUpperCase());
  }

  @Override
  protected UUID getId(CategoryType categoryType) {
    return categoryType.id();
  }

  @Override
  protected String orderByClause() {
    return "name ASC";
  }

  public List<CategoryType> readNoEx(List<UUID> ids) {
    try {
      return read(ids);
    } catch (Exception e) {
      return List.of();
    }
  }
}
