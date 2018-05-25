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
  public abstract double computeEmissionProbability(IHiddenState state);
  
  /**
   * Get possible candidates for matching among whole network
   * @param states
   * @return
   */
  public abstract Collection<IHiddenState> candidates(IHiddenStateCollection states);
  
  /**
   * Set the emission probability strategy
   */
  public abstract void setEmissionProbabilityStrategy(IEmissionProbablityStrategy epStrategy);
  
  /**
   * Get the emission probability strategy
   */
  public abstract IEmissionProbablityStrategy getEmissionProbabilityStrategy();
 }
