package fr.ign.cogit.HMMSpatialNetworkMatcher.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
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
public class CompositeHiddenState extends HiddenState implements IHiddenState{

  private List<HiddenState> states;

  public CompositeHiddenState(List<HiddenState> states) {
    super(new GM_LineString(new DirectPositionList()));
    this.states = new ArrayList<HiddenState>(states);
    this.setTransitionProbaStrategy(states.get(0).getTransitionProbaStrategy());
    this.computeGeometry();
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
    List<ILineString> list = this.states.stream().map(s->s.getGeom()).collect(Collectors.toList());
    this.setGeom(Operateurs.union(list));
  }

}
