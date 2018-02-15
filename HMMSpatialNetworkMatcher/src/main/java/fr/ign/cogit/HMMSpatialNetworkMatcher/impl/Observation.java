package fr.ign.cogit.HMMSpatialNetworkMatcher.impl;

import java.util.Collection;
import java.util.Objects;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IEmissionProbablityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenStateCollection;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.FT_Feature;

public class Observation extends FT_Feature implements IObservation{

  /**
   * Emission probability
   */
  private IEmissionProbablityStrategy emissionProbaStrategy;

  public Observation(IGeometry geometrie) {
    super(geometrie);
  }



  public void setEmissionProbaStrategy(
      IEmissionProbablityStrategy emissionProbaStrategy) {
    this.emissionProbaStrategy = emissionProbaStrategy;
  }

  /**
   * get the probability that this has been emited by state
   */
  public double computeEmissionProbability(IHiddenState state) {
    if(this.emissionProbaStrategy == null) {
      throw new RuntimeException("Emission probability strategy undefined for this observation");
    }
    return this.emissionProbaStrategy.compute(this, state);
  }

  /**
   * states should implements IFeatureCollection
   * otherwise calculation might be time consuming (a lot ... no spatial index)
   */
  public Collection<IHiddenState> candidates(IHiddenStateCollection states){
    return states.filter(this);
  }

  @Override
  public boolean equals(Object o) {
    if(o == null) {
      return false;
    }
    if(! (o instanceof Observation)) {
      return false;
    }
    Observation s = (Observation)o;
    return this.getGeom().equals(s.getGeom());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getGeom());
  }

  @Override
  public String toString() {
    return "Observation : " + this.getGeom().toString();
  }
}
