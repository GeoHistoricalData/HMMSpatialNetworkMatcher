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
    return new ArrayList<>(this.getElements());
  }


}
