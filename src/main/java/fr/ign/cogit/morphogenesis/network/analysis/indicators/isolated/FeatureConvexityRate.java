package fr.ign.cogit.morphogenesis.network.analysis.indicators.isolated;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;

public class FeatureConvexityRate {
  public static double mesure(IGeometry geom) {
    if (geom.coord().size() == 2) {
      return 1;
    }

    return geom.area() / geom.convexHull().area();
  }
}
