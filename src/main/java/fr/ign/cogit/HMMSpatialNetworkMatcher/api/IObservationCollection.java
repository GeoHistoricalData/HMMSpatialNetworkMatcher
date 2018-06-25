package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

import java.util.List;

/**
 * Interface for collection of IObservation
 * @author bcostes
 *
 */

public interface IObservationCollection{

  /**
   * Return this collection as a List
   * @return list of observations
   */
  List<IObservation> toList();
  
}
