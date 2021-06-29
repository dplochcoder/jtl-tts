package lua.util.collections;

import lua.NativeClass;
import lua.TableEntryIterable;
import lua.TableKeyIterable;
import lua.Wrapper;
import lua.util.Table;

@NativeClass
public final class Multiset<K> extends Wrapper<Table<K, Integer>> implements ReadableMultiset<K> {
  public Multiset() {
    super(Table.create());
  }

  @Override
  public int count(K key) {
    return wrapped().get(key, 0);
  }

  public void add(K key, int count) {
    set(key, count(key) + count);
  }

  public void add(K key) {
    add(key, 1);
  }

  @Override
  public TableKeyIterable<K> keys() {
    return wrapped().keys();
  }

  @Override
  public TableEntryIterable<K, Integer> counts() {
    return wrapped().entries();
  }

  private void set(K key, int count) {
    if (count == 0) {
      wrapped().remove(key);
    } else {
      wrapped().set(key, count);
    }
  }
}
