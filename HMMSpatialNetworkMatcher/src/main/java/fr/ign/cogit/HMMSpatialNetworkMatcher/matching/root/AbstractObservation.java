package fr.ign.cogit.HMMSpatialNetworkMatcher.matching.root;

import java.util.Collection;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IEmissionProbablityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenStateCollection;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;

/**
 * Mother-class of observations
 * @author bcostes
 *
 */
public abstract class AbstractObservation implements IObservation{
  /**
   * Emission probability
   */
  private IEmissionProbablityStrategy emissionProbaStrategy;

  @Override
  public void setEmissionProbabilityStrategy(
      IEmissionProbablityStrategy emissionProbaStrategy) {
    this.emissionProbaStrategy = emissionProbaStrategy;
  }
  
  public IEmissionProbablityStrategy getEmissionProbabilityStrategy () {
    return this.emissionProbaStrategy;
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

  @Override
  public Collection<IHiddenState> candidates(IHiddenStateCollection states) {
    return states.filter(this);
  }
}
