package fr.ign.cogit.HMMSpatialNetworkMatcher.impl;

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
public class CompositeObservation  extends Observation implements IObservation{

  private List<Observation> observations;

  public CompositeObservation(List<Observation> observations) {
    super(new GM_LineString(new DirectPositionList()));
    this.observations = new ArrayList<>(observations);
    this.setEmissionProbaStrategy(observations.get(0).getEmissionProbaStrategy());
    this.computeGeometry();
  }

  public void add(Observation state) {
    this.observations.add(state);
    this.computeGeometry();
  }

  public List<Observation> getObservations() {
    return this.observations;
  }

  public void setObservations(List<Observation> observations) {
    this.observations = observations;
    this.computeGeometry();
  }
  
  private void computeGeometry() {
    List<ILineString> list = this.observations.stream().map(s->s.getGeom()).collect(Collectors.toList());
    this.setGeom(Operateurs.union(list));
  }
}
