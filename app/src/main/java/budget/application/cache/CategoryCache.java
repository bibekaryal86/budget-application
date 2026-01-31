package budget.application.cache;

import budget.application.model.entity.Category;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CategoryCache implements InMemoryCache<Category> {

  private final ConcurrentHashMap<UUID, Category> store = new ConcurrentHashMap<>();

  @Override
  public List<Category> get() {
    return new ArrayList<>(store.values());
  }

  @Override
  public List<Category> get(List<UUID> ids) {
    if (CommonUtilities.isEmpty(ids)) {
      return new ArrayList<>(store.values());
    }
    return ids.stream().map(store::get).filter(Objects::nonNull).toList();
  }

  @Override
  public void put(List<Category> items) {
    items.forEach(item -> store.put(item.id(), item));
  }

  @Override
  public void put(Category item) {
    store.put(item.id(), item);
  }

  @Override
  public void clear() {
    store.clear();
  }

  @Override
  public void clear(List<UUID> ids) {
    ids.forEach(store::remove);
  }
}
