package fr.ign.cogit.HMMSpatialNetworkMatcher.api.parallel;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.*;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.matching.IHMMMatching;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.matching.core.HmmMatchingIteration;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Process the matching algorithm with recursive strategy
 * @author bcostes
 *
 */
public class HMMMatchingProcessParallel extends RecursiveTask<Map<IObservation, Set<IHiddenState>>> implements IHMMMatching{

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
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

  /**
   * Boolean to manage parallelization
   */
  private boolean firstIteration;

  /**
   * Current path processed by the recursive task
   */
  private Path<IObservation> currentPath;
  
  /**
   * For sysout only
   */
  private static int processAchieved = 0;

  private Random generator;
  
  public HMMMatchingProcessParallel(PathBuilder pathBuilder,
      IObservationCollection observations, IHiddenStateCollection states,
      PostProcessStrategy postProcessStrategy, boolean firsIteration, Random randomGenerator) {
    super();
    this.pathBuilder = pathBuilder;
    this.observations = observations;
    this.states = states;
    this.matching = new HashMap<>();
    this.simplifiedMatching = new HashMap<>();
    this.postProcessStrategy = postProcessStrategy;
    this.firstIteration = firsIteration;
    this.generator = randomGenerator;
  }


  /**
   * Main matching function
   */
  @Override
  public void match() {
    
    int processeurs = Runtime.getRuntime().availableProcessors();
    // Performs the parallel execution
    ForkJoinPool pool = new ForkJoinPool(processeurs);
    this.matching = pool.invoke(this);
    if(this.postProcessStrategy != null) {
      this.simplifiedMatching= this.postProcessStrategy.simplify(this);
    }
    else {
      this.simplifiedMatching = this.matching;
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

  @Override
  public Random getGenerator() {
	return generator; 
  }

  private void setCurrentPath(Path<IObservation> currentPath) {
    this.currentPath = currentPath;
  }




  /**
   * An execution of the parallel process. Compute() is called recursivelly.
   * This implementation leads to one path = one processor for a while
   */
  @Override
  protected Map<IObservation, Set<IHiddenState>> compute() {
    if(this.firstIteration) {
      // first iteraiton of the recrusive / parallel matching process
      // Generate Paths
      List<Path<IObservation>> paths = this.pathBuilder.buildPaths(this.observations, this.generator);
      List<HMMMatchingProcessParallel>hmmIterations = new ArrayList<>();
      for(Path<IObservation> path : paths) {
        // matching iteration for each path
        HMMMatchingProcessParallel hmmProcessParallel = new HMMMatchingProcessParallel
            (pathBuilder, observations, states, postProcessStrategy, false, generator);
        hmmProcessParallel.setCurrentPath(path);
        hmmIterations.add(hmmProcessParallel);
      }
      for(HMMMatchingProcessParallel hmmProcessParallel:hmmIterations){
        hmmProcessParallel.fork();
      }
      Map<IObservation, Set<IHiddenState>> result = new HashMap<>();
      // concat results
      for(HMMMatchingProcessParallel hmmIt: hmmIterations){
        result = this.compile(result, hmmIt.join());
      }

      return result;
    }
    else {
      // One iteration = one processor for a while = one path treated by the HMM
      HmmMatchingIteration hmmIt = new HmmMatchingIteration(this.currentPath, this.states);
      // matching
      hmmIt.match();
      //get matching result
      Map<IObservation, IHiddenState> matchingItResult = hmmIt.getMatching();
      // Structure to store temporary matching results
      Map<IObservation, Set<IHiddenState>> tempMatching = new HashMap<>();
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
      processAchieved ++;
      System.out.println(processAchieved +" recursive process achieved");
      
      return tempMatching;
    }

  }
  
  /**
   * Compile matching results from 2 parallel task, i.e two separate paths.
   * @param result1 results from task 1
   * @param result2 results from task 2
   * @return merged results
   */
  private Map<IObservation, Set<IHiddenState>> compile(
      Map<IObservation, Set<IHiddenState>> result1,
      Map<IObservation, Set<IHiddenState>> result2) {
    Map<IObservation, Set<IHiddenState>> result = new HashMap<>();
    
    for(IObservation a : result1.keySet()){
      result.put(a, result1.get(a));
    }
    for(IObservation a : result2.keySet()){
      if(!result.containsKey(a)){
        result.put(a, result2.get(a));
      }
      else{
        for(IHiddenState clust: result2.get(a)){
          if(!result.get(a).contains(clust)){
            result.get(a).add(clust);
          }
        }
      }
    }
    return result;  
  }

}
