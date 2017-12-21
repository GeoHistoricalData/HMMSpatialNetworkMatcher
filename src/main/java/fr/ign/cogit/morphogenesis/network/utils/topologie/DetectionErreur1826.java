package fr.ign.cogit.morphogenesis.network.utils.topologie;

import java.util.Collection;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.algo.geomstructure.Vector2D;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class DetectionErreur1826 {

  public static double deflectionAngle(Arc old_s, Arc remain) {
    ILineString p = old_s.getGeometrie();
    ILineString q = remain.getGeometrie();
    if (p.startPoint().equals(q.startPoint(), 0.5)
        || p.endPoint().equals(q.endPoint(), 0.5)) {
      q = q.reverse();
    }

    Vector2D v1 = null;
    Vector2D v2 = null;
    try {
      if (q.endPoint().equals(p.startPoint(), 0.5)) {
        v1 = new Vector2D(p.coord().get(0), p.coord().get(1));
        v2 = new Vector2D(q.coord().get(q.coord().size() - 2), q.coord().get(
            q.coord().size() - 1));
      } else if (p.endPoint().equals(q.startPoint(), 0.5)) {
        v1 = new Vector2D(p.coord().get(p.coord().size() - 2), p.coord().get(
            p.coord().size() - 1));
        v2 = new Vector2D(q.coord().get(0), q.coord().get(1));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (v1 == null) {
      return Double.POSITIVE_INFINITY;
    }
    return v1.angleVecteur(v2).getValeur();

  }

  public static void main(String[] args) {

    String input = "/media/Data/Benoit/these/donnees/vecteur/FILAIRES_L93_OK/1826_topologieOk.shp";
    String output = "/media/Data/Benoit/these/donnees/vecteur/FILAIRES_L93_OK/1826_ERRORS.shp";

    IPopulation<IFeature> inputFeatureCollection = ShapefileReader.read(input);
    inputFeatureCollection.initSpatialIndex(Tiling.class, false);

    IFeatureCollection<IFeature> col = new Population<IFeature>();

    for (IFeature f : inputFeatureCollection) {
      ILineString l = Operateurs.echantillone(new GM_LineString(f.getGeom()
          .coord()), 0.5);
      for (int i = 0; i < l.coord().size() - 3; i++) {
        IDirectPosition p1 = l.coord().get(i);
        IDirectPosition p2 = l.coord().get(i + 1);
        IDirectPosition p3 = l.coord().get(i + 2);
        Angle angle = Angle.angleTroisPoints(p1, p2, p3);
        if (Math.abs(Math.PI - angle.getValeur()) > Math.PI / 3) {
          Collection<IFeature> candidats = inputFeatureCollection.select(
              (new GM_Point(p2)), 10);
          if (candidats.isEmpty()) {
            continue;
          }
          for (IFeature candidat : candidats) {
            if (candidat.getGeom().equals(f.getGeom())) {
              continue;
            }
            if (candidat.getGeom().coord().get(0).distance(p2) < 10
                || (candidat.getGeom().coord()
                    .get(candidat.getGeom().coord().size() - 1).distance(p2) < 10)) {
              col.add(new DefaultFeature(new GM_Point(p2)));
              break;

            }
          }
        }
      }
    }

    ShapefileWriter.write(col, output);

  }
}
