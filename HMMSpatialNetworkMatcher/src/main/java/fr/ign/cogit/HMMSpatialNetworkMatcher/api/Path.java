package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a path of continuous spatial network edges (observations)
 */
public class Path extends LinkedList<IObservation>{

  public Path(List<IObservation> asList) {
    super(asList);
  }

  /**
   * 
   */
  private static final long serialVersionUID = -5583537086768775390L;
  
}
