package fr.ign.cogit.HMMSpatialNetworkMatcher.impl.ep;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IEmissionProbablityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.HiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.Observation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;

public class DirectionDifferenceEmissionProbability implements IEmissionProbablityStrategy{

  @Override
  public double compute(IObservation obs, IHiddenState state) {
    // TODO Auto-generated method stub
    if(!(obs instanceof Observation) || !(state instanceof HiddenState)) {
      throw new RuntimeException("Parameters must extends Observation and HiddenState to"
          + "compute HausdorffEmissionProbability");
    }

    ILineString l1 = (ILineString)((Observation)obs).getGeom().clone();
    ILineString l2 = (ILineString)((HiddenState)state).getGeom().clone();
    //
    Angle ori1 = Operateurs.directionPrincipale(l1.getControlPoint());
    Angle ori2 = Operateurs.directionPrincipale(l2.getControlPoint());
    Angle ori3 = Operateurs.directionPrincipale(l2.reverse().getControlPoint());
    double value = Math.min(Angle.ecart(ori1, ori2).getValeur()*180./Math.PI,
        Angle.ecart(ori1, ori3).getValeur()*180./Math.PI); 
    return -value;
  }

}

