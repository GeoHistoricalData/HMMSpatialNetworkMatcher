package fr.ign.cogit.HMMSpatialNetworkMatcher.impl;

import java.util.Objects;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.ITransitionProbabilityStrategy;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.FT_Feature;

public class HiddenState extends FT_Feature implements IHiddenState{

  /**
   * Transition probability
   */
  private ITransitionProbabilityStrategy transitionProbaStrategy;

  public HiddenState(IGeometry geometrie) {
   super(geometrie);
  }


  public void setTransitionProbaStrategy(
      ITransitionProbabilityStrategy transitionProbaStrategy) {
    this.transitionProbaStrategy = transitionProbaStrategy;
  }


  public double computeTransitionProbability(IHiddenState nextState, IObservation o1,
      IObservation o2) {
    return this.transitionProbaStrategy.compute(o1, this, o2, nextState);
  }
  
  @Override
  public boolean equals(Object o) {
    if(o == null) {
      return false;
    }
    if(! (o instanceof HiddenState)) {
      return false;
    }
    HiddenState s = (HiddenState)o;
    return s.getGeom().equals(this.getGeom());
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(this.geom);
  }
  
  
  @Override
  public String toString() {
    return "Hidden state : " + this.getGeom().toString();
  }

 
}
