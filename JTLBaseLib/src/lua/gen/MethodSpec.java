package lua.gen;

import java.io.Serializable;
import java.util.Collection;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

@AutoValue
public abstract class MethodSpec implements Serializable {
  public abstract String methodSpec();
  public abstract MethodCallRenderer callRenderer();
  public abstract Renderer functionRenderer();
  public abstract ImmutableSet<MethodReference> methodRefs();
  
  public static MethodSpec createNative(String methodSpec, MethodCallRenderer callRenderer) {
    return new AutoValue_MethodSpec(methodSpec, callRenderer, Renderer.EMPTY, ImmutableSet.of());
  }
  
  public static MethodSpec create(String methodSpec, MethodCallRenderer callRenderer, Renderer functionRenderer, Collection<MethodReference> methodRefs) {
    return new AutoValue_MethodSpec(methodSpec, callRenderer, functionRenderer, ImmutableSet.copyOf(methodRefs));
  }
}