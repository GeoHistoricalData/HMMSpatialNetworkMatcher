package fr.ign.cogit.morphogenesis.network.analysis.indicators.isolated;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;

public class FeatureElongation {
  public static double mesure(IGeometry geom) {
    if (geom.coord().size() == 2) {
      return 1;
    }

    double l = geom.envelope().maxX() - geom.envelope().minX();
    double h = geom.envelope().maxY() - geom.envelope().minY();

    return (Math.min(l, h)) / (Math.max(l, h));
  }
}
