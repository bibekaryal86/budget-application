package budget.application.cache;

public class CacheContext {

  private final CategoryTypeCache categoryTypeCache;
  private final CategoryCache categoryCache;

  public CacheContext() {
    this.categoryTypeCache = new CategoryTypeCache();
    this.categoryCache = new CategoryCache();
  }

  public CategoryTypeCache categoryType() {
    return categoryTypeCache;
  }

  public CategoryCache category() {
    return categoryCache;
  }
}
