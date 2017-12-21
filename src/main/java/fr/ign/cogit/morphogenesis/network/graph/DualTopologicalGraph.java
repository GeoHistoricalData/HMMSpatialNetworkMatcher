package fr.ign.cogit.morphogenesis.network.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cern.colt.matrix.DoubleMatrix2D;
import edu.uci.ics.jung.algorithms.importance.RandomWalkBetweenness;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import fr.ign.cogit.graphs.utils.Statistics;
import fr.ign.cogit.morphogenesis.network.graph.io.CentralitiesDualDual;
import fr.ign.cogit.morphogenesis.network.graph.randomWalk.RandomWalk;

public class DualTopologicalGraph extends AbstractGraph<Node, String> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private Map<Edge, List<Node>> mappingStrokesNodes;
  Logger logger = Logger.getLogger(DualTopologicalGraph.class);

  public DualTopologicalGraph() {
    super();
    this.setId("Global Dual Topological Graph");
  }

  // ***************************************************************************
  // ************************** Méthodes d'export ******************************
  // ***************************************************************************

  public Map<Node, Double> getCentralityNodes(String centrality) {
    Map<Node, Double> values = new HashMap<Node, Double>();
    if (centrality.equals(CentralitiesDualDual.CLOSENESS)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de proximité en cours ...");
      }
      for (Node edge : this.getVertices()) {
        ClosenessCentrality<Node, String> c = this.getClosenessCentrality();
        values.put(edge, c.getVertexScore(edge));
      }
    } else if (centrality.equals(CentralitiesDualDual.BETWEENNESS)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités intermédiaires en cours ...");
      }
      BetweennessCentrality<Node, String> c = this.getBetweennessCentrality();
      int n = this.getVertexCount();
      for (Node edge : this.getVertices()) {
        values.put(edge, c.getVertexScore(edge) / ((n - 1.) * (n - 2.)));
      }

    } else if (centrality.equals(CentralitiesDualDual.CLUSTERING)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de clustering en cours ...");
      }
      values = this.getClusteringCentrality();
    } else if (centrality.equals(CentralitiesDualDual.TWOCLUSTERING)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de 2-clustering en cours ...");
      }
      values = this.getKClusteringCentrality(2);
    }

    else if (centrality.equals(CentralitiesDualDual.CLUSTERING3)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de 3-clustering en cours ...");
      }
      Map<Node, Double> map = this.getKClusteringCentrality(3);
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesDualDual.CLUSTERING5)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de 5-clustering en cours ...");
      }
      Map<Node, Double> map = this.getKClusteringCentrality(5);
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesDualDual.RANDOMBETWEENNESS)) {
      if (logger.isInfoEnabled()) {
        logger
            .info("Calcul des centralités intermédiaires random walk en cours ...");
      }
      RandomWalkBetweenness<Node, String> b = getRandomBetweennessCentrality();
      for (Node edge : this.getVertices()) {
        values.put(edge, b.getVertexRankScore(edge));
      }
    } else if (centrality.equals(CentralitiesDualDual.DEGREE)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de degré en cours ...");
      }
      DegreeScorer<Node> c = this.getDegreeCentrality();
      for (Node edge : this.getVertices()) {
        values.put(edge, (double) c.getVertexScore(edge));
      }
    } else if (centrality.equals(CentralitiesDualDual.CONTROL)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des mesures de contrôle en cours ...");
      }
      values = this.getControlMetric();
    } else if (centrality.equals(CentralitiesDualDual.MEANDISTANCE)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des éloignements moyens en cours ...");
      }
      values = this.getMeanDistance();
    } else if (centrality.equals(CentralitiesDualDual.PAGERANK)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des page rank en cours ...");
      }
      values = this.getPageRank(0.85);
    } else if (centrality.equals(CentralitiesDualDual.WPAGERANK)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des weighted page rank en cours ...");
      }
      values = this.getWeightedPageRank(0.85);
    } else if (centrality.equals(CentralitiesDualDual.RANDOMWALK)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des randomw walkers ...");
      }
      RandomWalk<Node, String> map = this.getRandomWalk(1000);
      for (Node edge : this.getVertices()) {
        values.put(edge, (double) map.getVertexScore(edge));
      }
    } else if (centrality.equals(CentralitiesDualDual.EIGENVECTOR)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités spectrales ...");
      }
      Map<Node, Double> e = this.getEigenVectorCentrality();
      for (Node edge : this.getVertices()) {
        values.put(edge, e.get(edge));
      }
    } else if (centrality.equals(CentralitiesDualDual.GLOBALINTEGRATION)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des intégration globales ...");
      }
      Map<Node, Double> e = this.getGlobalIntegration();
      for (Node edge : this.getVertices()) {
        values.put(edge, e.get(edge));
      }
    } else if (centrality.equals(CentralitiesDualDual.LOCALINTEGRATION_2)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des intégration locales (2) ...");
      }
      Map<Node, Double> e = this.getLocalIntegration(2);
      for (Node edge : this.getVertices()) {
        values.put(edge, e.get(edge));
      }
    } else if (centrality.equals(CentralitiesDualDual.LOCALINTEGRATION_3)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des intégration locales (3) ...");
      }
      Map<Node, Double> e = this.getLocalIntegration(3);
      for (Node edge : this.getVertices()) {
        values.put(edge, e.get(edge));
      }
    } else if (centrality.equals(CentralitiesDualDual.LOCALINTEGRATION_4)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des intégration locales (4) ...");
      }
      Map<Node, Double> e = this.getLocalIntegration(4);
      for (Node edge : this.getVertices()) {
        values.put(edge, e.get(edge));
      }
    } else if (centrality.equals(CentralitiesDualDual.LOCALINTEGRATION_5)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des intégration locales (5) ...");
      }
      Map<Node, Double> e = this.getLocalIntegration(5);
      for (Node edge : this.getVertices()) {
        values.put(edge, e.get(edge));
      }
    } else if (centrality.equals(CentralitiesDualDual.KATZCENTRALITY_LOCAL)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de Katz locales ...");
      }
      Map<Node, Double> e = this.getKatzCentrality(1.5);
      for (Node edge : this.getVertices()) {
        values.put(edge, e.get(edge));
      }
    } else if (centrality.equals(CentralitiesDualDual.KATZCENTRALITY_GLOBAL)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de Katz globales ...");
      }
      Map<Node, Double> e = this.getKatzCentrality(1.0001);
      for (Node edge : this.getVertices()) {
        values.put(edge, e.get(edge));
      }
    } else if (centrality.equals(CentralitiesDualDual.DEGREE2)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de degré-2 ...");
      }
      Map<Node, Double> e = this.getKDegreeCentrality(2);
      for (Node node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }
    } else if (centrality.equals(CentralitiesDualDual.DEGREE3)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de degré-3...");
      }
      Map<Node, Double> e = this.getKDegreeCentrality(3);
      for (Node node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }

    } else if (centrality.equals(CentralitiesDualDual.DEGREE5)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de degré-5...");
      }
      Map<Node, Double> e = this.getKDegreeCentrality(5);
      for (Node node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }

    } else if (centrality.equals(CentralitiesDualDual.CLOSENESS2)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de proximité - 2...");
      }
      Map<Node, Double> e = this.getLocaleClosenessCentrality(2);
      for (Node node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }
    } else if (centrality.equals(CentralitiesDualDual.CLOSENESS3)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de proximité - 3...");
      }
      Map<Node, Double> e = this.getLocaleClosenessCentrality(3);
      for (Node node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }
    } else if (centrality.equals(CentralitiesDualDual.CLOSENESS5)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de proximité - 5...");
      }
      Map<Node, Double> e = this.getLocaleClosenessCentrality(5);
      for (Node node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }
    }
    values = (new Statistics<Node>()).centrerRéduire(values);
    return values;
  }

  public Map<Edge, Double> getCentralityEdges(String centrality) {
    Map<Node, Double> nodeValues = this.getCentralityNodes(centrality);
    Map<Edge, Double> result = new HashMap<Edge, Double>();
    for (Edge e : this.mappingStrokesNodes.keySet()) {
      double value = 0;
      for (Node n : this.mappingStrokesNodes.get(e)) {
        value += nodeValues.get(n);
      }
      value = value / ((double) this.mappingStrokesNodes.get(e).size());
      result.put(e, value);
    }
    result = (new Statistics<Edge>()).centrerRéduire(result);
    return result;
  }

  public Map<Edge, Double> getCentralityEdges(Map<Node, Double> nodeValues) {
    Map<Edge, Double> result = new HashMap<Edge, Double>();
    for (Edge e : this.mappingStrokesNodes.keySet()) {
      double value = 0;
      for (Node n : this.mappingStrokesNodes.get(e)) {
        value += nodeValues.get(n);
      }
      value = value / ((double) this.mappingStrokesNodes.get(e).size());
      result.put(e, value);
    }
    result = (new Statistics<Edge>()).centrerRéduire(result);
    return result;
  }

  // ***************************************************************************
  // ************************** Méthodes de calcul *****************************
  // ***************************************************************************

  public ClosenessCentrality<Node, String> getClosenessCentrality() {
    ClosenessCentrality<Node, String> b = new ClosenessCentrality<Node, String>(
        this);
    return b;
  }

  public BetweennessCentrality<Node, String> getBetweennessCentrality() {
    BetweennessCentrality<Node, String> b = new BetweennessCentrality<Node, String>(
        this);
    return b;
  }

  /***
   * Intégration globale
   * @return
   */
  public Map<Node, Double> getGlobalIntegration() {
    UnweightedShortestPath<Node, String> path = new UnweightedShortestPath<Node, String>(
        this);

    Map<Node, Double> globalIntegration = new HashMap<Node, Double>();
    // nombre de strokes (nombres d'axial lines)
    int n = this.getVertexCount();

    double Dn = 2 * (n * (Math.log((n + 2) / 3) / Math.log(2) - 1) + 1)
        / ((n - 1) * (n - 2));

    for (Node road : this.getVertices()) {
      double MDi = 0;
      Map<Node, Number> m = path.getDistanceMap(road);
      for (Node otherRoad : this.getVertices()) {
        MDi += distance_topo(m, otherRoad);
      }
      MDi *= 1. / (n - 1);
      double RAi = 2 * MDi / (n - 2);

      double inti = Dn / RAi;
      globalIntegration.put(road, inti);
      path.reset(road);
    }
    return globalIntegration;
  }

  public Map<Node, Double> getLocalIntegration(int depth) {
    int n = this.getVertexCount();
    if (depth < 1 || depth > n) {
      if (logger.isInfoEnabled()) {
        logger
            .warn("La valeur depth n'est pas compatible avec un calcul d'intégration local");
      }
      return null;
    }
    UnweightedShortestPath<Node, String> path = new UnweightedShortestPath<Node, String>(
        this);

    Map<Node, Double> localIntegration = new HashMap<Node, Double>();
    // nombre de strokes (nombres d'axial lines)

    double Dn = 2 * (n * (Math.log((n + 2) / 3) / Math.log(2) - 1) + 1)
        / ((n - 1) * (n - 2));

    for (Node road : this.getVertices()) {
      int cpt = 0;
      double MDi = 0;
      Map<Node, Number> m = path.getDistanceMap(road);
      for (Node otherRoad : this.getVertices()) {
        double dist = distance_topo(m, otherRoad);
        if (dist > depth) {
          continue;
        }
        MDi += distance_topo(m, otherRoad);
        cpt++;
      }
      MDi *= 1. / cpt;
      double RAi = 2 * MDi / (n - 2);

      double inti = Dn / RAi;

      localIntegration.put(road, inti);
      path.reset(road);
    }
    return localIntegration;
  }

  private int distance_topo(Map<Node, Number> m, Node geom) {
    if (!m.containsKey(geom)) {
      return Integer.MAX_VALUE;
    }
    return Integer.parseInt(m.get(geom).toString());
  }

  public void exportCentrality(String centrality, String file, int format) {
    return;
  }

  public void setMappingStrokesNodes(Map<Edge, List<Node>> mappingStrokesNodes) {
    this.mappingStrokesNodes = mappingStrokesNodes;
  }

  public Map<Edge, List<Node>> getMappingStrokesNodes() {
    return mappingStrokesNodes;
  }

  public Map<Node, Double> getLocaleClosenessCentrality(int depth) {
    if (depth < 1) {
      logger.error("Locale CLoseness centrality: depth must be 1 or more.");
      System.exit(-1);
    }
    Map<Node, Double> result = new HashMap<Node, Double>();
    UnweightedShortestPath<Node, String> sp = new UnweightedShortestPath<Node, String>(
        this);

    DoubleMatrix2D A = this.getKSUMAdjacencyMatrix(depth);

    int cpt = 0;
    for (Node node : this.getVertices()) {
      cpt++;
      // récupération des kneighbor
      List<Node> kneighbor = this.getNeighbors(node, A);
      Map<Node, Number> m = sp.getDistanceMap(node);
      double centrality = 0;
      for (Node otherNode : this.getVertices()) {
        if (node.equals(otherNode)) {
          continue;
        }
        if (!kneighbor.contains(otherNode)) {
          continue;
        }
        double dist = m.get(otherNode).doubleValue();
        centrality += dist;
      }
      if (centrality != 0) {
        centrality = ((double) kneighbor.size() - 1) / centrality;
      }
      result.put(node, centrality);
      sp.reset(node);
    }
    return result;
  }

  @Override
  public Map<Node, Double> getLocaleBetweennessCentrality(int depth) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<Node, Double> getFlowBetweennessCentrality() {
    // TODO Auto-generated method stub
    return null;
  }

}
