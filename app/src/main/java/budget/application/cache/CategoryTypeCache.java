package budget.application.cache;

import budget.application.model.entity.CategoryType;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CategoryTypeCache implements InMemoryCache<CategoryType> {

  private final ConcurrentHashMap<UUID, CategoryType> store = new ConcurrentHashMap<>();

  @Override
  public List<CategoryType> get() {
    return new ArrayList<>(store.values());
  }

  public List<CategoryType> get(List<UUID> ids) {
    if (CommonUtilities.isEmpty(ids)) {
      return new ArrayList<>(store.values());
    }
    return ids.stream().map(store::get).toList();
  }

  @Override
  public void put(List<CategoryType> items) {
    items.forEach(item -> store.put(item.id(), item));
  }

  @Override
  public void put(CategoryType item) {
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
