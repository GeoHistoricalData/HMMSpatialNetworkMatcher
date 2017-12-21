package hmmmatching.impl;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import hmmmatching.api.AbstractHiddenState;
import hmmmatching.api.AbstractObservation;

/**
 * Hmm State used in HMM and viterbi algorithm
 * @author bcostes
 *
 */
public class MatchingState extends AbstractHiddenState<ACluster>{
  

  
  public MatchingState(ACluster state) {
    super(state);
  }


  @Override
  public double computeTransitionProbability(AbstractHiddenState<?> nextState,
      AbstractObservation<?> obs1, AbstractObservation<?> obs2, HMMParameters parameters) {
    
    ACluster aref1 = this.getThis();
    ACluster aref2 = ((MatchingState)nextState).getThis();

    Arc acomp1 = ((MatchingObservation)obs1).getThis();
    Arc acomp2 = ((MatchingObservation)obs2).getThis();
    
    ILineString geomRef1 = (ILineString)aref1.getGeometrie().clone();
    ILineString geomRef2 = (ILineString)aref2.getGeometrie().clone();
    ILineString geomComp1 = (ILineString)acomp1.getGeometrie().clone();
    ILineString geomComp2 = (ILineString)acomp2.getGeometrie().clone();



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


    if(aref1.equals(aref2)){
      IDirectPosition pcompMiddle = null;
      Angle angle1 = null;
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

      IDirectPosition pproj = Operateurs.projection(pcompMiddle, geomRef1);
      if(pproj.equals(geomRef1.startPoint()) ||
          pproj.equals(geomRef1.endPoint())){

        Angle angle2 = new Angle(0);
        double d = Angle.ecart(angle1, angle2).getValeur();
//        if(d*180/Math.PI > parameters.angleThreshold_TP){
//          return Double.NEGATIVE_INFINITY;
//        }
        double proba = -d*180/Math.PI ;
        return proba;
      }
      else{
        ILineString newline = Operateurs.projectionEtInsertion(pcompMiddle, geomRef1);
        IDirectPosition p1 = null, p2 = null;
        for(int i=0; i< newline.coord().size(); i++){
          if(newline.getControlPoint(i).equals(pproj)){
            p1 = newline.getControlPoint(i-1);
            p2 = newline.getControlPoint(i+1);
            break;
          }
        }
        Angle angle2 = Angle.angleTroisPoints(p1, pcompMiddle, p2);
        double d = Angle.ecart(angle1, angle2).getValeur();
//        if(d*180/Math.PI > parameters.angleThreshold_TP){
//          return Double.NEGATIVE_INFINITY;
//        }
        double proba = -d*180/Math.PI ;
        return proba;
      }

    }

    else{
      for(Arc a1: aref1.getArcs()){
        if(aref2.getArcs().contains(a1)){
          return Double.NEGATIVE_INFINITY;
        }
      }
      for(Arc a1: aref2.getArcs()){
        if(aref1.getArcs().contains(a1)){
          return Double.NEGATIVE_INFINITY;
        }
      }



      IDirectPosition prefini1 = geomRef1.startPoint();
      IDirectPosition preffin1= geomRef1.endPoint();
      IDirectPosition prefini2 = geomRef2.startPoint();
      IDirectPosition preffin2 = geomRef2.endPoint();

      IDirectPosition p1 = null, p2 = null, p3 = null;

      if(!prefini1.equals(prefini2) &&
          !prefini1.equals(preffin2) &&
          !preffin1.equals(prefini2)&&
          !preffin1.equals(preffin2)){
        return Double.NEGATIVE_INFINITY;
      }

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

      IDirectPosition pp1 = null, pp2 = null, pp3 = null;

      
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
//      if(d*180/Math.PI > parameters.angleThreshold_TP){
//        return Double.NEGATIVE_INFINITY;
//      }
      double proba = -d*180/Math.PI ;
      return proba;
    }
  }
}
