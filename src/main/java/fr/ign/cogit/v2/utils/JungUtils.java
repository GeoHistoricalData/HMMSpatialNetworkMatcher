package fr.ign.cogit.v2.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;

public class JungUtils<V, E> {

  public JungUtils() {

  }

  /**
   * remplacement d'un arc par un autre. Les extrémités ne changent pas
   * @param graph
   * @param oldEdge
   * @param newEdge
   */
  public void replaceEdge(UndirectedSparseMultigraph<V, E> graph, E oldEdge,
      E newEdge) {
    if (!graph.getEdges().contains(oldEdge)) {
      return;
    }
    Pair<V> nodes = graph.getEndpoints(oldEdge);
    if(graph.containsEdge(newEdge)){
        graph.removeEdge(oldEdge);
        return;
    }
    graph.removeEdge(oldEdge);
    graph.addEdge(newEdge, nodes);
  }

  /**
   * remplacement d'un sommet par un autre
   * @param graph
   * @param oldNode
   * @param newNode
   */
  public void replaceNode(UndirectedSparseMultigraph<V, E> graph, V oldNode,
      V newNode) {
    if (!graph.getVertices().contains(oldNode)) {
      return;
    }
    // on va devoir supprimer oldNode;
    // récupération des arcs incidents
    List<E> incidents = new ArrayList<E>(graph.getIncidentEdges(oldNode));
    Map<E, Pair<V>> ends = new HashMap<E, Pair<V>>();
    for (E edge : incidents) {
      ends.put(edge, graph.getEndpoints(edge));
    }
    graph.removeVertex(oldNode);

    // insertion des arcs supprimés
    for (E edge : incidents) {
      if (ends.get(edge).getFirst().equals(oldNode)
          && ends.get(edge).getSecond().equals(oldNode)) {
        graph.addEdge(edge, newNode, newNode);
      } else if (ends.get(edge).getFirst().equals(oldNode)) {
        graph.addEdge(edge, newNode, ends.get(edge).getSecond());
      } else {
        graph.addEdge(edge, ends.get(edge).getFirst(), newNode);
      }
    }
  }
}
