package lua.util.collections;

import lua.NativeImpl;

public interface ReadableNestedTable<R, C, V> {
  @NativeImpl(NestedTable.class)
  V get(R row, C col);
  
  @NativeImpl(NestedTable.class)
  V get(R row, C col, V defaultValue);
  
  @NativeImpl(NestedTable.class)
  boolean isEmpty();
}