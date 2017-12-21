package fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.global;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import fr.ign.cogit.morphogenesis.exploring_tool.model.api.Edge;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.GeometricalGraph;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.Node;
import fr.ign.cogit.morphogenesis.exploring_tool.utils.GlobalMorphologicalIndicatorList;

public class ClusteringCoefficient extends GlobalMorphologicalIndicator {

  /**
   * Global cLustering Coefficient
   */
  public void calculate(GeometricalGraph g) {
    double sum = 0;
    for (Node v : g.getVertices()) {
      if (g.getNeighborCount(v) <= 1) {
        continue;
      }
      // les voisins de v
      Collection<Node> neighbors = g.getNeighbors(v);
      Set<Edge> edges = new HashSet<Edge>();
      for (Node e : neighbors) {
        // on ajoute le nombre d'arc connecté à e
        edges.addAll(g.getIncidentEdges(e));
      }
      // on supprime les arcs connectés à v
      edges.removeAll(g.getIncidentEdges(v));
      double l = 0.;
      for (Edge e : edges) {
        if (neighbors.contains(g.getEndpoints(e).getFirst())
            && neighbors.contains(g.getEndpoints(e).getSecond())) {
          l++;
        }
      }

      double m = g.getNeighborCount(v);
      sum += (2. * l) / (m * (m - 1));
    }
    sum /= (double) g.getVertices().size();
    g.getGlobalMorphologicalIndicators().put(
        GlobalMorphologicalIndicatorList.CLUSTERING_COEFF, sum);
    System.gc();
  }

}
