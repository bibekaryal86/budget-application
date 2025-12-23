package budget.application.db.dao;

import budget.application.db.mapper.TransactionRowMapper;
import budget.application.model.entity.Transaction;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TransactionDao extends BaseDao<Transaction> {

  public TransactionDao(Connection connection) {
    super(connection, new TransactionRowMapper());
  }

  @Override
  protected String tableName() {
    return "transaction";
  }

  @Override
  protected List<String> insertColumns() {
    return List.of("txn_date", "merchant", "total_amount", "notes");
  }

  @Override
  protected List<Object> insertValues(Transaction t) {
    return List.of(t.txnDate(), t.merchant(), t.totalAmount(), t.notes());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("txn_date", "merchant", "total_amount", "notes", "updated_at");
  }

  @Override
  protected List<Object> updateValues(Transaction t) {
    return List.of(t.txnDate(), t.merchant(), t.totalAmount(), t.notes(), LocalDateTime.now());
  }

  @Override
  protected UUID getId(Transaction t) {
    return t.id();
  }

    @Override
    protected String orderByClause() {
        return "txn_date DESC";
    }
}
