package lua.gen;

import java.util.HashMap;
import java.util.Map;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

final class CanonicalTypes {
  private final Elements elements;
  private final Types types;
  private final Map<Class<?>, TypeMirror> typeMirrors;
  
  CanonicalTypes(Elements elements, Types types) {
    this.elements = elements;
    this.types = types;
    this.typeMirrors = new HashMap<>();
  }
  
  TypeMirror get(Class<?> clazz) {
    return typeMirrors.computeIfAbsent(clazz, this::compute);
  }
  
  private TypeMirror compute(Class<?> clazz) {
    return types.erasure(elements.getTypeElement(clazz.getCanonicalName()).asType());
  }
}