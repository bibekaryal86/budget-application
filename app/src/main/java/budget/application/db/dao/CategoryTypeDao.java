package budget.application.db.dao;

import budget.application.db.mapper.CategoryTypeRowMapper;
import budget.application.model.entity.CategoryType;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryTypeDao extends BaseDao<CategoryType> {

  public CategoryTypeDao(String requestId, Connection connection) {
    super(requestId, connection, new CategoryTypeRowMapper());
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
  protected List<Object> insertValues(CategoryType ct) {
    return List.of(ct.name().toUpperCase());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("name");
  }

  @Override
  protected List<Object> updateValues(CategoryType ct) {
    return List.of(ct.name().toUpperCase());
  }

  @Override
  protected UUID getId(CategoryType ct) {
    return ct.id();
  }

  @Override
  protected String orderByClause() {
    return "name ASC";
  }

  public Optional<CategoryType> readByIdNoEx(UUID id) {
    try {
      return read(List.of(id)).stream().findFirst();
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
