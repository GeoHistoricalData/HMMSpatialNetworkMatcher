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
   * @return
   */
  List<IObservation> toList();
  
}
