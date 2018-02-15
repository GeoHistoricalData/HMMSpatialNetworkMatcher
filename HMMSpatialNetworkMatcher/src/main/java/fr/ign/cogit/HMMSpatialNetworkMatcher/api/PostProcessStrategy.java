package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

import java.util.Map;
import java.util.Set;

public interface PostProcessStrategy {
  
  /**
   * Method to simplify input matching links in order to deal with
   * expected unmatched objects
   * @param tempMatching
   * @return
   */
  public abstract Map<IObservation, IHiddenState> simplify(Map<IObservation, Set<IHiddenState>> tempMatching);

}
