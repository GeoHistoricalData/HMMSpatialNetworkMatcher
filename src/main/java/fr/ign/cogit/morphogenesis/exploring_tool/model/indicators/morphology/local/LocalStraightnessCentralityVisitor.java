package fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.Edge;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.GeometricalGraph;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.Node;
import fr.ign.cogit.morphogenesis.exploring_tool.utils.LocalMorphologicalndicatorList;

public class LocalStraightnessCentralityVisitor extends
    LocalMorphologicalIndicator {

  private static final int depth = 30;

  public LocalStraightnessCentralityVisitor() {
    super();
  }

  /**
   * Closeness centrality for geometrical graph
   */
  public void calculate(GeometricalGraph g) {
    Map<Node, Double> values = new HashMap<Node, Double>();

    int cpt = 0;
    for (Node node : g.getVertices()) {

      UndirectedSparseMultigraph<Node, Edge> gK = g.getKNeighborhood(node,
          depth);

      DijkstraShortestPath<Node, Edge> sp = new DijkstraShortestPath<Node, Edge>(
          gK, new Transformer<Edge, Double>() {
            public Double transform(Edge o) {
              return o.getLength();
            }
          });

      cpt++;
      // récupération des kneighbor
      List<Node> kneighbor = new ArrayList<Node>();
      kneighbor.addAll(gK.getVertices());
      kneighbor.remove(node);
      Map<Node, Number> m = sp.getDistanceMap(node);
      double centrality = 0;
      for (Node otherNode : g.getVertices()) {
        if (node.equals(otherNode)) {
          continue;
        }
        if (!kneighbor.contains(otherNode)) {
          continue;
        }
        double dist = m.get(otherNode).doubleValue();
        centrality += Math.sqrt((node.getX() - otherNode.getX())
            * (node.getX() - otherNode.getX())
            + (node.getY() - otherNode.getY())
            * (node.getY() - otherNode.getY()))
            / dist;
      }
      if (centrality != 0) {
        centrality = centrality / ((double) kneighbor.size() - 1);
      }
      values.put(node, centrality);
      sp.reset();
    }
    g.updateCentralityValues(LocalMorphologicalndicatorList.STRAIGHTNESSLOCAL,
        values);
    values = null;
    System.gc();
  }

}
