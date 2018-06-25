package fr.ign.cogit.HMMSpatialNetworkMatcher.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Composite pattern to model that it is possible to use more than just one algorithm
 * to compute emission probabilities
 * @author bcostes
 *
 */
public class CompositeEmissionProbabilityStrategy implements IEmissionProbablityStrategy{

  private Map<IEmissionProbablityStrategy, Double> strategies;
  
  public CompositeEmissionProbabilityStrategy() {
    this.strategies = new HashMap<>();
  }
  
  public void add(IEmissionProbablityStrategy strategy, double weight) {
    this.strategies.put(strategy, weight);
  }

  @SuppressWarnings("unused")
  public void setStrategies(Map<IEmissionProbablityStrategy, Double> strategies) {
    this.strategies = strategies;
  }

  public double compute(IObservation e1, IHiddenState e2) {
    double W=this.strategies.values().stream().reduce(0., (x,y)->x+y);
    double proba = this.strategies.entrySet().stream().map(
        entry -> entry.getValue() * entry.getKey().compute(e1,e2)).reduce(0., (x,y)->x+y);
    return (proba/W);
  }

}
