package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IEmissionProbablityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.ITransitionProbabilityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.PathBuilder;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.PostProcessStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.matching.IHMMMatching;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.matching.core.HMMMatchingProcess;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.parallel.HMMMatchingProcessParallel;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.io.HMMExporter;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.io.HMMImporter;

public class HMMMatchingLauncher {
  
  /**
   * ESRI Shapefiles
   */
  private String fileNetwork1, fileNetwork2;
  /**
   * Path builder strategy
   */
  private PathBuilder pathBuilder;
  /**
   * Emission probability strategy
   */
  private IEmissionProbablityStrategy epS;
  /**
   * Transition probability strategy
   */
  private ITransitionProbabilityStrategy tpS;
  /**
   * Post process strategy
   */
  private PostProcessStrategy postProcessStrategy;
  /**
   * Matching results
   */
  private Map<IObservation, Set<IHiddenState>> matching;
  /**
   * Filtered matching results
   */
  private Map<IObservation, Set<IHiddenState>> simplifiedMatching;
  /**
   * Lauch the matching process with recursive / parallel strategy
   */
  private boolean parralelProcess;
  
  public HMMMatchingLauncher(String fileNetwork1, String fileNetwork2,
      IEmissionProbablityStrategy epS, ITransitionProbabilityStrategy tpS, PathBuilder pathBuilder,
      PostProcessStrategy postProcessStrategy, boolean parralelProcess) {
    super();
    this.fileNetwork1 = fileNetwork1;
    this.fileNetwork2 = fileNetwork2;
    this.epS = epS;
    this.tpS = tpS;
    this.postProcessStrategy = postProcessStrategy;
    this.pathBuilder = pathBuilder;
    this.matching = new HashMap<>();
    this.simplifiedMatching = new HashMap<>();
    this.parralelProcess=parralelProcess;
  }


  public Map<IObservation, Set<IHiddenState>> getMatching() {
    return simplifiedMatching;
  }


  public void lauchMatchingProcess() {
    // load and prepare networks for matching
    HMMImporter preProcess = new HMMImporter();
    preProcess.loadAndPrepareNetworks(this.fileNetwork1, this.fileNetwork2);
    
    // get observations and hidden states
    ObservationPopulation observations = preProcess.getObservations();
    HiddenStatePopulation states = preProcess.getStates();
    
    // set emmision and transition probabilities stategies
    for(FeatObservation obs : observations) {
      obs.setEmissionProbabilityStrategy(this.epS);
    }
    for(FeatHiddenState hd: states) {
      hd.setTransitionProbabilityStrategy(this.tpS);
    }
    
    // initialize and launch main matching process
    IHMMMatching hmm = null;
    if(this.parralelProcess) {
    hmm = new HMMMatchingProcessParallel(this.pathBuilder, observations,
        states, postProcessStrategy, true);
    }
    else {
      hmm = new HMMMatchingProcess(this.pathBuilder, observations,
          states, postProcessStrategy);
    }
    hmm.match();
    
    //get matching results
    this.matching= hmm.getMatching();  
    this.simplifiedMatching = hmm.getSimplifiedMatching();
  }
  
  public void exportMatchingResults(String output) {
    HMMExporter exporter = new HMMExporter();
    exporter.export(this.matching, this.simplifiedMatching, output);
  }
  
   

}
