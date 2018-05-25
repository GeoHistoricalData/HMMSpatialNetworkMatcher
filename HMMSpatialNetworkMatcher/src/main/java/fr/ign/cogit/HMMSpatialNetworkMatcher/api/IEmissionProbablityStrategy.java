package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

/**
 * Strategy pattern ; several algorithms to compute emission probabilities
 * @author bcostes
 *
 */
public interface IEmissionProbablityStrategy {

  public abstract double compute(IObservation obs, IHiddenState state);
  
}
