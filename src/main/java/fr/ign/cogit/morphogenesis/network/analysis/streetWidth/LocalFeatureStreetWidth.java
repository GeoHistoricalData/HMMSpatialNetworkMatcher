package fr.ign.cogit.morphogenesis.network.analysis.streetWidth;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.FT_Feature;

public class LocalFeatureStreetWidth extends FT_Feature {

  private double WIDTH;

  public LocalFeatureStreetWidth(IGeometry geom) {
    super(geom);
  }

  public void setWIDTH(double wIDTH) {
    WIDTH = wIDTH;
  }

  public double getWIDTH() {
    return WIDTH;
  }

}
