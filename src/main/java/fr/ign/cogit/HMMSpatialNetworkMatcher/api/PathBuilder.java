package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

import java.util.List;
import java.util.Random;

/**
 * Strategy Pattern to build several types of Path
 * @author bcostes
 *
 */
public interface PathBuilder {

  /**
   * Method to build paths of continuous observations
   * @param observations collection of observations
   * @return paths
   */
  List<Path<IObservation>> buildPaths(IObservationCollection observations, Random generator);
}
