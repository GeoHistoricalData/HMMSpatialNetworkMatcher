package fr.ign.cogit.morphogenesis.network.analysis.indicators.export;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ParseException;
import fr.ign.cogit.geoxygene.util.conversion.WktGeOxygene;
import fr.ign.cogit.morphogenesis.network.graph.DualTopologicalGraph;
import fr.ign.cogit.morphogenesis.network.graph.Node;
import fr.ign.cogit.morphogenesis.network.graph.io.CentralitiesPrimal;
import fr.ign.cogit.morphogenesis.network.graph.io.GraphReader;
import geocodage.IOUtils;

public class Node2PostgisDual {

  public static void insert() throws SQLException {

    String tablenameNoeuds = "gis_poubelle_emprise_noeuds";
    String tablenameNoeudsGeom = "ind_spatial_poubelle_noeuds_dual";

    String host = "127.0.0.1";
    String port = "5432";
    String sourceName = "these";
    String login = "postgres";
    String password = " ";
    Connection destDbConnection = null;

    // **********************************************************************************
    // ****************************** Graphe géométrique
    // ********************************
    // **********************************************************************************

    DualTopologicalGraph g = GraphReader
        .createDualTopologicalGraph("/media/Data/Benoit/these/donnees/vecteur/STROKES_L93/poubelle_emprise_strokes.shp");

    System.out.println(g.getVertexCount());
    // mapping noeud / id
    Map<Node, Integer> map = new HashMap<Node, Integer>();

    try {
      destDbConnection = IOUtils.createDBConnection(host, port, sourceName,
          login, password);
      PreparedStatement stSelectData = destDbConnection
          .prepareStatement("SELECT id, ST_ASTEXT(the_geom) as geom FROM "
              + tablenameNoeuds);
      // initialise le résultat de la requète
      ResultSet query = null;
      // exécution de la requete de sélection
      query = stSelectData.executeQuery();

      if (query != null) {
        while (query.next()) {

          int id = query.getInt("id");
          IGeometry geom;

          try {
            geom = WktGeOxygene.makeGeOxygene(query.getString("geom"));
            Node node = null;
            for (Node n : g.getVertices()) {
              if ((new GM_Point(new DirectPosition(n.getX(), n.getY())))
                  .distance(geom) < 0.05) {
                node = n;
                break;
              }
            }
            if (node == null) {
              /*
               * System.err.println("Noeud null !");
               * System.err.println(geom.toString()); System.exit(-1000);
               */
            }
            map.put(node, id);
          } catch (ParseException e) {
            e.printStackTrace();
          }

        }
      }
    } catch (SQLException e) {
      // afficher la stack trace pour comprendre l'erreur
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      IOUtils.closeDBConnection(destDbConnection);
    }
    // les noeuds

    Map<Node, Double> closeness = g
        .getCentralityNodes(CentralitiesPrimal.CLOSENESS);
    Map<Node, Double> closeness3 = g
        .getCentralityNodes(CentralitiesPrimal.CLOSENESS3);
    Map<Node, Double> closeness5 = g
        .getCentralityNodes(CentralitiesPrimal.CLOSENESS5);
    Map<Node, Double> betweenness = g
        .getCentralityNodes(CentralitiesPrimal.BETWEENNESS);
    /*
     * Map<Node, Double> betweenness3 = g
     * .getCentralityNodes(CentralitiesPrimal.BETWEENNESS3); Map<Node, Double>
     * betweenness5 = g .getCentralityNodes(CentralitiesPrimal.BETWEENNESS5);
     */
    // Map<Node, Double> betweenness10 = g
    // .getCentralityNodes(CentralitiesPrimal.BETWEENNESS10);
    Map<Node, Double> degree = g.getCentralityNodes(CentralitiesPrimal.DEGREE);
    Map<Node, Double> degree3 = g
        .getCentralityNodes(CentralitiesPrimal.DEGREE3);
    Map<Node, Double> degree5 = g
        .getCentralityNodes(CentralitiesPrimal.DEGREE5);
    Map<Node, Double> control = g
        .getCentralityNodes(CentralitiesPrimal.CONTROL);
    Map<Node, Double> mean_distance = new HashMap<Node, Double>();
    for (Node n : g.getVertices()) {
      mean_distance.put(n, 1. / closeness.get(n));
    }
    Map<Node, Double> eigenvector = g
        .getCentralityNodes(CentralitiesPrimal.EIGENVECTOR);
    Map<Node, Double> katz_local = g
        .getCentralityNodes(CentralitiesPrimal.KATZCENTRALITY_LOCAL);
    Map<Node, Double> katz_global = g
        .getCentralityNodes(CentralitiesPrimal.KATZCENTRALITY_GLOBAL);
    Map<Node, Double> pagerank = g
        .getCentralityNodes(CentralitiesPrimal.PAGERANK);
    Map<Node, Double> clustering = g
        .getCentralityNodes(CentralitiesPrimal.CLUSTERING);
    Map<Node, Double> clustering2 = g
        .getCentralityNodes(CentralitiesPrimal.TWOCLUSTERING);
    Map<Node, Double> clustering3 = g
        .getCentralityNodes(CentralitiesPrimal.CLUSTERING3);
    Map<Node, Double> clustering5 = g
        .getCentralityNodes(CentralitiesPrimal.CLUSTERING5);

    try {
      destDbConnection = IOUtils.createDBConnection(host, port, sourceName,
          login, password);
      PreparedStatement stInsertData = destDbConnection
          .prepareStatement("INSERT INTO " + tablenameNoeudsGeom
              + "(the_geom, closeness, closeness_3, closeness_5, betweenness,"
              + "  degree, degree_3, degree_5,"
              + "control, mean_distance, eigenvector,"
              + " katz_local, katz_global," + " pagerank, clustering, id)"
              + " VALUES (ST_geomfromtext(?),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      int nb_transfert = 0;
      for (Node n : g.getVertices()) {

        if (!map.containsKey(n)) {
          continue;
        }

        stInsertData.setString(1, WktGeOxygene.makeWkt(new GM_Point(
            new DirectPosition(n.getX(), n.getY()))));
        stInsertData.setDouble(2, closeness.get(n));
        stInsertData.setDouble(3, closeness3.get(n));
        stInsertData.setDouble(4, closeness5.get(n));
        stInsertData.setDouble(5, betweenness.get(n));
        // stInsertData.setDouble(7, betweenness3.get(n));
        // stInsertData.setDouble(8, betweenness5.get(n));
        // stInsertData.setDouble(9, betweenness10.get(n));
        stInsertData.setDouble(6, degree.get(n));
        stInsertData.setDouble(7, degree3.get(n));
        stInsertData.setDouble(8, degree5.get(n));
        stInsertData.setDouble(9, control.get(n));
        stInsertData.setDouble(10, mean_distance.get(n));
        stInsertData.setDouble(11, eigenvector.get(n));
        stInsertData.setDouble(12, katz_local.get(n));
        stInsertData.setDouble(13, katz_global.get(n));
        stInsertData.setDouble(14, pagerank.get(n));
        stInsertData.setDouble(15, clustering.get(n));
        // stInsertData.setDouble(16, clustering2.get(n));
        // stInsertData.setDouble(17, clustering3.get(n));
        // stInsertData.setDouble(18, clustering5.get(n));
        stInsertData.setInt(19, map.get(n));

        stInsertData.addBatch();
        nb_transfert++;
        if (nb_transfert % 50 == 0) {
          int[] status = new int[50];
          try {
            status = stInsertData.executeBatch();
          } catch (SQLException e) {

          }
        }
      }
      int[] status = new int[nb_transfert];
      status = stInsertData.executeBatch();

    } catch (SQLException e) {
      // afficher la stack trace pour comprendre l'erreur
      e.printStackTrace();
      System.out.println(e.getNextException());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();

    } finally {
      IOUtils.closeDBConnection(destDbConnection);
    }
  }

  public static void update() throws SQLException {

    String tablenameNoeuds = "gis_vasserot_noeuds";
    String tablenameNoeudsGeom = "ind_spatial_vasserot_noeuds_dual";

    String host = "127.0.0.1";
    String port = "5432";
    String sourceName = "these";
    String login = "postgres";
    String password = " ";
    Connection destDbConnection = null;

    // **********************************************************************************
    // ****************************** Graphe géométrique
    // ********************************
    // **********************************************************************************

    DualTopologicalGraph g = GraphReader
        .createDualTopologicalGraph("/media/Data/Benoit/these/donnees/vecteur/STROKES_L93/vasserot_strokes.shp");

    // mapping noeud / id
    Map<Node, Integer> map = new HashMap<Node, Integer>();

    PreparedStatement stSelectData = destDbConnection
        .prepareStatement("SELECT id, ST_ASTEXT(the_geom) as geom FROM "
            + tablenameNoeuds);
    // initialise le résultat de la requète
    ResultSet query = null;
    // exécution de la requete de sélection
    query = stSelectData.executeQuery();

    if (query != null) {
      while (query.next()) {

        int id = query.getInt("id");
        IGeometry geom;

        try {
          geom = WktGeOxygene.makeGeOxygene(query.getString("geom"));
          Node node = null;
          for (Node n : g.getVertices()) {
            if ((new GM_Point(new DirectPosition(n.getX(), n.getY())))
                .distance(geom) < 0.005) {
              node = n;
              break;
            }
          }
          if (node == null) {
            System.err.println("Noeud null !");
            System.exit(-1000);
          }
          map.put(node, id);
        } catch (ParseException e) {
          e.printStackTrace();
        }

      }
    }

    // les noeuds

    Map<Node, Double> update = g
        .getCentralityNodes(CentralitiesPrimal.RANDOMBETWEENNESS);

    try {
      destDbConnection = IOUtils.createDBConnection(host, port, sourceName,
          login, password);
      PreparedStatement stInsertData = destDbConnection
          .prepareStatement("UPDATE " + tablenameNoeudsGeom
              + " SET betweenness_rw = ? " + "WHERE id = ?");
      int nb_transfert = 0;
      for (Node n : g.getVertices()) {

        stInsertData.setDouble(1, update.get(n));
        stInsertData.setInt(2, map.get(n));

        stInsertData.addBatch();
        nb_transfert++;
        if (nb_transfert % 50 == 0) {
          int[] status = new int[50];
          try {
            status = stInsertData.executeBatch();
          } catch (SQLException e) {
            e.getNextException().printStackTrace();
          }
        }
      }
      int[] status = new int[nb_transfert];
      status = stInsertData.executeBatch();

    } catch (SQLException e) {
      // afficher la stack trace pour comprendre l'erreur
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      IOUtils.closeDBConnection(destDbConnection);
    }
  }

  public static void main(String[] args) throws SQLException {

    Node2PostgisDual.insert();

  }

}
