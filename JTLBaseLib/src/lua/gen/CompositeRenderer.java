package lua.gen;

import java.util.Collection;
import com.google.common.collect.ImmutableList;

public final class CompositeRenderer implements Renderer {
  private final ImmutableList<Renderer> renderers;

  public CompositeRenderer(Collection<? extends Renderer> renderers) {
    this.renderers = ImmutableList.copyOf(renderers);
  }
  
  public CompositeRenderer(Renderer... renderers) {
    this(ImmutableList.copyOf(renderers));
  }

  @Override
  public void render(StringBuilder sb, RendererContext context) {
    for (Renderer renderer : renderers) {
      renderer.render(sb, context);
    }
  }
}