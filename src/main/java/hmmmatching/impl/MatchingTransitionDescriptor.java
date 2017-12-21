package hmmmatching.impl;

import hmmmatching.api.AbstractHiddenState;

/**
 * Transition descriptor used in viterbi algorithm
 * @author bcostes
 *
 * @param <T>
 */
public class MatchingTransitionDescriptor{
  
  private String description;
  
  public MatchingTransitionDescriptor(AbstractHiddenState<?> state1, AbstractHiddenState<?> state2){
   // this.description = "Transition " + state1.toString() +" -> " + state2.toString();
    this.description = "";
  }

  public String getTransitionDescription() {
    return this.description;
  }
  

}
