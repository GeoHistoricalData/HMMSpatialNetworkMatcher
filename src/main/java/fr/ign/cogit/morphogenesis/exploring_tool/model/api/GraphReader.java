package fr.ign.cogit.morphogenesis.exploring_tool.model.api;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

import org.apache.log4j.Logger;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class GraphReader {

  private static Logger logger = Logger.getLogger(GraphReader.class);

  static String createGeometricalGraph = "Création du graphe géométrique ..."; //$NON-NLS-1$
  static String createGeometricalGraphOk = "Graphe géométrique créé."; //$NON-NLS-1$

  /**
   * @param thresold positif ou nul, on applique les corrections topologiques
   *          usuelles (rendre le graphe planaire, etc.) avec la précision
   *          thresold. Sinon on ne fait rien.
   * 
   */
  public static GeometricalGraph createGeometricalGraph(String file,
      double thresold) {

    IPopulation<IFeature> pop = ShapefileReader.read(file);
    return GraphReader.createGeometricalGraph(pop, thresold);

  }

  /**
   * @param thresold positif ou nul, on applique les corrections topologiques
   *          usuelles (rendre le graphe planaire, etc.) avec la précision
   *          thresold. Sinon on ne fait rien.
   * 
   */
  public static GeometricalGraph createGeometricalGraph(
      IFeatureCollection<IFeature> pop, double thresold) {

    if (logger.isInfoEnabled()) {
      logger.info(createGeometricalGraph);
    }

    GeometricalGraph graph = new GeometricalGraph();
    graph.setPop(pop);
    graph.getPop().initSpatialIndex(Tiling.class, true);

    CarteTopo map = new CarteTopo("void"); //$NON-NLS-1$
    IPopulation<Arc> arcs = map.getPopArcs();
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
    map.creeNoeudsManquants(thresold);
    map.rendPlanaire(thresold);
    map.fusionNoeuds(thresold);
    map.filtreArcsDoublons();

    // On créer les arcs et les noeuds

    for (Arc arc : map.getListeArcs()) {

      if (arc.getNoeudIni().getSortants().size()
          + arc.getNoeudIni().getEntrants().size() == 1
          && arc.getNoeudFin().getSortants().size()
              + arc.getNoeudFin().getEntrants().size() == 1) {
        continue;
      }
      double xini = (new Double(arc.getNoeudIni().getCoord().getX()))
          .doubleValue();
      double yini = (new Double(arc.getNoeudIni().getCoord().getY()))
          .doubleValue();
      double xfin = (new Double(arc.getNoeudFin().getCoord().getX()))
          .doubleValue();
      double yfin = (new Double(arc.getNoeudFin().getCoord().getY()))
          .doubleValue();

      Node node1 = new Node(xini, yini);
      Node node2 = new Node(xfin, yfin);

      List<Point2d> l = new ArrayList<Point2d>();
      for (IDirectPosition p : arc.getGeom().coord()) {
        double x = (new Double(p.getX())).doubleValue();
        double y = (new Double(p.getY())).doubleValue();
        l.add(new Point2d(x, y));
      }

      Edge edge = new Edge(l);
      edge.setFisrt(node1);
      edge.setLast(node2);
      graph.addEdge(edge, node1, node2);

    }

    if (logger.isInfoEnabled()) {
      logger.info(createGeometricalGraphOk);
    }

    map.nettoyer();
    System.gc();

    return graph;
  }
}
