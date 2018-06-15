package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm;

public class ParametersSet {
  
  public double SELECTION_THRESHOLD = 10; 
  public int PATH_MIN_LENGTH = 5;
  public boolean NETWORK_PROJECTION = false;
  
  private static ParametersSet instance;
  
  private ParametersSet() {}
  
  public static ParametersSet get() {
    if(instance == null) {
      instance = new ParametersSet();
    }
    return instance;
  }

}
