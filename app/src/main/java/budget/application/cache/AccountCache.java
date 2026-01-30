package budget.application.cache;

import budget.application.model.entity.Account;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AccountCache implements InMemoryCache<Account> {

  private final ConcurrentHashMap<UUID, Account> store = new ConcurrentHashMap<>();

  @Override
  public List<Account> get() {
    return new ArrayList<>(store.values());
  }

  @Override
  public List<Account> get(List<UUID> ids) {
    if (CommonUtilities.isEmpty(ids)) {
      return new ArrayList<>(store.values());
    }
    return ids.stream().map(store::get).filter(Objects::nonNull).toList();
  }

  @Override
  public void put(List<Account> items) {
    items.forEach(item -> store.put(item.id(), item));
  }

  @Override
  public void put(Account item) {
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
