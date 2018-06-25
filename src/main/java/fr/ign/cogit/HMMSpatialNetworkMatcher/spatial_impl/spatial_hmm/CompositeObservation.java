package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;

/**
 * Composite pattern on Observation
 * Used to model the hierarchical layer of the HMM Matching
 * @author bcostes
 *
 */
public class CompositeObservation  extends FeatObservation implements IObservation{

  private List<FeatObservation> observations;

  public CompositeObservation(List<FeatObservation> observations) {
    super(new GM_LineString(new DirectPositionList()));
    this.observations = new ArrayList<>(observations);
    this.setEmissionProbabilityStrategy(observations.get(0).getEmissionProbabilityStrategy());
    this.computeGeometry();
  }

  public void add(FeatObservation state) {
    this.observations.add(state);
    this.computeGeometry();
  }

  public List<FeatObservation> getObservations() {
    return this.observations;
  }

  public void setObservations(List<FeatObservation> observations) {
    this.observations = observations;
    this.computeGeometry();
  }
  
  private void computeGeometry() {
    List<ILineString> list = this.observations.stream().map(FeatObservation::getGeom).collect(Collectors.toList());
    this.setGeom(Operateurs.union(list));
  }
}
