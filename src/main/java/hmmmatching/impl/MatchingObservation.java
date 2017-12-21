package hmmmatching.impl;

import java.util.ArrayList;
import java.util.Collection;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.contrib.geometrie.Distances;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.distance.Frechet;
import hmmmatching.api.AbstractHiddenState;
import hmmmatching.api.AbstractObservation;

public class MatchingObservation extends AbstractObservation<Arc>{
  

  public MatchingObservation(Arc observation) {
    super(observation);
  }


  public AClusterCollection candidates(CarteTopo net, double selection) {
    Collection<Arc> candidates = net.getPopArcs().select(this.getThis().getGeom(), selection); 
    AClusterCollection clustercol = new AClusterCollection(new ArrayList<Arc>(candidates));
    return clustercol;
  }




  @Override
  public double computeEmissionProbability(AbstractHiddenState<?> state, HMMParameters parameters) {

    if(!(state instanceof MatchingState)){
      try {
        throw new Exception("Uncompatible types for HMM state, HMMMatchingState expected");
      } catch (Exception e) {
        e.printStackTrace();
      }
      return Double.NEGATIVE_INFINITY;
    }

    Arc a1 = this.getThis();
    ACluster a2 = ((MatchingState)state).getThis();

    ILineString l1 = Operateurs.resampling(
        a1.getGeometrie(),50);
    ILineString l2 = Operateurs.resampling(
        a2.getGeometrie(),50);
    
    double d1 = Distances.premiereComposanteHausdorff(a1.getGeometrie(),a2.getGeometrie());
    double d2 = Distances.premiereComposanteHausdorff(a2.getGeometrie(),a1.getGeometrie());
    double distance = Math.min(d1,d2);
    


    //
    Angle ori1 = Operateurs.directionPrincipale(l1.getControlPoint());
    Angle ori2 = Operateurs.directionPrincipale(l2.getControlPoint());
    Angle ori3 = Operateurs.directionPrincipale(l2.reverse().getControlPoint());
    double value = Math.min(Angle.ecart(ori1, ori2).getValeur()*180./Math.PI,
        Angle.ecart(ori1, ori3).getValeur()*180./Math.PI);          



    /*
     * La probabilité d'émission est calculée par distance de Fréchet
     */


    double distance2 = Math.min(Frechet.discreteFrechet(l1,l2),
        Frechet.discreteFrechet(l1.reverse(), l2));

//    
//    double diffl = Math.abs(a1.longueur() - a2.longueur());
//    double dh = Math.max(d1, d2);
    

    //  double distance2 = Math.max(d1, d2);
    double proba = -(distance2 + distance + value );

    return proba;

  }

}
