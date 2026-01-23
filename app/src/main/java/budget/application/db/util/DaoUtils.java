package budget.application.db.util;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DaoUtils {

  public static void bindParams(PreparedStatement stmt, List<?> params) throws SQLException {
    for (int i = 0; i < params.size(); i++) {
      Object param = params.get(i);
      int idx = i + 1;

      switch (param) {
        case null -> {
          stmt.setNull(idx, Types.NULL);
          continue;
        }
        case Array array -> {
          stmt.setArray(idx, array);
          continue;
        }
        case List<?> list -> {
          if (list.isEmpty()) {
            stmt.setNull(idx, java.sql.Types.NULL);
            continue;
          }

          Object first = list.getFirst();

          if (first instanceof UUID) {
            UUID[] arr = list.toArray(UUID[]::new);
            stmt.setArray(idx, stmt.getConnection().createArrayOf("UUID", arr));
            continue;
          }

          if (first instanceof Integer) {
            Integer[] arr = list.toArray(Integer[]::new);
            stmt.setArray(idx, stmt.getConnection().createArrayOf("INTEGER", arr));
            continue;
          }

          if (first instanceof String) {
            String[] arr = list.toArray(String[]::new);
            stmt.setArray(idx, stmt.getConnection().createArrayOf("text", arr));
            continue;
          }

          String[] arr = list.stream().map(Object::toString).toArray(String[]::new);
          stmt.setArray(idx, stmt.getConnection().createArrayOf("text", arr));
          continue;
        }
        default -> {}
      }

      stmt.setObject(idx, param);
    }
  }

  public static String placeholders(int count) {
    return String.join(",", Collections.nCopies(count, "?"));
  }
}
