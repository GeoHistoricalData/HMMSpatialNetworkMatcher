package fr.ign.cogit.morphogenesis.network.graph.io;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.FT_Feature;

public class LocalFeature extends FT_Feature {
  private Double indicator;

  public LocalFeature(IGeometry geom) {
    super(geom);
    this.setGeom(geom);
  }

  public void setINDICATOR(Double indicator) {
    this.indicator = indicator;
  }

  public Double getINDICATOR() {
    return indicator;
  }
}
