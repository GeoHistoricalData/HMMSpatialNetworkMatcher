package fr.ign.cogit.HMMSpatialNetworkMatcher;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IEmissionProbablityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.Observation;

public class TestObservation {
  
  static Observation obs;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    obs = Mockito.mock(Observation.class, Mockito.CALLS_REAL_METHODS);
  }


  @Test
  public void testEmissionProbability() {
    IHiddenState state = Mockito.mock(IHiddenState.class);
    
    IEmissionProbablityStrategy e = Mockito.mock(IEmissionProbablityStrategy.class);
    obs.setEmissionProbaStrategy(e);
    Mockito.when(e.compute(obs, state)).thenReturn(10.);

    assertEquals(obs.computeEmissionProbability(state), 10., 0.0001);
  }

}
