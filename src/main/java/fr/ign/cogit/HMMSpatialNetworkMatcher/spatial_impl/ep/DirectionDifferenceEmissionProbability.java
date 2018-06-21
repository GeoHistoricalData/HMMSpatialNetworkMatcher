package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.ep;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IEmissionProbablityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.FeatHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.FeatObservation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;

/**
 * Emission probability strategy that computes global orientation difference
 * between an Observation and a HiddenState.
 * @author bcostes
 *
 */
public class DirectionDifferenceEmissionProbability implements IEmissionProbablityStrategy{

  /**
   * Given IObservation and IHiddenState must extends Observation and HiddenState.
   */
  @Override
  public double compute(IObservation obs, IHiddenState state) {
    if(!(obs instanceof FeatObservation) || !(state instanceof FeatHiddenState)) {
      throw new RuntimeException("Parameters must extends Observation and HiddenState to"
          + "compute HausdorffEmissionProbability");
    }

    ILineString l1 = (ILineString)((FeatObservation)obs).getGeom().clone();
    ILineString l2 = (ILineString)((FeatHiddenState)state).getGeom().clone();
    //
    Angle ori1 = Operateurs.directionPrincipale(l1.getControlPoint());
    Angle ori2 = Operateurs.directionPrincipale(l2.getControlPoint());
    if(ori1.getValeur() > Math.PI/2){
      ori1 = new Angle(Math.PI - ori1.getValeur());
    }
    if(ori2.getValeur() > Math.PI/2){
      ori2 = new Angle(Math.PI - ori2.getValeur());
    }
    double value = Angle.ecart(ori1, ori2).getValeur()*180./Math.PI;
    return -value;
  }

}

