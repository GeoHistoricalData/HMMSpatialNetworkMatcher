package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.tp;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.ITransitionProbabilityStrategy;

@SuppressWarnings("unused")
public class LengthDifferenceTransitionProbability implements ITransitionProbabilityStrategy{

  public double compute(IObservation obs1, IHiddenState currentState,
      IObservation obs2, IHiddenState nextState) {
    // TODO Auto-generated method stub
    return 0;
  }

}
