package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

import java.util.LinkedList;
import java.util.List;

/**
 * A path of continuous spatial network edges (observations)
 * @author bcostes
 */
public class Path<O extends IObservation> extends LinkedList<O>{

  private static final long serialVersionUID = -4180734348189038494L;

  public Path(List<O> asList) {
    super(asList);
  }
  
}
