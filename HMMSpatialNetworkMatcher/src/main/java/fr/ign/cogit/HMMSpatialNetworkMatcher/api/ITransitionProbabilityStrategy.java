package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;

/**
 * Strategy pattern ; several algorithms to compute transition probabilities
 * @author bcostes
 *
 */
public interface ITransitionProbabilityStrategy {
  
  /**
   * Compute probability that obs2
   * has been emitted by nextState knowing that obs1 has been emitted by currentState
   * @param obs1
   * @param currentState
   * @param obs2
   * @param nextState
   * @return
   */
  public abstract double compute(IObservation obs1, IHiddenState currentState,
      IObservation obs2, IHiddenState nextState);

}
