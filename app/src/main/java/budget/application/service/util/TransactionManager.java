package budget.application.service.util;

import budget.application.db.repository.BaseRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public class TransactionManager {

    private final Connection connection;

    public TransactionManager(Connection connection) {
        this.connection = connection;
    }

    /**
     * Execute a function inside a UnitOfWork transaction.
     * Automatically commits on success and rolls back on failure.
     */
    public <T> T execute(Function<BaseRepository, T> work) throws SQLException {
        try (BaseRepository bs = new BaseRepository(connection)) {
            T result = work.apply(bs);
            bs.commit();
            return result;
        }
    }

    /**
     * Execute a void operation inside a UnitOfWork transaction.
     */
    public void executeVoid(TransactionVoidWork work) throws SQLException {
        try (BaseRepository bs = new BaseRepository(connection)) {
            work.apply(bs);
            bs.commit();
        }
    }

    @FunctionalInterface
    public interface TransactionVoidWork {
        void apply(BaseRepository bs) throws SQLException;
    }
}
