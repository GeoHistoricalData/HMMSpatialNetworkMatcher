package fr.ign.cogit.morphogenesis.network.graph.rewriting;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.algorithms.importance.RandomWalkBetweenness;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import fr.ign.cogit.morphogenesis.network.graph.rewriting.io.CentralitiesDual;
import fr.ign.cogit.morphogenesis.network.graph.rewriting.randomWalk.RandomWalk;

public class TopologicalGraph extends AbstractGraph<Edge, String> {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  Logger logger = Logger.getLogger(TopologicalGraph.class);

  public TopologicalGraph() {
    super();
    this.setId("Global Topological Graph");
  }

  // ***************************************************************************
  // ************************** Méthodes d'export ******************************
  // ***************************************************************************

  public Map<Edge, Double> getCentrality(String centrality) {
    Map<Edge, Double> values = new HashMap<Edge, Double>();
    if (centrality.equals(CentralitiesDual.CLOSENESS)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de proximité en cours ...");
      }
      for (Edge edge : this.getVertices()) {
        ClosenessCentrality<Edge, String> c = this.getClosenessCentrality();
        values.put(edge, c.getVertexScore(edge));
      }
    } else if (centrality.equals(CentralitiesDual.BETWEENNESS)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités intermédiaires en cours ...");
      }
      BetweennessCentrality<Edge, String> c = this.getBetweennessCentrality();
      int n = this.getVertexCount();
      for (Edge edge : this.getVertices()) {
        values.put(edge, c.getVertexScore(edge) / ((n - 1.) * (n - 2.)));
      }

    } else if (centrality.equals(CentralitiesDual.CLUSTERING)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de clustering en cours ...");
      }
      values = this.getClusteringCentrality();
    } else if (centrality.equals(CentralitiesDual.TWOCLUSTERING)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de 2-clustering en cours ...");
      }
      values = this.get2ClusteringCentrality();
    } else if (centrality.equals(CentralitiesDual.RANDOMBETWEENNESS)) {
      if (logger.isInfoEnabled()) {
        logger
            .info("Calcul des centralités intermédiaires random walk en cours ...");
      }
      RandomWalkBetweenness<Edge, String> b = getRandomBetweennessCentrality();
      for (Edge edge : this.getVertices()) {
        values.put(edge, b.getVertexRankScore(edge));
      }
    } else if (centrality.equals(CentralitiesDual.DEGREE)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de degré en cours ...");
      }
      DegreeScorer<Edge> c = this.getDegreeCentrality();
      for (Edge edge : this.getVertices()) {
        values.put(edge, (double) c.getVertexScore(edge));
      }
    } else if (centrality.equals(CentralitiesDual.CONTROL)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des mesures de contrôle en cours ...");
      }
      values = this.getControlMetric();
    } else if (centrality.equals(CentralitiesDual.MEANDISTANCE)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des éloignements moyens en cours ...");
      }
      values = this.getMeanDistance();
    } else if (centrality.equals(CentralitiesDual.PAGERANK)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des page rank en cours ...");
      }
      values = this.getPageRank(0.85);
    } else if (centrality.equals(CentralitiesDual.WPAGERANK)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des weighted page rank en cours ...");
      }
      values = this.getWeightedPageRank(0.85);
    } else if (centrality.equals(CentralitiesDual.RANDOMWALK)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des randomw walkers ...");
      }
      RandomWalk<Edge, String> map = this.getRandomWalk(1000);
      for (Edge edge : this.getVertices()) {
        values.put(edge, (double) map.getVertexScore(edge));
      }
    } else if (centrality.equals(CentralitiesDual.EIGENVECTOR)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités spectrales ...");
      }
      EigenvectorCentrality<Edge, String> e = this.getEigenVectorCentrality();
      for (Edge edge : this.getVertices()) {
        values.put(edge, e.getVertexScore(edge));
      }
    } else if (centrality.equals(CentralitiesDual.SIMPLEST)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des simplest centrality ...");
      }
      Map<Edge, Double> e = this.getSimplestCentrality();
      for (Edge edge : this.getVertices()) {
        values.put(edge, e.get(edge));
      }
    } else if (centrality.equals(CentralitiesDual.GLOBALINTEGRATION)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des intégration globales ...");
      }
      Map<Edge, Double> e = this.getGlobalIntegration();
      for (Edge edge : this.getVertices()) {
        values.put(edge, e.get(edge));
      }
    } else if (centrality.equals(CentralitiesDual.LOCALINTEGRATION)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des intégration locales ...");
      }
      Map<Edge, Double> e = this.getLocalIntegration(3);
      for (Edge edge : this.getVertices()) {
        values.put(edge, e.get(edge));
      }
    } else if (centrality.equals(CentralitiesDual.TOPOLOGICALMAP)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul de la carte topologique ...");
      }
      Map<Edge, Double> e = this.getTopologicalMap();
      for (Edge edge : this.getVertices()) {
        values.put(edge, e.get(edge));
      }
    }
    return values;
  }

  public void exportCentrality(String centrality, String file, int format) {

    Map<Edge, Double> values = getCentrality(centrality);
    exportIndicator(values, file, format);

  }

  // ***************************************************************************
  // ************************** Méthodes de calcul *****************************
  // ***************************************************************************
  public ClosenessCentrality<Edge, String> getClosenessCentrality() {
    ClosenessCentrality<Edge, String> b = new ClosenessCentrality<Edge, String>(
        this);
    return b;
  }

  public BetweennessCentrality<Edge, String> getBetweennessCentrality() {
    BetweennessCentrality<Edge, String> b = new BetweennessCentrality<Edge, String>(
        this);
    return b;
  }

  @Override
  public EigenvectorCentrality<Edge, String> getEigenVectorCentrality() {
    EigenvectorCentrality<Edge, String> e = new EigenvectorCentrality<Edge, String>(
        this);
    e.evaluate();
    return e;
  }

  /**
   * Simplest centrality
   * @return
   */
  public Map<Edge, Double> getSimplestCentrality() {
    Map<Edge, Double> simplestCentrality = new HashMap<Edge, Double>();
    UnweightedShortestPath<Edge, String> path = new UnweightedShortestPath<Edge, String>(
        this);
    for (Edge road : this.getVertices()) {
      double normalizer = 0;
      for (Edge otherRoad : this.getVertices()) {
        normalizer += length(otherRoad);
      }
      Map<Edge, Number> m = path.getDistanceMap(road);
      double centrality = 0;
      for (Edge otherRoad : this.getVertices()) {
        centrality += length(otherRoad) * distance_topo(m, otherRoad);
      }
      centrality = normalizer * (1. / centrality);
      simplestCentrality.put(road, centrality);
      path.reset(road);
    }
    return simplestCentrality;
  }

  /***
   * Intégration globale
   * @return
   */
  public Map<Edge, Double> getGlobalIntegration() {
    UnweightedShortestPath<Edge, String> path = new UnweightedShortestPath<Edge, String>(
        this);

    Map<Edge, Double> globalIntegration = new HashMap<Edge, Double>();
    // nombre de strokes (nombres d'axial lines)
    int n = this.getVertexCount();

    double Dn = 2 * (n * (Math.log((n + 2) / 3) / Math.log(2) - 1) + 1)
        / ((n - 1) * (n - 2));

    for (Edge road : this.getVertices()) {
      double MDi = 0;
      Map<Edge, Number> m = path.getDistanceMap(road);
      for (Edge otherRoad : this.getVertices()) {
        MDi += distance_topo(m, otherRoad);
      }
      MDi *= 1. / (n - 1);
      double RAi = 2 * (MDi - 1) / (n - 2);

      double inti = Dn / RAi;
      globalIntegration.put(road, inti);
      path.reset(road);
    }
    return globalIntegration;
  }

  public Map<Edge, Double> getLocalIntegration(int depth) {
    int n = this.getVertexCount();
    if (depth < 1 || depth > n) {
      if (logger.isInfoEnabled()) {
        logger
            .warn("La valeur depth n'est pas compatible avec un calcul d'intégration local");
      }
      return null;
    }
    UnweightedShortestPath<Edge, String> path = new UnweightedShortestPath<Edge, String>(
        this);

    Map<Edge, Double> localIntegration = new HashMap<Edge, Double>();
    // nombre de strokes (nombres d'axial lines)

    double Dn = 2 * (n * (Math.log((n + 2) / 3) / Math.log(2) - 1) + 1)
        / ((n - 1) * (n - 2));

    for (Edge road : this.getVertices()) {
      int cpt = 0;
      double MDi = 0;
      Map<Edge, Number> m = path.getDistanceMap(road);
      for (Edge otherRoad : this.getVertices()) {
        double dist = distance_topo(m, otherRoad);
        if (dist > depth) {
          continue;
        }
        MDi += distance_topo(m, otherRoad);
        cpt++;
      }
      MDi *= 1. / cpt;
      double RAi = 2 * (MDi - 1) / (n - 2);

      double inti = Dn / RAi;

      localIntegration.put(road, inti);
      path.reset(road);
    }
    return localIntegration;
  }

  /**
   * Carte topologique
   * @return
   */
  public Map<Edge, Double> getTopologicalMap() {
    Map<Edge, Double> map = new HashMap<Edge, Double>();

    UnweightedShortestPath<Edge, String> path = new UnweightedShortestPath<Edge, String>(
        this);

    if (logger.isInfoEnabled()) {
      logger.info("Détermination du centre topologique de la ville ...");
    }

    // détermination du centre topologique de la ville
    // on cherche la rue qui minimise distance_topo_total_weighted
    Edge city_center = null;
    double dist_center_min = Double.MAX_VALUE;
    for (Edge road : this.getVertices()) {
      double dist_road = distance_topo_total_weighted(road, path);
      if (dist_road < dist_center_min) {
        dist_center_min = dist_road;
        city_center = road;
      }
    }

    if (logger.isInfoEnabled()) {
      logger.info("Calcul des distances au centre topologique ...");
    }

    path = new UnweightedShortestPath<Edge, String>(this);

    // on calcul ensuite la distance topologique entre toutes les rues et le
    // centre topo
    for (Edge road : this.getVertices()) {
      Map<Edge, Number> m = path.getDistanceMap(road);
      map.put(road, (double) distance_topo(m, city_center));
      path.reset(road);
    }
    return map;
  }

  private int distance_topo(Map<Edge, Number> m, Edge geom) {
    if (!m.containsKey(geom)) {
      return Integer.MAX_VALUE;
    }
    return Integer.parseInt(m.get(geom).toString());
  }

  private double length(Edge e) {
    double length = 0;
    for (int i = 0; i < e.coords().size() - 1; i++) {
      length += Math.sqrt((e.coords().get(i).getX() - e.coords().get(i + 1)
          .getX())
          * (e.coords().get(i).getX() - e.coords().get(i + 1).getX())
          + (e.coords().get(i).getY() - e.coords().get(i + 1).getY())
          * (e.coords().get(i).getY() - e.coords().get(i + 1).getY()));
    }
    return length;
  }

  private double distance_topo_total_weighted(Edge geom,
      UnweightedShortestPath<Edge, String> path) {
    Map<Edge, Number> m = path.getDistanceMap(geom);
    double total_lenght = 0;
    for (Edge road : this.getVertices()) {
      total_lenght += length(road);
    }
    double distance = 0;
    for (Edge road : this.getVertices()) {
      distance += length(road) * distance_topo(m, road);
    }
    distance /= total_lenght;
    path.reset(geom);
    return distance;
  }
}
