package budget.application.db.repository;

import budget.application.db.dao.TransactionItemDao;
import budget.application.model.entity.TransactionItem;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class TransactionItemRepository {

  private final TransactionItemDao dao;

  public TransactionItemRepository(BaseRepository bs) {
    this.dao = new TransactionItemDao(bs.connection());
  }

  public TransactionItem create(TransactionItem ti) throws SQLException {
    return dao.create(ti);
  }

  public List<TransactionItem> read(List<UUID> ids) throws SQLException {
    return dao.read(ids);
  }

  public TransactionItem update(TransactionItem ti) throws SQLException {
    return dao.update(ti);
  }

  public int delete(List<UUID> ids) throws SQLException {
    return dao.delete(ids);
  }

  public List<TransactionItem> readByTransactionIds(List<UUID> txnIds) throws SQLException {
    return dao.readByTransactionIds(txnIds);
  }

  public void createItems(List<TransactionItem> items) throws SQLException {
    for (TransactionItem item : items) {
      dao.create(item);
    }
  }
}
