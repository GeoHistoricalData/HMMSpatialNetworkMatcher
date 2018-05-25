package fr.ign.cogit.HMMSpatialNetworkMatcher.test_impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.CompositeHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.HiddenState;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;

public class TestCompositeHiddenState {

  static List<HiddenState> states;
  static HiddenState s1, s2;
  static CompositeHiddenState cstate;
  
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    IDirectPosition p1 = new DirectPosition(0,0);
    IDirectPosition p2 = new DirectPosition(1,1);
    IDirectPosition p3 = new DirectPosition(2,2);

    
    s1 = new HiddenState(new GM_LineString(
        new DirectPositionList(Arrays.asList(p1,p2))));
    s2 = new HiddenState(new GM_LineString(
        new DirectPositionList(Arrays.asList(p2,p3))));
    states = Arrays.asList(s1,s2);
    cstate = new CompositeHiddenState(states);
  }
  

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testAdd() {
    IDirectPosition p1 = new DirectPosition(2,2);
    IDirectPosition p2 = new DirectPosition(3,3);
    HiddenState s = new HiddenState(new GM_LineString(
        new DirectPositionList(Arrays.asList(p1,p2))));
    cstate.add(s);
    assertEquals(cstate.getStates().size(), 3);
    assertTrue(cstate.getStates().contains(s));
  }

  @Test
  public void testGetStates() {
    assertEquals(cstate.getStates().size(), 2);
    assertTrue(cstate.getStates().contains(s1));
    assertTrue(cstate.getStates().contains(s2));
  }

  @Test
  public void testComputeGeometry() {
    IDirectPosition p1 = new DirectPosition(0,0);
    IDirectPosition p2 = new DirectPosition(1,1);
    IDirectPosition p3 = new DirectPosition(2,2);

    assertEquals(cstate.getGeom(), new GM_LineString(new DirectPositionList(Arrays.asList(p1,p2,p3))));
    
    IDirectPosition p4 = new DirectPosition(3,3);

    
    s1 = new HiddenState(new GM_LineString(
        new DirectPositionList(Arrays.asList(p1,p2))));
    s2 = new HiddenState(new GM_LineString(
        new DirectPositionList(Arrays.asList(p4,p3))));
    HiddenState s3 = new HiddenState(new GM_LineString(
        new DirectPositionList(Arrays.asList(p2,p3))));
    states = Arrays.asList(s1,s2, s3);
    cstate = new CompositeHiddenState(states);
    
    assertEquals(cstate.getGeom(), new GM_LineString(new DirectPositionList(Arrays.asList(p1,p2,p3,p4))));
  }

}
