package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

import java.util.Map;
import java.util.Set;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.matching.IHMMMatching;

public interface PostProcessStrategy {
  
  /**
   * Method to simplify input matching links in order to deal with
   * expected unmatched objects.
   * @param hmmProcess matching process
   * @return simplified observations and hidden states
   */
  Map<IObservation, Set<IHiddenState>> simplify(IHMMMatching hmmProcess);

}
