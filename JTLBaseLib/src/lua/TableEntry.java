package lua;

public interface TableEntry<K, V> {
  K getKey();
  V getValue();
}