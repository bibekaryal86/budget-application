package budget.application.db.dao;

import budget.application.cache.CategoryCache;
import budget.application.db.mapper.CategoryRowMappers;
import budget.application.db.util.DaoUtils;
import budget.application.model.dto.CategoryResponse;
import budget.application.model.entity.Category;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class CategoryDao extends BaseDao<Category> {

  private final CategoryRowMappers.CategoryRowMapperResponse categoryRowMapperResponse;

  public CategoryDao(Connection connection, CategoryCache categoryCache) {
    super(connection, new CategoryRowMappers.CategoryRowMapper(), categoryCache);
    this.categoryRowMapperResponse = new CategoryRowMappers.CategoryRowMapperResponse();
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
  protected List<Object> insertValues(Category category) {
    return List.of(category.categoryTypeId(), category.name().toUpperCase());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("category_type_id", "name");
  }

  @Override
  protected List<Object> updateValues(Category category) {
    return List.of(category.categoryTypeId(), category.name().toUpperCase());
  }

  @Override
  protected UUID getId(Category category) {
    return category.id();
  }

  @Override
  protected String orderByClause() {
    return "name ASC";
  }

  public List<Category> readNoEx(List<UUID> ids) {
    try {
      return read(ids);
    } catch (Exception e) {
      return List.of();
    }
  }

  public List<CategoryResponse.Category> readCategories(
      List<UUID> categoryIds, List<UUID> categoryTypeIds) throws SQLException {
    log.debug(
        "Read Categories: CategoryIds=[{}], CategoryTypeIds=[{}]", categoryIds, categoryTypeIds);
    StringBuilder sql =
        new StringBuilder(
            """
          SELECT
              c.id AS category_id,
              c.name AS category_name,
              ct.id AS category_type_id,
              ct.name AS category_type_name
          FROM category c
          JOIN category_type ct
            ON ct.id = c.category_type_id
          """);

    List<Object> params = new ArrayList<>();
    final boolean[] whereAdded = {false};

    Consumer<String> addWhere =
        (condition) -> {
          sql.append(whereAdded[0] ? " AND " : " WHERE ");
          sql.append(condition);
          whereAdded[0] = true;
        };

    if (!CommonUtilities.isEmpty(categoryIds)) {
      addWhere.accept("c.id IN (" + DaoUtils.placeholders(categoryIds.size()) + ")");
      params.addAll(categoryIds);
    }
    if (!CommonUtilities.isEmpty(categoryTypeIds)) {
      addWhere.accept(
          "c.category_type_id IN (" + DaoUtils.placeholders(categoryTypeIds.size()) + ")");
      params.addAll(categoryTypeIds);
    }
    sql.append(" ORDER BY ct.name, c.name ASC ");

    log.debug("Read Categories SQL=[{}]", sql);

    try (PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
      if (!params.isEmpty()) {
        DaoUtils.bindParams(preparedStatement, params, Boolean.TRUE);
      }

      List<CategoryResponse.Category> results = new ArrayList<>();
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          results.add(categoryRowMapperResponse.map(resultSet));
        }
      }
      return results;
    }
  }
}
