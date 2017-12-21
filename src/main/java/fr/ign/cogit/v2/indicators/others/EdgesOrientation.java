package fr.ign.cogit.v2.indicators.others;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.v2.indicators.IOtherGeometricalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

/**
 * @author bcostes
 * 
 * @param <V>
 * @param <E>
 */
public class EdgesOrientation extends IOtherGeometricalIndicator {

  public EdgesOrientation() {
    this.name = "Edges Orientation";
  }

  @Override
  public List<Double> calculateGeometricalIndicator(JungSnapshot graph) {
    List<Double> result = new ArrayList<Double>();
    for (GraphEntity g : graph.getEdges()) {
      result.add(EdgesOrientation.globalOrientation((ILineString) g
          .getGeometry().toGeoxGeometry()) * 180. / Math.PI);
    }

    return result;
  }

  /**
   * Orientation d'une linestring calculée comme l'angle entre le segment
   * joignant les deux extrémités et l'axe des abscisses Défini à PI près
   * @param line
   * @return
   */
  public static double FLOrientation(ILineString line) {
    IDirectPosition p1 = line.coord().get(0);
    IDirectPosition p2 = line.coord().get(line.coord().size() - 1);
    return orientation(p1, p2);
  }

  /**
   * Orientation moyenne : moyenne pondérée des orientations des segments
   * consituant la linestring
   * @param line
   * @return
   */
  public static double meanOrientation(ILineString line) {
    double orientation = 0;
    double weight = 0;

    for (int i = 0; i < line.coord().size() - 1; i++) {
      IDirectPosition p1 = line.coord().get(i);
      IDirectPosition p2 = line.coord().get(i + 1);
      double w = p1.distance(p2);
      orientation += w * orientation(p1, p2);

      weight += w;
    }
    orientation /= weight;
    return orientation;
  }

  /**
   * Direction principale par accumulation
   * @param line
   * @return
   */
  public static double mainOrientation(ILineString line) {
    double A_PREC = Math.PI / 180.0;
    double A_MAX = Math.PI;
    double A_CONTRIB = Math.PI / 18;
    int NTESTS = (int) (A_MAX / A_PREC);
    List<Double> ori = new ArrayList<Double>();
    List<Double> wei = new ArrayList<Double>();
    IDirectPositionList coords = line.coord();
    if (coords.size() < 2) {
      return -1;
    }
    IDirectPosition p1 = coords.get(0);
    for (int i = 1; i < coords.size(); i++) {
      IDirectPosition p2 = coords.get(i);
      ori.add(new Double(orientation(p1, p2)));
      wei.add(new Double(p1.distance(p2)));
      p1 = p2;
    }

    // accumulation
    double[] accu = new double[NTESTS];
    double ori_test = 0.0d;
    for (int i = 0; i < NTESTS; i++) {
      for (int j = 0; j < ori.size(); j++) {
        double delta_a = Math.abs(ori_test - ori.get(j).doubleValue());
        if (delta_a < A_CONTRIB) {
          // Une orientation contribue d'autant plus qu'elle est proche de la
          // direction testée et que la longueur du segment relatif est
          // importante.
          double value = ((A_CONTRIB - delta_a) / A_CONTRIB)
              * wei.get(j).doubleValue();

          accu[i] += value;
        }
      }
      ori_test += A_PREC;
    }
    // On retire l'orientation principale dans l'accumulateur.
    double main = 0.0d;
    int id = 0;
    for (int i = 0; i < accu.length; i++) {
      if (accu[i] > main) {
        main = accu[i];
        id = i;
      }
    }

    return id * A_PREC;
  }

  /**
   * Direction principale au sens de la droite de régression passant par le
   * nuage des points de la polyline
   * @param line
   * @return
   */
  public static double globalOrientation(ILineString line) {
    return Operateurs.directionPrincipale(
        Operateurs.echantillone(line, 10).coord()).getValeur();
  }

  private static double orientation(IDirectPosition p1, IDirectPosition p2) {
    double orientation = Math.atan((p1.getY() - p2.getY())
        / (p1.getX() - p2.getX()));
    if (orientation < 0) {
      orientation += Math.PI;
    }
    return orientation;
  }

}
