package fr.ign.cogit.morphogenesis.exploring_tool;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.FT_Feature;

public class LocalFeatureDeleteMe extends FT_Feature {

  public LocalFeatureDeleteMe(IGeometry g) {
    super(g);
  }

  public void setCENTRALITY(double cENTRALITY) {
    CENTRALITY = cENTRALITY;
  }

  public double getCENTRALITY() {
    return CENTRALITY;
  }

  private double CENTRALITY;
}
