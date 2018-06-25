package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

/**
 * Strategy pattern ; several algorithms to compute emission probabilities
 * @author bcostes
 *
 */
public interface IEmissionProbablityStrategy {

  double compute(IObservation obs, IHiddenState state);
  
}
