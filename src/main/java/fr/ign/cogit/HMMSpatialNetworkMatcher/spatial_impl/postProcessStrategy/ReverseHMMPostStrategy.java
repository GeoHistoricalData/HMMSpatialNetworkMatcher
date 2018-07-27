package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.postProcessStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IEmissionProbablityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.ITransitionProbabilityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.PostProcessStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.matching.IHMMMatching;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.matching.core.HMMMatchingProcess;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.CompositeHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.FeatHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.FeatObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.HiddenStatePopulation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.ObservationPopulation;

/**
 * Match the reversed network and compile results to filter unexpected matched entities
 * @author bcostes
 *
 */
@SuppressWarnings("unused")
public class ReverseHMMPostStrategy implements PostProcessStrategy{
  @Override
  public Map<IObservation, Set<IHiddenState>> simplify(IHMMMatching hmmProcess) {
	Random generator = hmmProcess.getGenerator();
    ObservationPopulation observations = new ObservationPopulation();
    HiddenStatePopulation states = new HiddenStatePopulation();


    IEmissionProbablityStrategy emp = hmmProcess.getObservations().toList().get(0).getEmissionProbabilityStrategy();
    ITransitionProbabilityStrategy tp = hmmProcess.getStates().toList().get(0).getTransitionProbabilityStrategy();

    for(IObservation obs : hmmProcess.getObservations().toList()) {
      FeatHiddenState state = new FeatHiddenState(((FeatObservation)obs).getGeom());
      state.setTransitionProbabilityStrategy(tp);
      states.add(state);
    }
    for(IHiddenState s : hmmProcess.getStates().toList()) {
      FeatObservation obs = new FeatObservation(((FeatHiddenState)s).getGeom());
      obs.setEmissionProbabilityStrategy(emp);
      observations.add(obs);
    }

    HMMMatchingProcess newProcess = new HMMMatchingProcess(hmmProcess.getPathBuilder(),
        observations, states, null, generator);
    newProcess.match();

    Map<FeatObservation, List<FeatHiddenState>> matching = new HashMap<>();
    for(IObservation a : hmmProcess.getMatching().keySet()){
      List<FeatHiddenState> ss = new ArrayList<>();
      for(IHiddenState state : hmmProcess.getMatching().get(a)) {
        if(state instanceof CompositeHiddenState) {
          ss.addAll(((CompositeHiddenState)state).getStates());
        }
        else {
          ss.add((FeatHiddenState)state);
        }
      }
      matching.put((FeatObservation)a, ss);
    }

    Map<FeatObservation, List<FeatHiddenState>> matchingR = new HashMap<>();
    for(IObservation a : newProcess.getMatching().keySet()){
      List<FeatHiddenState> ss = new ArrayList<>();
      for(IHiddenState state : newProcess.getMatching().get(a)) {
        if(state instanceof CompositeHiddenState) {
          ss.addAll(((CompositeHiddenState)state).getStates());
        }
        else {
          ss.add((FeatHiddenState)state);
        }
      }
      matchingR.put((FeatObservation)a, ss);
    }


    /*
     *  compiling direct and reverse matching
     */

    Map<IObservation, Set<IHiddenState>> matchingF = new HashMap<>();
    for (FeatObservation o1 : matching.keySet()) {
      for (FeatHiddenState smatched1 : matching.get(o1)) {

        FeatObservation s1 = null;
        for(FeatObservation o2 : matchingR.keySet()) {
          if(o2.getGeom().equals(smatched1.getGeom())) {
            s1 = o2;
            break;
          }
        }
        if(s1 == null) {
          continue;
        }
        // s1 is smatched

        FeatHiddenState o2 = null;
        for(FeatHiddenState s2 : matchingR.get(s1)) {
          if(s2.getGeom().equals(o1.getGeom())) {
            o2 = s2;
            break;
          }
        }
        if(o2 == null) {
          continue;
        }

        // o1 is o2
        if (matchingF.containsKey(o1)) {
          matchingF.get(o1).add(smatched1);
        } else {
          Set<IHiddenState> set = new HashSet<>();
          set.add(smatched1);
          matchingF.put(o1, set);
        }

      }

    }
    return matchingF;
  }

}
