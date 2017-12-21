package fr.ign.cogit.morphogenesis.network.analysis.indicators.isolated;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_Polygon;

/**
 * Indice de Miller
 * @author bcostes
 * 
 */
public class FormFactor {

  public static double mesure(IGeometry geom) {

    if (geom.coord().size() == 2) {
      return -1;
    }
    IDirectPositionList list = new DirectPositionList();
    list.addAll(geom.coord());
    list.add(geom.coord().get(0));
    GM_LineString ls = new GM_LineString(list);
    GM_Polygon poly = new GM_Polygon(ls);
    // aire de l'entité
    double area = poly.area();
    // Récupération de la plus grande distance entre les points du polygone
    // Algorithme en O(n * log(n)) pour trouver le diamètre d'un polygone
    // convexe
    // via la plus grande distance entre les points antipodaux
    // le diamètre d'un polygone est égal au diamètre de son enveloppe convexe
    /*
     * IDirectPositionList l = poly.coord(); double maxDist = 0; for (int i = 1;
     * i < l.size(); i++) { for (int j = 0; j < i; j++) { double d =
     * l.get(i).distance(l.get(j)); if (d > maxDist) { maxDist = d; } } }
     */

    double formFactor = 4 * Math.PI * area
        / (poly.getExterior().length() * poly.getExterior().length());
    return formFactor;
  }

}
