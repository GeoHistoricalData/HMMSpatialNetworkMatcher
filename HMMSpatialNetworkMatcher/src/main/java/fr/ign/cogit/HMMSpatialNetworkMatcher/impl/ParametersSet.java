package fr.ign.cogit.HMMSpatialNetworkMatcher.impl;

public class ParametersSet {
  
  public double SELECTION_THRESHOLD = 100; 
  
  private static ParametersSet instance;
  
  private ParametersSet() {}
  
  public static ParametersSet get() {
    if(instance == null) {
      instance = new ParametersSet();
    }
    return instance;
  }

}
