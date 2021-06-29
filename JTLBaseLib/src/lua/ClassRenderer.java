package lua;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.base.Verify;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import lua.gen.ClassSpec;
import lua.gen.ClassSpecLoader;
import lua.gen.DefaultRendererContext;
import lua.gen.MethodReference;
import lua.gen.MethodSpec;
import lua.gen.RendererContext;

public final class ClassRenderer {
  private final ClassSpecLoader loader;
  
  public ClassRenderer() {
    this.loader = new ClassSpecLoader();
  }
  
  public String renderAllFunctionsToLua(Class<?> clazz) {
    // Load the MethodSpec graph.
    ImmutableGraph<MethodReference> methodReferenceGraph = loadMethodRefGraph(clazz);
    ImmutableList<ImmutableSet<MethodReference>> topology = getTopology(methodReferenceGraph);

    StringBuilder sb = new StringBuilder();
    
    // Render the functions.
    RendererContext context = new DefaultRendererContext(loader);
    for (ImmutableSet<MethodReference> layer : topology) {
      for (MethodReference ref : layer) {
        MethodSpec spec = loader.getMethodSpec(ref);
        spec.functionRenderer().render(sb, context);
      }
    }
    
    return sb.toString();
  }
  
  private ImmutableGraph<MethodReference> loadMethodRefGraph(Class<?> clazz) {
    Deque<MethodReference> queue = new ArrayDeque<>();

    ClassSpec classSpec = loader.getClassSpec(clazz);
    for (MethodSpec methodSpec : classSpec.methodSpecs()) {
      queue.add(MethodReference.create(clazz.getCanonicalName(), methodSpec.methodSpec()));
    }
    
    Set<MethodReference> visited = new HashSet<>();
    ImmutableGraph.Builder<MethodReference> builder = GraphBuilder.directed().immutable();
    while (!queue.isEmpty()) {
      MethodReference methodRef = queue.removeFirst();
      if (!visited.add(methodRef)) {
        continue;
      }
      builder.addNode(methodRef);

      MethodSpec methodSpec = loader.getMethodSpec(methodRef);
      for (MethodReference ref : methodSpec.methodRefs()) {
        if (!ref.equals(methodRef)) {
          queue.addLast(ref);
          builder.putEdge(methodRef, ref);
        }
      }
    }
    
    return builder.build();
  }
  
  private static <T extends Comparable<T>> ImmutableList<ImmutableSet<T>> getTopology(Graph<T> graph) {
    Map<T, ImmutableSet<T>> cycles = new HashMap<>();
    Graph<T> transitive = Graphs.transitiveClosure(graph);
    
    // Identify cycles.
    for (T node : transitive.nodes()) {
      if (cycles.containsKey(node)) {
        continue;
      }

      List<T> nodes = new ArrayList<>();
      nodes.add(node);
      for (T neighbor : transitive.successors(node)) {
        if (neighbor != node && transitive.hasEdgeConnecting(neighbor, node)) {
          nodes.add(neighbor);
        }
      }
      ImmutableSet<T> cycle = nodes.stream().sorted().collect(ImmutableSet.toImmutableSet());

      for (T cycleNode : cycle) {
        cycles.put(cycleNode, cycle);
      }
    }
    
    // Compute the cycle graph, which must be a DAG.
    MutableGraph<ImmutableSet<T>> cycleGraph = GraphBuilder.directed().allowsSelfLoops(false).build();
    for (ImmutableSet<T> cycle : cycles.values()) {
      cycleGraph.addNode(cycle);
      for (T node : cycle) {
        for (T neighbor : graph.successors(node)) {
          if (!cycle.contains(neighbor)) {
            cycleGraph.putEdge(cycle, cycles.get(neighbor));
          }
        }
      }
    }
    Verify.verify(!Graphs.hasCycle(cycleGraph));
    
    Multiset<ImmutableSet<T>> outEdges = HashMultiset.create(cycleGraph.nodes().size());
    List<ImmutableSet<T>> bases = new ArrayList<>();
    for (ImmutableSet<T> cycle : cycleGraph.nodes()) {
      int outDegree = cycleGraph.outDegree(cycle);
      if (outDegree > 0) {
        outEdges.add(cycle, cycleGraph.outDegree(cycle));
      } else {
        bases.add(cycle);
      }
    }

    ImmutableList.Builder<ImmutableSet<T>> builder = ImmutableList.builder();
    Comparator<ImmutableSet<T>> setComparator = Comparator.comparing(s -> s.asList().get(0));
    while (!bases.isEmpty()) {
      Collections.sort(bases, setComparator);
      List<ImmutableSet<T>> newBases = new ArrayList<>();
      for (ImmutableSet<T> cycle : bases) {
        builder.add(cycle.stream().sorted().collect(ImmutableSet.toImmutableSet()));
        for (ImmutableSet<T> in : cycleGraph.predecessors(cycle)) {
          if (outEdges.remove(in, 1) == 1) {
            newBases.add(in);
          }
        }
      }
      bases = newBases;
    }
    Verify.verify(outEdges.isEmpty());
    
    return builder.build();
  }
}