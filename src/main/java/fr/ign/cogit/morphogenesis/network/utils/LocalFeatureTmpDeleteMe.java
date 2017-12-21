package fr.ign.cogit.morphogenesis.network.utils;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.FT_Feature;

public class LocalFeatureTmpDeleteMe extends FT_Feature {

  private double centrality;

  public LocalFeatureTmpDeleteMe(IGeometry g) {
    super(g);
  }

  public void setCentrality(double centrality) {
    this.centrality = centrality;
  }

  public double getCentrality() {
    return centrality;
  }

}
