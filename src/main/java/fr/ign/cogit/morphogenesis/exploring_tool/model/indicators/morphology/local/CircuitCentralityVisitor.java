package fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.local;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.Edge;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.GeometricalGraph;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.Node;
import fr.ign.cogit.morphogenesis.exploring_tool.utils.LocalMorphologicalndicatorList;

public class CircuitCentralityVisitor extends LocalMorphologicalIndicator {
  public CircuitCentralityVisitor() {
    super();
  }

  /**
   * Closeness centrality for geometrical graph
   */
  public void calculate(GeometricalGraph g) {
    Map<Node, Double> values = new HashMap<Node, Double>();
    DijkstraShortestPath<Node, Edge> sp = new DijkstraShortestPath<Node, Edge>(
        g, new Transformer<Edge, Double>() {
          public Double transform(Edge o) {
            return o.getLength();
          }

        });
    for (Node node : g.getVertices()) {
      Map<Node, Number> m = sp.getDistanceMap(node);
      double centrality = 0;
      for (Node otherNode : g.getVertices()) {
        if (node.equals(otherNode)) {
          continue;
        }
        if (!m.containsKey(otherNode)) {
          continue;
        }

        centrality += Math.pow(
            Math.sqrt((node.getX() - otherNode.getX())
                * (node.getX() - otherNode.getX())
                + (node.getY() - otherNode.getY())
                * (node.getY() - otherNode.getY()))
                - Double.parseDouble(m.get(otherNode).toString()), 2);

      }
      sp.reset(node);
      centrality /= (g.getVertexCount() - 1);
      values.put(node, centrality);
    }
    sp.reset();
    g.updateCentralityValues(LocalMorphologicalndicatorList.CIRCUIT, values);
    values = null;
    System.gc();
  }
}
