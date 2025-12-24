package budget.application.db.repository;

import budget.application.db.dao.CompositeDao;
import budget.application.model.dto.request.CompositeRequest;
import budget.application.model.dto.response.CompositeResponse;
import budget.application.service.util.TransactionManager;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.SQLException;
import java.util.List;

public class CompositeRepository {

  private final TransactionManager tx;

  public CompositeRepository(TransactionManager tx) {
    this.tx = tx;
  }

  public CompositeResponse read(CompositeRequest req) throws SQLException {
    return tx.execute(
        bs -> {
          CompositeDao dao = new CompositeDao(bs.connection());
          List<CompositeResponse.TransactionComposite> data = dao.read(req);
          return new CompositeResponse(data, ResponseMetadata.emptyResponseMetadata());
        });
  }
}
