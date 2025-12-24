package budget.application.db.repository;

import budget.application.db.dao.CompositeDao;
import budget.application.model.dto.request.CompositeRequest;
import budget.application.model.dto.response.CompositeResponse;
import java.sql.SQLException;
import java.util.List;

public class CompositeRepository {

  private final CompositeDao dao;

  public CompositeRepository(String requestId, BaseRepository bs) {
    this.dao = new CompositeDao(requestId, bs.connection());
  }

  public List<CompositeResponse.TransactionComposite> readCompositeTransactions(CompositeRequest cr)
      throws SQLException {
    return dao.compositeTransactions(cr);
  }
}
