package fr.ign.cogit.HMMSpatialNetworkMatcher.api.matching;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenStateCollection;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservationCollection;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.PathBuilder;

/**
 * Interface for HMM Matching Process. Defines common methods used by the HMMMatchingProcess
 * and the HMMMAtchingProcessParallel classes.
 * @author bcostes
 *
 */
public interface IHMMMatching {
  
  /**
   * Get matching result.
   * @return matching result
   */
  Map<IObservation, Set<IHiddenState>> getMatching();
  
  /**
   * Run the matching algorithm.
   */
  void match();
  
  /**
   * Get the observations used by the matching.
   * @return observations used
   */
  IObservationCollection getObservations();

  /**
   * Get the hidden states used by the matching.
   * @return states
   */
  IHiddenStateCollection getStates();
  
  /**
   * Get the strategy used to build paths.
   * @return strategy
   */
  PathBuilder getPathBuilder();
  
  /**
   * Get the matching links simplified by the post process strategy.
   * @return simplified matching
   */
  Map<IObservation, Set<IHiddenState>> getSimplifiedMatching();
  
  Random getGenerator();

}
