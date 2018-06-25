package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

/**
 * Strategy pattern ; several algorithms to compute transition probabilities
 * @author bcostes
 *
 */
public interface ITransitionProbabilityStrategy {
  
  /**
   * Compute probability that obs2
   * has been emitted by nextState knowing that obs1 has been emitted by currentState
   * @param obs1 observation 1
   * @param currentState current state
   * @param obs2 observation 2
   * @param nextState next state
   * @return transition probability
   */
  double compute(IObservation obs1, IHiddenState currentState,
                 IObservation obs2, IHiddenState nextState);

}
