package lua.gen;

import com.google.common.collect.ImmutableList;

public interface MethodCallRendererContext extends RendererContext {
  ImmutableList<Renderer> getArguments();
  
  default Renderer getArgument(int index) {
    return getArguments().get(index);
  }
}