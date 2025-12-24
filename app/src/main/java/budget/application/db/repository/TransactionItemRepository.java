package budget.application.db.repository;

import budget.application.db.dao.TransactionItemDao;
import budget.application.model.entity.TransactionItem;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionItemRepository {

  private final TransactionItemDao dao;

  public TransactionItemRepository(String requestId, BaseRepository bs) {
    this.dao = new TransactionItemDao(requestId, bs.connection());
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

  public List<TransactionItem> createItems(List<TransactionItem> itemsIn) throws SQLException {
    List<TransactionItem> itemsOut = new ArrayList<>();
    for (TransactionItem item : itemsIn) {
      itemsOut.add(dao.create(item));
    }
    return itemsOut;
  }

  public int deleteByTransactionIds(List<UUID> txnIds) throws SQLException {
    return dao.deleteByTransactionIds(txnIds);
  }
}
