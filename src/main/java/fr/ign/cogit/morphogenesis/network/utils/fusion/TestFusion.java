package fr.ign.cogit.morphogenesis.network.utils.fusion;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class TestFusion {

  /**
   * @param args
   */
  public static void main(String[] args) {

    IPopulation<IFeature> pop = ShapefileReader
        .read("/home/bcostes/Bureau/tmp/fusion/lignes.shp");

    List<ILineString> l = new ArrayList<ILineString>();

    for (IFeature f : pop) {
      l.add(new GM_LineString(f.getGeom().coord()));
    }

    // line string la plus longue
    double lemax = 0;
    for (ILineString ll : l) {
      if (ll.length() > lemax) {
        lemax = ll.length();
      }
    }

    int nbPts = (int) ((double) lemax / 1);

    // resampling of linestrings
    List<ILineString> lE = new ArrayList<ILineString>();
    for (ILineString ll : l) {
      double pasVariable = ll.length() / (nbPts);
      lE.add(Operateurs.resampling(ll, pasVariable));

    }

    // re-ordering of linestring
    // TODO : on suppose pour le moment que c'est le cas
    IDirectPosition pos = lE.get(0).getControlPoint(0);
    for (int i = 1; i < lE.size(); i++) {
      if (lE.get(i).getControlPoint(0).distance(pos) > lE.get(i)
          .getControlPoint(lE.get(i).coord().size() - 1).distance(pos)) {
        lE.set(i, lE.get(i).reverse());
      }
    }

    IDirectPosition p1, p2 = null;
    int cpt = 0;
    double x1 = 0, x2 = 0, y1 = 0, y2 = 0;
    for (ILineString ll : lE) {
      x1 += ll.getControlPoint(0).getX();
      y1 += ll.getControlPoint(0).getY();
      x2 += ll.getControlPoint(ll.coord().size() - 1).getX();
      y2 += ll.getControlPoint(ll.coord().size() - 1).getY();
      cpt++;
    }

    p1 = new DirectPosition(x1 / (double) lE.size(), y1 / (double) lE.size());
    p2 = new DirectPosition(x2 / (double) lE.size(), y2 / (double) lE.size());

    ILineString lprov = new GM_LineString();
    lprov.addControlPoint(p1);
    lprov.addControlPoint(p2);

    IDirectPositionList lprovE = new DirectPositionList();
    lprovE.add(p1);

    for (int i = 0; i < nbPts; i++) {

      IDirectPosition pi = new DirectPosition();

      double newx = 0, newy = 0;
      cpt = 0;
      for (ILineString ll : lE) {
        newx += ll.getControlPoint(i).getX();
        newy += ll.getControlPoint(i).getY();
        cpt++;
      }

      pi.setX(newx / (double) lE.size());
      pi.setY(newy / (double) lE.size());

      lprovE.add(pi);

    }
    lprovE.add(p2);

    TestFusion.filter(lprovE, 5);

    IPopulation<IFeature> out = new Population<IFeature>();
    out.add(new DefaultFeature(new GM_LineString(lprovE)));
    ShapefileWriter.write(out, "/home/bcostes/Bureau/tmp/fusion/fusion.shp");

  }

  public static void filter(IDirectPositionList points, double thresold) {
    int cpt = 1;
    while (cpt < points.size() - 1) {
      double angle = Angle.angleTroisPoints(points.get(cpt - 1),
          points.get(cpt), points.get(cpt + 1)).getValeur();
      angle = angle * 180. / Math.PI;
      if (Math.abs(angle - 180.) < thresold) {
        points.remove(cpt);
      } else {
        cpt++;
      }

    }
  }
}
