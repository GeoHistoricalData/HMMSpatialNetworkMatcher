package fr.ign.cogit.morphogenesis.network.graph.rewriting;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import edu.uci.ics.jung.algorithms.importance.RandomWalkBetweenness;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import fr.ign.cogit.morphogenesis.network.graph.rewriting.io.CentralitiesPrimal;
import fr.ign.cogit.morphogenesis.network.graph.rewriting.randomWalk.RandomWalk;

public class GeometricalGraph extends AbstractGraph<Node, Edge> {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  Logger logger = Logger.getLogger(GeometricalGraph.class);

  public GeometricalGraph() {
    super();
    this.setId("Global Geometrical Graph");
  }

  public GeometricalGraph(GeometricalGraph g) {
    super();
    this.setId("Global Geometrical Graph");
    for (Edge s : g.getEdges()) {
      this.addEdge(s, s.first(), s.last());
    }
  }

  // ***************************************************************************
  // ************************** Méthodes d'export ******************************
  // ***************************************************************************
  public Map<Edge, Double> getCentrality(String centrality) {
    Map<Edge, Double> values = new HashMap<Edge, Double>();
    if (centrality.equals(CentralitiesPrimal.CLOSENESS)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de proximité en cours ...");
      }
      for (Edge edge : this.getEdges()) {
        ClosenessCentrality<Node, Edge> c = this.getClosenessCentrality();
        values
            .put(
                edge,
                (c.getVertexScore(edge.first()) + c.getVertexScore(edge.last())) / 2.);
      }
    } else if (centrality.equals(CentralitiesPrimal.BETWEENNESS)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités intermédiaires en cours ...");
      }
      BetweennessCentrality<Node, Edge> b = getBetweennessCentrality();
      int n = this.getVertexCount();
      for (Edge edge : this.getEdges()) {
        values
            .put(
                edge,
                ((b.getVertexScore(edge.first()) + b.getVertexScore(edge.last())) / 2.)
                    / ((n - 1.) * (n - 2.)));
      }
    } else if (centrality.equals(CentralitiesPrimal.RANDOMBETWEENNESS)) {
      if (logger.isInfoEnabled()) {
        logger
            .info("Calcul des centralités intermédiaires random walk en cours ...");
      }
      RandomWalkBetweenness<Node, Edge> b = getRandomBetweennessCentrality();
      for (Edge edge : this.getEdges()) {
        values.put(edge, ((b.getVertexRankScore(edge.first()) + b
            .getVertexRankScore(edge.last())) / 2.));
      }
    } else if (centrality.equals(CentralitiesPrimal.DEGREE)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de degré en cours ...");
      }
      DegreeScorer<Node> c = this.getDegreeCentrality();
      for (Edge edge : this.getEdges()) {
        values
            .put(
                edge,
                (c.getVertexScore(edge.first()) + c.getVertexScore(edge.last())) / 2.);
      }
    } else if (centrality.equals(CentralitiesPrimal.CONTROL)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des mesures de contrôle en cours ...");
      }
      Map<Node, Double> map = this.getControlMetric();
      for (Edge edge : this.getEdges()) {
        values.put(edge, (map.get(edge.first()) + map.get(edge.last())) / 2.);
      }
    } else if (centrality.equals(CentralitiesPrimal.MEANDISTANCE)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des éloignements moyens en cours ...");
      }
      Map<Node, Double> map = this.getMeanDistance();
      for (Edge edge : this.getEdges()) {
        values.put(edge, (map.get(edge.first()) + map.get(edge.last())) / 2.);
      }
    } else if (centrality.equals(CentralitiesPrimal.CLUSTERING)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de clustering en cours ...");
      }
      Map<Node, Double> map = this.getClusteringCentrality();
      for (Edge edge : this.getEdges()) {
        values.put(edge, (map.get(edge.first()) + map.get(edge.last())) / 2.);
      }
    } else if (centrality.equals(CentralitiesPrimal.TWOCLUSTERING)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de 2-clustering en cours ...");
      }
      Map<Node, Double> map = this.get2ClusteringCentrality();
      for (Edge edge : this.getEdges()) {
        values.put(edge, (map.get(edge.first()) + map.get(edge.last())) / 2.);
      }
    } else if (centrality.equals(CentralitiesPrimal.EFFICIENCY)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités d'efficacité en cours ...");
      }
      Map<Node, Double> map = this.getEfficiencyCentrality();
      for (Edge edge : this.getEdges()) {
        values.put(edge, (map.get(edge.first()) + map.get(edge.last())) / 2.);
      }
    } else if (centrality.equals(CentralitiesPrimal.STRAIGHTNESS)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de rectitude en cours ...");
      }
      Map<Node, Double> map = this.getStraightnessCentrality();
      for (Edge edge : this.getEdges()) {
        values.put(edge, (map.get(edge.first()) + map.get(edge.last())) / 2.);
      }
    } else if (centrality.equals(CentralitiesPrimal.CIRCUIT)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des circuités en cours ...");
      }
      Map<Node, Double> map = this.getCircuitCentrality();
      for (Edge edge : this.getEdges()) {
        values.put(edge, (map.get(edge.first()) + map.get(edge.last())) / 2.);
      }
    } else if (centrality.equals(CentralitiesPrimal.INFORMATION)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités d'information ...");
      }
      Map<Node, Double> map = this.getInformationCentrality();
      for (Edge edge : this.getEdges()) {
        values.put(edge, (map.get(edge.first()) + map.get(edge.last())) / 2.);
      }
    } else if (centrality.equals(CentralitiesPrimal.PAGERANK)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des page rank en cours ...");
      }
      Map<Node, Double> map = this.getPageRank(0.85);
      for (Edge edge : this.getEdges()) {
        values.put(edge, (map.get(edge.first()) + map.get(edge.last())) / 2.);
      }
    } else if (centrality.equals(CentralitiesPrimal.WPAGERANK)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des weighted page rank en cours ...");
      }
      Map<Node, Double> map = this.getWeightedPageRank(0.85);
      for (Edge edge : this.getEdges()) {
        values.put(edge, (map.get(edge.first()) + map.get(edge.last())) / 2.);
      }
    } else if (centrality.equals(CentralitiesPrimal.RANDOMWALK)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des randomw walkers ...");
      }
      RandomWalk<Node, Edge> map = this.getRandomWalk(10000);
      for (Edge edge : this.getEdges()) {
        values
            .put(edge, (map.getVertexScore(edge.first()) + map
                .getVertexScore(edge.last())) / 2.);
      }

    } else if (centrality.equals(CentralitiesPrimal.EIGENVECTOR)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités spectrales ...");
      }
      EigenvectorCentrality<Node, Edge> e = this.getEigenVectorCentrality();
      for (Edge edge : this.getEdges()) {
        values
            .put(
                edge,
                (e.getVertexScore(edge.first()) + e.getVertexScore(edge.last())) / 2.);
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

  public ClosenessCentrality<Node, Edge> getClosenessCentrality() {
    ClosenessCentrality<Node, Edge> closenessCentrality = new ClosenessCentrality<Node, Edge>(
        this, new Transformer<Edge, Double>() {
          public Double transform(Edge o) {
            return Math.sqrt((o.first().getX() - o.last().getX())
                * (o.first().getX() - o.last().getX())
                + (o.first().getY() - o.last().getY())
                * (o.first().getY() - o.last().getY()));
          }

        });
    return closenessCentrality;
  }

  public BetweennessCentrality<Node, Edge> getBetweennessCentrality() {
    BetweennessCentrality<Node, Edge> b = new BetweennessCentrality<Node, Edge>(
        this, new Transformer<Edge, Double>() {
          public Double transform(Edge o) {
            return Math.sqrt((o.first().getX() - o.last().getX())
                * (o.first().getX() - o.last().getX())
                + (o.first().getY() - o.last().getY())
                * (o.first().getY() - o.last().getY()));
          }

        });
    return b;
  }

  /**
   * Calcul de la centralité d"efficacité (Latora, Marchiori)
   * @return
   */
  public Map<Node, Double> getEfficiencyCentrality() {
    Map<Node, Double> b = new HashMap<Node, Double>();
    DijkstraShortestPath<Node, Edge> sp = new DijkstraShortestPath<Node, Edge>(
        this, new Transformer<Edge, Double>() {
          public Double transform(Edge o) {
            return Math.sqrt((o.first().getX() - o.last().getX())
                * (o.first().getX() - o.last().getX())
                + (o.first().getY() - o.last().getY())
                * (o.first().getY() - o.last().getY()));
          }

        });
    for (Node node : this.getVertices()) {

      double numerateur = 0;
      double denominateur = 0;

      Map<Node, Number> m = sp.getDistanceMap(node);
      double centrality = 0;
      for (Node otherNode : this.getVertices()) {
        if (node.equals(otherNode)) {
          continue;
        }
        if (!m.containsKey(otherNode)) {
          continue;
        }

        numerateur += 1. / Double.parseDouble(m.get(otherNode).toString());
        denominateur += 1. / Math.sqrt((node.getX() - otherNode.getX())
            * (node.getX() - otherNode.getX())
            + (node.getY() - otherNode.getY())
            * (node.getY() - otherNode.getY()));
      }
      sp.reset(node);
      centrality = numerateur / denominateur;
      b.put(node, centrality);
    }
    return b;
  }

  /**
   * Centralité de rectitude (vragovic)
   * @return
   */
  public Map<Node, Double> getStraightnessCentrality() {
    Map<Node, Double> b = new HashMap<Node, Double>();
    DijkstraShortestPath<Node, Edge> sp = new DijkstraShortestPath<Node, Edge>(
        this, new Transformer<Edge, Double>() {
          public Double transform(Edge o) {
            return Math.sqrt((o.first().getX() - o.last().getX())
                * (o.first().getX() - o.last().getX())
                + (o.first().getY() - o.last().getY())
                * (o.first().getY() - o.last().getY()));
          }

        });
    for (Node node : this.getVertices()) {

      Map<Node, Number> m = sp.getDistanceMap(node);
      double centrality = 0;
      for (Node otherNode : this.getVertices()) {
        if (node.equals(otherNode)) {
          continue;
        }
        if (!m.containsKey(otherNode)) {
          continue;
        }

        centrality += Math.sqrt((node.getX() - otherNode.getX())
            * (node.getX() - otherNode.getX())
            + (node.getY() - otherNode.getY())
            * (node.getY() - otherNode.getY()))
            / Double.parseDouble(m.get(otherNode).toString());

      }
      sp.reset(node);
      centrality /= (this.getVertexCount() - 1);
      b.put(node, centrality);
    }
    return b;
  }

  /**
   * Circuité (Kansky)
   * @return
   */
  public Map<Node, Double> getCircuitCentrality() {
    Map<Node, Double> b = new HashMap<Node, Double>();
    DijkstraShortestPath<Node, Edge> sp = new DijkstraShortestPath<Node, Edge>(
        this, new Transformer<Edge, Double>() {
          public Double transform(Edge o) {
            return Math.sqrt((o.first().getX() - o.last().getX())
                * (o.first().getX() - o.last().getX())
                + (o.first().getY() - o.last().getY())
                * (o.first().getY() - o.last().getY()));
          }

        });
    for (Node node : this.getVertices()) {

      Map<Node, Number> m = sp.getDistanceMap(node);
      double centrality = 0;
      for (Node otherNode : this.getVertices()) {
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
      centrality /= (this.getVertexCount() - 1);
      b.put(node, centrality);
    }
    return b;
  }

  /**
   * calcul de la centralité d'information (Latora, Marchiori)
   * @return
   */
  public Map<Node, Double> getInformationCentrality() {
    Map<Node, Double> b = new HashMap<Node, Double>();

    // Calcul de l'efficacité globale
    double globalEfficiency = 0;
    Map<Node, Double> sc = this.getStraightnessCentrality();
    for (Node node : this.getVertices()) {
      globalEfficiency += sc.get(node);
    }
    globalEfficiency /= (double) this.getVertexCount();

    int nb = this.getVertexCount();
    int cpt = 1;

    for (Node node : this.getVertices()) {
      System.out.println(cpt + " / " + nb);
      cpt++;
      GeometricalGraph g2 = new GeometricalGraph(this);
      g2.removeVertex(node);
      double localEfficiency = 0;
      sc = g2.getStraightnessCentrality();
      for (Node node2 : g2.getVertices()) {
        localEfficiency += sc.get(node2);
      }
      localEfficiency /= (double) g2.getVertexCount();

      double centrality = (globalEfficiency - localEfficiency)
          / globalEfficiency;
      b.put(node, centrality);

    }

    return b;
  }

  @Override
  public EigenvectorCentrality<Node, Edge> getEigenVectorCentrality() {
    EigenvectorCentrality<Node, Edge> e = new EigenvectorCentrality<Node, Edge>(
        this, new Transformer<Edge, Double>() {
          public Double transform(Edge o) {
            return Math.sqrt((o.first().getX() - o.last().getX())
                * (o.first().getX() - o.last().getX())
                + (o.first().getY() - o.last().getY())
                * (o.first().getY() - o.last().getY()));
          }

        });
    e.evaluate();
    return e;
  }

}
