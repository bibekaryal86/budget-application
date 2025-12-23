package budget.application.db.repository;

import budget.application.db.dao.TransactionCompositeDao;
import budget.application.model.dto.request.TransactionRequestComposite;
import budget.application.model.dto.response.TransactionResponseComposite;
import budget.application.service.util.TransactionManager;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.SQLException;
import java.util.List;

public class TransactionCompositeRepository {

  private final TransactionManager tx;

  public TransactionCompositeRepository(TransactionManager tx) {
    this.tx = tx;
  }

  public TransactionResponseComposite read(TransactionRequestComposite req) throws SQLException {
    return tx.execute(
        bs -> {
          TransactionCompositeDao dao = new TransactionCompositeDao(bs.connection());
          List<TransactionResponseComposite.TransactionComposite> data = dao.read(req);
          return new TransactionResponseComposite(data, ResponseMetadata.emptyResponseMetadata());
        });
  }
}
