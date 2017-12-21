package hmmmatching.api;

import hmmmatching.impl.HMMParameters;

public abstract class AbstractHiddenState<T>{
  
  protected T state;
  
  public AbstractHiddenState(T state){
    this.state = state;
  }

  public T getThis() {
    return this.state;
  }
  
  @Override
  public String toString(){
    return "[State] : " + this.state.toString();
  }
  
  /**
   * Compute probability that obs2 has been emitted by nextState knowing that obs1 has been emitted by this
   * @param obs1
   * @param nextState
   * @param obs2
   * @return
   */
  public abstract double computeTransitionProbability(AbstractHiddenState<?> nextState,
      AbstractObservation<?> obs1, AbstractObservation<?> obs2, HMMParameters parameters);
  
}
