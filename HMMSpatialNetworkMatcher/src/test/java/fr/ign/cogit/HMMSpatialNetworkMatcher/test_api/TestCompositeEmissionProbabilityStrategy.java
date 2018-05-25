package fr.ign.cogit.HMMSpatialNetworkMatcher.test_api;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.CompositeEmissionProbabilityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IEmissionProbablityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;

public class TestCompositeEmissionProbabilityStrategy {
  
  static CompositeEmissionProbabilityStrategy c;
  static IObservation o1;
  static IHiddenState h1;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    c = Mockito.mock(CompositeEmissionProbabilityStrategy.class, Mockito.CALLS_REAL_METHODS);
  }

  @Before
  public void setUp() throws Exception {
   IEmissionProbablityStrategy e1 = Mockito.mock(IEmissionProbablityStrategy.class);
   IEmissionProbablityStrategy e2 = Mockito.mock(IEmissionProbablityStrategy.class);
   IEmissionProbablityStrategy e3 = Mockito.mock(IEmissionProbablityStrategy.class);
   
   c.setStrategies(new HashMap<IEmissionProbablityStrategy, Double>());
   
   c.add(e1, 1.);
   c.add(e2, 1.);
   c.add(e3, 2.);
   
   o1 = Mockito.mock(IObservation.class);
   
   h1 = Mockito.mock(IHiddenState.class);
   
   Mockito.when(e1.compute(o1, h1)).thenReturn(10.);
   Mockito.when(e2.compute(o1, h1)).thenReturn(20.);
   Mockito.when(e3.compute(o1, h1)).thenReturn(100.);
  }


  @Test
  public void testAdd() {
    IEmissionProbablityStrategy e4 = Mockito.mock(IEmissionProbablityStrategy.class);
    c.add(e4, 1.);
    Mockito.when(e4.compute(o1, h1)).thenReturn(50.);
    double d = (2*100 + 10 + 20 + 50)/(2.+1.+1. + 1.);
    assertEquals(d, c.compute(o1, h1), 0.0001);
  }

  @Test
  public void testCompute() {
    double d = (2*100 + 10 + 20)/(2.+1.+1.);
    assertEquals(d, c.compute(o1, h1), 0.0001);
  }

}
