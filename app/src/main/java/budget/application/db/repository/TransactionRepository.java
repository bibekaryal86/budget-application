package budget.application.db.repository;

import budget.application.db.dao.TransactionDao;
import budget.application.db.dao.TransactionItemDao;
import budget.application.model.entity.Transaction;
import budget.application.model.entity.TransactionItem;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class TransactionRepository {

  private final TransactionDao txnDao;
  private final TransactionItemDao itemDao;

  public TransactionRepository(BaseRepository bs) {
    this.txnDao = new TransactionDao(bs.connection());
    this.itemDao = new TransactionItemDao(bs.connection());
  }

  public Transaction create(Transaction t) throws SQLException {
    return txnDao.create(t);
  }

  public List<Transaction> read(List<UUID> ids) throws SQLException {
    return txnDao.read(ids);
  }

  public Transaction update(Transaction t) throws SQLException {
    return txnDao.update(t);
  }

  public int delete(List<UUID> ids) throws SQLException {
    return txnDao.delete(ids);
  }

  // Aggregate operations
  public void createItems(List<TransactionItem> items) throws SQLException {
    for (TransactionItem item : items) {
      itemDao.create(item);
    }
  }

  public List<TransactionItem> readItems(UUID txnId) throws SQLException {
    return itemDao.readByTransactionId(txnId);
  }

  public int deleteItems(List<UUID> ids) throws SQLException {
    return itemDao.delete(ids);
  }
}
