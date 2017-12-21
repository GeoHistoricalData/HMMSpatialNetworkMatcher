package fr.ign.cogit.morphogenesis.network.analysis.indicators.export;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.WktGeOxygene;
import fr.ign.cogit.morphogenesis.network.graph.GeometricalGraph;
import fr.ign.cogit.morphogenesis.network.graph.Node;
import fr.ign.cogit.morphogenesis.network.graph.io.GraphReader;
import geocodage.IOUtils;

public class Node2Postgis {

  public static void main(String[] args) throws SQLException {

    String tablename = "gis_vasserot_modif_noeuds";

    GeometricalGraph g = GraphReader
        .createGeometricalGraph(
            "/media/Data/Benoit/these/donnees/vecteur/FILAIRES_L93_OK/1810_1836_VASSEROT_accentsOK_topologieOk_PONTS.shp",
            0);

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

    // les noeuds

    try {
      destDbConnection = IOUtils.createDBConnection(host, port, sourceName,
          login, password);
      PreparedStatement stInsertData = destDbConnection
          .prepareStatement("INSERT INTO " + tablename + "(the_geom)"
              + " VALUES (ST_geomfromtext(?))");
      int nb_transfert = 0;
      for (Node n : g.getVertices()) {

        stInsertData.setString(1, WktGeOxygene.makeWkt(new GM_Point(
            new DirectPosition(n.getX(), n.getY()))));

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

      // Récupération des id des noeuds
      Map<Node, Integer> id_nodes = new HashMap<Node, Integer>();

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
}
