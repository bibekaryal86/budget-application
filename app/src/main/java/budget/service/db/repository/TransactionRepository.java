package budget.service.db.repository;

import budget.service.db.dao.TransactionDao;
import budget.service.db.dao.TransactionItemDao;
import budget.service.model.entities.Transaction;
import budget.service.model.entities.TransactionItem;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class TransactionRepository {

  private final TransactionDao txnDao;
  private final TransactionItemDao itemDao;

  public TransactionRepository(BaseRepository uow) {
    this.txnDao = new TransactionDao(uow.connection());
    this.itemDao = new TransactionItemDao(uow.connection());
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
  public List<TransactionItem> readItems(UUID txnId) throws SQLException {
    return itemDao.readByTransactionId(txnId);
  }

  public void createItems(List<TransactionItem> items) throws SQLException {
    for (TransactionItem item : items) {
      itemDao.create(item);
    }
  }
}
