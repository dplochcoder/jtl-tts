package lua.util.collections;

import lua.NativeImpl;
import lua.TableKeyIterable;

public interface ReadableSet<T> {
  @NativeImpl(Set.class)
  boolean contains(T value);
  
  @NativeImpl(Set.class)
  TableKeyIterable<T> keys();
}