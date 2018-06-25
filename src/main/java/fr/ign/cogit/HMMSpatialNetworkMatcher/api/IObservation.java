package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

import java.util.Collection;

/**
 * Interface for observation of the HMM
 * @author bcostes
 *
 */
public interface IObservation {
  
  /**
   * Compute emission probability with given HiddenState
   * @param state
   * @return
   */
  double computeEmissionProbability(IHiddenState state);
  
  /**
   * Get possible candidates for matching among whole network
   * @param states
   * @return
   */
  Collection<IHiddenState> candidates(IHiddenStateCollection states);
  
  /**
   * Set the emission probability strategy
   */
  void setEmissionProbabilityStrategy(IEmissionProbablityStrategy epStrategy);
  
  /**
   * Get the emission probability strategy
   */
  IEmissionProbablityStrategy getEmissionProbabilityStrategy();
 }
