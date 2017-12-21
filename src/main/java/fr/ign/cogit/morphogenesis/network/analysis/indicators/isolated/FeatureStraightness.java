package fr.ign.cogit.morphogenesis.network.analysis.indicators.isolated;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_Polygon;

public class FeatureStraightness {

  public static double mesure(IGeometry geom) {
    if (geom.coord().size() == 2) {
      return 1;
    }
    IDirectPositionList list = new DirectPositionList();
    list.addAll(geom.coord());
    list.add(geom.coord().get(0));
    GM_LineString ls = new GM_LineString(list);
    GM_Polygon poly = new GM_Polygon(ls);
    double area = poly.area();
    double areaRec = geom.envelope().getGeom().area();
    double result = area / areaRec;
    return result;
  }

}
