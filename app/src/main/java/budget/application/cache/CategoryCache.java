package budget.application.cache;

import budget.application.model.dto.CategoryResponse;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CategoryCache implements InMemoryCache<CategoryResponse.Category> {

  private final ConcurrentHashMap<UUID, CategoryResponse.Category> store =
      new ConcurrentHashMap<>();

  @Override
  public List<CategoryResponse.Category> get() {
    return new ArrayList<>(store.values());
  }

  @Override
  public List<CategoryResponse.Category> get(List<UUID> ids) {
    if (CommonUtilities.isEmpty(ids)) {
      return new ArrayList<>(store.values());
    }
    return ids.stream().map(store::get).toList();
  }

  @Override
  public void put(List<CategoryResponse.Category> items) {
    items.forEach(item -> store.put(item.id(), item));
  }

  @Override
  public void put(CategoryResponse.Category item) {
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
