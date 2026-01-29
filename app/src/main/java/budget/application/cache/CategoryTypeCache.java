package budget.application.cache;

import budget.application.model.dto.CategoryTypeResponse;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CategoryTypeCache implements InMemoryCache<CategoryTypeResponse.CategoryType> {

  private final ConcurrentHashMap<UUID, CategoryTypeResponse.CategoryType> store =
      new ConcurrentHashMap<>();

  @Override
  public List<CategoryTypeResponse.CategoryType> get() {
    return new ArrayList<>(store.values());
  }

  public List<CategoryTypeResponse.CategoryType> get(List<UUID> ids) {
    if (CommonUtilities.isEmpty(ids)) {
      return new ArrayList<>(store.values());
    }
    return ids.stream().map(store::get).toList();
  }

  @Override
  public void put(List<CategoryTypeResponse.CategoryType> items) {
    items.forEach(item -> store.put(item.id(), item));
  }

  @Override
  public void put(CategoryTypeResponse.CategoryType item) {
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
