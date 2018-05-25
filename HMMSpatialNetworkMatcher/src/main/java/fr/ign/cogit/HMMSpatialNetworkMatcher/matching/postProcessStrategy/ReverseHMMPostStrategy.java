package fr.ign.cogit.HMMSpatialNetworkMatcher.matching.postProcessStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IEmissionProbablityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.ITransitionProbabilityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.CompositeHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.HiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.HiddenStatePopulation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.Observation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.ObservationPopulation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.core.HMMMatchingProcess;

/**
 * Match the reversed network and compile results to filter unexpected matched entities
 * @author bcostes
 *
 */
public class ReverseHMMPostStrategy implements PostProcessStrategy{
  
  
 

  @Override
  public Map<IObservation, Set<IHiddenState>> simplify(HMMMatchingProcess hmmProcess) {
    // TODO Auto-generated method stub

    ObservationPopulation observations = new ObservationPopulation();
    HiddenStatePopulation states = new HiddenStatePopulation();


    IEmissionProbablityStrategy emp = ((Observation)hmmProcess.getObservations().toList().get(0)).getEmissionProbaStrategy();
    ITransitionProbabilityStrategy tp = ((HiddenState)hmmProcess.getStates().toList().get(0)).getTransitionProbaStrategy();

    for(IObservation obs : hmmProcess.getObservations().toList()) {
      HiddenState state = new HiddenState(((Observation)obs).getGeom());
      state.setTransitionProbaStrategy(tp);
      states.add(state);
    }
    for(IHiddenState s : hmmProcess.getStates().toList()) {
      Observation obs = new Observation(((HiddenState)s).getGeom());
      obs.setEmissionProbaStrategy(emp);
      observations.add(obs);
    }

    HMMMatchingProcess newProcess = new HMMMatchingProcess(hmmProcess.getPathBuilder(),
        observations, states, null);
    newProcess.match();

    Map<Observation, List<HiddenState>> matching = new HashMap<>();
    for(IObservation a : hmmProcess.getMatching().keySet()){
      List<HiddenState> ss = new ArrayList<>();
      for(IHiddenState state : hmmProcess.getMatching().get(a)) {
        if(state instanceof CompositeHiddenState) {
          ss.addAll(((CompositeHiddenState)state).getStates());
        }
        else {
          ss.add((HiddenState)state);
        }
      }
      matching.put((Observation)a, ss);
    }

    Map<Observation, List<HiddenState>> matchingR = new HashMap<>();
    for(IObservation a : newProcess.getMatching().keySet()){
      List<HiddenState> ss = new ArrayList<>();
      for(IHiddenState state : newProcess.getMatching().get(a)) {
        if(state instanceof CompositeHiddenState) {
          ss.addAll(((CompositeHiddenState)state).getStates());
        }
        else {
          ss.add((HiddenState)state);
        }
      }
      matchingR.put((Observation)a, ss);
    }


    /*
     *  compiling direct and reverse matching
     */

    Map<IObservation, Set<IHiddenState>> matchingF = new HashMap<>();
    for (Observation o1 : matching.keySet()) {
      for (HiddenState smatched1 : matching.get(o1)) {

        Observation s1 = null;
        for(Observation o2 : matchingR.keySet()) {
          if(o2.getGeom().equals(smatched1.getGeom())) {
            s1 = o2;
            break;
          }
        }
        if(s1 == null) {
          continue;
        }
        // s1 is smatched

        HiddenState o2 = null;
        for(HiddenState s2 : matchingR.get(s1)) {
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
