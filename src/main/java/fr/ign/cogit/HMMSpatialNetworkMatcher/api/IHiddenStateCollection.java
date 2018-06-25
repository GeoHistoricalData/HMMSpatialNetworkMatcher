package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

import java.util.Collection;
import java.util.List;

/**
 * Interface for collection of IHiddenState
 * @author bcostes
 *
 */
public interface IHiddenStateCollection{
  

/**
 * Method to filter elements from this collection
 * the observation
 * @param obs
 * @param threshold
 * @return
 */
Collection<IHiddenState> filter(IObservation obs);
  
  /**
   * Return this collection as a List
   * @return
   */
  List<IHiddenState> toList();

  
 
}
