package fr.ign.cogit.HMMSpatialNetworkMatcher.impl.ep;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IEmissionProbablityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.HiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.Observation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Distances;

public class LineMedianDIstanceEmissionProbability  implements IEmissionProbablityStrategy{

  public double compute(IObservation obs, IHiddenState state) {
    // TODO Auto-generated method stub
    if(!(obs instanceof Observation) || !(state instanceof HiddenState)) {
      throw new RuntimeException("Parameters must extends Observation and HiddenState to"
          + "compute HausdorffEmissionProbability");
    }
        
    ILineString l1 = (ILineString)((Observation)obs).getGeom().clone();
    ILineString l2 = (ILineString)((HiddenState)state).getGeom().clone();
    return -Distances.lineMedianDistance2(l1, l2);
  }
  
}