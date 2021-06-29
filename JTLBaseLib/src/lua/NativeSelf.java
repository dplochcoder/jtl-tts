package lua;

/**
 * Annotates member methods that simply refer to the unaltered Lua representation of the object.
 * 
 * <p>Equivalent to `@Native(repr = "$0)`.
 */
public @interface NativeSelf {
}