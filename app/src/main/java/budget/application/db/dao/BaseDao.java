package budget.application.db.dao;

import budget.application.cache.InMemoryCache;
import budget.application.db.mapper.RowMapper;
import budget.application.db.util.DaoUtils;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseDao<T> {
  protected static final Logger log = LoggerFactory.getLogger(BaseDao.class);

  protected final Connection connection;
  protected final RowMapper<T> mapper;
  protected final InMemoryCache<T> cache;

  protected BaseDao(
      final Connection connection, final RowMapper<T> mapper, final InMemoryCache<T> cache) {
    this.connection = connection;
    this.mapper = mapper;
    this.cache = cache;
  }

  // ---- ABSTRACT CONTRACT ----

  protected abstract String tableName();

  protected abstract List<String> insertColumns();

  protected abstract List<Object> insertValues(T entity);

  protected abstract List<String> updateColumns();

  protected abstract List<Object> updateValues(T entity);

  protected abstract UUID getId(T entity);

  protected abstract String orderByClause();

  // ---- GENERIC CRUD ----

  // 1) CREATE
  public T create(T entity) throws SQLException {
    log.debug(
        "Creating [{}] with InsertColumns=[{}], InsertValues=[{}]",
        tableName(),
        insertColumns(),
        insertValues(entity));
    String columns = String.join(", ", insertColumns());
    String placeholders = DaoUtils.placeholders(insertColumns().size());

    String sql =
        "INSERT INTO "
            + tableName()
            + " ("
            + columns
            + ") VALUES ("
            + placeholders
            + ") RETURNING *";

    log.debug("Create SQL=[{}]", sql);
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(preparedStatement, insertValues(entity), Boolean.FALSE);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          T result = mapper.map(resultSet);

          if (cache != null) {
            cache.put(result);
          }

          return result;
        }
      }
    }

    return null;
  }

  // 2) READ
  public List<T> read(List<UUID> ids) throws SQLException {
    log.debug(
        "Reading [{}] with Ids=[{}]",
        tableName(),
        CommonUtilities.isEmpty(ids) ? "ALL" : ids.toString());
    if (CommonUtilities.isEmpty(ids)) {
      return readAll();
    } else {
      return readByIds(ids);
    }
  }

  private List<T> readAll() throws SQLException {
    if (cache != null) {
      List<T> cacheResults = cache.get();
      if (!CommonUtilities.isEmpty(cacheResults)) {
        return cacheResults;
      }
    }

    String sql = "SELECT * FROM " + tableName() + " ORDER BY " + orderByClause();

    log.debug("Read All SQL=[{}] ", sql);
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery()) {

      List<T> results = new ArrayList<>();
      while (resultSet.next()) results.add(mapper.map(resultSet));

      if (cache != null) {
          cache.clear();
          cache.put(results);
      }

      return results;
    }
  }

  private List<T> readByIds(List<UUID> ids) throws SQLException {
    if (cache != null) {
      List<T> cacheResults = cache.get(ids);
      if (!CommonUtilities.isEmpty(cacheResults)) {
        return cacheResults;
      }
    }
    String sql =
        "SELECT * FROM " + tableName() + " WHERE id IN (" + DaoUtils.placeholders(ids.size()) + ")";

    log.debug("Read By Ids SQL=[{}] ", sql);
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(preparedStatement, ids, Boolean.TRUE);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) results.add(mapper.map(resultSet));
        return results;
      }
    }
  }

  // 3) UPDATE
  public T update(T entity) throws SQLException {
    if (cache != null) {
      cache.clear(List.of(getId(entity)));
    }
    List<String> columns = updateColumns();
    List<Object> values = updateValues(entity);

    log.debug(
        "Updating [{}] with UpdateColumns=[{}], UpdateValues=[{}]", tableName(), columns, values);
    String setClause = String.join(", ", columns.stream().map(c -> c + " = ?").toList());

    String sql = "UPDATE " + tableName() + " SET " + setClause + " WHERE id = ? RETURNING *";
    log.debug("Update SQL=[{}]", sql);
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(preparedStatement, values, Boolean.FALSE);
      preparedStatement.setObject(values.size() + 1, getId(entity));

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          T result = mapper.map(resultSet);

          if (cache != null) {
            cache.put(result);
          }

          return result;
        }
      }
    }

    return null;
  }

  // 4) DELETE
  public int delete(List<UUID> ids) throws SQLException {
    log.debug("Deleting [{}] with Ids=[{}]", tableName(), ids);

    if (cache != null) {
      cache.clear(ids);
    }

    if (ids == null || ids.isEmpty()) return 0;
    String sql =
        "DELETE FROM " + tableName() + " WHERE id IN (" + DaoUtils.placeholders(ids.size()) + ")";
    log.debug("Delete SQL=[{}]", sql);
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(preparedStatement, ids, Boolean.FALSE);
      return preparedStatement.executeUpdate();
    }
  }
}
