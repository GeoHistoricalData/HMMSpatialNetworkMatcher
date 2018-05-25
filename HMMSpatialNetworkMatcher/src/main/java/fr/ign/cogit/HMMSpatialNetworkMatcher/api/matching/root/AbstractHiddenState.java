package fr.ign.cogit.HMMSpatialNetworkMatcher.api.matching.root;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.ITransitionProbabilityStrategy;
/**
 * Mother-class of hidden states
 * @author bcostes
 *
 */
public abstract class AbstractHiddenState implements IHiddenState{
  /**
   * Transition probability
   */
  private ITransitionProbabilityStrategy transitionProbaStrategy;

  @Override
  public void setTransitionProbabilityStrategy(
      ITransitionProbabilityStrategy transitionProbaStrategy) {
    this.transitionProbaStrategy = transitionProbaStrategy;
  }
  
  @Override
  public ITransitionProbabilityStrategy getTransitionProbabilityStrategy() {
    return this.transitionProbaStrategy;
  }


  @Override
  public double computeTransitionProbability(IHiddenState nextState, IObservation o1,
      IObservation o2) {
    return this.transitionProbaStrategy.compute(o1, this, o2, nextState);
  }
  
}
