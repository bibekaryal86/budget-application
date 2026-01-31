package budget.application.cache;

import java.util.List;
import java.util.UUID;

public interface InMemoryCache<T> {
  List<T> get();

  List<T> get(List<UUID> ids);

  void put(List<T> items);

  void put(T item);

  void clear();

  void clear(List<UUID> ids);
}
