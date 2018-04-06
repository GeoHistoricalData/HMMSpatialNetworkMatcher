package fr.ign.cogit.HMMSpatialNetworkMatcher.matching;

import java.util.Map;
import java.util.Set;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;

public interface PostProcessStrategy {
  
  /**
   * Method to simplify input matching links in order to deal with
   * expected unmatched objects
   * @param tempMatching
   * @return
   */
  public abstract Map<IObservation, IHiddenState> simplify(Map<IObservation, Set<IHiddenState>> tempMatching);

}
