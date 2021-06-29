package lua.util.collections;

import lua.NativeClass;
import lua.TableKeyIterable;
import lua.Wrapper;
import lua.util.Table;

@NativeClass
public final class Set<T> extends Wrapper<Table<T, Integer>> implements ReadableSet<T> {
  public Set() {
    super(Table.create());
  }
  
  @Override
  public boolean contains(T value) {
    return wrapped().contains(value);
  }
  
  public void add(T value) {
    wrapped().set(value, 1);
  }
  
  public void remove(T value) {
    wrapped().remove(value);
  }
  
  public void update(Set<T> other) {
    for (T t : other.keys()) {
      add(t);
    }
  }

  @Override
  public TableKeyIterable<T> keys() {
    return wrapped().keys();
  }
}