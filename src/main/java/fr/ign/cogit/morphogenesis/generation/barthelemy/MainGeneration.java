package fr.ign.cogit.morphogenesis.generation.barthelemy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IAggregate;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_Aggregate;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class MainGeneration {

  // TODO : delete
  public static int cpt = 1;
  public static IPopulation<IFeature> popNodes = new Population<IFeature>();

  public static void run() {

    // Liste des centers
    List<Center> centers = new ArrayList<Center>();

    // le réseau
    // Graph<Point, ILineString> network = new UndirectedSparseMultigraph<Point,
    // ILineString>();

    IPopulation<IFeature> network = new Population<IFeature>();

    // initialisation
    initialize(centers, network);

    export(network, Parameters.OUTPUT_IMAGES + "output" + 0 + ".shp");

    int time = 1;
    int cpt_images = 0;

    while (true) {

      /*
       * System.out.println("ITERATION : " + time);
       * System.out.println("CENTERS : " + centers.size());
       * System.out.println("network.size() : " + network.size());
       */

      // Génération de nouveaux centers ?
      if (time % Parameters.NEW_CENTERS_DELAY == 0) {
        grow_window();
        MainGeneration.addNewCenters(centers);

        Graph2Image.export(network, Parameters.OUTPUT_IMAGES + "output"
            + cpt_images + ".png");

        if (Parameters.NEW_CENTERS_COUNT >= Parameters.CENTERS_MAX) {
          break;
        }
        cpt_images++;

        export(network, Parameters.OUTPUT_IMAGES + "output" + cpt_images
            + ".shp");

        /*
         * export(centers, Parameters.OUTPUT_IMAGES + "output_" + cpt_images +
         * ".shp");
         */
      }

      // Liste des noeud du réseau les plus proches des centers
      // un noeud peut etre affecté par plusieurs centers
      Map<IDirectPosition, List<Center>> map = new HashMap<IDirectPosition, List<Center>>();

      for (Center center : centers) {
        if (!center.isActivated()) {
          continue;
        }

        List<IDirectPosition> closestPoint = new ArrayList<IDirectPosition>();
        List<IGeometry> ll = new ArrayList<IGeometry>();
        for (IFeature feat : network) {
          ll.add(feat.getGeom());
        }
        IAggregate<IGeometry> aggregate = new GM_Aggregate<IGeometry>(ll);

        closestPoint = GeometryUtils.projection(
            new DirectPosition(center.getX(), center.getY()), aggregate);

        filter(center, centers, map, network, closestPoint);

        if (closestPoint.isEmpty()) {
          center.isActivated(false);
          continue;
        }

        for (IDirectPosition point : closestPoint) {

          if (map.containsKey(point)) {
            map.get(point).add(center);
          } else {
            List<Center> l = new ArrayList<Center>();
            l.add(center);
            map.put(point, l);
          }
        }

        /*
         * for (Point2D node : network.getVertices()) { // le noeud est il
         * confondu avec le center ? if (center.equals(node)) { continue; } //
         * node est -il dans le voisinage du centre? double dist =
         * node.distanceSq(center); boolean nodeActivated = true; for (Point2D
         * otherNode : network.getVertices()) { if (node.equals(otherNode)) {
         * continue; } double d = Math.max(otherNode.distanceSq(center),
         * otherNode.distanceSq(node)); if (d < dist) { nodeActivated = false;
         * break; } } for (Center otherCenter : centers) { if
         * (center.equals(otherCenter)) { continue; } double d =
         * Math.max(otherCenter.distanceSq(center),
         * otherCenter.distanceSq(node)); if (d < dist) { nodeActivated = false;
         * break; } } if (!nodeActivated) { // le noeud node n'est pas dans le
         * voisinage du center continue; } nbNodeActivated++; if
         * (map.containsKey(node)) { map.get(node).add(center); } else {
         * List<Center> l = new ArrayList<Center>(); l.add(center);
         * map.put(node, l); } }
         */
        /*
         * if (nbNodeActivated == 0) { // TODO: les center peuvent il se ranimer
         * ? // le center n'est plus activé center.isActivated(false); }
         */

      }

      // Maintenant, on va créer des arcs ...
      for (IDirectPosition node : map.keySet()) {
        // pour chaque noeud du réseau activé
        if (map.get(node).size() == 1) {
          // un seul center concerné !
          joinOneCenter(map.get(node).get(0), node, network);
        } else {
          // plusieurs centers
          network.addAll(LocalOptimizationPrinciple.optimize(node,
              map.get(node), popNodes));
        }
      }

      time++;
    }

    // exort
    Graph2Image.export(network, Parameters.OUTPUT_IMAGES + "output_"
        + cpt_images + ".png");
  }

  private static void filter(Center center, List<Center> centers,
      Map<IDirectPosition, List<Center>> map, IPopulation<IFeature> network,
      List<IDirectPosition> closestPoints) {

    /*
     * Collection<IFeature> selection = popNodes.select( new
     * GM_Point(closestPoint), Parameters.DIST_MIN); if (!selection.isEmpty()) {
     * if (selection.size() == 1) { closestPoint = (DirectPosition)
     * selection.iterator().next().getGeom() .coord().get(0); } else {
     * Iterator<IFeature> it = selection.iterator(); IFeature fmin = it.next();
     * double dmin = fmin.getGeom().distance(new GM_Point(closestPoint)); while
     * (it.hasNext()) { IFeature f = it.next(); double d =
     * f.getGeom().distance(new GM_Point(closestPoint)); if (d < dmin) { d =
     * dmin; fmin = f; } } closestPoint = (DirectPosition)
     * fmin.getGeom().coord().get(0); }
     * 
     * return closestPoint; }
     */

    // on ne garde que ceux dans le voisinage
    List<IDirectPosition> tmp = new ArrayList<IDirectPosition>();
    IDirectPosition centerPoint = new DirectPosition(center.getX(),
        center.getY());
    for (IDirectPosition point : closestPoints) {
      if (point.equals(centerPoint)) {
        continue;
      }

      double dist = point.distance(centerPoint);
      IGeometry buf1 = (new GM_Point(point)).buffer(dist);
      IGeometry buf2 = (new GM_Point(centerPoint)).buffer(dist);
      IGeometry bufIntersection = buf1.intersection(buf2);

      boolean isInNeighbourhood = true;
      for (Center otherCenter : centers) {
        if (otherCenter.equals(center)) {
          continue;
        }
        if (bufIntersection.contains(new GM_Point(new DirectPosition(
            otherCenter.getX(), otherCenter.getY())))) {
          isInNeighbourhood = false;
          break;
        }
      }

      if (!isInNeighbourhood) {
        continue;
      }
      // c'est bon pour les centers, on test le réseau
      Collection<IFeature> selection = network.select(
          (new GM_Point(centerPoint)), dist + 1);
      if (!selection.isEmpty()) {
        for (IFeature feat : selection) {
          if (!feat.getGeom().intersects(bufIntersection)) {
            continue;
          }
          IGeometry intersection = bufIntersection.intersection(feat.getGeom());
          if (!intersection.equals(new GM_Point(point))) {
            isInNeighbourhood = false;
            break;
          }
        }
      }

      if (!isInNeighbourhood) {
        continue;
      }
      if (!tmp.isEmpty()) {
        List<IDirectPosition> tmp2 = new ArrayList<IDirectPosition>();
        for (IDirectPosition otherPoint : tmp) {
          if (otherPoint.distance(point) < Parameters.DIST_MIN) {
            double d = otherPoint.distance(centerPoint);
            if (d < dist) {
              tmp2.add(otherPoint);
              isInNeighbourhood = false;
            }
          } else {
            tmp2.add(otherPoint);
          }
        }
        if (isInNeighbourhood) {
          tmp2.add(point);
        }
        tmp.clear();
        tmp.addAll(tmp2);
      } else {
        tmp.add(point);
      }
    }

    closestPoints.clear();
    closestPoints.addAll(tmp);

    // on regarde si on a pas déja un point près
    if (!map.isEmpty()) {
      for (int i = 0; i < closestPoints.size(); i++) {
        IDirectPosition point = closestPoints.get(i);
        for (IDirectPosition pt : map.keySet()) {
          if (pt.distance(point) < Parameters.DIST_MIN) {
            closestPoints.set(i, pt);
            break;
          }
        }
      }
    }
    return;

  }

  private static void joinOneCenter(Center center, IDirectPosition node,
      IPopulation<IFeature> network) {

    IDirectPositionList list = new DirectPositionList();
    list.add(node);
    list.add(new DirectPosition(center.getX(), center.getY()));
    network.add(new DefaultFeature(new GM_LineString(list)));

    GM_Point pt1 = new GM_Point(network.get(network.size() - 1).getGeom()
        .coord().get(0));
    GM_Point pt2 = new GM_Point(network.get(network.size() - 1).getGeom()
        .coord()
        .get(network.get(network.size() - 1).getGeom().coord().size() - 1));
    DefaultFeature f1 = new DefaultFeature(pt1);
    DefaultFeature f2 = new DefaultFeature(pt2);

    if (!popNodes.contains(f1)) {
      popNodes.add(f1);
    }

    if (!popNodes.contains(f2)) {
      popNodes.add(f2);
    }

  }

  private static void initialize(List<Center> centers,
      IPopulation<IFeature> network) {

    MainGeneration.addNewCenters(centers);

    Center center1 = centers.get(0);
    Center center2 = centers.get(1);
    center1.isActivated(false);
    center2.isActivated(false);

    IDirectPositionList l = new DirectPositionList();
    l.add(new DirectPosition(center1.getX(), center1.getY()));
    l.add(new DirectPosition(center2.getX(), center2.getY()));

    network.add(new DefaultFeature(new GM_LineString(l)));

    GM_Point pt1 = new GM_Point(network.get(0).getGeom().coord().get(0));
    GM_Point pt2 = new GM_Point(network.get(0).getGeom().coord()
        .get(network.get(0).getGeom().coord().size() - 1));
    DefaultFeature f1 = new DefaultFeature(pt1);
    DefaultFeature f2 = new DefaultFeature(pt2);

    if (!popNodes.contains(f1)) {
      popNodes.add(f1);
    }

    if (!popNodes.contains(f2)) {
      popNodes.add(f2);
    }

  }

  private static void addNewCenters(List<Center> centers) {

    for (int i = 0; i < Parameters.NEW_CENTERS_COUNT; i++) {

      double x = Parameters.XC
          + RandomGenerator.randomExponential(1. / Parameters.RC)
          * Parameters.WINDOWSIZE / 2.;
      double y = Parameters.YC
          + RandomGenerator.randomExponential(1. / Parameters.RC)
          * Parameters.WINDOWSIZE / 2.;

      /*
       * double x = Parameters.XC + RandomGenerator.randomGaussian(1)
       * Parameters.WINDOWSIZE / 2.; double y = Parameters.YC +
       * RandomGenerator.randomGaussian(1) Parameters.WINDOWSIZE / 2.;
       */

      /*
       * Random r = new Random(); double xmax = Parameters.XWINDOWLEFTDOWN +
       * Parameters.WINDOWSIZE; double ymax = Parameters.YWINDOWLEFTDOWN +
       * Parameters.WINDOWSIZE; double x = Parameters.XWINDOWLEFTDOWN + (xmax -
       * Parameters.XWINDOWLEFTDOWN) * r.nextDouble(); double y =
       * Parameters.YWINDOWLEFTDOWN + (ymax - Parameters.YWINDOWLEFTDOWN) *
       * r.nextDouble();
       */

      Center center = new Center(x, y);
      centers.add(center);

    }
  }

  private static void export(IPopulation<IFeature> network, String file) {
    ShapefileWriter.write(network, file);
  }

  private static void export(List<Center> centers, String file) {
    IPopulation<IFeature> pop = new Population<IFeature>();
    for (Center center : centers) {
      pop.add(new DefaultFeature(new GM_Point(new DirectPosition(center.getX(),
          center.getY()))));
    }
    ShapefileWriter.write(pop, file);
  }

  public static void grow_window() {
    Random r = new Random();
    double random = r.nextDouble();

    if (random < Parameters.PROBA_WINDOW_GROWTH) {

      System.out.println("OKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");

      Parameters.WINDOWSIZE += Parameters.WINDOW_GROWTH;
      double xc = (Parameters.XWINDOWLEFTDOWN) + Parameters.WINDOWSIZE / 2.;
      double yc = (Parameters.YWINDOWLEFTDOWN) + Parameters.WINDOWSIZE / 2.;

      if (xc < Parameters.XMAX && yc < Parameters.YMAX) {
        Parameters.XC = xc;
        Parameters.YC = yc;
      }
    }
  }

  public static void main(String args[]) {
    MainGeneration.run();
  }

}
