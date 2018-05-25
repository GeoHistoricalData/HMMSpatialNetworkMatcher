package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

/**
 * Interface for hidden state of the HMM
 * @author bcostes
 *
 */
public interface IHiddenState {
  
  /**
   * Compute probability that o2
   * has been emitted by nextState knowing that o1 has been emitted by this
   * @param nextState
   * @param o1
   * @param o2
   * @return
   */
  public abstract double computeTransitionProbability(IHiddenState nextState, IObservation o1, IObservation o2);
  
  /**
   * Set the transition probability strategy
   */
  public abstract void setTransitionProbabilityStrategy(ITransitionProbabilityStrategy epStrategy);
  
  /**
   * Get the transition probability strategy
   */
  public abstract ITransitionProbabilityStrategy getTransitionProbabilityStrategy();

}
