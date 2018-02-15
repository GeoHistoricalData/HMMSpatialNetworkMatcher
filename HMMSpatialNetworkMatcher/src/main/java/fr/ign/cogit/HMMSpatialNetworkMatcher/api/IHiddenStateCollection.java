package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

import java.util.Collection;

/**
 * Interface for collection of IHiddenState
 * @author bcostes
 *
 */
public interface IHiddenStateCollection {
  

/**
 * Method to filter elements from this collection
 * the observation
 * @param obs
 * @param threshold
 * @return
 */
  public Collection<IHiddenState> filter(IObservation obs);
  
}
