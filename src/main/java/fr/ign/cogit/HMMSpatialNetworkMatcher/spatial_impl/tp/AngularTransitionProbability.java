package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.tp;

import java.util.ArrayList;
import java.util.Arrays;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.ITransitionProbabilityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.CompositeHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.FeatHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.FeatObservation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;

public class AngularTransitionProbability implements ITransitionProbabilityStrategy{

  public double compute(IObservation obs1, IHiddenState currentState,
      IObservation obs2, IHiddenState nextState) {

    if(!(obs1 instanceof FeatObservation) || !(obs2 instanceof FeatObservation) || 
        !(currentState instanceof FeatHiddenState) || !(nextState instanceof FeatHiddenState)) {
      throw new RuntimeException("Parameters must extends Observation and HiddenState to"
          + "compute AngularTransitionProbability");
    }

    ILineString geomRef1 = (ILineString)((FeatHiddenState)currentState).getGeom().clone();
    ILineString geomRef2 = (ILineString)((FeatHiddenState)nextState).getGeom().clone();
    ILineString geomComp1 = (ILineString)((FeatObservation)obs1).getGeom().clone();
    ILineString geomComp2 = (ILineString)((FeatObservation)obs2).getGeom().clone();



    if(!geomComp1.startPoint().equals(geomComp2.startPoint()) &&
        !geomComp1.endPoint().
        equals(geomComp2.startPoint()) &&
        !geomComp1.startPoint().
        equals(geomComp2.endPoint()) &&
        !geomComp1.endPoint().equals(
            geomComp2.endPoint())){
      return Double.NEGATIVE_INFINITY;
    } 

    if(!geomRef1.startPoint().equals(geomRef2.startPoint()) &&
        !geomRef1.endPoint().
        equals(geomRef2.startPoint()) &&
        !geomRef1.startPoint().
        equals(geomRef2.endPoint()) &&
        !geomRef1.endPoint().equals(
            geomRef2.endPoint())){
      return Double.NEGATIVE_INFINITY;
    } 


    if(currentState.equals(nextState)){
      // on récupère le point intermédiaire sur la polyligne obs1 + obs2
      IDirectPosition pcompMiddle;
      Angle angle1;
      if(geomComp1.startPoint().equals(geomComp2.startPoint())){
        pcompMiddle = geomComp1.startPoint();
        angle1 =  Angle.angleTroisPoints(
            geomComp1.getControlPoint(1), pcompMiddle, geomComp2.getControlPoint(1));
      }
      else if(geomComp1.startPoint().equals(geomComp2.endPoint())){
        pcompMiddle = geomComp1.startPoint();
        angle1 =  Angle.angleTroisPoints(
            geomComp1.getControlPoint(1), pcompMiddle, geomComp2.getControlPoint(
                geomComp2.getControlPoint().size()-2));
      }
      else if(geomComp1.endPoint().equals(geomComp2.startPoint())){
        pcompMiddle = geomComp1.endPoint();
        angle1 =  Angle.angleTroisPoints(
            geomComp1.getControlPoint(geomComp1.getControlPoint().size()-2),
            pcompMiddle, geomComp2.getControlPoint(1));
      }
      else{
        pcompMiddle = geomComp1.endPoint();
        angle1 =  Angle.angleTroisPoints(
            geomComp1.getControlPoint(geomComp1.getControlPoint().size()-2),
            pcompMiddle, geomComp2.getControlPoint(geomComp2.getControlPoint().size()-2));
      }

      // on récupère son abscisse curviligne (en % de la longueur de la polyligne fusionnée)
      ILineString lF = Operateurs.union(new ArrayList<>(Arrays.asList(geomComp1, geomComp2)));
      double posMiddle = lF.paramForPoint(pcompMiddle)[0];
      // en relatif
      double posMiddleR = posMiddle / lF.length();
      // abscisse curv corresondante sur l'autre ligne
      double posMiddleRef = posMiddleR * geomRef1.length();
      // le point à cet abscisse
      IDirectPosition pMiddleRef = geomRef1.param(posMiddleRef);
      // le point juste avant et juste apres
      // si premier point = pMiddleRef ou dernier point = pMiddleRef
      if(geomRef1.startPoint().equals(pMiddleRef) 
          ||geomRef1.endPoint().equals(pMiddleRef)) {
        Angle angle2 = new Angle(0);
        double d = Angle.ecart(angle1, angle2).getValeur();
        return -d;
      }
      IDirectPosition p1 = geomRef1.startPoint();
      IDirectPosition p2 = geomRef1.endPoint();
      Angle angle2 = Angle.angleTroisPoints(p1, pMiddleRef, p2);

      double d = Angle.ecart(angle1, angle2).getValeur();
      return -d;
    }

    else{

      if(currentState instanceof CompositeHiddenState) {
        for(FeatHiddenState a1: ((CompositeHiddenState) currentState).getStates()){
          if(nextState instanceof CompositeHiddenState) {
            if(((CompositeHiddenState) nextState).getStates().contains(a1)) {
              return Double.NEGATIVE_INFINITY;
            }
          }
          else {
            if(a1.equals(nextState)) {
              return Double.NEGATIVE_INFINITY;
            }
          }
        }
      }

      if(nextState instanceof CompositeHiddenState) {
        for(FeatHiddenState a1: ((CompositeHiddenState) nextState).getStates()){
          if(currentState instanceof CompositeHiddenState) {
            if(((CompositeHiddenState) currentState).getStates().contains(a1)) {
              return Double.NEGATIVE_INFINITY;
            }
          }
          else {
            if(a1.equals(currentState)) {
              return Double.NEGATIVE_INFINITY;
            }
          }
        }
      }



      IDirectPosition prefini1 = geomRef1.startPoint();
      IDirectPosition preffin1= geomRef1.endPoint();
      IDirectPosition prefini2 = geomRef2.startPoint();
      IDirectPosition preffin2 = geomRef2.endPoint();

      IDirectPosition p1, p2, p3;

      if(prefini1.equals(prefini2)){
        p1 =  geomRef1.getControlPoint(1);
        p2 = geomRef1.startPoint();
        p3 = geomRef2.getControlPoint(1);
      }
      else if(prefini1.equals(preffin2)){
        p1 =  geomRef1.getControlPoint(1);
        p2 = geomRef1.startPoint();
        p3 = geomRef2.getControlPoint(geomRef2.getControlPoint().size()-2);
      }
      else if(preffin1.equals(prefini2)){
        p1 = geomRef1.getControlPoint( geomRef1.getControlPoint().size()-2);
        p2 = geomRef2.startPoint();
        p3 = geomRef2.getControlPoint(1);
      }
      else{
        p1 =  geomRef1.getControlPoint( geomRef1.getControlPoint().size()-2);
        p2 = geomRef1.endPoint();
        p3 = geomRef2.getControlPoint(geomRef2.getControlPoint().size()-2);
      }
      Angle angle1 = Angle.angleTroisPoints(p1, p2, p3);

      IDirectPosition pcompini1 = geomComp1.startPoint();
      IDirectPosition pcompfin1 = geomComp1.endPoint();
      IDirectPosition pcompini2 = geomComp2.startPoint();
      IDirectPosition pcompfin2 = geomComp2.endPoint();

      IDirectPosition pp1, pp2, pp3;


      if(pcompini1.equals(pcompini2)){
        pp1 =  geomComp1.getControlPoint(1);
        pp2 = geomComp1.startPoint();
        pp3 = geomComp2.getControlPoint(1);
      }
      else if(pcompini1.equals(pcompfin2)){
        pp1 =  geomComp1.getControlPoint(1);
        pp2 = geomComp1.startPoint();
        pp3 = geomComp2.getControlPoint(geomComp2.getControlPoint().size()-2);
      }
      else if(pcompfin1.equals(pcompini2)){
        pp1 = geomComp1.getControlPoint( geomComp1.getControlPoint().size()-2);
        pp2 = geomComp2.startPoint();
        pp3 = geomComp2.getControlPoint(1);
      }
      else{
        pp1 =  geomComp1.getControlPoint( geomComp1.getControlPoint().size()-2);
        pp2 = geomComp1.endPoint();
        pp3 = geomComp2.getControlPoint(geomComp2.getControlPoint().size()-2);
      }


      // on va  translater pp1 de (p2-pp2)et calculer l'angle p1 p2 pp1
      double tx = (p2.getX() - pp2.getX());
      double ty = (p2.getY() - pp2.getY());
      IDirectPosition newpp1 = new DirectPosition(pp1.getX() + tx, pp1.getY() + ty);
      Angle ecart = Angle.angleTroisPoints(p1, p2, newpp1);
      if(ecart.getValeur() > Math.PI){
        ecart = new Angle(2*Math.PI - ecart.getValeur());
      }
      if(ecart.getValeur() > Math.PI / 2.){
        //on ne parcourt pas les arcs dans le bon sens ...
        return Double.NEGATIVE_INFINITY;
      }
      double tx2 = (p2.getX() - pp2.getX());
      double ty2 = (p2.getY() - pp2.getY());
      IDirectPosition newpp3 = new DirectPosition(pp3.getX() + tx2, pp3.getY() + ty2);
      Angle ecart2 = Angle.angleTroisPoints(p3, p2, newpp3);
      if(ecart2.getValeur() > Math.PI){
        ecart2 = new Angle(2*Math.PI - ecart2.getValeur());
      }
      if(ecart2.getValeur() > Math.PI / 2.){
        //on ne parcourt pas les arcs dans le bon sens ...
        return Double.NEGATIVE_INFINITY;

      }

      Angle angle2 = Angle.angleTroisPoints(pp1, pp2 , pp3);
      double d = Angle.ecart(angle1, angle2).getValeur();

      return -d;
    }

  }

}