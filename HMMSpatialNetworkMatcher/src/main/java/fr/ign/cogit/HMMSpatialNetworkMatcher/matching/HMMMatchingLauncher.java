package fr.ign.cogit.HMMSpatialNetworkMatcher.matching;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IEmissionProbablityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.ITransitionProbabilityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.PathBuilder;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.HiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.HiddenStatePopulation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.Observation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.ObservationPopulation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.core.HMMMatchingProcess;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.io.HMMExporter;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.io.HMMImporter;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.postProcessStrategy.PostProcessStrategy;

public class HMMMatchingLauncher {
  
  private String fileNetwork1, fileNetwork2;
  private PathBuilder pathBuilder;
  private IEmissionProbablityStrategy epS;
  private ITransitionProbabilityStrategy tpS;
  private PostProcessStrategy postProcessStrategy;
  private Map<IObservation, Set<IHiddenState>> matching;
  private Map<IObservation, Set<IHiddenState>> simplifiedMatching;
  public HMMMatchingLauncher(String fileNetwork1, String fileNetwork2,
      IEmissionProbablityStrategy epS, ITransitionProbabilityStrategy tpS, PathBuilder pathBuilder,
      PostProcessStrategy postProcessStrategy) {
    super();
    this.fileNetwork1 = fileNetwork1;
    this.fileNetwork2 = fileNetwork2;
    this.epS = epS;
    this.tpS = tpS;
    this.postProcessStrategy = postProcessStrategy;
    this.pathBuilder = pathBuilder;
    this.matching = new HashMap<>();
    this.simplifiedMatching = new HashMap<>();
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
    for(Observation obs : observations) {
      obs.setEmissionProbaStrategy(this.epS);
    }
    for(HiddenState hd: states) {
      hd.setTransitionProbaStrategy(this.tpS);
    }
    
    // initialize and launch main matching process
    HMMMatchingProcess hmm = new HMMMatchingProcess(this.pathBuilder, observations,
        states, postProcessStrategy);
    hmm.match();
    
    //get matching results
    this.matching= hmm.getMatching();  
    this.simplifiedMatching = hmm.getSimplifiedMatching();
  }
  
  public void exportMatchingResults(String output) {
    HMMExporter exporter = new HMMExporter();
    System.out.println("this.matching.size() :" +this.matching.size());
    System.out.println("this.simplifiedMatching.size() :" +this.simplifiedMatching.size());
    exporter.export(this.matching, this.simplifiedMatching, output);
  }
  
   

}
