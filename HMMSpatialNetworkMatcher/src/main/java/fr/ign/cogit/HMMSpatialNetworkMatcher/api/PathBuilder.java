package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

import java.util.Collection;
import java.util.List;

public interface PathBuilder {

  /**
   * Method to build paths of continuous observations
   * @param observations
   * @return
   */
  public abstract List<Path> buildPaths(Collection<IObservation> observations);
}
