package fr.ign.cogit.morphogenesis.generation.barthelemy;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_Polygon;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;

public class LocalOptimizationPrinciple {

  public static int cpt = 0;

  public static IPopulation<IFeature> optimize(IDirectPosition node,
      List<Center> centers, IPopulation<IFeature> popNodes) {

    IPopulation<IFeature> result = new Population<IFeature>();

    IDirectPosition newNode = null;
    IDirectPositionList list = new DirectPositionList();

    if (centers.size() == 2) {
      double vecx = 0, vecy = 0;
      for (Center center : centers) {
        double dist = node.distance(new DirectPosition(center.getX(), center
            .getY()));
        vecx += (center.getX() - node.getX()) / dist;
        vecy += (center.getY() - node.getY()) / dist;
      }

      list.add(node);
      newNode = new DirectPosition(node.getX() + vecx, node.getY() + vecy);
      list.add(newNode);
      while (true) {
        node = new DirectPosition(newNode.getX(), newNode.getY());
        vecx = 0;
        vecy = 0;
        newNode = new DirectPosition();
        for (Center center : centers) {
          double dist = node.distance(new DirectPosition(center.getX(), center
              .getY()));
          vecx += (center.getX() - node.getX()) / dist;
          vecy += (center.getY() - node.getY()) / dist;
        }
        newNode = new DirectPosition(node.getX() + vecx, node.getY() + vecy);
        list.add(newNode);
        if (node.distance(newNode) < 0.00001) {
          break;
        }
      }
      result.add(new DefaultFeature(new GM_LineString(list)));
      GM_Point pt1 = new GM_Point(result.get(result.size() - 1).getGeom()
          .coord().get(0));
      GM_Point pt2 = new GM_Point(result.get(result.size() - 1).getGeom()
          .coord()
          .get(result.get(result.size() - 1).getGeom().coord().size() - 1));
      DefaultFeature f1 = new DefaultFeature(pt1);
      DefaultFeature f2 = new DefaultFeature(pt2);
      if (!popNodes.contains(f1)) {
        popNodes.add(f1);
      }

      if (!popNodes.contains(f2)) {
        popNodes.add(f2);
      }
    } else {
      list.add(node);
      IDirectPositionList l = new DirectPositionList();
      for (Center center : centers) {
        l.add(new DirectPosition(center.getX(), center.getY()));
      }
      l.add(new DirectPosition(centers.get(0).getX(), centers.get(0).getY()));
      GM_Polygon poly = new GM_Polygon(new GM_LineString(l));
      IDirectPosition centroid = poly.centroid();
      double vecx = 0, vecy = 0;
      List<ILineString> ls = new ArrayList<ILineString>();
      for (Center center : centers) {
        l = new DirectPositionList();
        l.add(centroid);
        l.add(new DirectPosition(center.getX(), center.getY()));
        ls.add(new GM_LineString(l));
      }

      newNode = new DirectPosition(node.getX() + vecx, node.getY() + vecy);
      list.add(newNode);
      while (true) {
        node = new DirectPosition(newNode.getX(), newNode.getY());
        vecx = 0;
        vecy = 0;
        newNode = new DirectPosition();
        for (Center center : centers) {
          double dist = node.distance(new DirectPosition(center.getX(), center
              .getY()));
          vecx += (center.getX() - node.getX()) / dist;
          vecy += (center.getY() - node.getY()) / dist;
        }
        newNode = new DirectPosition(node.getX() + vecx, node.getY() + vecy);
        list.add(newNode);

        ILineString tmp = new GM_LineString(list);
        boolean goOn = true;
        for (ILineString line : ls) {
          if (line.intersects(tmp)) {
            goOn = false;
            break;
          }
        }
        if (!goOn) {
          break;
        }

      }
      result.add(new DefaultFeature(new GM_LineString(list)));
      GM_Point pt1 = new GM_Point(result.get(result.size() - 1).getGeom()
          .coord().get(0));
      GM_Point pt2 = new GM_Point(result.get(result.size() - 1).getGeom()
          .coord()
          .get(result.get(result.size() - 1).getGeom().coord().size() - 1));
      DefaultFeature f1 = new DefaultFeature(pt1);
      DefaultFeature f2 = new DefaultFeature(pt2);
      if (!popNodes.contains(f1)) {
        popNodes.add(f1);
      }

      if (!popNodes.contains(f2)) {
        popNodes.add(f2);
      }
    }

    // on va créer autant d'arc qu'il y a de centre concernés
    for (Center center : centers) {
      list = new DirectPositionList();
      node = new DirectPosition(newNode.getX(), newNode.getY());
      list.add(node);
      list.add(new DirectPosition(center.getX(), center.getY()));
      result.add(new DefaultFeature(new GM_LineString(list)));
      GM_Point pt1 = new GM_Point(result.get(result.size() - 1).getGeom()
          .coord().get(0));
      GM_Point pt2 = new GM_Point(result.get(result.size() - 1).getGeom()
          .coord()
          .get(result.get(result.size() - 1).getGeom().coord().size() - 1));
      DefaultFeature f1 = new DefaultFeature(pt1);
      DefaultFeature f2 = new DefaultFeature(pt2);
      if (!popNodes.contains(f1)) {
        popNodes.add(f1);
      }

      if (!popNodes.contains(f2)) {
        popNodes.add(f2);
      }

    }

    /*
     * ShapefileWriter.write(result,
     * "/home/bcostes/Bureau/test_rasterize_gdal/tmp/" + cpt + ".shp"); cpt++;
     */

    return result;
  }

  /*
   * public static void main(String args[]) {
   * 
   * IPopulation<IFeature> nodePop = ShapefileReader
   * .read("/home/bcostes/Bureau/test_rasterize_gdal/tmp/node.shp");
   * IDirectPosition node = nodePop.get(0).getGeom().coord().get(0);
   * 
   * IPopulation<IFeature> centerPop = ShapefileReader
   * .read("/home/bcostes/Bureau/test_rasterize_gdal/tmp/center.shp");
   * List<Center> centers = new ArrayList<Center>();
   * 
   * for (IFeature f : centerPop) { IDirectPosition pt =
   * f.getGeom().coord().get(0); Center center = new Center(pt.getX(),
   * pt.getY()); centers.add(center); }
   * 
   * List<ILineString> list = LocalOptimizationPrinciple.optimize(node,
   * centers);
   * 
   * IFeatureCollection<IFeature> pop = new Population<IFeature>(); for
   * (ILineString l : list) { pop.add(new DefaultFeature(l)); }
   * ShapefileWriter.write(pop,
   * "/home/bcostes/Bureau/test_rasterize_gdal/tmp/test.shp");
   * 
   * }
   */
}
