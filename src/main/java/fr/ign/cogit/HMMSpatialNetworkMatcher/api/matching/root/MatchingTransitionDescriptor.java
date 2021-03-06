package fr.ign.cogit.HMMSpatialNetworkMatcher.api.matching.root;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;

/**
 * Transition descriptor used in viterbi algorithm.
 * @author bcostes
 *
 */
public class MatchingTransitionDescriptor{
  
  private String description;

  @SuppressWarnings("unused")
  public MatchingTransitionDescriptor(IHiddenState state1, IHiddenState state2){
   // this.description = "Transition " + state1.toString() +" -> " + state2.toString();
    this.description = "";
  }

  @SuppressWarnings("unused")
  public String getTransitionDescription() {
    return this.description;
  }
}
