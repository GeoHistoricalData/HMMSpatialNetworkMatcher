package fr.ign.cogit.HMMSpatialNetworkMatcher.test_impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.HiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.Observation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.tp.AngularTransitionProbability;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;

public class TestAngularTransitionProbability {
  
  static Observation obs1, obs2;
  static HiddenState currentState, nextState;
  static AngularTransitionProbability proba;
  
  @BeforeClass
  public static void setUpBeforeClass() {
    proba = new AngularTransitionProbability();
  }
  
  @Before
  public void setUp() {
    IDirectPosition p1 = new DirectPosition(0,0);
    IDirectPosition p2 = new DirectPosition(1,1);
    IDirectPosition p3 = new DirectPosition(2,0);

    obs1 = new Observation(new GM_LineString(new DirectPositionList(Arrays.asList(p1,p2))));
    obs2 = new Observation(new GM_LineString(new DirectPositionList(Arrays.asList(p2,p3))));
  }


  /**
   * Test lorsque les deux observations ne sont pas connectées
   */
  @Test
  public void testCompute1() {
    IDirectPosition p1 = new DirectPosition(0,0);
    IDirectPosition p2 = new DirectPosition(1,0);
    IDirectPosition p3 = new DirectPosition(2,2);
    IDirectPosition p4 = new DirectPosition(3,3);

    obs1 = new Observation(new GM_LineString(new DirectPositionList(Arrays.asList(p1,p2))));
    obs2 = new Observation(new GM_LineString(new DirectPositionList(Arrays.asList(p3,p4))));
    currentState = new HiddenState(new GM_LineString(new DirectPositionList()));
    nextState = new HiddenState(new GM_LineString(new DirectPositionList()));

    assertEquals(proba.compute(obs1, currentState, obs2, nextState), Double.NEGATIVE_INFINITY, 0.001);
  }


  /**
   * Test lorsque les deux états cachés ne sont pas connectées
   */
  @Test
  public void testCompute2() {
    IDirectPosition p1 = new DirectPosition(0,0);
    IDirectPosition p2 = new DirectPosition(1,0);
    IDirectPosition p3 = new DirectPosition(2,2);
    IDirectPosition p4 = new DirectPosition(3,3);

    currentState = new HiddenState(new GM_LineString(new DirectPositionList(Arrays.asList(p1,p2))));
    nextState = new HiddenState(new GM_LineString(new DirectPositionList(Arrays.asList(p3,p4))));
    obs1 = new Observation(new GM_LineString(new DirectPositionList(Arrays.asList(p1,p2))));
    obs2 = new Observation(new GM_LineString(new DirectPositionList(Arrays.asList(p1,p2))));

    assertEquals(proba.compute(obs1, currentState, obs2, nextState), Double.NEGATIVE_INFINITY, 0.001);
  }
  
  /**
   * Test lorsque les deux états sont indentiques
   */
  @Test
  public void testCompute3() {
    // test1
    IDirectPosition p1 = new DirectPosition(1,0);
    IDirectPosition p2 = new DirectPosition(3,2);
    currentState = new HiddenState(new GM_LineString(new DirectPositionList(Arrays.asList(p1,p2))));
    nextState = currentState;
    assertEquals(proba.compute(obs1, currentState, obs2, nextState),-90.0 , 0.001);
    
    p1 = new DirectPosition(0,1);
    p2 = new DirectPosition(1,2);
    IDirectPosition p3 = new DirectPosition(2,1);
    currentState = new HiddenState(new GM_LineString(new DirectPositionList(Arrays.asList(p1,p2,p3))));
    nextState = currentState;
    assertEquals(proba.compute(obs1, currentState, obs2, nextState),0 , 0.001);
  }
  
  /**
   * Test lorsque les deux états sont différents
   */
  @Test
  public void testCompute4() {
    // test1
    IDirectPosition p1 = new DirectPosition(0,1);
    IDirectPosition p2 = new DirectPosition(1,2);
    IDirectPosition p3 = new DirectPosition(2,3);

    currentState = new HiddenState(new GM_LineString(new DirectPositionList(Arrays.asList(p1,p2))));
    nextState = new HiddenState(new GM_LineString(new DirectPositionList(Arrays.asList(p2,p3))));;
    assertEquals(proba.compute(obs1, currentState, obs2, nextState),-90.0 , 0.001);

    // test2
    currentState = new HiddenState(new GM_LineString(new DirectPositionList(Arrays.asList(p3,p2))));
    nextState = new HiddenState(new GM_LineString(new DirectPositionList(Arrays.asList(p2,p1))));
    // car on ne parcourt pas les arcs dans le bon sens
    assertEquals(proba.compute(obs1, currentState, obs2, nextState), Double.NEGATIVE_INFINITY , 0.001);
    
    // test3
    p1 = new DirectPosition(0,1);
    p2 = new DirectPosition(1,0);
    p3 = new DirectPosition(2,1);

    currentState = new HiddenState(new GM_LineString(new DirectPositionList(Arrays.asList(p1,p2))));
    nextState = new HiddenState(new GM_LineString(new DirectPositionList(Arrays.asList(p2,p3))));;
    assertEquals(proba.compute(obs1, currentState, obs2, nextState),-180.0 , 0.001);

  }
}
