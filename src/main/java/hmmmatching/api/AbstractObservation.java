package hmmmatching.api;

import hmmmatching.impl.HMMParameters;

public abstract class AbstractObservation<O>{
  
  protected O observation;

  public AbstractObservation(O observation){
    this.observation = observation;
  }
  
  public O getThis(){
    return this.observation;
  }
  
  @Override
  public String toString(){
    return "[Observation] : " + this.observation.toString();
  }
  
  
  public abstract double computeEmissionProbability(AbstractHiddenState<?> state, HMMParameters parameters);
    
}