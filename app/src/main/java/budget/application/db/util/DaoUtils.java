package budget.application.db.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class DaoUtils {

  public static void bindParams(PreparedStatement stmt, List<?> params) throws SQLException {
    for (int i = 0; i < params.size(); i++) {
      Object param = params.get(i);

      if (param instanceof List<?> list) {
        if (list.isEmpty()) {
          stmt.setArray(i + 1, stmt.getConnection().createArrayOf("text", new String[0]));
        } else if (list.getFirst() instanceof String) {
          Object[] array = list.toArray(new Object[0]);
          stmt.setArray(i + 1, stmt.getConnection().createArrayOf("text", array));
        } else {
          // default, we can add integer, uuid, etc as needed
          String[] array = list.stream().map(Object::toString).toArray(String[]::new);
          stmt.setArray(i + 1, stmt.getConnection().createArrayOf("varchar", array));
        }
      } else if (param == null) {
        stmt.setNull(i + 1, java.sql.Types.NULL);
      } else {
        stmt.setObject(i + 1, param);
      }
    }
  }

  public static String placeholders(int count) {
    return String.join(",", Collections.nCopies(count, "?"));
  }
}
