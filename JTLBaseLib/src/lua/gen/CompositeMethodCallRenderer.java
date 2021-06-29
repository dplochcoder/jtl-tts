package lua.gen;

import java.util.Collection;
import com.google.common.collect.ImmutableList;

public final class CompositeMethodCallRenderer implements MethodCallRenderer {
  private final ImmutableList<MethodCallRenderer> renderers;

  public CompositeMethodCallRenderer(Collection<? extends MethodCallRenderer> renderers) {
    this.renderers = ImmutableList.copyOf(renderers);
  }
  
  public CompositeMethodCallRenderer(MethodCallRenderer... renderers) {
    this(ImmutableList.copyOf(renderers));
  }
  
  @Override
  public void render(StringBuilder sb, MethodCallRendererContext context) throws UnsupportedLuaMethodException {
    for (MethodCallRenderer renderer : renderers) {
      renderer.render(sb, context);
    }
  }
}