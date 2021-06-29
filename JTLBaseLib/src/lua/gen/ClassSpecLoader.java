package lua.gen;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import com.google.common.io.BaseEncoding;

public final class ClassSpecLoader {

  private final Map<String, ClassSpec> classSpecs;

  public ClassSpecLoader() {
    classSpecs = new HashMap<>();
  }

  public ClassSpec getClassSpec(String canonicalClassName) {
    ClassSpec spec = classSpecs.get(canonicalClassName);
    if (spec != null) {
      return spec;
    }
    
    try {
      spec = loadClassSpec(canonicalClassName);
    } catch (Exception e) {
      spec = ClassSpec.empty();
    }
    
    classSpecs.put(canonicalClassName, spec);
    return spec;
  }
  
  public ClassSpec getClassSpec(Class<?> clazz) {
    return getClassSpec(clazz.getCanonicalName());
  }
  
  public MethodSpec getMethodSpec(MethodReference ref) {
    return getClassSpec(ref.canonicalClassName()).getMethodSpec(ref.methodSpec());
  }

  private static ClassSpec loadClassSpec(String canonicalClassName) throws Exception {
    String classSpecName = canonicalClassName + "_ClassSpec";
    Class<?> clazz = Class.forName(classSpecName);
    Object spec = clazz.getDeclaredField("SPEC").get(null);
    byte[] serialSpec = BaseEncoding.base64().decode((String) spec);

    try (ByteArrayInputStream bais = new ByteArrayInputStream(serialSpec);
        ObjectInputStream ois = new ObjectInputStream(bais)) {
      return (ClassSpec) ois.readObject();
    }
  }
}
