package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservationCollection;
import fr.ign.cogit.geoxygene.feature.Population;

/**
 * Implementation IObservationCollection based on a Population of Observation
 * @author bcostes
 *
 */
public class ObservationPopulation extends Population<FeatObservation> implements IObservationCollection{

  @Override
  public List<IObservation> toList() {
    List<IObservation> list = new ArrayList<>();
    list.addAll(this.getElements());
    return list;
  }


}
