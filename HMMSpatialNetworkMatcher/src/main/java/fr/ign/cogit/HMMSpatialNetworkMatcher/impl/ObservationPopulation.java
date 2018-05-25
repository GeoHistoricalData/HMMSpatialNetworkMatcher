package fr.ign.cogit.HMMSpatialNetworkMatcher.impl;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservationCollection;
import fr.ign.cogit.geoxygene.feature.Population;

public class ObservationPopulation extends Population<Observation> implements IObservationCollection{

  @Override
  public List<IObservation> toList() {
    List<IObservation> list = new ArrayList<>();
    list.addAll(this.getElements());
    return list;
  }


}
