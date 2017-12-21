package fr.ign.cogit.morphogenesis.network.graph.io;

import java.awt.geom.Point2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.graph.util.Pair;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopoFactory;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.WktGeOxygene;
import fr.ign.cogit.geoxygene.util.index.Tiling;
import fr.ign.cogit.morphogenesis.network.graph.DualTopologicalGraph;
import fr.ign.cogit.morphogenesis.network.graph.Edge;
import fr.ign.cogit.morphogenesis.network.graph.GeometricalGraph;
import fr.ign.cogit.morphogenesis.network.graph.Node;
import fr.ign.cogit.morphogenesis.network.graph.TopologicalGraph;
import fr.ign.cogit.morphogenesis.network.graph.social.Individu;
import fr.ign.cogit.morphogenesis.network.graph.social.Relation;
import fr.ign.cogit.morphogenesis.network.graph.social.SocialGraph;
import geocodage.IOUtils;

public class GraphReader {

  private static Logger logger = Logger.getLogger(GraphReader.class);

  /**
   * @param thresold positif ou nul, on applique les corrections topologiques
   *          usuelles (rendre le graphe planaire, etc.) avec la précision
   *          thresold. Sinon on ne fait rien.
   * 
   */
  public static GeometricalGraph createGeometricalGraph(String file,
      double thresold) {

    if (logger.isInfoEnabled()) {
      logger.info("Création du graphe géométrique ...");
    }

    GeometricalGraph graph = new GeometricalGraph();

    CarteTopo map = null;
    IPopulation<IFeature> pop = ShapefileReader.read(file);
    // on cast les multi_line_string
    IFeatureCollection<IFeature> inputFeatureCollectionCorrected = new Population<IFeature>();
    // On simplifie les multi line string et on passe par des localFeature
    // pour conserver les attributs
    for (IFeature feat : pop) {
      if (feat.getGeom() instanceof IMultiCurve) {
        for (int i = 0; i < ((IMultiCurve<?>) feat.getGeom()).size(); i++) {
          inputFeatureCollectionCorrected.add(new DefaultFeature(
              ((IMultiCurve<?>) feat.getGeom()).get(i)));
        }
      } else {
        inputFeatureCollectionCorrected.add(new DefaultFeature(feat.getGeom()));
      }
    }

    graph.setPop(pop);

    // Correction topologiques
    if (thresold >= 0) {
      map = CarteTopoFactory.newCarteTopo("", inputFeatureCollectionCorrected,
          thresold, false);
      map.filtreDoublons(thresold);
      map.filtreArcsDoublons();
      map.fusionNoeuds(thresold);
      map.filtreNoeudsSimples();
      map.filtreNoeudsIsoles();
    } else {
      map = CarteTopoFactory.newCarteTopo(inputFeatureCollectionCorrected);
    }

    List<Edge> listEdges = new ArrayList<Edge>();
    List<Node> listNodes = new ArrayList<Node>();
    for (Arc arc : map.getListeArcs()) {
      Edge edge = new Edge(arc.getGeom().coord());
      listEdges.add(edge);
    }
    for (Noeud n : map.getListeNoeuds()) {
      Node node = new Node(n.getCoord().getX(), n.getCoord().getY());
      listNodes.add(node);
    }

    for (Noeud n : map.getListeNoeuds()) {
      Node node = new Node(n.getCoord().getX(), n.getCoord().getY());
      listNodes.add(node);
    }

    // On créer les arcs et les noeuds

    for (Edge edge : listEdges) {
      Node node1 = new Node(edge.coords().get(0).getX(), edge.coords().get(0)
          .getY());
      Node node2 = new Node(edge.coords().get(edge.coords().size() - 1).getX(),
          edge.coords().get(edge.coords().size() - 1).getY());

      for (Node n : listNodes) {
        if (n.equals(node1)) {
          node1 = n;
        }
        if (n.equals(node2)) {
          node2 = n;
        }
      }

      edge.setFisrt(node1);
      edge.setLast(node2);

      graph.addEdge(edge, node1, node2);

    }

    // on enlève les strokes isolés
    List<Node> toRemove = new ArrayList<Node>();
    for (Edge edge : graph.getEdges()) {
      Pair<Node> nodes = graph.getEndpoints(edge);
      if (graph.getNeighborCount(nodes.getFirst())
          + graph.getNeighborCount(nodes.getSecond()) == 2) {
        toRemove.add(nodes.getFirst());
        toRemove.add(nodes.getSecond());
      }
    }
    for (Node geom : toRemove) {
      graph.removeVertex(geom);
    }

    if (logger.isInfoEnabled()) {
      logger.info("Graphe géométrique créé.");
    }
    return graph;
  }

  public static TopologicalGraph createTopologicalGraph(String file) {

    if (logger.isInfoEnabled()) {
      logger.info("Création du graphe topologique ...");
    }

    TopologicalGraph graph = new TopologicalGraph();
    IPopulation<IFeature> pop = ShapefileReader.read(file);

    graph.setPop(pop);

    List<Edge> listEdges = new ArrayList<Edge>();
    for (IFeature feat : pop) {
      Edge edge = new Edge(feat.getGeom().coord());
      listEdges.add(edge);
    }

    List<IFeature> alreadyDone = new ArrayList<IFeature>();

    pop.initSpatialIndex(Tiling.class, false);

    for (IFeature feat : pop) {
      alreadyDone.add(feat);
      Collection<IFeature> candidats = pop.select(feat.getGeom(), 5);
      for (IFeature feat2 : candidats) {
        if (feat.equals(feat2) || alreadyDone.contains(feat2)) {
          continue;
        }
        if (feat.getGeom().intersects(feat2.getGeom())) {
          // les arcs s'intersectent
          Edge edge1 = listEdges.get(listEdges.indexOf(new Edge(feat.getGeom()
              .coord())));
          Edge edge2 = listEdges.get(listEdges.indexOf(new Edge(feat2.getGeom()
              .coord())));

          // Un noeud fictif à l'intersection des deux
          String n = edge1.toString() + ", " + edge2.toString();
          graph.addEdge(n, edge1, edge2);

        }
      }

    }

    if (logger.isInfoEnabled()) {
      logger.info("Graphe topologique créé.");
    }
    return graph;
  }

  public static DualTopologicalGraph createDualTopologicalGraph(String file) {

    if (logger.isInfoEnabled()) {
      logger.info("Création du graphe dual topologique ...");
    }

    DualTopologicalGraph graph = new DualTopologicalGraph();
    IPopulation<IFeature> pop = ShapefileReader.read(file);
    graph.setPop(pop);

    IFeatureCollection<IFeature> inputFeatureCollectionCorrected = new Population<IFeature>();

    for (IFeature feat : pop) {
      if (feat.getGeom() instanceof IMultiCurve) {
        for (int i = 0; i < ((IMultiCurve<?>) feat.getGeom()).size(); i++) {
          inputFeatureCollectionCorrected.add(new DefaultFeature(
              ((IMultiCurve<?>) feat.getGeom()).get(i)));
        }
      } else {
        inputFeatureCollectionCorrected.add(new DefaultFeature(feat.getGeom()));
      }
    }

    graph.setPop(pop);

    CarteTopo ct = new CarteTopo("void"); //$NON-NLS-1$
    IPopulation<Arc> arcs = ct.getPopArcs();
    logger.debug("Creating the edges..."); //$NON-NLS-1$
    for (IFeature feature : pop) {
      Arc arc = arcs.nouvelElement();
      try {
        GM_LineString line = new GM_LineString(feature.getGeom().coord());
        arc.setGeometrie(line);
        arc.addCorrespondant((IFeature) feature);
      } catch (ClassCastException e) {
        e.printStackTrace();
      }
    }
    logger.debug("CarteTopo operations..."); //$NON-NLS-1$
    ct.creeNoeudsManquants(0.1);
    ct.filtreArcsDoublons();
    ct.filtreNoeudsIsoles();
    ct.rendPlanaire(0.1);
    ct.filtreDoublons(0.1);
    ct.filtreArcsDoublons();

    pop.initSpatialIndex(Tiling.class, false);

    List<Node> nodes = new ArrayList<Node>();
    for (Noeud n : ct.getListeNoeuds()) {
      nodes.add(new Node(n.getCoord().getX(), n.getCoord().getY()));

    }

    Map<Edge, List<Node>> mappingStrokesNodes = new HashMap<Edge, List<Node>>();

    for (IFeature stroke : pop) {
      List<Node> nodesStroke = new ArrayList<Node>();
      for (Node n : nodes) {
        if (stroke.getGeom().distance(
            new GM_Point(new DirectPosition(n.getX(), n.getY()))) < 0.0005) {
          if (!nodesStroke.contains(n)) {
            nodesStroke.add(n);
          }
        }
      }
      mappingStrokesNodes.put(new Edge(stroke.getGeom().coord()), nodesStroke);
      // on a tous les noeuds du stroke
      for (Node n1 : nodesStroke) {
        for (Node n2 : nodesStroke) {
          if (!n1.equals(n2)) {
            String edge = "Edge : " + n1.toString() + " , " + n2.toString();
            graph.addEdge(edge, n1, n2);
          }
        }
      }
    }

    if (logger.isInfoEnabled()) {
      logger.info("Graphe dual topologique créé.");
    }
    graph.setMappingStrokesNodes(mappingStrokesNodes);
    return graph;
  }

  public static SocialGraph createSocialGraph(String host, String user,
      String password, String port, String dbname, String tableIndividus,
      String tableRelations, String attIdCompConnexe) throws SQLException {

    SocialGraph g = new SocialGraph();
    Connection destDbConnection = null;

    try { // initialisation des connexions aux bases (source puis dest)
      destDbConnection = IOUtils.createDBConnection(host, port, dbname, user,
          password);

      // les individus
      Map<Integer, Individu> mappingIndividus = new HashMap<Integer, Individu>();

      PreparedStatement stSelectData = destDbConnection
          .prepareStatement("SELECT id_individu, nom, prenom, profession, num_adr, nom_adr,num_complement, "
              + "qualite, sexe, "
              + attIdCompConnexe
              + " FROM "
              + tableIndividus);
      ResultSet query = null;
      query = stSelectData.executeQuery();

      int cpt = 0;
      if (query != null) {
        while (query.next()) {
          int id = query.getInt("id_individu");
          String nom = query.getString("nom");
          String prenom = query.getString("prenom");
          String profession = query.getString("profession");
          String num_adr = query.getString("num_adr");
          String nom_adr = query.getString("nom_adr");
          String num_complement = query.getString("num_complement");
          String qualite = query.getString("qualite");
          String sexe = query.getString("sexe");
          int id_composante_connexe = query.getInt(attIdCompConnexe);

          /*
           * String method = query.getString("method");
           * 
           * IGeometry gg = WktGeOxygene.makeGeOxygene(query.getString("geom"));
           * Point2D pt = new Point2D.Double(gg.coord().get(0).getX(),
           * gg.coord() .get(0).getY());
           */

          Individu ind = new Individu(id, nom, prenom, profession, num_adr,
              nom_adr, num_complement, qualite, sexe, "", null);
          ind.setIdConnectedComponent(id_composante_connexe);
          mappingIndividus.put(id, ind);

          cpt++;
        }
      }

      stSelectData = destDbConnection
          .prepareStatement("SELECT id_individu1, id_individu2, type_lien, id_lien, "
              + attIdCompConnexe + " FROM " + tableRelations);
      query = null;
      query = stSelectData.executeQuery();
      if (query != null) {
        while (query.next()) {
          int id = query.getInt("id_lien");
          int id_ind1 = query.getInt("id_individu1");
          int id_ind2 = query.getInt("id_individu2");
          String type = query.getString("type_lien");
          int id_composante_connexe = query.getInt(attIdCompConnexe);

          Relation lien = new Relation(id, mappingIndividus.get(id_ind1),
              mappingIndividus.get(id_ind2), type);
          lien.setIdConnectedComponent(id_composante_connexe);

          g.addEdge(lien, lien.getFirst(), lien.getSecond());

        }
      }

      // Les relations

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeDBConnection(destDbConnection);
    }

    System.out.println("vertices : " + g.getVertexCount() + " ; edges : "
        + g.getEdgeCount());

    return g;
  }

  public static SocialGraph createSocialGraphGeocoded(String host, String user,
      String password, String port, String dbname, String tableIndividus,
      String tableRelations, String attIdCompConnexe) throws SQLException {

    SocialGraph g = new SocialGraph();
    Connection destDbConnection = null;

    try { // initialisation des connexions aux bases (source puis dest)
      destDbConnection = IOUtils.createDBConnection(host, port, dbname, user,
          password);

      // les individus
      Map<Integer, Individu> mappingIndividus = new HashMap<Integer, Individu>();

      PreparedStatement stSelectData = destDbConnection
          .prepareStatement("SELECT id_individu, nom, prenom, profession, num_adr, nom_adr,num_complement, "
              + "qualite, sexe, ST_ASTEXT(the_geom) as the_geom , "
              + attIdCompConnexe + " FROM " + tableIndividus);
      ResultSet query = null;
      query = stSelectData.executeQuery();

      int cpt = 0;
      if (query != null) {
        while (query.next()) {
          int id = query.getInt("id_individu");
          String nom = query.getString("nom");
          String prenom = query.getString("prenom");
          String profession = query.getString("profession");
          String num_adr = query.getString("num_adr");
          String nom_adr = query.getString("nom_adr");
          String num_complement = query.getString("num_complement");
          String qualite = query.getString("qualite");
          String sexe = query.getString("sexe");
          IGeometry geom = WktGeOxygene.makeGeOxygene(query
              .getString("the_geom"));
          int id_composante_connexe = query.getInt(attIdCompConnexe);

          /*
           * String method = query.getString("method");
           * 
           * IGeometry gg = WktGeOxygene.makeGeOxygene(query.getString("geom"));
           * Point2D pt = new Point2D.Double(gg.coord().get(0).getX(),
           * gg.coord() .get(0).getY());
           */

          Individu ind = new Individu(id, nom, prenom, profession, num_adr,
              nom_adr, num_complement, qualite, sexe, "", null);
          ind.setIdConnectedComponent(id_composante_connexe);
          Point2D pt = new Point2D.Double();
          pt.setLocation(geom.coord().get(0).getX(), geom.coord().get(0).getY());
          ind.setPosition(pt);
          mappingIndividus.put(id, ind);

          cpt++;
        }
      }

      stSelectData = destDbConnection
          .prepareStatement("SELECT id_individu1, id_individu2, type_lien, id_lien, "
              + attIdCompConnexe + " FROM " + tableRelations);
      query = null;
      query = stSelectData.executeQuery();
      if (query != null) {
        while (query.next()) {
          int id = query.getInt("id_lien");
          int id_ind1 = query.getInt("id_individu1");
          int id_ind2 = query.getInt("id_individu2");
          String type = query.getString("type_lien");
          int id_composante_connexe = query.getInt(attIdCompConnexe);

          Relation lien = new Relation(id, mappingIndividus.get(id_ind1),
              mappingIndividus.get(id_ind2), type);
          lien.setIdConnectedComponent(id_composante_connexe);

          g.addEdge(lien, lien.getFirst(), lien.getSecond());

        }
      }

      // Les relations

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeDBConnection(destDbConnection);
    }

    System.out.println("vertices : " + g.getVertexCount() + " ; edges : "
        + g.getEdgeCount());

    return g;
  }

  public static void main(String args[]) {
    DualTopologicalGraph g = GraphReader
        .createDualTopologicalGraph("/media/Data/Benoit/these/analyses/centralites/strokes_L93_ebf/1810_1836_VASSEROT_strokes.shp");

    /*
     * Map<Node, Double> cc = g.getCentrality(CentralitiesDualDual.CLOSENESS);
     * Map<Node, Double> bc = g.getCentrality(CentralitiesDualDual.BETWEENNESS);
     * Map<Node, Double> cluster = g
     * .getCentrality(CentralitiesDualDual.CLUSTERING); Map<Node, Double>
     * control = g.getCentrality(CentralitiesDualDual.CONTROL); Map<Node,
     * Double> dc = g.getCentrality(CentralitiesDualDual.DEGREE); Map<Node,
     * Double> inte = g .getCentrality(CentralitiesDualDual.GLOBALINTEGRATION);
     */

    /*
     * Map<Node, Double> md =
     * g.getCentrality(CentralitiesDualDual.MEANDISTANCE); Map<Node, Double> pr
     * = g.getCentrality(CentralitiesDualDual.PAGERANK); Map<Node, Double>
     * cluster2 = g .getCentrality(CentralitiesDualDual.TWOCLUSTERING);
     */

    String se = "CC;BC;cluster;cnt;dc;int;loc;md;pr;cluster2\n";
    /*
     * for (Node s : g.getVertices()) { se += cc.get(s) + ";" + bc.get(s) + ";"
     * + cluster.get(s) + ";" + control.get(s) + ";" + dc.get(s) + ";" +
     * inte.get(s) + ";" + loc.get(s) + ";" + md.get(s) + ";" + pr.get(s) + ";"
     * + cluster2.get(s) + "\n"; } try { FileWriter fr = new
     * FileWriter("/home/bcostes/Bureau/tmp/dual_dual.txt"); BufferedWriter br =
     * new BufferedWriter(fr); br.write(se);
     * 
     * } catch (IOException e) { // TODO Auto-generated catch block
     * e.printStackTrace(); }
     */

  }
}
