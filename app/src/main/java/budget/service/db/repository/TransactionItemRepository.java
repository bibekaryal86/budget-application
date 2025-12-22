package budget.service.db.repository;

import budget.service.db.dao.TransactionItemDao;
import budget.service.model.entities.TransactionItem;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class TransactionItemRepository {

  private final TransactionItemDao dao;

  public TransactionItemRepository(BaseRepository uow) {
    this.dao = new TransactionItemDao(uow.connection());
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
}
