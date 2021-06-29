package lua.gen;

import java.io.Serializable;
import java.util.Map;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;

public final class ClassSpec implements Serializable {
  private static final ClassSpec EMPTY = new ClassSpec(ImmutableMap.of());
  
  public static ClassSpec empty() { return EMPTY; }
  
  private final ImmutableMap<String, MethodSpec> methodSpecs;
  
  public ClassSpec(Map<String, MethodSpec> methodSpecs) {
    this.methodSpecs = ImmutableMap.copyOf(methodSpecs);
  }
  
  public MethodSpec getMethodSpec(String methodSpec) {
    return methodSpecs.get(methodSpec);
  }

  public ImmutableCollection<MethodSpec> methodSpecs() {
    return methodSpecs.values();
  }
}