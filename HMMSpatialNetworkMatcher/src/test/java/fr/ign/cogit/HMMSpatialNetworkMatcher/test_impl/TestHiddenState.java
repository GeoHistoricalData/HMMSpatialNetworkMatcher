package fr.ign.cogit.HMMSpatialNetworkMatcher;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.ITransitionProbabilityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.HiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.Observation;

public class TestHiddenState {
  
  static HiddenState state;
  
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    state = Mockito.mock(HiddenState.class, Mockito.CALLS_REAL_METHODS );
  }


  @Test
  public void testTransitionProbability() {
    HiddenState nextState = Mockito.mock(HiddenState.class);
    Observation o1 = Mockito.mock(Observation.class);
    Observation o2 = Mockito.mock(Observation.class);
    
    ITransitionProbabilityStrategy t = Mockito.mock(ITransitionProbabilityStrategy.class);
    Mockito.when(t.compute(o1, state, o2, nextState)).thenReturn(10.);
    state.setTransitionProbaStrategy(t);

    assertEquals(state.computeTransitionProbability(nextState, o1, o2), 10.,0.0001);
  }

}
