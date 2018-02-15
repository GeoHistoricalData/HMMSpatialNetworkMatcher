package fr.ign.cogit.HMMSpatialNetworkMatcher.impl;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.FT_Feature;

/**
 * Composite pattern on HiddenState
 * Used to model the hierarchical layer of the HMM Matching
 * @author bcostes
 *
 */
public abstract class CompositeHiddenState extends FT_Feature implements IHiddenState{

  private List<HiddenState> states;

  public CompositeHiddenState(IGeometry geom) {
    super(geom);
    this.states = new ArrayList<HiddenState>();
  }

  public void add(HiddenState state) {
    this.states.add(state);
    this.computeGeometry();
  }

  public List<HiddenState> getStates() {
    return states;
  }

  public void setStates(List<HiddenState> states) {
    this.states = states;
    this.computeGeometry();
  }

  private void computeGeometry() {
    for(int i=1; i< this.states.size(); i++) {
      geom = geom.union(this.states.get(i).getGeom());
    }
    this.setGeom(geom);
  }

}
