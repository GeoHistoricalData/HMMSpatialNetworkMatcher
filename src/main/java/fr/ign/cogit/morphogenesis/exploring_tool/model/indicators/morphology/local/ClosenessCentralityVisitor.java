package fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.local;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.Edge;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.GeometricalGraph;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.Node;
import fr.ign.cogit.morphogenesis.exploring_tool.utils.LocalMorphologicalndicatorList;
import fr.ign.cogit.voronoi.SurfaceVoronoi;

/**
 * Closeness centrality
 * @author bcostes
 * 
 */
public class ClosenessCentralityVisitor extends LocalMorphologicalIndicator {

  public ClosenessCentralityVisitor() {
    super();
  }

  /**
   * Closeness centrality for geometrical graph
   */
  public void calculate(GeometricalGraph g) {
    Map<Node, Double> values = new HashMap<Node, Double>();
    int n = g.getVertexCount();

    // pondération voronoi

    // cration de l'enveloppe convexe
    IGeometry env = g.getPop().getGeomAggregate().convexHull();
    Map<Node, Double> nodeVoronoiSurface = new HashMap<Node, Double>();
    for (Node nn : g.getVertices()) {
      nodeVoronoiSurface.put(nn, 0.);
    }
    SurfaceVoronoi.surfaceVoronoi(env, nodeVoronoiSurface);

    final Map<Edge, Double> edgeVoronoiSurface = new HashMap<Edge, Double>();
    for (Edge ee : g.getEdges()) {
      edgeVoronoiSurface.put(ee, 0.);
    }
    SurfaceVoronoi.surfaceVoronoi(edgeVoronoiSurface, nodeVoronoiSurface);

    /*
     * for (Edge ee : g.getEdges()) { ee.setLength(edgeVoronoiSurface.get(ee));
     * }
     */

    for (Edge ee : g.getEdges()) {
      ee.setLength(1);
    }

    DijkstraShortestPath<Node, Edge> sp = new DijkstraShortestPath<Node, Edge>(
        g, new Transformer<Edge, Double>() {
          public Double transform(Edge o) {
            return o.getLength();
          }
        });

    for (Node node : g.getVertices()) {
      double sum = 0;
      Map<Node, Number> mapSp = sp.getDistanceMap(node);
      for (Node node2 : mapSp.keySet()) {
        sum += mapSp.get(node2).doubleValue();
      }
      sp.reset(node);
      sum = ((double) (n - 1)) / sum;
      values.put(node, sum);
    }
    sp.reset();
    sp = null;
    // values = (new Statistics<Node>()).centrerRéduire(values);
    g.updateCentralityValues(LocalMorphologicalndicatorList.CLOSENESS, values);
    values = null;
    System.gc();
  }
}
