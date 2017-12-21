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
import fr.ign.cogit.morphogenesis.network.graph.GeometricalGraph;
import fr.ign.cogit.morphogenesis.network.graph.Node;
import fr.ign.cogit.morphogenesis.network.graph.io.CentralitiesPrimal;
import fr.ign.cogit.morphogenesis.network.graph.io.GraphReader;
import fr.ign.cogit.morphogenesis.network.graph.social.Individu;
import fr.ign.cogit.morphogenesis.network.graph.social.SocialGraph;
import geocodage.IOUtils;

public class Individu2Postgis {

  public static void insert() throws SQLException {

    String tablenameNoeuds = "socialgraph_maxc_noeuds";
    String tablenameNoeudsGeom = "ind_spatial_socialgraph_maxc_noeuds";
    String tablenameRelation = "socialgraph_maxc_arcs";

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

    SocialGraph g = GraphReader.createSocialGraphGeocoded(host, login,
        password, port, sourceName, tablenameNoeuds, tablenameRelation,
        "id_composante_connexe");

    System.out.println(g.getClusteringCoefficient());
    System.out.println(g.getCharacteristicPathLength());
    System.out.println(g.getSmallWorldProperties()[0] + " , "
        + g.getSmallWorldProperties()[1]);

    // mapping noeud / id
    Map<Individu, Integer> map = new HashMap<Individu, Integer>();

    for (Individu i : g.getVertices()) {
      map.put(i, i.getId());
    }

    // les noeuds

    Map<Individu, Double> closeness = g
        .getCentralityNodes(CentralitiesPrimal.CLOSENESS);
    Map<Individu, Double> closeness3 = g
        .getCentralityNodes(CentralitiesPrimal.CLOSENESS3);
    Map<Individu, Double> closeness5 = g
        .getCentralityNodes(CentralitiesPrimal.CLOSENESS5);
    Map<Individu, Double> closeness10 = g
        .getCentralityNodes(CentralitiesPrimal.CLOSENESS10);
    Map<Individu, Double> betweenness = g
        .getCentralityNodes(CentralitiesPrimal.BETWEENNESS);
    /*
     * Map<Node, Double> betweenness3 = g
     * .getCentralityNodes(CentralitiesPrimal.BETWEENNESS3); Map<Node, Double>
     * betweenness5 = g .getCentralityNodes(CentralitiesPrimal.BETWEENNESS5);
     */
    // Map<Node, Double> betweenness10 = g
    // .getCentralityNodes(CentralitiesPrimal.BETWEENNESS10);
    Map<Individu, Double> degree = g
        .getCentralityNodes(CentralitiesPrimal.DEGREE);
    Map<Individu, Double> degree3 = g
        .getCentralityNodes(CentralitiesPrimal.DEGREE3);
    Map<Individu, Double> degree5 = g
        .getCentralityNodes(CentralitiesPrimal.DEGREE5);
    Map<Individu, Double> degree10 = g
        .getCentralityNodes(CentralitiesPrimal.DEGREE10);
    Map<Individu, Double> control = g
        .getCentralityNodes(CentralitiesPrimal.CONTROL);
    Map<Individu, Double> mean_distance = new HashMap<Individu, Double>();
    for (Individu n : g.getVertices()) {
      mean_distance.put(n, 1. / closeness.get(n));
    }
    Map<Individu, Double> eigenvector = g
        .getCentralityNodes(CentralitiesPrimal.EIGENVECTOR);
    Map<Individu, Double> katz_local = g
        .getCentralityNodes(CentralitiesPrimal.KATZCENTRALITY_LOCAL);
    Map<Individu, Double> katz_global = g
        .getCentralityNodes(CentralitiesPrimal.KATZCENTRALITY_GLOBAL);
    Map<Individu, Double> pagerank = g
        .getCentralityNodes(CentralitiesPrimal.PAGERANK);
    Map<Individu, Double> clustering = g
        .getCentralityNodes(CentralitiesPrimal.CLUSTERING);
    Map<Individu, Double> clustering2 = g
        .getCentralityNodes(CentralitiesPrimal.TWOCLUSTERING);
    Map<Individu, Double> clustering3 = g
        .getCentralityNodes(CentralitiesPrimal.CLUSTERING3);
    Map<Individu, Double> clustering5 = g
        .getCentralityNodes(CentralitiesPrimal.CLUSTERING5);
    Map<Individu, Double> clustering10 = g
        .getCentralityNodes(CentralitiesPrimal.CLUSTERING10);

    try {
      destDbConnection = IOUtils.createDBConnection(host, port, sourceName,
          login, password);
      PreparedStatement stInsertData = destDbConnection
          .prepareStatement("INSERT INTO "
              + tablenameNoeudsGeom
              + "(the_geom, closeness, closeness_3, closeness_5, closeness_10, betweenness,"
              + "  degree, degree_3, degree_5,"
              + "degree_10, control, mean_distance, eigenvector,"
              + " katz_local, katz_global,"
              + " pagerank, clustering, clustering_2, clustering_3, clustering_5, clustering_10, id)"
              + " VALUES (ST_geomfromtext(?),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      int nb_transfert = 0;
      for (Individu n : g.getVertices()) {
        System.out.println(g.getVertices().size());

        stInsertData.setString(1, WktGeOxygene
            .makeWkt(new GM_Point(new DirectPosition(n.getPosition().getX(), n
                .getPosition().getY()))));
        stInsertData.setDouble(2, closeness.get(n));
        stInsertData.setDouble(3, closeness3.get(n));
        stInsertData.setDouble(4, closeness5.get(n));
        stInsertData.setDouble(5, closeness10.get(n));
        stInsertData.setDouble(6, betweenness.get(n));
        // stInsertData.setDouble(7, betweenness3.get(n));
        // stInsertData.setDouble(8, betweenness5.get(n));
        // stInsertData.setDouble(9, betweenness10.get(n));
        stInsertData.setDouble(7, degree.get(n));
        stInsertData.setDouble(8, degree3.get(n));
        stInsertData.setDouble(9, degree5.get(n));
        stInsertData.setDouble(10, degree10.get(n));
        stInsertData.setDouble(11, control.get(n));
        stInsertData.setDouble(12, mean_distance.get(n));
        stInsertData.setDouble(13, eigenvector.get(n));
        stInsertData.setDouble(14, katz_local.get(n));
        stInsertData.setDouble(15, katz_global.get(n));
        stInsertData.setDouble(16, pagerank.get(n));
        stInsertData.setDouble(17, clustering.get(n));
        stInsertData.setDouble(18, clustering2.get(n));
        stInsertData.setDouble(19, clustering3.get(n));
        stInsertData.setDouble(20, clustering5.get(n));
        stInsertData.setDouble(21, clustering10.get(n));
        stInsertData.setInt(22, map.get(n));

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
    String tablenameNoeudsGeom = "ind_spatial_vasserot_noeuds_geom";

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

    GeometricalGraph g = GraphReader
        .createGeometricalGraph(
            "/media/Data/Benoit/these/donnees/vecteur/FILAIRES_L93_OK/1810_1836_VASSEROT_accentsOK_topologieOk.shp",
            1);

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

    Individu2Postgis.insert();

  }

}
