package fr.ign.cogit.v2.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Jama.Matrix;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.generalisation.GaussianFilter;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.tag.STGraph;

public class GeometryUtils {

  public static final IPopulation<IFeature> POP = new Population<IFeature>();

  /**
   * @param lines on suppose que les extrémités sont déja égales (contrainte)
   * @return
   */
  public static ILineString mergeLineString(STGraph g, Map<ILineString, Double> lines,
      IDirectPosition pp1, IDirectPosition pp2) {

      
    Map<ILineString, Double> newlines = new HashMap<ILineString, Double>();
    for (ILineString l : lines.keySet()) {
      IDirectPositionList list = new DirectPositionList();
      for (int i = 0; i < l.coord().size(); i++) {
        list.add(new DirectPosition(l.getControlPoint(i).getX(), l
            .getControlPoint(i).getY()));
      }
      newlines.put(new GM_LineString(list), lines.get(l));
    }
    if (pp1.equals(pp2)) {
      // simple translation
      for (ILineString l : newlines.keySet()) {
        double tx = pp1.getX() - l.getControlPoint(0).getX();
        double ty = pp1.getY() - l.getControlPoint(0).getY();
        for (int i = 0; i < l.coord().size(); i++) {
          l.setControlPoint(i, new DirectPosition(l.getControlPoint(i).getX()
              + tx, l.getControlPoint(i).getY() + ty));
        }
      }
    } else {
        newlines = GeometryUtils.helmert(newlines, pp1, pp2);
    }

    if (newlines.size() == 1) {
      IDirectPositionList lprovE = new DirectPositionList();
      lprovE.addAll(newlines.keySet().iterator().next().coord());
     // GeometryUtils.filterLowAngles(lprovE, 5);
       GeometryUtils.filterLargeAngles(lprovE, 150);
      // GeometryUtils.filterLastAngles(lprovE, 95);
      return new GM_LineString(lprovE);
    }

//    // line string la plus longue
    double lemax = 0;
    for (ILineString ll : newlines.keySet()) {
      if (ll.length() > lemax) {
        lemax = ll.length();
      }
    }
    
    int nbPts = (int) ((double) lemax / 1);
    // resampling of linestrings
    Map<ILineString, Double> lE = new HashMap<ILineString, Double>();
    for (ILineString ll : newlines.keySet()) {
      double pasVariable = ll.length() / (double) (nbPts);
      lE.put(Operateurs.resampling(ll, pasVariable), newlines.get(ll));
    }


    IDirectPositionList lprovE = new DirectPositionList();
    lprovE.add(pp1);

    for (int i = 1; i < nbPts - 1; i++) {
      IDirectPosition pi = new DirectPosition();
      double newx = 0, newy = 0;
      double weight = 0.;
      for (ILineString ll : lE.keySet()) {
        newx +=  lE.get(ll) * ll.getControlPoint(i).getX();
        newy +=  lE.get(ll)* ll.getControlPoint(i).getY();
        weight+= lE.get(ll);
      }
      pi.setX(newx /weight);
      pi.setY(newy / weight);
      lprovE.add(pi);
    }
    lprovE.add(pp2);
    
    
//    MCMC mcmc = new MCMC();
//    ILineString lmerged = mcmc.run(newlines.keySet());
//    IDirectPositionList lprovE = lmerged.getControlPoint();
//    lprovE.set(0, pp1);
//    lprovE.set(lprovE.size()-1, pp2);
//
   // GeometryUtils.filterLowAngles(lprovE, 5);
   // GeometryUtils.filterLargeAngles(lprovE, 150);
    // GeometryUtils.filterLastAngles(lprovE, 95);
    return  /*GaussianFilter.gaussianFilter(*/new GM_LineString(lprovE)/*,2,5)*/;
  }

  public static void filterLowAngles(IDirectPositionList points,
      double thresold) {
    if (points.size() == 2) {
      return;
    }
    
    while (true) {
      IDirectPositionList newL = new DirectPositionList();
      newL.addAll(points);
      for (int i = 1; i < points.size() - 1; i++) {
        double angle = Angle.angleTroisPoints(points.get(i - 1), points.get(i),
            points.get(i + 1)).getValeur();
        angle = angle * 180. / Math.PI;
        if (Math.abs(angle - 180.) < thresold) {
          newL.remove(i);
          break;
        }
      }
      if (points.size() == newL.size()) {
        break;
      } else {
        points.clear();
        points.addAll(newL);
      }
    }

  }

  public static void filterLargeAngles(IDirectPositionList points,
      double thresold) {
    if (points.size() == 2) {
      return;
    }
    while (true) {
      IDirectPositionList newL = new DirectPositionList();
      newL.addAll(points);
      for (int i = 1; i < points.size() - 1; i++) {
        double angle = Angle.angleTroisPoints(points.get(i - 1), points.get(i),
            points.get(i + 1)).getValeur();
        angle = angle * 180. / Math.PI;
        if (Math.abs(angle - 180.) > thresold) {
          newL.remove(i);
          break;
        }
      }
      if (points.size() == newL.size()) {
        break;
      } else {
        points.clear();
        points.addAll(newL);
      }
    }
  }

  public static  Map<ILineString, Double> helmert(Map<ILineString, Double> lines,
      IDirectPosition pp1, IDirectPosition pp2) {

    // on récupère les extrémités
    // re-ordering of linestring
      Map<ILineString, Double> newlines = new HashMap<ILineString, Double>();
    for (ILineString l : lines.keySet()) {
      if (l.getControlPoint(0).distance(pp1) > l
          .getControlPoint(l.coord().size() - 1).distance(pp1)) {
          newlines.put(l.reverse(), lines.get(l));
      }
      else{
          newlines.put(l, lines.get(l));
      }
    }
    Map<ILineString, Double> result = new HashMap<ILineString, Double>();
    for (ILineString l : newlines.keySet()) {
      if (l.getControlPoint(0).equals(pp1)
          && l
              .getControlPoint(l.getControlPoint().size() - 1)
              .equals(pp2)) {
        result.put(new GM_LineString(l.coord()), newlines.get(l));
        continue;
      }

      IDirectPosition p1 = l.getControlPoint(0);
      IDirectPosition p2 = l.getControlPoint(
         l.getControlPoint().size() - 1);

      Matrix X = new Matrix(4, 4);
      X.set(0, 0, p1.getX());
      X.set(1, 0, p1.getY());
      X.set(2, 0, 1);
      X.set(3, 0, 0);
      X.set(0, 1, p1.getY());
      X.set(1, 1, -p1.getX());
      X.set(2, 1, 0);
      X.set(3, 1, 1);
      X.set(0, 2, p2.getX());
      X.set(1, 2, p2.getY());
      X.set(2, 2, 1);
      X.set(3, 2, 0);
      X.set(0, 3, p2.getY());
      X.set(1, 3, -p2.getX());
      X.set(2, 3, 0);
      X.set(3, 3, 1);
      Matrix Y = new Matrix(1, 4);
      Y.set(0, 0, pp1.getX());
      Y.set(0, 1, pp1.getY());
      Y.set(0, 2, pp2.getX());
      Y.set(0, 3, pp2.getY());
      Matrix A = Y.times(X.inverse());
      double a = A.get(0, 0);
      double b = A.get(0, 1);
      double c = A.get(0, 2);
      double d = A.get(0, 3);
      IDirectPositionList ll = new DirectPositionList();
      for (IDirectPosition p : l.getControlPoint()) {
        ll.add(new DirectPosition(a * p.getX() + b * p.getY() + c, -b
            * p.getX() + a * p.getY() + d));
        if (p.distance(ll.get(ll.size() - 1)) > 100) {

          /*
           * System.out.println(POP.size()); ShapefileWriter.write(POP,
           * "/home/bcostes/Bureau/tmp/problem.shp"); System.exit(0);
           */

        }
      }
      ll.set(0, pp1);
      ll.set(l.getControlPoint().size()-1, pp2);
      result.put(new GM_LineString(ll), newlines.get(l));
      
    }

    return result;
  }
  
  public static  Set<ILineString> helmert(Set<ILineString> lines,
      IDirectPosition pp1, IDirectPosition pp2) {

    // on récupère les extrémités
    // re-ordering of linestring
    Set<ILineString> newlines = new HashSet<ILineString>();
    for (ILineString l : lines) {
      if (l.getControlPoint(0).distance(pp1) > l
          .getControlPoint(l.coord().size() - 1).distance(pp1)) {
          newlines.add(l.reverse());
      }
      else{
          newlines.add(l);
      }
    }
    Set<ILineString> result = new HashSet<ILineString>();
    for (ILineString l : newlines) {
      if (l.getControlPoint(0).equals(pp1)
          && l
              .getControlPoint(l.getControlPoint().size() - 1)
              .equals(pp2)) {
        result.add(new GM_LineString(l.coord()));
        continue;
      }

      IDirectPosition p1 = l.getControlPoint(0);
      IDirectPosition p2 = l.getControlPoint(
         l.getControlPoint().size() - 1);

      Matrix X = new Matrix(4, 4);
      X.set(0, 0, p1.getX());
      X.set(1, 0, p1.getY());
      X.set(2, 0, 1);
      X.set(3, 0, 0);
      X.set(0, 1, p1.getY());
      X.set(1, 1, -p1.getX());
      X.set(2, 1, 0);
      X.set(3, 1, 1);
      X.set(0, 2, p2.getX());
      X.set(1, 2, p2.getY());
      X.set(2, 2, 1);
      X.set(3, 2, 0);
      X.set(0, 3, p2.getY());
      X.set(1, 3, -p2.getX());
      X.set(2, 3, 0);
      X.set(3, 3, 1);
      Matrix Y = new Matrix(1, 4);
      Y.set(0, 0, pp1.getX());
      Y.set(0, 1, pp1.getY());
      Y.set(0, 2, pp2.getX());
      Y.set(0, 3, pp2.getY());
      Matrix A = Y.times(X.inverse());
      double a = A.get(0, 0);
      double b = A.get(0, 1);
      double c = A.get(0, 2);
      double d = A.get(0, 3);
      IDirectPositionList ll = new DirectPositionList();
      for (IDirectPosition p : l.getControlPoint()) {
        ll.add(new DirectPosition(a * p.getX() + b * p.getY() + c, -b
            * p.getX() + a * p.getY() + d));
        if (p.distance(ll.get(ll.size() - 1)) > 100) {

          /*
           * System.out.println(POP.size()); ShapefileWriter.write(POP,
           * "/home/bcostes/Bureau/tmp/problem.shp"); System.exit(0);
           */

        }
      }
      ll.set(0, pp1);
      ll.set(l.getControlPoint().size()-1, pp2);
      result.add(new GM_LineString(ll));
      
    }

    return result;
  }
  
  
  public static void main(String args[]){
      
      
      IDirectPositionList l = ShapefileReader.read("/home/bcostes/Bureau/test_geometry.shp").get(0).getGeom().coord();
      
      System.out.println(l);
      
      
      GeometryUtils.filterLowAngles(l, 5);
      GeometryUtils.filterLargeAngles(l, 150);

      
      IPopulation<IFeature> out = new Population<IFeature>();
      out.add(new DefaultFeature(new GM_LineString(l)));
      ShapefileWriter.write(out, "/home/bcostes/Bureau/test_geometry_corrected.shp");
  }

  
}
