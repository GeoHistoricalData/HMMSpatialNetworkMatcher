package fr.ign.cogit.HMMSpatialNetworkMatcher.matching;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenStateCollection;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservationCollection;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.Path;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.PathBuilder;

/**
 * Process the matching algorithm
 * Call HmmMatchingIteration for each Path of the first network
 * @author bcostes
 *
 */
public class HMMMatchingProcess {

  /**
   * Strategy to build path of observations
   */
  private PathBuilder pathBuilder;
  /**
   * The collection of observations, corresponding to the first network
   */
  private IObservationCollection observations;
  /**
   * The collection of hiddenstate, corresponding to the second network
   */
  private IHiddenStateCollection states;
  
  /**
   * Matching result
   */
  private Map<IObservation, Set<IHiddenState>> matching;
  /**
   * Simplified matching result
   */
  private Map<IObservation, IHiddenState> simplifiedMatching;
  /**
   * Strategy to deal with unmatched objects
   */
  private PostProcessStrategy postProcessStrategy;


  public HMMMatchingProcess(PathBuilder pathBuilder,
      IObservationCollection observations, IHiddenStateCollection states,
      PostProcessStrategy postProcessStrategy) {
    super();
    this.pathBuilder = pathBuilder;
    this.observations = observations;
    this.states = states;
    this.matching = new HashMap<>();
    this.simplifiedMatching = new HashMap<IObservation, IHiddenState>();
    this.postProcessStrategy = postProcessStrategy;
  }


  /**
   * Main matching function
   */
  public void match() {

    // Generate Paths
    List<Path> paths = this.pathBuilder.buildPaths(this.observations);


    // Structure to store temporary matching results
    Map<IObservation, Set<IHiddenState>> tempMatching = new HashMap<IObservation, Set<IHiddenState>>();

    int pathSize = paths.size();
    int cpt = 0;
    
    for(Path path : paths) {
      cpt++;
      System.out.println(cpt + " / " + pathSize);
      // matching iteration for each path
      HmmMatchingIteration hmmIt = new HmmMatchingIteration(path, this.states);
      // matching
      hmmIt.match();
      //get matching result
      Map<IObservation, IHiddenState> matchingItResult = hmmIt.getMatching();
            
      // add matching result in temporary result structure
      for(IObservation o : matchingItResult.keySet()) {
        if(tempMatching.containsKey(o)) {
          tempMatching.get(o).add(matchingItResult.get(o));
        }
        else {
          Set<IHiddenState> set = new HashSet<IHiddenState>();
          set.add(matchingItResult.get(o));
          tempMatching.put(o, set);
        }
      }
    }
    // deal with unmatched entities
   // this.matching = this.postProcessStrategy.simplify(tempMatching);
    // TODO : remove this
    this.matching = tempMatching;
  }


  public Map<IObservation, IHiddenState> getSimplifiedMatching() {
    return this.simplifiedMatching;
  }


  public Map<IObservation, Set<IHiddenState>> getMatching() {
    return matching;
  }

}
