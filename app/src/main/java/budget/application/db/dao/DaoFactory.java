package budget.application.db.dao;

import java.sql.Connection;

@FunctionalInterface
public interface DaoFactory<T> {
  T create(Connection connection);
}
