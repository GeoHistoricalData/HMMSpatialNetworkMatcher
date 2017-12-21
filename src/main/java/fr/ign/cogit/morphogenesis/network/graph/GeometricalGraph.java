package fr.ign.cogit.morphogenesis.network.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import cern.colt.matrix.DoubleMatrix2D;
import edu.uci.ics.jung.algorithms.flows.EdmondsKarpMaxFlow;
import edu.uci.ics.jung.algorithms.importance.RandomWalkBetweenness;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import fr.ign.cogit.graphs.utils.Statistics;
import fr.ign.cogit.morphogenesis.network.graph.io.CentralitiesPrimal;
import fr.ign.cogit.morphogenesis.network.graph.randomWalk.RandomWalk;

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

  public Map<Edge, Double> getCentralityEdges(String centrality) {
    Map<Edge, Double> result = new HashMap<Edge, Double>();
    Map<Node, Double> values = this.getCentralityNodes(centrality);

    for (Edge edge : this.getEdges()) {
      result.put(edge,
          (values.get(edge.first()) + values.get(edge.last())) / 2.);
    }
    result = (new Statistics<Edge>()).centrerRéduire(result);

    return result;
  }

  public Map<Edge, Double> getCentralityEdges(Map<Node, Double> values) {
    Map<Edge, Double> result = new HashMap<Edge, Double>();
    for (Edge edge : this.getEdges()) {
      result.put(edge,
          (values.get(edge.first()) + values.get(edge.last())) / 2.);
    }
    result = (new Statistics<Edge>()).centrerRéduire(result);

    return result;
  }

  public Map<Node, Double> getCentralityNodes(String centrality) {
    Map<Node, Double> values = new HashMap<Node, Double>();
    if (centrality.equals(CentralitiesPrimal.CLOSENESS)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de proximité en cours ...");
      }
      for (Node node : this.getVertices()) {
        ClosenessCentrality<Node, Edge> c = this.getClosenessCentrality();
        values.put(node, c.getVertexScore(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.BETWEENNESS)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités intermédiaires en cours ...");
      }
      BetweennessCentrality<Node, Edge> b = getBetweennessCentrality();
      int n = this.getVertexCount();
      for (Node node : this.getVertices()) {
        values.put(node, (b.getVertexScore(node)) / ((n - 1.) * (n - 2.)));
      }
    } else if (centrality.equals(CentralitiesPrimal.BETWEENNESS3)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités intermédiaires -3  en cours ...");
      }
      Map<Node, Double> b = getLocaleBetweennessCentrality(3);
      for (Node node : this.getVertices()) {
        values.put(node, (b.get(node)));
      }
    } else if (centrality.equals(CentralitiesPrimal.BETWEENNESS5)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités intermédiaires -5  en cours ...");
      }
      Map<Node, Double> b = getLocaleBetweennessCentrality(5);
      for (Node node : this.getVertices()) {
        values.put(node, (b.get(node)));
      }
    } else if (centrality.equals(CentralitiesPrimal.BETWEENNESS10)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités intermédiaires -10  en cours ...");
      }
      Map<Node, Double> b = getLocaleBetweennessCentrality(10);
      for (Node node : this.getVertices()) {
        values.put(node, (b.get(node)));
      }
    } else if (centrality.equals(CentralitiesPrimal.BETWEENNESS15)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités intermédiaires -15  en cours ...");
      }
      Map<Node, Double> b = getLocaleBetweennessCentrality(15);
      for (Node node : this.getVertices()) {
        values.put(node, (b.get(node)));
      }
    }

    else if (centrality.equals(CentralitiesPrimal.RANDOMBETWEENNESS)) {
      if (logger.isInfoEnabled()) {
        logger
            .info("Calcul des centralités intermédiaires random walk en cours ...");
      }
      RandomWalkBetweenness<Node, Edge> b = getRandomBetweennessCentrality();
      for (Node node : this.getVertices()) {
        values.put(node, b.getVertexRankScore(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.DEGREE)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de degré en cours ...");
      }
      DegreeScorer<Node> c = this.getDegreeCentrality();
      for (Node node : this.getVertices()) {
        values.put(node, (double) c.getVertexScore(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.CONTROL)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des mesures de contrôle en cours ...");
      }
      Map<Node, Double> map = this.getControlMetric();
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.MEANDISTANCE)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des éloignements moyens en cours ...");
      }
      Map<Node, Double> map = this.getMeanDistance();
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.CLUSTERING)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de clustering en cours ...");
      }
      Map<Node, Double> map = this.getClusteringCentrality();
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.TWOCLUSTERING)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de 2-clustering en cours ...");
      }
      Map<Node, Double> map = this.getKClusteringCentrality(2);
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.CLUSTERING3)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de 3-clustering en cours ...");
      }
      Map<Node, Double> map = this.getKClusteringCentrality(3);
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.CLUSTERING5)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de 5-clustering en cours ...");
      }
      Map<Node, Double> map = this.getKClusteringCentrality(5);
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.CLUSTERING10)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de 10-clustering en cours ...");
      }
      Map<Node, Double> map = this.getKClusteringCentrality(10);
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.EFFICIENCY)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités d'efficacité en cours ...");
      }
      Map<Node, Double> map = this.getEfficiencyCentrality();
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.STRAIGHTNESS)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de rectitude en cours ...");
      }
      Map<Node, Double> map = this.getStraightnessCentrality();
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.STRAIGHTNESS5)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de rectitude-5 en cours ...");
      }
      Map<Node, Double> map = this.getLocaleStraightnessCentrality(5);
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.STRAIGHTNESS10)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de rectitude-10 en cours ...");
      }
      Map<Node, Double> map = this.getLocaleStraightnessCentrality(10);
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.STRAIGHTNESS15)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de rectitude-15 en cours ...");
      }
      Map<Node, Double> map = this.getLocaleStraightnessCentrality(15);
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.STRAIGHTNESS3)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de rectitude-3 en cours ...");
      }
      Map<Node, Double> map = this.getLocaleStraightnessCentrality(3);
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.CIRCUIT)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des circuités en cours ...");
      }
      Map<Node, Double> map = this.getCircuitCentrality();
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.INFORMATION)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités d'information ...");
      }
      Map<Node, Double> map = this.getInformationCentrality();
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.PAGERANK)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des page rank en cours ...");
      }
      Map<Node, Double> map = this.getPageRank(0.85);
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.WPAGERANK)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des weighted page rank en cours ...");
      }
      Map<Node, Double> map = this.getWeightedPageRank(0.85);
      for (Node node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.RANDOMWALK)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des randomw walkers ...");
      }
      RandomWalk<Node, Edge> map = this.getRandomWalk(10000);
      for (Node node : this.getVertices()) {
        values.put(node, (double) map.getVertexScore(node));
      }

    } else if (centrality.equals(CentralitiesPrimal.EIGENVECTOR)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités spectrales ...");
      }
      Map<Node, Double> e = this.getEigenVectorCentrality();
      for (Node node : this.getVertices()) {
        values.put(node, e.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.KATZCENTRALITY_LOCAL)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de Katz locales ...");
      }
      Map<Node, Double> e = this.getKatzCentrality(1.5);
      for (Node node : this.getVertices()) {
        values.put(node, e.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.KATZCENTRALITY_GLOBAL)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de Katz globales ...");
      }
      Map<Node, Double> e = this.getKatzCentrality(1.0001);
      for (Node node : this.getVertices()) {
        values.put(node, e.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.FLOWBETWEENNESS)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités flow betweenness ...");
      }
      Map<Node, Double> e = this.getFlowBetweennessCentrality();
      for (Node node : this.getVertices()) {
        values.put(node, e.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.DEGREE2)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de degré-2 ...");
      }
      Map<Node, Double> e = this.getKDegreeCentrality(2);
      for (Node node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.DEGREE3)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de degré-3...");
      }
      Map<Node, Double> e = this.getKDegreeCentrality(3);
      for (Node node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }

    } else if (centrality.equals(CentralitiesPrimal.DEGREE5)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de degré-5...");
      }
      Map<Node, Double> e = this.getKDegreeCentrality(5);
      for (Node node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }

    } else if (centrality.equals(CentralitiesPrimal.DEGREE10)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de degré-10...");
      }
      Map<Node, Double> e = this.getKDegreeCentrality(10);
      for (Node node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }

    } else if (centrality.equals(CentralitiesPrimal.CLOSENESS2)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de proximité - 2...");
      }
      Map<Node, Double> e = this.getLocaleClosenessCentrality(2);
      for (Node node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.CLOSENESS3)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de proximité - 3...");
      }
      Map<Node, Double> e = this.getLocaleClosenessCentrality(3);
      for (Node node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.CLOSENESS5)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de proximité - 5...");
      }
      Map<Node, Double> e = this.getLocaleClosenessCentrality(5);
      for (Node node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.CLOSENESS10)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de proximité - 10...");
      }
      Map<Node, Double> e = this.getLocaleClosenessCentrality(10);
      for (Node node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }
    }
    values = (new Statistics<Node>()).centrerRéduire(values);
    return values;
  }

  public void exportCentrality(String centrality, String file, int format) {

    Map<Edge, Double> values = getCentralityEdges(centrality);
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
    sp.reset();
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

  @SuppressWarnings("rawtypes")
  @Override
  public Map<Node, Double> getFlowBetweennessCentrality() {
    Map<Node, Double> result = new HashMap<Node, Double>();

    Transformer<Edge, Double> capTransformer = new Transformer<Edge, Double>() {
      public Double transform(Edge o) {
        return Math.sqrt((o.first().getX() - o.last().getX())
            * (o.first().getX() - o.last().getX())
            + (o.first().getY() - o.last().getY())
            * (o.first().getY() - o.last().getY()));
      }
    };

    // This Factory produces new edges for use by the algorithm
    Factory<Edge> edgeFactory = new Factory<Edge>() {
      public Edge create() {
        Edge e = new Edge();
        return e;
      }
    };

    Map<Node, Double> centralities = new HashMap<Node, Double>();
    double totalMaxFLow = 0;
    List<Node> tagged = new ArrayList<Node>();
    for (Node n1 : this.getVertices()) {
      centralities.put(n1, 0.);
    }
    for (Node n2 : this.getVertices()) {
      tagged.add(n2);
      System.out.println(tagged.size() + " / " + this.getVertexCount());
      for (Node n3 : this.getVertices()) {
        if (tagged.contains(n3)) {
          continue;
        }
        Map<Edge, Double> edgeFlowMap = new HashMap<Edge, Double>();

        Factory<DirectedGraph<Node, Edge>> graphFactory = new Factory<DirectedGraph<Node, Edge>>() {
          public DirectedGraph<Node, Edge> create() {
            DirectedGraph<Node, Edge> g = new DirectedSparseMultigraph<Node, Edge>();
            return g;
          }
        };

        DirectedGraph<Node, Edge> directedGraph = graphFactory.create();
        for (Node v : this.getVertices())
          directedGraph.addVertex(v);
        for (Edge e : this.getEdges()) {
          Pair<Node> endpoints = this.getEndpoints(e);
          Node v1 = endpoints.getFirst();
          Node v2 = endpoints.getSecond();
          directedGraph.addEdge(new Edge(e), v1, v2, EdgeType.DIRECTED);
          directedGraph.addEdge(new Edge(e), v2, v1, EdgeType.DIRECTED);
        }

        @SuppressWarnings("unchecked")
        EdmondsKarpMaxFlow alg = new EdmondsKarpMaxFlow(directedGraph, n2, n3,
            capTransformer, edgeFlowMap, edgeFactory);
        alg.evaluate();

        totalMaxFLow += alg.getMaxFlow();

        for (Node n1 : this.getVertices()) {
          if (n1.equals(n2) || n1.equals(n3)) {
            continue;
          }

          if (alg.getFlowGraph().getVertices().contains(n1)) {
            centralities.put(n1, centralities.get(n1) + alg.getMaxFlow());
          }
        }

      }
    }
    for (Node n1 : this.getVertices()) {
      centralities.put(n1, centralities.get(n1) / totalMaxFLow);
    }

    return centralities;
  }

  public Map<Node, Double> getLocaleClosenessCentrality(int depth) {
    if (depth < 1) {
      logger.error("Locale CLoseness centrality: depth must be 1 or more.");
      System.exit(-1);
    }
    Map<Node, Double> result = new HashMap<Node, Double>();
    DijkstraShortestPath<Node, Edge> sp = new DijkstraShortestPath<Node, Edge>(
        this, new Transformer<Edge, Double>() {
          public Double transform(Edge o) {
            return Math.sqrt((o.first().getX() - o.last().getX())
                * (o.first().getX() - o.last().getX())
                + (o.first().getY() - o.last().getY())
                * (o.first().getY() - o.last().getY()));
          }

        });

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
    if (depth < 1) {
      logger.error("Locale CLoseness centrality: depth must be 1 or more.");
      System.exit(-1);
    }
    Map<Node, Double> result = new HashMap<Node, Double>();
    DijkstraShortestPath<Node, Edge> sp = new DijkstraShortestPath<Node, Edge>(
        this, new Transformer<Edge, Double>() {
          public Double transform(Edge o) {
            return Math.sqrt((o.first().getX() - o.last().getX())
                * (o.first().getX() - o.last().getX())
                + (o.first().getY() - o.last().getY())
                * (o.first().getY() - o.last().getY()));
          }

        });

    DoubleMatrix2D A = this.getKSUMAdjacencyMatrix(depth);

    for (Node node : this.getVertices()) {
      int nbPassBy = 0;
      int nbTotal = 0;
      // récupération des kneighbor
      List<Node> kneighbor = this.getNeighbors(node, A);
      double centrality = 0;
      for (Node otherNode1 : this.getVertices()) {
        if (node.equals(otherNode1)) {
          continue;
        }
        if (!kneighbor.contains(otherNode1)) {
          continue;
        }
        for (Node otherNode2 : this.getVertices()) {
          if (node.equals(otherNode2)) {
            continue;
          }
          if (otherNode1.equals(otherNode2)) {
            continue;
          }
          if (!kneighbor.contains(otherNode2)) {
            continue;
          }

          if (sp.getDistance(otherNode1, otherNode2) != null) {
            nbTotal++;
            List<Edge> edges = sp.getPath(otherNode1, otherNode2);
            for (Edge e : edges) {
              if (e.first().equals(node) || e.last().equals(node)) {
                nbPassBy++;
                break;
              }
            }
          }
        }
      }
      centrality = ((double) nbPassBy) / ((double) nbTotal);
      centrality = centrality / (kneighbor.size() - 1) * (kneighbor.size() - 2);
      result.put(node, centrality);
      sp.reset(node);
    }
    return result;
  }

  public Map<Node, Double> getLocaleStraightnessCentrality(int depth) {
    if (depth < 1) {
      logger.error("Locale CLoseness centrality: depth must be 1 or more.");
      System.exit(-1);
    }
    Map<Node, Double> result = new HashMap<Node, Double>();
    DijkstraShortestPath<Node, Edge> sp = new DijkstraShortestPath<Node, Edge>(
        this, new Transformer<Edge, Double>() {
          public Double transform(Edge o) {
            return Math.sqrt((o.first().getX() - o.last().getX())
                * (o.first().getX() - o.last().getX())
                + (o.first().getY() - o.last().getY())
                * (o.first().getY() - o.last().getY()));
          }

        });

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
        centrality += Math.sqrt((node.getX() - otherNode.getX())
            * (node.getX() - otherNode.getX())
            + (node.getY() - otherNode.getY())
            * (node.getY() - otherNode.getY()))
            / dist;
      }
      if (centrality != 0) {
        centrality = centrality / ((double) kneighbor.size() - 1);
      }
      result.put(node, centrality);
      sp.reset(node);
    }
    return result;
  }
}
