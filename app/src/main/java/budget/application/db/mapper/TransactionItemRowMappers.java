package budget.application.db.mapper;

import budget.application.model.dto.CategoryResponse;
import budget.application.model.dto.CategoryTypeResponse;
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
    public TransactionItem map(ResultSet rs) throws SQLException {
      return new TransactionItem(
          rs.getObject("id", UUID.class),
          rs.getObject("transaction_id", UUID.class),
          rs.getObject("category_id", UUID.class),
          rs.getString("label"),
          rs.getBigDecimal("amount"),
          extractReportTags(rs.getArray("tags")));
    }
  }

  public static class TransactionItemRowMapperResponse
      implements RowMapper<TransactionItemResponse.TransactionItem> {
    @Override
    public TransactionItemResponse.TransactionItem map(ResultSet rs) throws SQLException {
      return new TransactionItemResponse.TransactionItem(
          rs.getObject("txn_item_id", UUID.class),
          null,
          new CategoryResponse.Category(
              rs.getObject("category_id", UUID.class),
              new CategoryTypeResponse.CategoryType(
                  rs.getObject("category_type_id", UUID.class), rs.getString("category_type_name")),
              rs.getString("category_name")),
          rs.getString("txn_item_label"),
          rs.getBigDecimal("txn_item_amount"),
          extractReportTags(rs.getArray("txn_item_tags")));
    }
  }

  private static List<String> extractReportTags(Array sqlArray) throws SQLException {
    if (sqlArray == null) {
      return List.of();
    }

    String[] tagsArray = (String[]) sqlArray.getArray();
    if (tagsArray == null || tagsArray.length == 0) {
      return List.of();
    }

    return List.of(tagsArray);
  }
}
