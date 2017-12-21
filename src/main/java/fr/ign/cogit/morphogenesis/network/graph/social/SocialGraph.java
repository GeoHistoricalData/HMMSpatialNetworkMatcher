package fr.ign.cogit.morphogenesis.network.graph.social;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import cern.colt.matrix.DoubleMatrix2D;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import fr.ign.cogit.graphs.utils.Statistics;
import fr.ign.cogit.morphogenesis.network.graph.AbstractGraph;
import fr.ign.cogit.morphogenesis.network.graph.io.CentralitiesDual;
import fr.ign.cogit.morphogenesis.network.graph.io.CentralitiesPrimal;
import fr.ign.cogit.morphogenesis.network.graph.io.GraphReader;
import fr.ign.cogit.morphogenesis.network.utils.ConnectedComponents;
import geocodage.IOUtils;

public class SocialGraph extends AbstractGraph<Individu, Relation> {

  /**
   * 
   */
  Logger logger = Logger.getLogger(SocialGraph.class);

  private static final long serialVersionUID = -6540365958759573773L;
  private List<UndirectedSparseMultigraph<Individu, Relation>> connectedComponents;

  public SocialGraph() {
    super();
    this.setId("Social Graph");
    this.connectedComponents = new ArrayList<UndirectedSparseMultigraph<Individu, Relation>>();
  }

  public SocialGraph(SocialGraph g) {
    super();
    this.setId("Social Graph");
    for (Relation s : g.getEdges()) {
      this.addEdge(s, s.getFirst(), s.getSecond());
    }
    this.connectedComponents = new ArrayList<UndirectedSparseMultigraph<Individu, Relation>>(
        g.getConnectedComponents());
  }

  @Override
  public ClosenessCentrality<Individu, Relation> getClosenessCentrality() {
    ClosenessCentrality<Individu, Relation> b = new ClosenessCentrality<Individu, Relation>(
        this);
    return b;
  }

  @Override
  public BetweennessCentrality<Individu, Relation> getBetweennessCentrality() {
    BetweennessCentrality<Individu, Relation> b = new BetweennessCentrality<Individu, Relation>(
        this);
    return b;
  }

  @Override
  public void exportCentrality(String centrality, String file, int format) {
    // TODO Auto-generated method stub

  }

  public Map<Individu, Double> getCentralityNodes(String centrality) {
    Map<Individu, Double> values = new HashMap<Individu, Double>();
    if (centrality.equals(CentralitiesDual.CLOSENESS)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de proximité en cours ...");
      }
      for (Individu edge : this.getVertices()) {
        ClosenessCentrality<Individu, Relation> c = this
            .getClosenessCentrality();
        values.put(edge, c.getVertexScore(edge));
      }
    }
    if (centrality.equals(CentralitiesDual.BETWEENNESS)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités intermédiaires en cours ...");
      }
      BetweennessCentrality<Individu, Relation> b = getBetweennessCentrality();
      int n = this.getVertexCount();
      for (Individu node : this.getVertices()) {
        values.put(node, (b.getVertexScore(node)) / ((n - 1.) * (n - 2.)));
      }
    } else if (centrality.equals(CentralitiesPrimal.DEGREE)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de degré en cours ...");
      }
      DegreeScorer<Individu> c = this.getDegreeCentrality();
      for (Individu node : this.getVertices()) {
        values.put(node, (double) c.getVertexScore(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.CONTROL)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des mesures de contrôle en cours ...");
      }
      Map<Individu, Double> map = this.getControlMetric();
      for (Individu node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.MEANDISTANCE)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des éloignements moyens en cours ...");
      }
      Map<Individu, Double> map = this.getMeanDistance();
      for (Individu node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.CLUSTERING)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de clustering en cours ...");
      }
      Map<Individu, Double> map = this.getClusteringCentrality();
      for (Individu node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.TWOCLUSTERING)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de 2-clustering en cours ...");
      }
      Map<Individu, Double> map = this.getKClusteringCentrality(2);
      for (Individu node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.CLUSTERING3)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de 3-clustering en cours ...");
      }
      Map<Individu, Double> map = this.getKClusteringCentrality(3);
      for (Individu node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.CLUSTERING5)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de 5-clustering en cours ...");
      }
      Map<Individu, Double> map = this.getKClusteringCentrality(5);
      for (Individu node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.CLUSTERING10)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de 10-clustering en cours ...");
      }
      Map<Individu, Double> map = this.getKClusteringCentrality(10);
      for (Individu node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.PAGERANK)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des page rank en cours ...");
      }
      Map<Individu, Double> map = this.getPageRank(0.85);
      for (Individu node : this.getVertices()) {
        values.put(node, map.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.EIGENVECTOR)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités spectrales ...");
      }
      Map<Individu, Double> e = this.getEigenVectorCentrality();
      for (Individu node : this.getVertices()) {
        values.put(node, e.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.KATZCENTRALITY_LOCAL)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de Katz locales ...");
      }
      Map<Individu, Double> e = this.getKatzCentrality(1.5);
      for (Individu node : this.getVertices()) {
        values.put(node, e.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.KATZCENTRALITY_GLOBAL)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de Katz globales ...");
      }
      Map<Individu, Double> e = this.getKatzCentrality(1.0001);
      for (Individu node : this.getVertices()) {
        values.put(node, e.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.DEGREE2)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de degré-2 ...");
      }
      Map<Individu, Double> e = this.getKDegreeCentrality(2);
      for (Individu node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.DEGREE3)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de degré-3...");
      }
      Map<Individu, Double> e = this.getKDegreeCentrality(3);
      for (Individu node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }

    } else if (centrality.equals(CentralitiesPrimal.DEGREE5)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de degré-5...");
      }
      Map<Individu, Double> e = this.getKDegreeCentrality(5);
      for (Individu node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }

    } else if (centrality.equals(CentralitiesPrimal.DEGREE10)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de degré-10...");
      }
      Map<Individu, Double> e = this.getKDegreeCentrality(10);
      for (Individu node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }

    } else if (centrality.equals(CentralitiesPrimal.CLOSENESS2)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de proximité - 2...");
      }
      Map<Individu, Double> e = this.getLocaleClosenessCentrality(2);
      for (Individu node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.CLOSENESS3)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de proximité - 3...");
      }
      Map<Individu, Double> e = this.getLocaleClosenessCentrality(3);
      for (Individu node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.CLOSENESS5)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de proximité - 5...");
      }
      Map<Individu, Double> e = this.getLocaleClosenessCentrality(5);
      for (Individu node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }
    } else if (centrality.equals(CentralitiesPrimal.CLOSENESS10)) {
      if (logger.isInfoEnabled()) {
        logger.info("Calcul des centralités de proximité - 10...");
      }
      Map<Individu, Double> e = this.getLocaleClosenessCentrality(10);
      for (Individu node : this.getVertices()) {
        values.put(node, (double) e.get(node));
      }
    }
    values = (new Statistics<Individu>()).centrerRéduire(values);
    return values;
  }

  public Map<Relation, Double> getCentralityEdges(Map<Individu, Double> values) {
    Map<Relation, Double> result = new HashMap<Relation, Double>();
    for (Relation edge : this.getEdges()) {
      result.put(edge,
          (values.get(edge.getFirst()) + values.get(edge.getSecond())) / 2.);
    }
    result = (new Statistics<Relation>()).centrerRéduire(result);

    return result;
  }

  public static void main(String[] args) throws SQLException {
    String host = "127.0.0.1";
    String user = "postgres";
    String password = " ";
    String dbname = "these";
    String port = "5432";
    String tableIndividus = "source_minutier1851_individus_paris";
    String tableRelations = "source_minutier1851_liens_paris";
    String attIdCompConnexe = "id_composante_connexe";

    SocialGraph socialGraph = GraphReader.createSocialGraph(host, user,
        password, port, dbname, tableIndividus, tableRelations,
        attIdCompConnexe);

    // socialGraph.buildConnectedComponents();

    socialGraph.fillTableConnectedComponenets(host, user, password, port,
        dbname, tableIndividus, tableRelations, attIdCompConnexe);

    final SocialGraph socialGraphMax = new SocialGraph();
    for (Relation e : socialGraph.getConnectedComponents().get(0).getEdges()) {
      socialGraphMax.addEdge(e, e.getFirst(), e.getSecond());

    }

    System.out.println(socialGraphMax.getVertexCount());

    System.out.println(socialGraphMax.getClusteringCoefficient());
    System.out.println(socialGraphMax.getCharacteristicPathLength());
    System.out.println(socialGraphMax.getSmallWorldProperties()[0] + " , "
        + socialGraphMax.getSmallWorldProperties()[1]);

    for (Individu i : socialGraphMax.getVertices()) {
      System.out.println(socialGraphMax.getIncidentEdges(i).size());
    }

    List<Individu> individus = new ArrayList<Individu>();
    individus.addAll(socialGraphMax.getVertices());
    Collections.sort(individus, new Comparator<Individu>() {
      public int compare(Individu o1, Individu o2) {
        Individu i1 = null, i2 = null;
        for (Individu i : socialGraphMax.getVertices()) {
          if (i.equals(o1)) {
            i1 = o1;
          }
          if (i.equals(o2)) {
            i2 = o2;
          }
          if (i1 != null && i2 != null) {
            break;
          }
        }
        if (socialGraphMax.getIncidentEdges(i1).size() > socialGraphMax
            .getIncidentEdges(i2).size()) {
          return -1;
        } else if (socialGraphMax.getIncidentEdges(i1).size() < socialGraphMax
            .getIncidentEdges(i2).size()) {
          return 1;
        } else {
          return 0;
        }
      }
    });

    for (int i = 0; i < 100; i++) {
      System.out.println(individus.get(i).getProfession());
    }

    System.out.println("-----AAAAAAA------");

    /*
     * for (int i = individus.size() - 1; i > individus.size() - 200; i--) {
     * System.out.println(individus.get(i).getProfession()); }
     */
    Individu maxlien = individus.get(0);
    System.out.println(maxlien.getProfession() + " " + maxlien.getPrenom()
        + " " + maxlien.getNom());
    for (Relation t : socialGraphMax.getIncidentEdges(maxlien)) {
      Individu i2 = socialGraphMax.getOpposite(maxlien, t);
      System.out.println(t.getType() + " " + i2.getId() + " "
          + i2.getProfession() + " " + i2.getPrenom() + " " + i2.getNom());
    }

    System.out.println("-----AAAAAAA------");

    maxlien = individus.get(1);
    System.out.println(maxlien.getProfession() + " " + maxlien.getPrenom()
        + " " + maxlien.getNom());
    for (Relation t : socialGraphMax.getIncidentEdges(maxlien)) {
      Individu i2 = socialGraphMax.getOpposite(maxlien, t);
      System.out.println(t.getType() + " " + i2.getId() + " "
          + i2.getProfession() + " " + i2.getPrenom() + " " + i2.getNom());
    }

    /*
     * ConnectedComponents<Individu, Relation> c = new
     * ConnectedComponents<Individu, Relation>( socialGraph);
     * List<UndirectedSparseMultigraph<Individu, Relation>> connectedComposents
     * = c .buildConnectedComponents();
     */

    UndirectedSparseMultigraph<Individu, Relation> g = socialGraph
        .getConnectedComponents().get(0);

    Layout<Individu, Relation> layout = new KKLayout<Individu, //
    Relation>(g);

    layout.setSize(new Dimension(1000, 1000));
    VisualizationViewer<Individu, Relation> vv = new VisualizationViewer<Individu, Relation>(
        layout);
    vv.setPreferredSize(new Dimension(1200, 1000)); // Sets the viewing area //
                                                    // size
    Transformer<Individu, Shape> vertexStrokeTransformer = new Transformer<Individu, Shape>() {

      public Shape transform(Individu i) {
        Ellipse2D circle = new Ellipse2D.Double(-1, -1, 2, 2);
        return circle;
      }
    };

    final Stroke edgeStroke = new BasicStroke(0.05f);
    Transformer<Relation, Stroke> edgeStrokeTransformer = new Transformer<Relation, Stroke>() {
      public Stroke transform(Relation s) {
        return edgeStroke;
      }
    };

    vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
    vv.getRenderContext().setVertexShapeTransformer(vertexStrokeTransformer);
    DefaultModalGraphMouse<Individu, Relation> gm = new DefaultModalGraphMouse<Individu, Relation>();
    gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
    vv.setGraphMouse(gm);
    JFrame frame = new JFrame("Simple Graph View");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(vv);
    frame.pack();
    frame.setVisible(true);

  }

  public void setConnectedComponents(
      List<UndirectedSparseMultigraph<Individu, Relation>> connectedComponents) {
    this.connectedComponents = connectedComponents;
  }

  public List<UndirectedSparseMultigraph<Individu, Relation>> getConnectedComponents() {
    return connectedComponents;
  }

  public void fillTableConnectedComponenets(String host, String user,
      String password, String port, String dbname, String tableIndividus,
      String tableRelations, String attIdCompConnexe) {
    this.buildConnectedComponents();
    Connection destDbConnection = null;

    try {
      // initialisation des connexions aux bases (source puis dest)
      destDbConnection = IOUtils.createDBConnection(host, port, dbname, user,
          password);

      // écriture pour les individus

      PreparedStatement stUpdateData = destDbConnection
          .prepareStatement("UPDATE " + tableIndividus + " SET "
              + attIdCompConnexe + " =  ? WHERE id_individu = ?");
      // compteur de réponses à la première requète
      int nb_transfert = 0;

      for (Individu ind : this.getVertices()) {

        stUpdateData.setInt(1, ind.getIdConnectedComponent());
        stUpdateData.setInt(2, ind.getId());
        stUpdateData.addBatch();

        // increment du compteur
        nb_transfert++;

        // uncomment this part in case of big data
        if (nb_transfert % 50 == 0) {
          // vide le buffer de requètes
          int[] status = new int[50];
          try {
            status = stUpdateData.executeBatch();
          } catch (SQLException e) {
            e.getNextException().printStackTrace();
          }
        }
      }
      // initialise le tableau des codes de retour pour chaque requète.
      int[] status = new int[nb_transfert];
      status = stUpdateData.executeBatch();

      // écriture pour les relations

      stUpdateData = destDbConnection.prepareStatement("UPDATE "
          + tableRelations + " SET " + attIdCompConnexe
          + " =  ? WHERE id_lien = ?");
      // compteur de réponses à la première requète
      nb_transfert = 0;

      for (Relation rel : this.getEdges()) {
        stUpdateData.setInt(1, rel.getIdConnectedComponent());
        stUpdateData.setInt(2, rel.getId());
        stUpdateData.addBatch();

        // increment du compteur
        nb_transfert++;

        // uncomment this part in case of big data
        if (nb_transfert % 50 == 0) {
          // vide le buffer de requètes
          status = new int[50];
          try {
            status = stUpdateData.executeBatch();
          } catch (SQLException e) {
            e.getNextException().printStackTrace();
          }
        }
      }
      // initialise le tableau des codes de retour pour chaque requète.
      status = new int[nb_transfert];
      status = stUpdateData.executeBatch();

    } catch (SQLException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        IOUtils.closeDBConnection(destDbConnection);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public void buildConnectedComponents() {
    if (this.connectedComponents.isEmpty()) {
      if (this.getEdges().iterator().next().getIdConnectedComponent() == -1) {
        logger
            .info("Calcul des composantes connexes ... peut prendre du temps.");
        ConnectedComponents<Individu, Relation> c = new ConnectedComponents<Individu, Relation>(
            this);
        this.connectedComponents = c.buildConnectedComponents();
        for (int i = 0; i < this.getConnectedComponents().size(); i++) {
          UndirectedSparseMultigraph<Individu, Relation> component = this
              .getConnectedComponents().get(i);
          for (Individu ind : component.getVertices()) {
            ind.setIdConnectedComponent(i);
          }
          for (Relation rel : component.getEdges()) {
            rel.setIdConnectedComponent(i);
          }
        }
        logger.info("Calcul des composantes connexes ... terminé");
      } else {
        // numéro de composantes connexes déja présentes dans la base
        Map<Integer, List<Relation>> map = new HashMap<Integer, List<Relation>>();
        for (Relation rel : this.getEdges()) {
          if (map.containsKey(rel.getIdConnectedComponent())) {
            map.get(rel.getIdConnectedComponent()).add(rel);
          } else {
            List<Relation> l = new ArrayList<Relation>();
            l.add(rel);
            map.put(rel.getIdConnectedComponent(), l);
          }
        }
        for (Integer i : map.keySet()) {
          UndirectedSparseMultigraph<Individu, Relation> connectedComponent = new UndirectedSparseMultigraph<Individu, Relation>();
          for (Relation rel : map.get(i)) {
            connectedComponent.addEdge(rel, this.getEndpoints(rel));
          }
          this.connectedComponents.add(connectedComponent);
        }
        Collections.sort(this.connectedComponents,
            new Comparator<UndirectedSparseMultigraph<Individu, Relation>>() {
              public int compare(
                  UndirectedSparseMultigraph<Individu, Relation> o1,
                  UndirectedSparseMultigraph<Individu, Relation> o2) {
                if (o1.getVertexCount() > o2.getVertexCount()) {
                  return -1;
                } else if (o1.getVertexCount() < o2.getVertexCount()) {
                  return 1;
                } else {
                  if (o1.getEdgeCount() > o2.getEdgeCount()) {
                    return -1;
                  } else if (o1.getEdgeCount() < o2.getEdgeCount()) {
                    return 1;
                  } else {
                    return 0;
                  }
                }
              }
            });

      }
    } else {
      logger.info("Composantes connexes déjà calculées");
    }
  }

  @Override
  public Map<Individu, Double> getLocaleClosenessCentrality(int depth) {
    if (depth < 1) {
      logger.error("Locale CLoseness centrality: depth must be 1 or more.");
      System.exit(-1);
    }
    Map<Individu, Double> result = new HashMap<Individu, Double>();
    UnweightedShortestPath<Individu, Relation> sp = new UnweightedShortestPath<Individu, Relation>(
        this);

    DoubleMatrix2D A = this.getKSUMAdjacencyMatrix(depth);

    int cpt = 0;
    for (Individu node : this.getVertices()) {
      cpt++;
      // récupération des kneighbor
      List<Individu> kneighbor = this.getNeighbors(node, A);
      Map<Individu, Number> m = sp.getDistanceMap(node);
      double centrality = 0;
      for (Individu otherNode : this.getVertices()) {
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
  public Map<Individu, Double> getLocaleBetweennessCentrality(int depth) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<Individu, Double> getFlowBetweennessCentrality() {
    // TODO Auto-generated method stub
    return null;
  }
}
