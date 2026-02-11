package budget.application.db.mapper;

import budget.application.model.dto.TransactionItemResponse;
import budget.application.model.entity.TransactionItem;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class TransactionItemRowMappers {

  public static class TransactionItemRowMapper implements RowMapper<TransactionItem> {
    @Override
    public TransactionItem map(ResultSet resultSet) throws SQLException {
      return new TransactionItem(
          resultSet.getObject("id", UUID.class),
          resultSet.getObject("transaction_id", UUID.class),
          resultSet.getObject("category_id", UUID.class),
          resultSet.getObject("account_id", UUID.class),
          resultSet.getBigDecimal("amount"),
          extractReportTags(resultSet.getArray("tags")),
          resultSet.getString("notes"));
    }
  }

  public static class TransactionItemRowMapperResponse
      implements RowMapper<TransactionItemResponse.TransactionItem> {
    @Override
    public TransactionItemResponse.TransactionItem map(ResultSet resultSet) throws SQLException {
      return new TransactionItemResponse.TransactionItem(
          resultSet.getObject("txn_item_id", UUID.class),
          null,
          new CategoryRowMappers.CategoryRowMapperResponse().map(resultSet),
          new AccountRowMappers.AccountRowMapperResponse().map(resultSet),
          resultSet.getBigDecimal("txn_item_amount"),
          extractReportTags(resultSet.getArray("txn_item_tags")),
          resultSet.getString("txn_item_notes"));
    }
  }

  private static List<String> extractReportTags(Array array) throws SQLException {
    if (array == null) {
      return List.of();
    }

    String[] tagsArray = (String[]) array.getArray();
    if (tagsArray == null || tagsArray.length == 0) {
      return List.of();
    }

    return List.of(tagsArray);
  }
}
