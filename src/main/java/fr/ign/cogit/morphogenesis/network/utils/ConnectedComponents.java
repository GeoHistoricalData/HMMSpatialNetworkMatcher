package fr.ign.cogit.morphogenesis.network.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;

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
    List<UndirectedSparseMultigraph<E, V>> connectedComponents = new ArrayList<UndirectedSparseMultigraph<E, V>>();

    Map<Integer, Set<E>> verticesColors = new HashMap<Integer, Set<E>>();
    Set<E> processed = new HashSet<E>();

    E random = (E) g.getVertices().iterator().next();
    int color = 0;
    while (random != null) {
      if (!processed.contains(random)) {
        color++;
        colorEdges(random, color, verticesColors, processed);
      }
      List<E> untagged = new ArrayList<E>();
      untagged.addAll(g.getVertices());
      untagged.removeAll(processed);
      if (!untagged.isEmpty()) {
        random = (E) untagged.iterator().next();
      } else {
        random = null;
      }
    }

    // on récupère les arcs
    for (Integer i : verticesColors.keySet()) {
      Set<E> vertices = verticesColors.get(i);
      Set<V> edges = new HashSet<V>();
      for (E vertex : vertices) {
        for (V e : g.getIncidentEdges(vertex)) {
          if (!edges.contains(e)) {
            edges.add(e);
          }
        }
      }
      UndirectedSparseMultigraph<E, V> connectedComponent = new UndirectedSparseMultigraph<E, V>();
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

    Collections.sort(connectedComponents,
        new Comparator<UndirectedSparseMultigraph<E, V>>() {
      public int compare(UndirectedSparseMultigraph<E, V> o1,
          UndirectedSparseMultigraph<E, V> o2) {
        if (o1.getVertexCount() > o2.getVertexCount()) {
          return -1;
        } else if (o1.getVertexCount() < o2.getVertexCount()) {
          return 1;
        } else {
          if (o1.getEdgeCount() > o2.getEdgeCount()) {
            return -1;
          } else if (o1.getEdgeCount() < o2.getEdgeCount()) {
            return 1;
          } else {
            return 0;
          }
        }
      }
    });
    return connectedComponents;
  }

  /**
   * Colorie le sommet vertex et appel récursif sur les sommets voisins
   * @param g
   * @param vertex
   * @param i
   * @param edgesColors
   */
  private void colorEdges(E vertex, int color,
      Map<Integer, Set<E>> verticesColors, Set<E> processed) {
    if (verticesColors.containsKey(color)) {
      verticesColors.get(color).add(vertex);
    } else {
      Set<E> set = new HashSet<E>();
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
