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
    this.strategies = new  HashMap<IEmissionProbablityStrategy, Double>();
  }
  
  public void add(IEmissionProbablityStrategy strategy, double weight) {
    this.strategies.put(strategy, weight);
  }
  
  public void setStrategies(Map<IEmissionProbablityStrategy, Double> strategies) {
    this.strategies = strategies;
  }

  public double compute(IObservation e1, IHiddenState e2) {
    double proba = 0;
    double W=0;
    for(IEmissionProbablityStrategy e : this.strategies.keySet()) {
      proba += this.strategies.get(e) * e.compute(e1, e2);
      W += this.strategies.get(e);
    }
    return (proba/W);
  }

}
