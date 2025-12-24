package budget.application.db.dao;

import budget.application.db.mapper.RowMapper;
import budget.application.db.util.DaoUtils;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class BaseDao<T> {

  protected final Connection connection;
  protected final RowMapper<T> mapper;
  protected final String requestId;

  protected BaseDao(final String requestId, final Connection connection, final RowMapper<T> mapper) {
    this.connection = connection;
    this.mapper = mapper;
    this.requestId = requestId;
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
      log.debug("[{}] Creating [{}] with InsertColumns=[{}], InsertValues=[{}]", requestId, tableName(), insertColumns(), insertValues(entity));
    String cols = String.join(", ", insertColumns());
    String placeholders = DaoUtils.placeholders(insertColumns().size());

    String sql =
        "INSERT INTO " + tableName() + " (" + cols + ") VALUES (" + placeholders + ") RETURNING *";

    log.debug("[{}] Create SQL=[{}]", requestId, sql);
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(stmt, insertValues(entity));
      stmt.executeUpdate();
    }

    return entity;
  }

  // 2) READ
  public List<T> read(List<UUID> ids) throws SQLException {
      log.debug("[{}] Reading [{}] with Ids=[{}]", requestId, tableName(), CommonUtilities.isEmpty(ids) ? "ALL" : ids.toString());
    if (CommonUtilities.isEmpty(ids)) {
      return readAll();
    } else {
      return readByIds(ids);
    }
  }

  private List<T> readAll() throws SQLException {
    String sql = "SELECT * FROM " + tableName() + " ORDER BY " + orderByClause();

      log.debug("[{}] Read All SQL=[{}] ", requestId, sql);
    try (PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      List<T> results = new ArrayList<>();
      while (rs.next()) results.add(mapper.map(rs));
      return results;
    }
  }

  private List<T> readByIds(List<UUID> ids) throws SQLException {
    String sql =
        "SELECT * FROM " + tableName() + " WHERE id IN (" + DaoUtils.placeholders(ids.size()) + ")";

      log.debug("[{}] Read By Ids SQL=[{}] ", requestId, sql);
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(stmt, ids);

      try (ResultSet rs = stmt.executeQuery()) {
        List<T> results = new ArrayList<>();
        while (rs.next()) results.add(mapper.map(rs));
        return results;
      }
    }
  }

  // 3) UPDATE
  public T update(T entity) throws SQLException {
    List<String> cols = updateColumns();
    List<Object> values = updateValues(entity);

      log.debug("[{}] Updating [{}] with UpdateColumns=[{}], UpdateValues=[{}]", requestId, tableName(), cols, values);
    String setClause = String.join(", ", cols.stream().map(c -> c + " = ?").toList());

    String sql = "UPDATE " + tableName() + " SET " + setClause + " WHERE id = ? RETURNING *";
      log.debug("[{}] Update SQL=[{}]", requestId, sql);
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(stmt, values);
      stmt.setObject(values.size() + 1, getId(entity));
      stmt.executeUpdate();
    }

    return entity;
  }

  // 4) DELETE
  public int delete(List<UUID> ids) throws SQLException {
    if (ids == null || ids.isEmpty()) return 0;
// TODO add logs
    String sql =
        "DELETE FROM " + tableName() + " WHERE id IN (" + DaoUtils.placeholders(ids.size()) + ")";

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(stmt, ids);
      return stmt.executeUpdate();
    }
  }
}
