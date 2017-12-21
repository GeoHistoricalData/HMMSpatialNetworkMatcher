package fr.ign.cogit.morphogenesis.network.analysis.indicators.export;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopoFactory;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Face;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.WktGeOxygene;
import fr.ign.cogit.morphogenesis.network.analysis.indicators.isolated.FeatureConvexityRate;
import fr.ign.cogit.morphogenesis.network.analysis.indicators.isolated.FeatureElongation;
import fr.ign.cogit.morphogenesis.network.analysis.indicators.isolated.FormFactor;
import fr.ign.cogit.morphogenesis.network.graph.GeometricalGraph;
import fr.ign.cogit.morphogenesis.network.graph.io.GraphReader;
import geocodage.IOUtils;

public class Indicators2Postgis {

  public static void main(String[] args) throws SQLException {

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
            "/media/Data/Benoit/these/donnees/vecteur/FILAIRES_L93_OK/1885_ALPHAND_POUBELLE_emprise_topologieOk.shp",
            1);

    // les noeuds

    /*
     * Map<Node, Double> closeness = g
     * .getCentralityNodes(CentralitiesPrimal.CLOSENESS); Map<Node, Double>
     * betweenness = g .getCentralityNodes(CentralitiesPrimal.BETWEENNESS); //
     * Map<Node, Double> betweenness_rw = g //
     * .getCentralityNodes(CentralitiesPrimal.RANDOMBETWEENNESS); Map<Node,
     * Double> degree = g.getCentralityNodes(CentralitiesPrimal.DEGREE);
     * Map<Node, Double> control = g
     * .getCentralityNodes(CentralitiesPrimal.CONTROL); Map<Node, Double>
     * mean_distance = new HashMap<Node, Double>(); for (Node n :
     * g.getVertices()) { mean_distance.put(n, 1. / closeness.get(n)); }
     * Map<Node, Double> efficiency = g
     * .getCentralityNodes(CentralitiesPrimal.EFFICIENCY); Map<Node, Double>
     * straightness = g .getCentralityNodes(CentralitiesPrimal.STRAIGHTNESS);
     * Map<Node, Double> circuit = g
     * .getCentralityNodes(CentralitiesPrimal.CIRCUIT);
     * 
     * // Map<Node, Double> information = g //
     * .getCentralityNodes(CentralitiesPrimal.INFORMATION);
     * 
     * Map<Node, Double> eigenvector = g
     * .getCentralityNodes(CentralitiesPrimal.EIGENVECTOR); Map<Node, Double>
     * katz_local = g
     * .getCentralityNodes(CentralitiesPrimal.KATZCENTRALITY_LOCAL); Map<Node,
     * Double> katz_global = g
     * .getCentralityNodes(CentralitiesPrimal.KATZCENTRALITY_GLOBAL); Map<Node,
     * Double> pagerank = g .getCentralityNodes(CentralitiesPrimal.PAGERANK);
     * Map<Node, Double> clustering = g
     * .getCentralityNodes(CentralitiesPrimal.CLUSTERING); Map<Node, Double>
     * clustering2 = g .getCentralityNodes(CentralitiesPrimal.TWOCLUSTERING);
     * 
     * try { destDbConnection = IOUtils.createDBConnection(host, port,
     * sourceName, login, password); PreparedStatement stInsertData =
     * destDbConnection .prepareStatement("INSERT INTO " + tablenameNoeuds +
     * "(the_geom, closeness, betweenness," +
     * " degree, control, mean_distance, efficiency, straightness," +
     * " circuit, eigenvector, katz_local, katz_global," +
     * " pagerank, clustering, clustering_2)" +
     * " VALUES (ST_geomfromtext(?), ?,?,?,?,?,?,?,?,?,?,?,?,?,?)"); int
     * nb_transfert = 0; for (Node n : g.getVertices()) {
     * 
     * stInsertData.setString(1, WktGeOxygene.makeWkt(new GM_Point( new
     * DirectPosition(n.getX(), n.getY())))); stInsertData.setDouble(2,
     * closeness.get(n)); stInsertData.setDouble(3, betweenness.get(n)); //
     * stInsertData.setDouble(4, betweenness_rw.get(n));
     * stInsertData.setDouble(4, degree.get(n)); stInsertData.setDouble(5,
     * control.get(n)); stInsertData.setDouble(6, mean_distance.get(n));
     * stInsertData.setDouble(7, efficiency.get(n)); stInsertData.setDouble(8,
     * straightness.get(n)); stInsertData.setDouble(9, circuit.get(n)); //
     * stInsertData.setDouble(11, information.get(n));
     * stInsertData.setDouble(10, eigenvector.get(n));
     * stInsertData.setDouble(11, katz_local.get(n)); stInsertData.setDouble(12,
     * katz_global.get(n)); stInsertData.setDouble(13, pagerank.get(n));
     * stInsertData.setDouble(14, clustering.get(n)); stInsertData.setDouble(15,
     * clustering2.get(n));
     * 
     * stInsertData.addBatch(); nb_transfert++; if (nb_transfert % 50 == 0) {
     * int[] status = new int[50]; try { status = stInsertData.executeBatch(); }
     * catch (SQLException e) { e.getNextException().printStackTrace(); } } }
     * int[] status = new int[nb_transfert]; status =
     * stInsertData.executeBatch();
     * 
     * // Récupération des id des noeuds Map<Node, Integer> id_nodes = new
     * HashMap<Node, Integer>();
     * 
     * } catch (SQLException e) { // afficher la stack trace pour comprendre
     * l'erreur e.printStackTrace(); } catch (Exception e) { // TODO
     * Auto-generated catch block e.printStackTrace(); } finally {
     * IOUtils.closeDBConnection(destDbConnection); }
     * 
     * // les arcs
     * 
     * Map<Edge, Double> closeness_arcs = g.getCentralityEdges(closeness);
     * Map<Edge, Double> betweenness_arcs = g.getCentralityEdges(betweenness);
     * // Map<Edge, Double> betweenness_rw_arcs = g //
     * .getCentralityEdges(betweenness_rw); Map<Edge, Double> degree_arcs =
     * g.getCentralityEdges(degree); Map<Edge, Double> control_arcs =
     * g.getCentralityEdges(control); Map<Edge, Double> mean_distance_arcs =
     * g.getCentralityEdges(mean_distance); Map<Edge, Double> efficiency_arcs =
     * g.getCentralityEdges(efficiency); Map<Edge, Double> straightness_arcs =
     * g.getCentralityEdges(straightness); Map<Edge, Double> circuit_arcs =
     * g.getCentralityEdges(circuit); // Map<Edge, Double> information_arcs =
     * g.getCentralityEdges(information); Map<Edge, Double> eigenvector_arcs =
     * g.getCentralityEdges(eigenvector); Map<Edge, Double> katz_local_arcs =
     * g.getCentralityEdges(katz_local); Map<Edge, Double> katz_global_arcs =
     * g.getCentralityEdges(katz_global); Map<Edge, Double> pagerank_arcs =
     * g.getCentralityEdges(pagerank); Map<Edge, Double> clustering_arcs =
     * g.getCentralityEdges(clustering); Map<Edge, Double> clustering2_arcs =
     * g.getCentralityEdges(clustering2);
     * 
     * try { // initialisation des connexions aux bases (source puis dest)
     * destDbConnection = IOUtils.createDBConnection(host, port, sourceName,
     * login, password); PreparedStatement stInsertData = destDbConnection
     * .prepareStatement("INSERT INTO " + tablenameArcs +
     * "(the_geom, closeness, betweenness, degree,control," +
     * " mean_distance, efficiency, straightness, circuit," +
     * "eigenvector, katz_local, katz_global, pagerank, clustering, " +
     * "clustering_2)" +
     * " VALUES (ST_geomfromtext(?),?,?,?,?,?,?,?,?,?,?,?,?,?, ?)"); // compteur
     * de réponses à la première requète int nb_transfert = 0;
     * 
     * for (Edge e : g.getEdges()) {
     * 
     * IDirectPositionList l = new DirectPositionList(); for (Point2D.Double p :
     * e.coords()) { l.add(new DirectPosition(p.x, p.y)); }
     * 
     * stInsertData.setString(1, WktGeOxygene.makeWkt(new GM_LineString(l)));
     * stInsertData.setDouble(2, closeness_arcs.get(e));
     * stInsertData.setDouble(3, betweenness_arcs.get(e)); //
     * stInsertData.setDouble(4, betweenness_rw_arcs.get(e));
     * stInsertData.setDouble(4, degree_arcs.get(e)); stInsertData.setDouble(5,
     * control_arcs.get(e)); stInsertData.setDouble(6,
     * mean_distance_arcs.get(e)); stInsertData.setDouble(7,
     * efficiency_arcs.get(e)); stInsertData.setDouble(8,
     * straightness_arcs.get(e)); stInsertData.setDouble(9,
     * circuit_arcs.get(e)); // stInsertData.setDouble(11,
     * information_arcs.get(e)); stInsertData.setDouble(10,
     * eigenvector_arcs.get(e)); stInsertData.setDouble(11,
     * katz_local_arcs.get(e)); stInsertData.setDouble(12,
     * katz_global_arcs.get(e)); stInsertData.setDouble(13,
     * pagerank_arcs.get(e)); stInsertData.setDouble(14,
     * clustering_arcs.get(e)); stInsertData.setDouble(15,
     * clustering2_arcs.get(e)); stInsertData.addBatch(); // increment du
     * compteur nb_transfert++;
     * 
     * // uncomment this part in case of big data if (nb_transfert % 50 == 0) {
     * // vide le buffer de requètes int[] status = new int[50]; try { status =
     * stInsertData.executeBatch(); } catch (SQLException ee) {
     * ee.getNextException().printStackTrace(); }
     * 
     * }
     * 
     * } // initialise le tableau des codes de retour pour chaque requète. int[]
     * status = new int[nb_transfert]; status = stInsertData.executeBatch();
     * 
     * } catch (SQLException e) { // afficher la stack trace pour comprendre
     * l'erreur e.printStackTrace(); } catch (Exception e) { // TODO
     * Auto-generated catch block e.printStackTrace(); } finally {
     * IOUtils.closeDBConnection(destDbConnection); }
     */

    // **********************************************************************************
    // ****************************** Graphe topologique
    // ********************************
    // **********************************************************************************

    /*
     * TopologicalGraph g_topo = GraphReader .createTopologicalGraph(
     * "/media/Data/Benoit/these/donnees/vecteur/STROKES_L93/poubelle_emprise_strokes.shp"
     * );
     * 
     * Map<Edge, Double> closeness_topo = g_topo
     * .getCentralityEdges(CentralitiesDual.CLOSENESS); Map<Edge, Double>
     * betweenness_topo = g_topo
     * .getCentralityEdges(CentralitiesDual.BETWEENNESS); // Map<Edge, Double>
     * betweenness_rw_topo = g_topo //
     * .getCentralityEdges(CentralitiesDual.RANDOMBETWEENNESS); Map<Edge,
     * Double> degree_topo = g_topo
     * .getCentralityEdges(CentralitiesDual.DEGREE); Map<Edge, Double>
     * control_topo = g_topo .getCentralityEdges(CentralitiesDual.CONTROL);
     * Map<Edge, Double> mean_distance_topo = new HashMap<Edge, Double>(); for
     * (Edge e : g_topo.getVertices()) { mean_distance_topo.put(e, 1. /
     * closeness_topo.get(e)); } Map<Edge, Double> eigenvector_topo = g_topo
     * .getCentralityEdges(CentralitiesDual.EIGENVECTOR); Map<Edge, Double>
     * katz_local_topo = g_topo
     * .getCentralityEdges(CentralitiesDual.KATZCENTRALITY_LOCAL); Map<Edge,
     * Double> katz_global_topo = g_topo
     * .getCentralityEdges(CentralitiesDual.KATZCENTRALITY_GLOBAL); Map<Edge,
     * Double> pagerank_topo = g_topo
     * .getCentralityEdges(CentralitiesDual.PAGERANK); Map<Edge, Double>
     * clustering_topo = g_topo
     * .getCentralityEdges(CentralitiesDual.CLUSTERING); Map<Edge, Double>
     * clustering2_topo = g_topo
     * .getCentralityEdges(CentralitiesDual.TWOCLUSTERING); Map<Edge, Double>
     * simplest_topo = g_topo .getCentralityEdges(CentralitiesDual.SIMPLEST);
     * Map<Edge, Double> global_integration_topo = g_topo
     * .getCentralityEdges(CentralitiesDual.GLOBALINTEGRATION); Map<Edge,
     * Double> local_integration_2_topo = g_topo
     * .getCentralityEdges(CentralitiesDual.LOCALINTEGRATION_2); Map<Edge,
     * Double> local_integration_3_topo = g_topo
     * .getCentralityEdges(CentralitiesDual.LOCALINTEGRATION_3); Map<Edge,
     * Double> local_integration_4_topo = g_topo
     * .getCentralityEdges(CentralitiesDual.LOCALINTEGRATION_4); Map<Edge,
     * Double> local_integration_5_topo = g_topo
     * .getCentralityEdges(CentralitiesDual.LOCALINTEGRATION_5);
     * 
     * try { destDbConnection = IOUtils.createDBConnection(host, port,
     * sourceName, login, password); PreparedStatement stInsertData =
     * destDbConnection .prepareStatement("UPDATE " + tablenameArcs +
     * " SET closeness_topo = ?, betweenness_topo = ? = ?," +
     * " degree_topo = ?, control_topo = ?, mean_distance_topo = ?," +
     * " eigenvector_topo = ?, katz_local_topo = ?, katz_global_topo = ?," +
     * " pagerank_topo = ?, clustering_topo = ?, clustering_2_topo = ?," +
     * "simplest = ?, global_integration_topo = ?, local_integration_2_topo = ?,"
     * + "local_integration_3_topo = ?,local_integration_4_topo = ?," +
     * "local_integration_5_topo = ? " +
     * "WHERE ST_CONTAINS(ST_BUFFER( ST_GEOMETRYFROMTEXT(?), 0.05), the_geom)");
     * int nb_transfert = 0; for (Edge n : g_topo.getVertices()) {
     * 
     * stInsertData.setDouble(1, closeness_topo.get(n));
     * stInsertData.setDouble(2, betweenness_topo.get(n)); // //
     * stInsertData.setDouble(3, betweenness_rw_topo.get(n));
     * stInsertData.setDouble(3, degree_topo.get(n)); stInsertData.setDouble(4,
     * control_topo.get(n)); stInsertData.setDouble(5,
     * mean_distance_topo.get(n)); stInsertData.setDouble(6,
     * eigenvector_topo.get(n)); stInsertData.setDouble(7,
     * katz_local_topo.get(n)); stInsertData.setDouble(8,
     * katz_global_topo.get(n)); stInsertData.setDouble(9,
     * pagerank_topo.get(n)); stInsertData.setDouble(10,
     * clustering_topo.get(n)); stInsertData.setDouble(11,
     * clustering2_topo.get(n)); stInsertData.setDouble(12,
     * simplest_topo.get(n)); stInsertData.setDouble(13,
     * global_integration_topo.get(n)); stInsertData.setDouble(14,
     * local_integration_2_topo.get(n)); stInsertData.setDouble(15,
     * local_integration_3_topo.get(n)); stInsertData.setDouble(16,
     * local_integration_4_topo.get(n)); stInsertData.setDouble(17,
     * local_integration_5_topo.get(n)); IDirectPositionList l = new
     * DirectPositionList(); for (Point2D.Double pt : n.coords()) { l.add(new
     * DirectPosition(pt.getX(), pt.getY())); } stInsertData.setString(19,
     * WktGeOxygene.makeWkt(new GM_LineString(l))); stInsertData.addBatch();
     * nb_transfert++; if (nb_transfert % 50 == 0) { int[] status = new int[50];
     * try { status = stInsertData.executeBatch(); } catch (SQLException e) {
     * e.getNextException().printStackTrace(); } } } int[] status = new
     * int[nb_transfert]; status = stInsertData.executeBatch();
     * 
     * } catch (SQLException e) { e.printStackTrace(); } catch (Exception e) {
     * e.printStackTrace(); } finally {
     * IOUtils.closeDBConnection(destDbConnection); }
     */

    // ***************************************************************
    // *********** Graphe topologique dual****************************
    // ***************************************************************

    /*
     * DualTopologicalGraph g_topo_dual = GraphReader
     * .createDualTopologicalGraph(
     * "/media/Data/Benoit/these/donnees/vecteur/STROKES_L93/poubelle_emprise_strokes.shp"
     * );
     * 
     * Map<Node, Double> closeness_topo_dual = g_topo_dual
     * .getCentralityNodes(CentralitiesDualDual.CLOSENESS); Map<Node, Double>
     * betweenness_topo_dual = g_topo_dual
     * .getCentralityNodes(CentralitiesDualDual.BETWEENNESS); /* Map<Node,
     * Double> betweenness_rw_topo_dual = g_topo_dual
     * .getCentralityNodes(CentralitiesDual.RANDOMBETWEENNESS);
     */
    /*
     * Map<Node, Double> degree_topo_dual = g_topo_dual
     * .getCentralityNodes(CentralitiesDualDual.DEGREE); Map<Node, Double>
     * control_topo_dual = g_topo_dual
     * .getCentralityNodes(CentralitiesDualDual.CONTROL); Map<Node, Double>
     * mean_distance_topo_dual = new HashMap<Node, Double>(); for (Node e :
     * g_topo_dual.getVertices()) { mean_distance_topo_dual.put(e, 1. /
     * closeness_topo_dual.get(e)); } Map<Node, Double> eigenvector_topo_dual =
     * g_topo_dual .getCentralityNodes(CentralitiesDualDual.EIGENVECTOR);
     * Map<Node, Double> katz_local_topo_dual = g_topo_dual
     * .getCentralityNodes(CentralitiesDualDual.KATZCENTRALITY_LOCAL); Map<Node,
     * Double> katz_global_topo_dual = g_topo_dual
     * .getCentralityNodes(CentralitiesDualDual.KATZCENTRALITY_GLOBAL);
     * Map<Node, Double> pagerank_topo_dual = g_topo_dual
     * .getCentralityNodes(CentralitiesDualDual.PAGERANK); Map<Node, Double>
     * clustering_topo_dual = g_topo_dual
     * .getCentralityNodes(CentralitiesDualDual.CLUSTERING); Map<Node, Double>
     * clustering2_topo_dual = g_topo_dual
     * .getCentralityNodes(CentralitiesDualDual.TWOCLUSTERING); Map<Node,
     * Double> global_integration = g_topo_dual
     * .getCentralityNodes(CentralitiesDualDual.GLOBALINTEGRATION); Map<Node,
     * Double> local_integration_2 = g_topo_dual
     * .getCentralityNodes(CentralitiesDualDual.LOCALINTEGRATION_2); Map<Node,
     * Double> local_integration_3 = g_topo_dual
     * .getCentralityNodes(CentralitiesDualDual.LOCALINTEGRATION_3); Map<Node,
     * Double> local_integration_4 = g_topo_dual
     * .getCentralityNodes(CentralitiesDualDual.LOCALINTEGRATION_4); Map<Node,
     * Double> local_integration_5 = g_topo_dual
     * .getCentralityNodes(CentralitiesDualDual.LOCALINTEGRATION_5); try {
     * destDbConnection = IOUtils.createDBConnection(host, port, sourceName,
     * login, password); PreparedStatement stInsertData = destDbConnection
     * .prepareStatement("UPDATE " + tablenameNoeuds +
     * " SET closeness_topo_dual = ?, betweenness_topo_dual = ?," +
     * " degree_topo_dual = ?, control_topo_dual = ?, mean_distance_topo_dual = ?,"
     * +
     * " eigenvector_topo_dual = ?, katz_local_topo_dual = ?, katz_global_topo_dual = ?,"
     * +
     * " pagerank_topo_dual = ?, clustering_topo_dual = ?, clustering_2_topo_dual = ?,"
     * + " global_integration_topo_dual = ?, local_integration_2_topo_dual = ?,"
     * + "local_integration_3_topo_dual = ?,local_integration_4_topo_dual = ?,"
     * + "local_integration_5_topo_dual = ? " +
     * "WHERE ST_DISTANCE(ST_GEOMETRYFROMTEXT(?), the_geom)<0.05"); int
     * nb_transfert = 0; for (Node n : g_topo_dual.getVertices()) {
     * 
     * stInsertData.setDouble(1, closeness_topo_dual.get(n));
     * stInsertData.setDouble(2, betweenness_topo_dual.get(n)); //
     * stInsertData.setDouble(3, degree_topo_dual.get(n));
     * stInsertData.setDouble(4, control_topo_dual.get(n));
     * stInsertData.setDouble(5, mean_distance_topo_dual.get(n));
     * stInsertData.setDouble(6, eigenvector_topo_dual.get(n));
     * stInsertData.setDouble(7, katz_local_topo_dual.get(n));
     * stInsertData.setDouble(8, katz_global_topo_dual.get(n));
     * stInsertData.setDouble(9, pagerank_topo_dual.get(n));
     * stInsertData.setDouble(10, clustering_topo_dual.get(n));
     * stInsertData.setDouble(11, clustering2_topo_dual.get(n));
     * stInsertData.setDouble(12, global_integration.get(n));
     * stInsertData.setDouble(13, local_integration_2.get(n));
     * stInsertData.setDouble(14, local_integration_3.get(n));
     * stInsertData.setDouble(15, local_integration_4.get(n));
     * stInsertData.setDouble(16, local_integration_5.get(n));
     * stInsertData.setString(17, WktGeOxygene.makeWkt(new GM_Point( new
     * DirectPosition(n.getX(), n.getY()))));
     * 
     * stInsertData.addBatch(); nb_transfert++; if (nb_transfert % 50 == 0) {
     * int[] status = new int[50]; try { status = stInsertData.executeBatch(); }
     * catch (SQLException e) { e.getNextException().printStackTrace(); } } }
     * int[] status = new int[nb_transfert]; status =
     * stInsertData.executeBatch(); } catch (SQLException e) {
     * e.printStackTrace(); } catch (Exception e) { e.printStackTrace(); }
     * finally { IOUtils.closeDBConnection(destDbConnection); }
     * 
     * Map<Edge, Double> closeness_topo_dual_arcs = g_topo_dual
     * .getCentralityEdges(closeness_topo_dual); Map<Edge, Double>
     * betweenness_topo_dual_arcs = g_topo_dual
     * .getCentralityEdges(betweenness_topo_dual); Map<Edge, Double>
     * degree_topo_dual_arcs = g_topo_dual
     * .getCentralityEdges(degree_topo_dual); Map<Edge, Double>
     * control_topo_dual_arcs = g_topo_dual
     * .getCentralityEdges(control_topo_dual); Map<Edge, Double>
     * mean_distance_topo_dual_arcs = g_topo_dual
     * .getCentralityEdges(mean_distance_topo_dual); Map<Edge, Double>
     * eigenvector_topo_dual_arcs = g_topo_dual
     * .getCentralityEdges(eigenvector_topo_dual); Map<Edge, Double>
     * katz_local_topo_dual_arcs = g_topo_dual
     * .getCentralityEdges(katz_local_topo_dual); Map<Edge, Double>
     * katz_global_topo_dual_arcs = g_topo_dual
     * .getCentralityEdges(katz_global_topo_dual); Map<Edge, Double>
     * pagerank_topo_dual_arcs = g_topo_dual
     * .getCentralityEdges(pagerank_topo_dual); Map<Edge, Double>
     * clustering_topo_dual_arcs = g_topo_dual
     * .getCentralityEdges(clustering_topo_dual); Map<Edge, Double>
     * clustering2_topo_dual_arcs = g_topo_dual
     * .getCentralityEdges(clustering2_topo_dual); Map<Edge, Double>
     * global_integration_topo_dual_arcs = g_topo_dual
     * .getCentralityEdges(global_integration); Map<Edge, Double>
     * local_integration_2_topo_dual_arcs = g_topo_dual
     * .getCentralityEdges(local_integration_2); Map<Edge, Double>
     * local_integration_3_topo_dual_arcs = g_topo_dual
     * .getCentralityEdges(local_integration_3); Map<Edge, Double>
     * local_integration_4_topo_dual_arcs = g_topo_dual
     * .getCentralityEdges(local_integration_3); Map<Edge, Double>
     * local_integration_5_topo_dual_arcs = g_topo_dual
     * .getCentralityEdges(local_integration_4);
     * 
     * try { destDbConnection = IOUtils.createDBConnection(host, port,
     * sourceName, login, password); PreparedStatement stInsertData =
     * destDbConnection .prepareStatement("UPDATE " + tablenameArcs +
     * " SET closeness_topo_dual = ?, betweenness_topo_dual = ?," +
     * " degree_topo_dual = ?, control_topo_dual = ?, mean_distance_topo_dual = ?,"
     * +
     * " eigenvector_topo_dual = ?, katz_local_topo_dual = ?, katz_global_topo_dual = ?,"
     * +
     * " pagerank_topo_dual = ?, clustering_topo_dual = ?, clustering_2_topo_dual = ?,"
     * + " global_integration_topo_dual = ?, local_integration_2_topo_dual = ?,"
     * + "local_integration_3_topo_dual = ?,local_integration_4_topo_dual = ?,"
     * + "local_integration_5_topo_dual = ? " +
     * "WHERE ST_CONTAINS(ST_BUFFER( ST_GEOMETRYFROMTEXT(?), 0.05), the_geom)");
     * int nb_transfert = 0; for (Edge n :
     * g_topo_dual.getMappingStrokesNodes().keySet()) {
     * 
     * stInsertData.setDouble(1, closeness_topo_dual_arcs.get(n));
     * stInsertData.setDouble(2, betweenness_topo_dual_arcs.get(n));
     * stInsertData.setDouble(3, degree_topo_dual_arcs.get(n));
     * stInsertData.setDouble(4, control_topo_dual_arcs.get(n));
     * stInsertData.setDouble(5, mean_distance_topo_dual_arcs.get(n));
     * stInsertData.setDouble(6, eigenvector_topo_dual_arcs.get(n));
     * stInsertData.setDouble(7, katz_local_topo_dual_arcs.get(n));
     * stInsertData.setDouble(8, katz_global_topo_dual_arcs.get(n));
     * stInsertData.setDouble(9, pagerank_topo_dual_arcs.get(n));
     * stInsertData.setDouble(10, clustering_topo_dual_arcs.get(n));
     * stInsertData.setDouble(11, clustering2_topo_dual_arcs.get(n));
     * stInsertData.setDouble(12, global_integration_topo_dual_arcs.get(n));
     * stInsertData.setDouble(13, local_integration_2_topo_dual_arcs.get(n));
     * stInsertData.setDouble(14, local_integration_3_topo_dual_arcs.get(n));
     * stInsertData.setDouble(15, local_integration_4_topo_dual_arcs.get(n));
     * stInsertData.setDouble(16, local_integration_5_topo_dual_arcs.get(n));
     * 
     * IDirectPositionList l = new DirectPositionList(); for (Point2D.Double pt
     * : n.coords()) { l.add(new DirectPosition(pt.getX(), pt.getY())); }
     * stInsertData.setString(17, WktGeOxygene.makeWkt(new GM_LineString(l)));
     * 
     * stInsertData.addBatch(); nb_transfert++; if (nb_transfert % 50 == 0) {
     * int[] status = new int[50]; try { status = stInsertData.executeBatch(); }
     * catch (SQLException e) { e.getNextException().printStackTrace(); } } }
     * int[] status = new int[nb_transfert]; status =
     * stInsertData.executeBatch(); } catch (SQLException e) {
     * e.printStackTrace(); } catch (Exception e) { e.printStackTrace(); }
     * finally { IOUtils.closeDBConnection(destDbConnection); }
     */

    /*
     * les faces
     */

    // les faces

    IFeatureCollection<IFeature> inputFeatureCollectionCorrected = new Population<IFeature>();
    for (IFeature feat : g.getPop()) {
      if (feat.getGeom() instanceof IMultiCurve) {
        for (int i = 0; i < ((IMultiCurve<?>) feat.getGeom()).size(); i++) {
          inputFeatureCollectionCorrected.add(new DefaultFeature(
              ((IMultiCurve<?>) feat.getGeom()).get(i)));
        }
      } else {
        inputFeatureCollectionCorrected.add(new DefaultFeature(feat.getGeom()));
      }
    }
    CarteTopo map = CarteTopoFactory
        .newCarteTopo(inputFeatureCollectionCorrected);
    map.creeTopologieFaces();
    Map<Face, Double> form_factor = new HashMap<Face, Double>();
    Map<Face, Double> elongation = new HashMap<Face, Double>();
    Map<Face, Double> convexityRate = new HashMap<Face, Double>();

    for (Face f : map.getListeFaces()) {
      form_factor.put(f, FormFactor.mesure(f.getGeom()));
      elongation.put(f, FeatureElongation.mesure(f.getGeom()));
      convexityRate.put(f, FeatureConvexityRate.mesure(f.getGeom()));
    }

    String tablename = "ind_spatial_poubelle_faces";

    try { // initialisation des connexions aux bases (source puis dest)
      destDbConnection = IOUtils.createDBConnection(host, port, sourceName,
          login, password);
      PreparedStatement stInsertData = destDbConnection
          .prepareStatement("INSERT INTO " + tablename
              + "(the_geom, centroid, form_factor, convexity_rate, elongation)"
              + " VALUES (ST_geomfromtext(?), ST_geomfromtext(?), ?, ?, ?)"); // compteur
      // de réponses à la première requète
      int nb_transfert = 0;

      for (Face f : map.getListeFaces()) {

        // on indique que le premier "?" est renseigné par l'entier "idbati" //
        // lu de la précédente requète

        stInsertData.setString(1, WktGeOxygene.makeWkt(f.getGeom()));
        stInsertData.setString(2,
            WktGeOxygene.makeWkt(new GM_Point(f.getGeom().centroid())));
        stInsertData.setDouble(3, form_factor.get(f));
        stInsertData.setDouble(4, convexityRate.get(f));
        stInsertData.setDouble(5, elongation.get(f));

        stInsertData.addBatch();

        // increment du compteur nb_transfert++;

        // uncomment this part in case of big data
        if (nb_transfert % 50 == 0) {
          // vide le buffer de requètes
          int[] status = new int[50];
          try {
            status = stInsertData.executeBatch();
          } catch (SQLException ee) {
            ee.getNextException().printStackTrace();
          }

        }

      }

      // initialise le tableau des codes de retour pour chaque requète. int[]
      int[] status = new int[nb_transfert]; // execute l'insert multiple (tout
                                            // le
      // buffer de requètes est lu) // NOTE : lorsque beaucoup de géométries
      // (les
      // contours MULTIPOLYGON des // communes par exemples) sont dans le buffer
      // celui ci devient vite trop gros pour la taille de la JVM - // il ne
      // faut pas hésiter à vider ce buffer (par exécution) toutes les 50 //
      // communes par exemple
      status = stInsertData.executeBatch();

    } catch (SQLException e) { // afficher la stack trace pour comprendre
      // l'erreu
      e.printStackTrace();
    } catch (Exception e) {
    }// TODO
     // Auto-generated
    finally { // on passe
      // toujours dans un finally, qu'il y ait eu Exception ou non. // ceci
      // garantit de toujours fermer une base après l'avoir ouvert // autrement
      // on
      // peut se retrouver à bouffer toutes les ressources de sa // base // (et
      // c'est pas drole quand vous avez excédé les 100 connections max //
      // simultanées possibles)
      IOUtils.closeDBConnection(destDbConnection);
    }

  }
}
