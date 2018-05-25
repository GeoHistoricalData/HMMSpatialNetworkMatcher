package fr.ign.cogit.HMMSpatialNetworkMatcher.test_matching;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.Path;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.CompositeHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.HiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.Observation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.core.HmmMatchingIteration;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;

public class TestHmmMatchingIteration {

  static HmmMatchingIteration hmmMatchingIt;
  static Path path;
  static LinkedList<IHiddenState> states;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @Before
  public void setUp() {
    if(path != null) {
      path.clear();
    }
    if(states != null) {
      states.clear();
    }
    hmmMatchingIt = new HmmMatchingIteration(path, states);
  }


  @Test
  public void testMatch1() {
    IDirectPosition p1 = new DirectPosition(0,0);
    IDirectPosition p2 = new DirectPosition(1,0);
    IDirectPosition p3 = new DirectPosition(2,0);

    IGeometry line1 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p1,p2)));
    IGeometry line2 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p2,p3)));


    Observation obs1 = Mockito.mock(Observation.class);
    Mockito.when(obs1.getGeom()).thenReturn(line1);
    Observation obs2 = Mockito.mock(Observation.class);
    Mockito.when(obs2.getGeom()).thenReturn(line2);

    path = new Path(Arrays.asList(obs1, obs2));

    IDirectPosition p4 = new DirectPosition(0,-1);
    IDirectPosition p5 = new DirectPosition(1,-1);
    IDirectPosition p6 = new DirectPosition(2,-1);
    IDirectPosition p7 = new DirectPosition(0,1);
    IDirectPosition p8 = new DirectPosition(1,1);
    IDirectPosition p9 = new DirectPosition(2,1);

    IGeometry line3 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p4,p5)));
    IGeometry line4 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p5,p6)));
    IGeometry line5 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p7,p8)));
    IGeometry line6 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p8,p9)));


    HiddenState hd1 = Mockito.mock(HiddenState.class);
    Mockito.when(hd1.getGeom()).thenReturn(line3);
    HiddenState hd2 = Mockito.mock(HiddenState.class);
    Mockito.when(hd2.getGeom()).thenReturn(line4);
    HiddenState hd3 = Mockito.mock(HiddenState.class);
    Mockito.when(hd3.getGeom()).thenReturn(line5);
    HiddenState hd4 = Mockito.mock(HiddenState.class);
    Mockito.when(hd4.getGeom()).thenReturn(line6);

    states = new LinkedList<HiddenState>();
    states.addAll(Arrays.asList(hd1, hd2, hd3, hd4));

    CompositeHiddenState hdC1 = Mockito.mock(CompositeHiddenState.class);
    IGeometry line7 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p4, p5, p6)));
    Mockito.when(hdC1.getGeom()).thenReturn(line7);
    CompositeHiddenState hdC2 = Mockito.mock(CompositeHiddenState.class);
    IGeometry line8 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p7, p8, p9)));
    Mockito.when(hdC2.getGeom()).thenReturn(line8);

    Collection<HiddenState> l1 = new ArrayList<HiddenState>();
    l1.add(hd1);
    l1.add(hd2);
    l1.add(hd3);
    l1.add(hd4);
    l1.add(hdC1);
    l1.add(hdC2);
    Mockito.when(obs1.candidates(states)).thenReturn(l1);

    Mockito.when(obs1.computeEmissionProbability(hd1)).thenReturn(-10.);
    Mockito.when(obs1.computeEmissionProbability(hd2)).thenReturn(-40.);
    Mockito.when(obs1.computeEmissionProbability(hd3)).thenReturn(-8.);
    Mockito.when(obs1.computeEmissionProbability(hd4)).thenReturn(-48.);
    Mockito.when(obs1.computeEmissionProbability(hdC1)).thenReturn(-55.);
    Mockito.when(obs1.computeEmissionProbability(hdC2)).thenReturn(-45.);

    Mockito.when(obs2.candidates(states)).thenReturn(l1);
    Mockito.when(obs2.computeEmissionProbability(hd1)).thenReturn(-48.);
    Mockito.when(obs2.computeEmissionProbability(hd2)).thenReturn(-10.);
    Mockito.when(obs2.computeEmissionProbability(hd3)).thenReturn(-40.);
    Mockito.when(obs2.computeEmissionProbability(hd4)).thenReturn(-8.);
    Mockito.when(obs2.computeEmissionProbability(hdC1)).thenReturn(-55.);
    Mockito.when(obs2.computeEmissionProbability(hdC2)).thenReturn(-45.);

    Mockito.when(hd1.computeTransitionProbability(hd2, obs1, obs2)).thenReturn(-5.);
    Mockito.when(hd3.computeTransitionProbability(hd4, obs1, obs2)).thenReturn(-7.);
    Mockito.when(hdC1.computeTransitionProbability(hdC1, obs1, obs2)).thenReturn(-35.);
    Mockito.when(hdC2.computeTransitionProbability(hdC2, obs1, obs2)).thenReturn(-30.);
    Mockito.when(hd1.computeTransitionProbability(hd4, obs1, obs2)).thenReturn(Double.NEGATIVE_INFINITY);
    Mockito.when(hd3.computeTransitionProbability(hd2, obs1, obs2)).thenReturn(Double.NEGATIVE_INFINITY);


    hmmMatchingIt.setPath(path);
    hmmMatchingIt.setHiddenStates(states);

    hmmMatchingIt.match();

    assertEquals(hmmMatchingIt.getMatching().size(), 2);
    assertEquals(hmmMatchingIt.getMatching().get(obs1), hd3);
    assertEquals(hmmMatchingIt.getMatching().get(obs2), hd4);
  }

  @Test
  public void testMatch2() {

    IDirectPosition p1 = new DirectPosition(0,0);
    IDirectPosition p2 = new DirectPosition(1,0);
    IDirectPosition p3 = new DirectPosition(2,0);
    IDirectPosition p33 = new DirectPosition(3,0);


    IGeometry line1 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p1,p2)));
    IGeometry line2 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p2,p3)));
    IGeometry line33 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p3,p33)));

    Observation obs1 = Mockito.mock(Observation.class);
    Mockito.when(obs1.getGeom()).thenReturn(line1);
    Observation obs2 = Mockito.mock(Observation.class);
    Mockito.when(obs2.getGeom()).thenReturn(line2);
    Observation obs3 = Mockito.mock(Observation.class);
    Mockito.when(obs3.getGeom()).thenReturn(line33);

    path = new Path(Arrays.asList(obs1, obs2, obs3));

    IDirectPosition p4 = new DirectPosition(0,-1);
    IDirectPosition p5 = new DirectPosition(1,-1);
    IDirectPosition p7 = new DirectPosition(2,-1);
    IDirectPosition p8 = new DirectPosition(3,-1);


    IGeometry line3 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p4,p5)));

    IGeometry line6 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p7,p8)));

    HiddenState hd1 = Mockito.mock(HiddenState.class);
    Mockito.when(hd1.getGeom()).thenReturn(line3);
    HiddenState hd4 = Mockito.mock(HiddenState.class);
    Mockito.when(hd4.getGeom()).thenReturn(line6);

    states = new LinkedList<IHiddenState>();

    List<IHiddenState> l1 = new ArrayList<IHiddenState>();
    l1.add(hd1);
    Mockito.when(obs1.candidates(states)).thenReturn(l1);
    Mockito.when(obs1.computeEmissionProbability(hd1)).thenReturn(-10.);

    Mockito.when(obs2.candidates(states)).thenReturn(new ArrayList<IHiddenState>());


    List<IHiddenState> l2 = new ArrayList<IHiddenState>();
    l2.add(hd4);
    Mockito.when(obs3.candidates(states)).thenReturn(l2);
    Mockito.when(obs3.computeEmissionProbability(hd4)).thenReturn(-10.);


    Mockito.when(hd1.computeTransitionProbability(hd4, obs1, obs2)).thenReturn(Double.NEGATIVE_INFINITY);

    hmmMatchingIt.setPath(path);
    hmmMatchingIt.setHiddenStates(states);

    hmmMatchingIt.match();
    assertEquals(hmmMatchingIt.getMatching().size(), 2);
    assertEquals(hmmMatchingIt.getMatching().get(obs1), hd1);
    assertEquals(hmmMatchingIt.getMatching().get(obs3), hd4);
  }


  @Test
  public void testMatch3() {

    IDirectPosition p1 = new DirectPosition(0,0);
    IDirectPosition p2 = new DirectPosition(1,0);
    IDirectPosition p3 = new DirectPosition(2,0);
    IDirectPosition p4 = new DirectPosition(3,0);


    IGeometry line1 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p1,p2)));
    IGeometry line2 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p2,p3)));
    IGeometry line3 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p3,p4)));
    
    Observation obs1 = Mockito.mock(Observation.class);
    Mockito.when(obs1.getGeom()).thenReturn(line1);
    Observation obs2 = Mockito.mock(Observation.class);
    Mockito.when(obs2.getGeom()).thenReturn(line2);
    Observation obs3 = Mockito.mock(Observation.class);
    Mockito.when(obs3.getGeom()).thenReturn(line3);

    path = new Path(Arrays.asList(obs1, obs2, obs3));

    IDirectPosition p5 = new DirectPosition(0,-1);
    IDirectPosition p6 = new DirectPosition(2,-1);
    IDirectPosition p7 = new DirectPosition(3,-1);
    IDirectPosition p8 = new DirectPosition(4,1);

    IGeometry line4 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p5,p6)));
    IGeometry line5 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p6,p7)));
    IGeometry line6 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p7,p8)));


    HiddenState hd1 = Mockito.mock(HiddenState.class);
    Mockito.when(hd1.getGeom()).thenReturn(line4);
    HiddenState hd2 = Mockito.mock(HiddenState.class);
    Mockito.when(hd2.getGeom()).thenReturn(line5);
    HiddenState hd3 = Mockito.mock(HiddenState.class);
    Mockito.when(hd3.getGeom()).thenReturn(line6);

    states = new LinkedList<IHiddenState>();
    states.addAll(Arrays.asList(hd1, hd2, hd3));

    CompositeHiddenState hdC1 = Mockito.mock(CompositeHiddenState.class);
    IGeometry line7 = new GM_LineString(new DirectPositionList(
        Arrays.asList(p6, p7, p8)));
    Mockito.when(hdC1.getGeom()).thenReturn(line7);

    List<IHiddenState> l1 = new ArrayList<IHiddenState>();
    l1.add(hd1);
    Mockito.when(obs1.candidates(states)).thenReturn(l1);
    Mockito.when(obs1.computeEmissionProbability(hd1)).thenReturn(-25.);

    List<IHiddenState> l2 = new ArrayList<IHiddenState>();
    l2.add(hd1);
    l2.add(hd2);
    Mockito.when(obs2.candidates(states)).thenReturn(l2);
    Mockito.when(obs2.computeEmissionProbability(hd1)).thenReturn(-35.);
    Mockito.when(obs2.computeEmissionProbability(hd2)).thenReturn(-25.);

    List<IHiddenState> l3 = new ArrayList<IHiddenState>();
    l3.add(hd2);
    l3.add(hd3);
    l3.add(hdC1);
    Mockito.when(obs3.candidates(states)).thenReturn(l3);
    Mockito.when(obs3.computeEmissionProbability(hd2)).thenReturn(-25.);
    Mockito.when(obs3.computeEmissionProbability(hd3)).thenReturn(-25.);
    Mockito.when(obs3.computeEmissionProbability(hdC1)).thenReturn(-5.);
    
    Mockito.when(hd1.computeTransitionProbability(hd1, obs1, obs2)).thenReturn(-8.);
    Mockito.when(hd1.computeTransitionProbability(hd2, obs1, obs2)).thenReturn(-5.);
    Mockito.when(hd1.computeTransitionProbability(hd2, obs2, obs3)).thenReturn(-5.);
    Mockito.when(hd2.computeTransitionProbability(hd3, obs2, obs3)).thenReturn(-5.);
    Mockito.when(hd2.computeTransitionProbability(hdC1, obs2, obs3)).thenReturn(Double.NEGATIVE_INFINITY);
    Mockito.when(hd1.computeTransitionProbability(hd3, obs2, obs3)).thenReturn(Double.NEGATIVE_INFINITY);
    Mockito.when(hd1.computeTransitionProbability(hdC1, obs2, obs3)).thenReturn(-5.);


    hmmMatchingIt.setPath(path);
    hmmMatchingIt.setHiddenStates(states);

    hmmMatchingIt.match();

    System.out.println(hd1);
    System.out.println(hd2);
    System.out.println(hd3);
    System.out.println(hdC1);

    System.out.println(hmmMatchingIt.getMatching().size());
    System.out.println(hmmMatchingIt.getMatching().get(obs1));
    System.out.println(hmmMatchingIt.getMatching().get(obs2));
    System.out.println(hmmMatchingIt.getMatching().get(obs3));

    assertEquals(hmmMatchingIt.getMatching().size(), 3);
    assertEquals(hmmMatchingIt.getMatching().get(obs1), hd1);
    assertEquals(hmmMatchingIt.getMatching().get(obs2), hd1);
    assertEquals(hmmMatchingIt.getMatching().get(obs3), hdC1);
  }
}
