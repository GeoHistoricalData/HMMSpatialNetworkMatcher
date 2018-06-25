package fr.ign.cogit.HMMSpatialNetworkMatcher.api.matching.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bmw.hmm.SequenceState;
import com.bmw.hmm.Transition;
import com.bmw.hmm.ViterbiAlgorithm;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenStateCollection;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.Path;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.matching.root.MatchingTransitionDescriptor;

/**
 * One iteration of the algorithm (matching of one path).
 * @author bcostes
 *
 */
public class HmmMatchingIteration {

  /**
   * The sequence of observations to be matched
   */
  private Path<IObservation> path;
  /**
   * Matching result
   */
  private Map<IObservation, IHiddenState> matching;

  /**
   * Collection of hidden states, representing the edges we try to match with the
   * observations in path
   */
  private IHiddenStateCollection hiddenStates;
  
  public HmmMatchingIteration(Path<IObservation> path, IHiddenStateCollection hiddenStates) {
    this.path = path;
    this.hiddenStates = hiddenStates;
    this.matching = new HashMap<>();
  }

  /**
   * Main matching process for this iteration
   */
  public void match() {    
    if(this.path.isEmpty()) {
      return;
    }
    @SuppressWarnings("unchecked")
    Iterator<IObservation> itObservations = ((LinkedList<IObservation>)this.path.clone()).iterator();

    // first observation of the path
    IObservation currentO = itObservations.next();
    
    // possible matching states
    Collection<IHiddenState> currentCandidates = currentO.candidates(this.hiddenStates);

    // initial emission probabilities
    Map<IHiddenState, Double> startP = new HashMap<>();
    
   
    // is there at least one potential candidate ?
    boolean notAllCandidatesInfinity = false;
    for(IHiddenState state : currentCandidates) {
      double eP = currentO.computeEmissionProbability(state);
      if(Double.isFinite(eP)) {
        notAllCandidatesInfinity = true;
      }
      startP.put(state, eP);
    }

    // if no candidates or no possible candidates
    if(currentCandidates.isEmpty() || !notAllCandidatesInfinity) {
      // remove the observation from path
      this.path.remove(currentO);
      // and try again with new shorter path
      this.match();
      return;
    }

    ViterbiAlgorithm<IHiddenState, IObservation, MatchingTransitionDescriptor> viterbi = new ViterbiAlgorithm<>(true);
    viterbi.startWithInitialObservation(currentO, currentCandidates, startP);
    boolean viterbiIsBroken = false;
    
    

    while(itObservations.hasNext()){
      // next observation if it exists
      IObservation nextO = itObservations.next();

      Collection<IHiddenState> nextCandidates = nextO.candidates(this.hiddenStates);

      if(nextCandidates.isEmpty()){
        // viterbi is broken because there is no hidden state candidate for matching with nextO
        viterbiIsBroken = true;
        break;
      }

      // emission probabilities
      Map<IHiddenState, Double> eP = new HashMap<>();
      for(IHiddenState state  :nextCandidates){
        eP.put(state, nextO.computeEmissionProbability(state));
      }
      // transition probabilities
      final Map<Transition<IHiddenState>, Double> transitionLogProbabilities = new LinkedHashMap<>();
      //   final Map<Transition<MatchingState>, MatchingTransitionDescriptor> transitionDescriptors = new LinkedHashMap<>();

      for(IHiddenState state1 :currentCandidates){
        for(IHiddenState state2: nextCandidates){
          transitionLogProbabilities.put(new Transition<>(state1, state2),
              state1.computeTransitionProbability(state2, currentO, nextO));
          // transitionDescriptors.put(new Transition<MatchingState>(state1, state2), new MatchingTransitionDescriptor(state1, state2));

        }
      }

      viterbi.nextStep(nextO, nextCandidates, eP,
          transitionLogProbabilities);

      if(viterbi.isBroken()){
        // viterbi is broken because there is not possible transition
        viterbiIsBroken = true;
        break;
      }

      currentO = nextO;
      currentCandidates = nextCandidates;
    }

    // get most likely path in transition/emission graph
    // done for the whole path, or for the sequence of observations that leaded ot a broken viterbi
    final List<SequenceState<IHiddenState, IObservation, MatchingTransitionDescriptor>> result =
        viterbi.computeMostLikelySequence();

    Set<IObservation> processed = new HashSet<>();
    for(SequenceState<IHiddenState, IObservation, MatchingTransitionDescriptor> s : result){
      processed.add(s.observation);
      this.matching.put(s.observation, s.state); 
    }

    if(viterbiIsBroken){
      // we delete all the processed observation ending with the one which broke the viterbi
      // then we start again with the rest of the path
      path.removeAll(processed);
      this.match();
    }

  }

  @SuppressWarnings("unused")
  public Path getPath() {
    return path;
  }

  @SuppressWarnings("unused")
  public void setPath(Path<IObservation> path) {
    this.path = path;
  }

  public Map<IObservation, IHiddenState> getMatching() {
    return matching;
  }

  public void setMatching(Map<IObservation, IHiddenState> matching) {
    this.matching = matching;
  }

  @SuppressWarnings("unused")
  public IHiddenStateCollection getHiddenStates() {
    return hiddenStates;
  }

  @SuppressWarnings("unused")
  public void setHiddenStates(IHiddenStateCollection hiddenStates) {
    this.hiddenStates = hiddenStates;
  }
}
