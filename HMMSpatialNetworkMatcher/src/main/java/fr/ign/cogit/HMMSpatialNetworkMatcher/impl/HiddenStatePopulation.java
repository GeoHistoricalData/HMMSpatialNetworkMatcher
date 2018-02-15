package fr.ign.cogit.HMMSpatialNetworkMatcher.impl;

import java.util.ArrayList;
import java.util.Collection;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenStateCollection;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.geoxygene.feature.Population;

public class HiddenStatePopulation extends Population<HiddenState> implements IHiddenStateCollection{

  public Collection<IHiddenState> filter(IObservation obs) {
    Observation o = (Observation) obs;
    Collection<IHiddenState> result = new ArrayList<IHiddenState>();
    for(HiddenState s : this.select(o.getGeom(), ParametersSet.get().SELECTION_THRESHOLD)) {
      result.add(s);
    }
    return result;
  }

}
