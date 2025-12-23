package budget.application.db.repository;

import budget.application.db.dao.TransactionDao;
import budget.application.model.entity.Transaction;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class TransactionRepository {

  private final TransactionDao txnDao;

  public TransactionRepository(BaseRepository bs) {
    this.txnDao = new TransactionDao(bs.connection());
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
}
