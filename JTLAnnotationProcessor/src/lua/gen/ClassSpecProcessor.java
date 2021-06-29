package lua.gen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import com.google.auto.service.AutoService;
import com.google.common.base.Verify;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.common.io.BaseEncoding;
import com.sun.source.util.Trees;
import lua.Native;
import lua.NativeImpl;
import lua.NativeMember;
import lua.NativeGlobal;
import lua.NativePrototype;
import lua.NativeSelf;

@AutoService(Processor.class)
@SupportedAnnotationTypes("lua.NativeClass")
final class ClassSpecProcessor extends AbstractProcessor {

  private Trees trees;
  private CanonicalTypes canonicalTypes;

  @Override
  public void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    trees = Trees.instance(processingEnv);
    canonicalTypes = new CanonicalTypes(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
  }

  @Override
  public boolean process(Set<? extends TypeElement> types, RoundEnvironment env) {
    for (TypeElement type : types) {
      if (type.getKind() != ElementKind.CLASS && type.getKind() != ElementKind.INTERFACE) {
        throw abortWithError("@NativeClass only supports classes and interfaces", type);
      }

      handleType(type);
    }

    return true;
  }

  private void handleType(TypeElement type) {
    String className = type.getSimpleName().toString();
    if (className.isEmpty()) {
      throw abortWithError("@NativeClass does not support anonymous classes", type);
    }

    Map<String, MethodSpec> methodSpecs = new HashMap<>();
    for (Element e : type.getEnclosedElements()) {
      switch (e.getKind()) {
        case CONSTRUCTOR:
        case METHOD:
          Optional<MethodSpec> methodSpec = createMethodSpec(type, (ExecutableElement) e);
          if (methodSpec.isPresent()) {
            String specString = methodSpec.get().methodSpec();
            if (methodSpecs.containsKey(specString)) {
              throw abortWithError(
                  "Multiple methods with the same name and number of arguments are not supported",
                  e);
            }

            methodSpecs.put(specString, methodSpec.get());
          }
          break;
        default:
          break;
      }

      // FIXME: Constants
      // FIXME: Superclass and Interfaces
    }

    String genClassName = className + "_ClassSpec";
    ClassSpec classSpec = new ClassSpec(methodSpecs);
    try {
      JavaFileObject jfo = processingEnv.getFiler().createSourceFile(genClassName, type);
      try (Writer w = jfo.openWriter()) {
        String packageName =
            processingEnv.getElementUtils().getPackageOf(type).getQualifiedName().toString();
        String classSpecStr = serializeToString(classSpec);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("package %s;\n\n", packageName));
        sb.append(String.format("public final class %s {\n", genClassName));
        sb.append(String.format("  public static final String SPEC = \"%s\";\n", classSpecStr));
        sb.append("}\n");

        w.write(sb.toString());
      }
    } catch (IOException ex) {
      throw abortWithError("IOException: " + ex, type);
    }
  }

  private static String serializeToString(Serializable o) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(o);
      oos.flush();
      baos.flush();

      return BaseEncoding.base64().encode(baos.toByteArray());
    }
  }

  private static final ImmutableList<Class<? extends Annotation>> ANNOTATION_CLASSES =
      ImmutableList.of(Native.class, NativeImpl.class, NativeGlobal.class, NativeMember.class,
          NativePrototype.class, NativeSelf.class);

  private String getMethodSpecString(ExecutableElement method) {
    return method.getSimpleName().toString() + "_" + method.getParameters().size();
  }

  private Optional<MethodSpec> createMethodSpec(TypeElement type, ExecutableElement method) {
    ClassToInstanceMap<Annotation> annotations = MutableClassToInstanceMap.create();
    for (Class<? extends Annotation> clazz : ANNOTATION_CLASSES) {
      putAnnotation(clazz, method, annotations);
    }
    if (annotations.size() > 1) {
      throw abortWithError("Conflicting @Native annotations on method", method);
    }

    int numParams = method.getParameters().size();
    boolean isStatic = method.getModifiers().contains(Modifier.STATIC);
    String methodSpecString = getMethodSpecString(method);
    String fullMethodName = type.getQualifiedName().toString().replace('.', '_') + "_" + methodSpecString; 
    
    // FIXME: Constructors
    boolean isConstructor = method.getKind() == ElementKind.CONSTRUCTOR;

    if (annotations.isEmpty()) {
      BlockRenderer blockRenderer = new BlockRenderer(trees.getPath(type).getCompilationUnit(), canonicalTypes, processingEnv.getTypeUtils(), trees, processingEnv.getMessager());
      blockRenderer.acceptBlock(trees.getTree(method).getBody());

      List<String> parameters = new ArrayList<>();
      if (!isStatic) {
        parameters.add("self");
      }
      for (VariableElement param : method.getParameters()) {
        parameters.add(param.getSimpleName().toString());
      }
      
      List<Renderer> renderers = new ArrayList<>();
      String header = String.format("function %s(%s)\n", fullMethodName, parameters.stream().collect(Collectors.joining(", ")));
      renderers.add(new LiteralRenderer(header));
      renderers.addAll(blockRenderer.renderers());
      renderers.add(new LiteralRenderer("end"));
      
      return Optional.of(MethodSpec.create(
          methodSpecString,
          new DirectMethodCallRenderer(fullMethodName, parameters.size()),
          new CompositeRenderer(renderers),
          blockRenderer.methodReferences()));
    }
    
    if (annotations.containsKey(NativeImpl.class)) {
      // FIXME: NativeImpl hooks
      return Optional.empty();
    }

    Native aNative = annotations.getInstance(Native.class);
    if (aNative != null) {
      String template = aNative.value();
      if (template.isEmpty()) {
        // FIXME: Custom natives
        return Optional.empty();
      }

      MethodCallRenderer callRenderer = NativeTemplateParser.parseNativeTemplate(template);
      Renderer functionRenderer = Renderer.EMPTY;
      if (!aNative.functionDef().isEmpty()) {
        functionRenderer = new LiteralRenderer(aNative.functionDef() + "\n\n");
      }
      return Optional.of(MethodSpec.create(methodSpecString, callRenderer, functionRenderer, ImmutableSet.of()));
    }
    
    if (annotations.containsKey(NativeSelf.class)) {
      if (numParams > 0 || isStatic) {
        throw abortWithError("@NativeSelf can only be used on 0-arg member methods", method);
      }

      return Optional.of(MethodSpec.createNative(methodSpecString, new SingleArgumentRenderer(0)));
    }
    
    if (annotations.containsKey(NativeMember.class)) {
      if (isStatic) {
        throw abortWithError("@NativeMember cannot be applied to static methods", method);
      }
    } else if (annotations.containsKey(NativePrototype.class)) {
      if (isStatic) {
        throw abortWithError("@NativePrototype cannot be applied to static methods", method);
      }
    } else {
      Verify.verify(annotations.containsKey(NativeGlobal.class));
    }

    MethodCallRenderer callRenderer;
    if (isStatic) {
      callRenderer = new CompositeMethodCallRenderer(
          new LiteralRenderer(method.getSimpleName().toString() + "("),
          new MultiArgumentRenderer(0, numParams),
          new LiteralRenderer(")"));
    } else {
      String operator = annotations.containsKey(NativePrototype.class) ? ":" : ".";
      callRenderer = new CompositeMethodCallRenderer(
          new SingleArgumentRenderer(0),
          new LiteralRenderer(operator + method.getSimpleName().toString() + "("),
          new MultiArgumentRenderer(1, numParams),
          new LiteralRenderer(")"));
    }
    return Optional.of(MethodSpec.createNative(methodSpecString, callRenderer));
  }

  private static <T extends Annotation> void putAnnotation(Class<T> clazz, ExecutableElement method,
      ClassToInstanceMap<? super T> map) {
    T annotation = method.getAnnotation(clazz);
    if (annotation != null) {
      map.putInstance(clazz, annotation);
    }
  }

  private AbortProcessingException abortWithError(String msg, Element e) {
    processingEnv.getMessager().printMessage(Kind.ERROR, msg, e);
    return new AbortProcessingException();
  }
}
