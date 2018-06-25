package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.utils;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;

import java.util.*;


/**
 * Calcul les composantes connexes d'un graph non orienté
 * @author bcostes
 * 
 */
public class ConnectedComponents<E, V> {

  private UndirectedSparseMultigraph<E, V> g;

  public ConnectedComponents(UndirectedSparseMultigraph<E, V> g) {
    super();
    this.g = g;
  }

  public List<UndirectedSparseMultigraph<E, V>> buildConnectedComponents() {
    List<UndirectedSparseMultigraph<E, V>> connectedComponents = new ArrayList<>();

    Map<Integer, Set<E>> verticesColors = new HashMap<>();
    Set<E> processed = new HashSet<>();

    E random = g.getVertices().iterator().next();
    int color = 0;
    while (random != null) {
      if (!processed.contains(random)) {
        color++;
        colorEdges(random, color, verticesColors, processed);
      }
      List<E> untagged = new ArrayList<>(g.getVertices());
      untagged.removeAll(processed);
      if (!untagged.isEmpty()) {
        random = untagged.iterator().next();
      } else {
        random = null;
      }
    }

    // on récupère les arcs
    for (Integer i : verticesColors.keySet()) {
      Set<E> vertices = verticesColors.get(i);
      Set<V> edges = new HashSet<>();
      for (E vertex : vertices) {
        for (V e : g.getIncidentEdges(vertex)) {
          if (!edges.contains(e)) {
            edges.add(e);
          }
        }
      }
      UndirectedSparseMultigraph<E, V> connectedComponent = new UndirectedSparseMultigraph<>();
      if(edges.isEmpty()){
        for(E v: vertices){
          connectedComponent.addVertex(v);
        }
      }
      else{
        for (V edge : edges) {
          connectedComponent.addEdge(edge, g.getEndpoints(edge));
        }
      }
      connectedComponents.add(connectedComponent);
    }

    connectedComponents.sort((o1, o2) -> {
      if (o1.getVertexCount() > o2.getVertexCount()) {
        return -1;
      } else if (o1.getVertexCount() < o2.getVertexCount()) {
        return 1;
      } else {
        return Integer.compare(o2.getEdgeCount(), o1.getEdgeCount());
      }
    });
    return connectedComponents;
  }

  /**
   * Colorie le sommet vertex et appel récursif sur les sommets voisins
   * @param vertex vertex
   * @param color color
   * @param verticesColors map of colored vertices
   * @param processed set of processed vertices
   */
  private void colorEdges(E vertex, int color,
      Map<Integer, Set<E>> verticesColors, Set<E> processed) {
    if (verticesColors.containsKey(color)) {
      verticesColors.get(color).add(vertex);
    } else {
      Set<E> set = new HashSet<>();
      set.add(vertex);
      verticesColors.put(color, set);
    }
    processed.add(vertex);
    for (E neighbor : g.getNeighbors(vertex)) {
      if (!processed.contains(neighbor)) {
        colorEdges(neighbor, color, verticesColors, processed);
      }
    }
  }

}

