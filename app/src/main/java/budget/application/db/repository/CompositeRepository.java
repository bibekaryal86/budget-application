package budget.application.db.repository;

import budget.application.db.dao.CompositeDao;
import budget.application.model.dto.request.CompositeRequest;
import budget.application.model.dto.response.CompositeResponse;
import java.sql.SQLException;
import java.util.List;

public class CompositeRepository {

  private final CompositeDao dao;

  public CompositeRepository(BaseRepository bs) {
    this.dao = new CompositeDao(bs.connection());
  }

  public List<CompositeResponse.TransactionComposite> read(CompositeRequest req)
      throws SQLException {
    return dao.read(req);
  }
}
