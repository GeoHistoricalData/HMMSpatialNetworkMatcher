package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;

/**
 * Composite pattern on HiddenState
 * Used to model the hierarchical layer of the HMM Matching
 * @author bcostes
 *
 */
public class CompositeHiddenState extends FeatHiddenState{

  private List<FeatHiddenState> states;

  public CompositeHiddenState(List<FeatHiddenState> states) {
    super(new GM_LineString(new DirectPositionList()));
    this.states = new ArrayList<FeatHiddenState>(states);
    this.setTransitionProbabilityStrategy(states.get(0).getTransitionProbabilityStrategy());
    this.computeGeometry();
  }

  public void add(FeatHiddenState state) {
    this.states.add(state);
    this.computeGeometry();
  }

  public List<FeatHiddenState> getStates() {
    return states;
  }

  public void setStates(List<FeatHiddenState> states) {
    this.states = states;
    this.computeGeometry();
  }
  
  private void computeGeometry() {
    List<ILineString> list = this.states.stream().map(s->s.getGeom()).collect(Collectors.toList());
    this.setGeom(Operateurs.union(list));
  }

}
