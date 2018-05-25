package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

import java.util.LinkedList;
import java.util.List;

/**
 * A path of continuous spatial network edges (observations)
 * @author bcostes
 */
public class Path extends LinkedList<IObservation>{

  private static final long serialVersionUID = -4180734348189038494L;

  public Path(List<IObservation> asList) {
    super(asList);
  }
  
}
