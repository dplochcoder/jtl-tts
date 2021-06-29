package lua.gen;

import java.io.Serializable;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ComparisonChain;

@AutoValue
public abstract class MethodReference implements Comparable<MethodReference>, Serializable {
  public abstract String canonicalClassName();
  public abstract String methodSpec();
  
  public final int compareTo(MethodReference that) {
    return ComparisonChain.start()
        .compare(this.canonicalClassName(), that.canonicalClassName())
        .compare(this.methodSpec(), that.methodSpec())
        .result();
  }
  
  public static MethodReference create(String canonicalClassName, String methodSpec) {
    return new AutoValue_MethodReference(canonicalClassName, methodSpec);
  }
}