package fr.ign.cogit.morphogenesis.network.analysis.indicators.isolated;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;

public class SegmentAngle {

  static final double epsilon = Math.PI / 18; // 10°

  public static List<Double> mesure(Noeud n) {
    List<Double> result = new ArrayList<Double>();
    List<Arc> arcs = new ArrayList<Arc>();
    arcs.addAll(n.getEntrants());
    arcs.addAll(n.getSortants());
    int degree = arcs.size(); // graphe non orienté => les entrants
    // sont aussi sortants et vice versa

    if (degree == 2) {
      // noeud simple, cas trivial
      return result;
    }
    // au moins 3 arcs en jeu
    for (int i = 0; i < n.arcs().size() - 1; i++) {
      Arc arci = n.arcs().get(i);
      // le point
      IDirectPosition narci = null;
      if (arci.getNoeudFin().equals(n.getCoord())) {
        narci = arci.getCoord().get(arci.getCoord().size() - 2); // l'avant
        // dernier
      } else {
        narci = arci.getCoord().get(1); // le second
      }
      for (int j = i + 1; j < n.arcs().size(); j++) {
        Arc arcj = n.arcs().get(j);
        // le point
        IDirectPosition narcj = null;
        if (arcj.getNoeudFin().equals(n.getCoord())) {
          narcj = arcj.getCoord().get(arcj.getCoord().size() - 2); // l'avant
          // dernier
        } else {
          narcj = arcj.getCoord().get(1); // le second
        }
        // l'angle entre les trois points
        // si supérieur à PI on soustrait à deux pi pour se ramener à [0 PI]
        Angle angleij = Angle.angleAPiPres(Angle.angleTroisPoints(narci,
            n.getCoord(), narcj));
        System.out.println(angleij.getValeur());
        result.add(angleij.getValeur());
      }
    }
    return result;
  }

}
