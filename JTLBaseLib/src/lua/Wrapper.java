package lua;

/**
 * Classes which wrap a single value and have no other members.
 * 
 * <p>By implementing this interface, the generated Lua will use the wrapped type as the representation for this one,
 * increasing efficiency by avoiding table lookups.
 */
public abstract class Wrapper<T> {
  protected Wrapper(T wrapped) {}
  protected T wrapped() { return null; }
}