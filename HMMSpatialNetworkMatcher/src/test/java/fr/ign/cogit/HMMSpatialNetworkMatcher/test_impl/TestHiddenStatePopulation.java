package fr.ign.cogit.HMMSpatialNetworkMatcher.test_impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.HiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.HiddenStatePopulation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.Observation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.core.ParametersSet;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;

public class TestHiddenStatePopulation {

  static HiddenStatePopulation popH;
  static IObservation o;
  static HiddenState s1, s2, s3, s4;
  static Collection<IHiddenState> result;

  @BeforeClass
  public static void setUpBeforeClass() {
    popH = new HiddenStatePopulation();

    IDirectPosition p1 = new DirectPosition(0,0);
    IDirectPosition p2 = new DirectPosition(1,1);
    IDirectPosition p3 = new DirectPosition(0,1);
    IDirectPosition p4 = new DirectPosition(1,0);
    IDirectPosition p5 = new DirectPosition(3,3);
    IDirectPosition p6 = new DirectPosition(4,4);
    IDirectPosition p7 = new DirectPosition(0.25,1);
    IDirectPosition p8 = new DirectPosition(0.75,1); 

    s1 = new HiddenState(new GM_LineString(
        new DirectPositionList(Arrays.asList(p1,p2))));
    s2 = new HiddenState(new GM_LineString(
        new DirectPositionList(Arrays.asList(p1,p3))));
    s3 = new HiddenState(new GM_LineString(
        new DirectPositionList(Arrays.asList(p1,p4))));
    s4 = new HiddenState(new GM_LineString(
        new DirectPositionList(Arrays.asList(p5,p6))));
    
    popH.add(s1);
    popH.add(s2);
    popH.add(s3);
    popH.add(s4);

    o = new Observation(new GM_LineString(new DirectPositionList(Arrays.asList(p7,p8))));
  }


  @After
  public void tearDown() {
    result.clear();
  }

  @Test
  public void testFilter1() {

    ParametersSet.get().SELECTION_THRESHOLD = 1;
    result = popH.filter(o);

    assertEquals(result.size(), 3);
    assertTrue(result.contains(s1));
    assertTrue(result.contains(s2));
    assertTrue(result.contains(s3));
    assertTrue(!result.contains(s4));
  }
  
  @Test
  public void testFilter2() {

    ParametersSet.get().SELECTION_THRESHOLD = 0.5;
    result = popH.filter(o);

    assertEquals(result.size(), 2);
    assertTrue(result.contains(s1));
    assertTrue(result.contains(s2));
    assertTrue(!result.contains(s4));
    assertTrue(!result.contains(s3));
  }

  @Test
  public void testFilter3() {
    ParametersSet.get().SELECTION_THRESHOLD = 5;
    result = popH.filter(o);

    assertEquals(result.size(), 4);
    assertTrue(result.contains(s1));
    assertTrue(result.contains(s2));
    assertTrue(result.contains(s3));
    assertTrue(result.contains(s4));
  }
  
  @Test
  public void testFilter4() {

    ParametersSet.get().SELECTION_THRESHOLD = 0.1;
    result = popH.filter(o);

    assertEquals(result.size(), 0);
  }
}
