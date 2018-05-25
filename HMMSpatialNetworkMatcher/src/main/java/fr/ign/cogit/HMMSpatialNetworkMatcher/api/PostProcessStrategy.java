package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

import java.util.Map;
import java.util.Set;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.matching.core.HMMMatchingProcess;

public interface PostProcessStrategy {
  
  /**
   * Method to simplify input matching links in order to deal with
   * expected unmatched objects
   * @param tempMatching
   * @return
   */
  public abstract Map<IObservation, Set<IHiddenState>> simplify(HMMMatchingProcess hmmProcess);

}