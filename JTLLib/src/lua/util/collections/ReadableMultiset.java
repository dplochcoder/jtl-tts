package lua.util.collections;

import lua.NativeImpl;
import lua.TableEntryIterable;
import lua.TableKeyIterable;

public interface ReadableMultiset<K> {
  @NativeImpl(Multiset.class)
  int count(K key);
  
  @NativeImpl(Multiset.class)
  TableKeyIterable<K> keys();
  
  @NativeImpl(Multiset.class)
  TableEntryIterable<K, Integer> counts();
}