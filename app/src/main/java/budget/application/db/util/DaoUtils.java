package budget.application.db.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class DaoUtils {

  public static void bindParams(PreparedStatement stmt, List<?> params) throws SQLException {
    for (int i = 0; i < params.size(); i++) {
      stmt.setObject(i + 1, params.get(i));
    }
  }

  public static String placeholders(int count) {
    return String.join(",", Collections.nCopies(count, "?"));
  }
}
