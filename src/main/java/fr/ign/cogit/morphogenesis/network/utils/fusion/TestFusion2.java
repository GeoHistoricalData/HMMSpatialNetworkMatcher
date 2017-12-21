package fr.ign.cogit.morphogenesis.network.utils.fusion;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class TestFusion2 {

  public static double[] minMaxAngle(IDirectPosition source,
      IDirectPosition target, double ouverture) {
    double[] result = new double[2];

    // récupération des coordonnées polaires (teta): on se place dans le repère
    // d'origine source
    double x = target.getX() - source.getX();
    double y = target.getY() - source.getY();
    double teta = 2 * Math.atan(y / (x * x + Math.sqrt(x * x + y * y)));

    // angle min: teta + ouverture mod 2 pi
    double tetaMin = teta + ouverture;

    // angle max: teta - ouverture mod 2 pi
    double tetaMax = teta - ouverture;

    if (tetaMin > tetaMax) {
      double tetaTmp = tetaMin;
      tetaMin = tetaMax;
      tetaMax = tetaTmp;
    }

    result[0] = tetaMin;
    result[1] = tetaMax;

    return result;
  }

  public static IDirectPositionList circleSample(IDirectPosition source,
      double[] tetaMinMax, double rayon, double pas) {
    IDirectPositionList result = new DirectPositionList();

    for (double teta = tetaMinMax[0]; teta <= tetaMinMax[1]; teta += pas) {
      double x = source.getX() + rayon * Math.cos(teta);
      double y = source.getY() + rayon * Math.sin(teta);
      result.add(new DirectPosition(x, y));
    }
    return result;

  }

  public static ILineString buildLineString(List<ILineString> lines,
      double ouverture, double rayon, double pas) {

    // récupération du premier source et target
    double x1 = 0, x2 = 0, y1 = 0, y2 = 0;
    for (ILineString line : lines) {
      x1 += line.getControlPoint(0).getX();
      y1 += line.getControlPoint(0).getY();
      x2 += line.getControlPoint(line.coord().size() - 1).getX();
      y2 += line.getControlPoint(line.coord().size() - 1).getY();
    }

    x1 /= (double) lines.size();
    x2 /= (double) lines.size();
    y1 /= (double) lines.size();
    y2 /= (double) lines.size();

    IDirectPosition source = null;
    IDirectPosition target = null;

    if (x1 < x2) {

      source = new DirectPosition(x1, y1);
      target = new DirectPosition(x2, y2);
    } else {
      source = new DirectPosition(x2, y2);
      target = new DirectPosition(x1, y1);
    }

    IDirectPositionList line = new DirectPositionList();
    line.add(source);

    int cpt = 0;

    while (source.distance(target) > rayon && source.getX() < target.getX()) {
      IDirectPositionList l = TestFusion2.circleSample(source,
          TestFusion2.minMaxAngle(source, target, ouverture), rayon, pas);

      source = optimize(l, lines);
      line.add(source);
      cpt++;
    }
    line.add(target);
    ILineString lineMerge = new GM_LineString(line);
    return lineMerge;

  }

  public static IDirectPosition optimize(IDirectPositionList points,
      List<ILineString> lines) {
    IDirectPosition ptOpt = null;
    double dmin = Double.MAX_VALUE;
    for (IDirectPosition point : points) {
      double dist = 0;
      double x = 0;
      double y = 0;
      for (ILineString line : lines) {
        IDirectPosition proj = Operateurs.projection(point, line);
        x += proj.getX();
        y += proj.getY();
      }
      x /= (double) lines.size();
      y /= (double) lines.size();

      dist = point.distance(new DirectPosition(x, y));

      if (dist < dmin) {
        dmin = dist;
        ptOpt = point;
      }
    }
    return ptOpt;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    IPopulation<IFeature> pop = ShapefileReader
        .read("/home/bcostes/Bureau/tmp/fusion/lignes.shp");

    List<ILineString> lines = new ArrayList<ILineString>();

    for (IFeature f : pop) {
      lines.add(new GM_LineString(f.getGeom().coord()));
    }

    double ouverture = Math.PI / 8;
    double rayon = 50;
    double pas = 0.005;

    ILineString l = TestFusion2.buildLineString(lines, ouverture, rayon, pas);
    System.out.println(l.length());

    IPopulation<IFeature> out = new Population<IFeature>();
    out.add(new DefaultFeature(l));

    ShapefileWriter.write(out, "/home/bcostes/Bureau/tmp/fusion/fusion2.shp");

  }

}
