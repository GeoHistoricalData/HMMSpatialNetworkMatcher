package fr.ign.cogit.HMMSpatialNetworkMatcher.api.matching.core;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.*;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.matching.IHMMMatching;

import java.util.*;

/**
 * Process the matching algorithm
 * Call HmmMatchingIteration for each Path of the first network
 * @author bcostes
 *
 */
public class HMMMatchingProcess implements IHMMMatching{

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
  private Map<IObservation, Set<IHiddenState>> simplifiedMatching;
  /**
   * Strategy to deal with unmatched objects
   */
  private PostProcessStrategy postProcessStrategy;

  private Random generator;

  public HMMMatchingProcess(PathBuilder pathBuilder,
      IObservationCollection observations, IHiddenStateCollection states,
      PostProcessStrategy postProcessStrategy, Random generator) {
    super();
    this.pathBuilder = pathBuilder;
    this.observations = observations;
    this.states = states;
    this.matching = new HashMap<>();
    this.simplifiedMatching = new HashMap<>();
    this.postProcessStrategy = postProcessStrategy;
    this.generator = generator;
  }


  /**
   * Main matching function
   */
  @Override
  public void match() {

    // Generate Paths
    List<Path<IObservation>> paths = this.pathBuilder.buildPaths(this.observations, this.generator);


    // Structure to store temporary matching results
    Map<IObservation, Set<IHiddenState>> tempMatching = new HashMap<>();

    int pathSize = paths.size();
    int cpt = 0;
    
    for(Path<IObservation> path : paths) {
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
          Set<IHiddenState> set = new HashSet<>();
          set.add(matchingItResult.get(o));
          tempMatching.put(o, set);
        }
      }
    }
    this.matching = tempMatching;
    // deal with unmatched entities
    if(this.postProcessStrategy != null) {
      this.simplifiedMatching= this.postProcessStrategy.simplify(this);
    }
    else {
      this.simplifiedMatching = tempMatching;
    }
  }

  @Override
  public Map<IObservation, Set<IHiddenState>> getSimplifiedMatching() {
    return this.simplifiedMatching;
  }

  @Override
  public Map<IObservation, Set<IHiddenState>> getMatching() {
    return matching;
  }

  @Override
  public IObservationCollection getObservations() {
    return observations;
  }
  
  @Override
  public IHiddenStateCollection getStates() {
    return states;
  }

  @Override
  public PathBuilder getPathBuilder() {
    return pathBuilder;
  }
 
  public Random getGenerator() {
	return generator;
  }

}
