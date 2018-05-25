package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

import java.util.List;

/**
 * Empty interface for collection of IObservation
 * @author bcostes
 *
 */

public interface IObservationCollection{

  public abstract List<IObservation> toList();
  
}
