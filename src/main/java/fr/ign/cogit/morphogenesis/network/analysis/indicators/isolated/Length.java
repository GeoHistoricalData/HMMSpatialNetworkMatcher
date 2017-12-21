package fr.ign.cogit.morphogenesis.network.analysis.indicators.isolated;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;

public class Length {
  public static double mesure(IGeometry geom) {
    return geom.length();
  }
}
