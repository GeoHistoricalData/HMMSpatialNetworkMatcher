package fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.local;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.Edge;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.GeometricalGraph;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.Node;
import fr.ign.cogit.morphogenesis.exploring_tool.utils.LocalMorphologicalndicatorList;
import fr.ign.cogit.voronoi.SurfaceVoronoi;

public class BetweennessCentralityVisitor extends LocalMorphologicalIndicator {

  public BetweennessCentralityVisitor() {
    super();
  }

  /**
   * Closeness centrality for geometrical graph
   */
  public void calculate(GeometricalGraph g) {

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

    for (Edge ee : g.getEdges()) {
      ee.setLength(Math.random() * 1000);
    }

    BetweennessCentrality<Node, Edge> betweennessCentrality = new BetweennessCentrality<Node, Edge>(
        g, new Transformer<Edge, Double>() {
          public Double transform(Edge o) {
            return o.getLength();
          }
        });
    Map<Node, Double> values = new HashMap<Node, Double>();
    int n = g.getVertexCount();
    for (Node node : g.getVertices()) {
      values.put(node, betweennessCentrality.getVertexScore(node)
          / ((n - 1.) * (n - 2.)));
    }
    // values = (new Statistics<Node>()).centrerRéduire(values);
    g.updateCentralityValues(LocalMorphologicalndicatorList.BETWEENNESS, values);
    values = null;
    betweennessCentrality = null;
    System.gc();
  }

}
