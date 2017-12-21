package fr.ign.cogit.morphogenesis.network.clustering;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.FT_Feature;

public class LocalFeatureNodeCommunities extends FT_Feature {

  public LocalFeatureNodeCommunities(IGeometry g) {
    super(g);
  }

  public void setCommunity(int community) {
    this.community = community;
  }

  public int getCommunity() {
    return community;
  }

  private int community;

}
