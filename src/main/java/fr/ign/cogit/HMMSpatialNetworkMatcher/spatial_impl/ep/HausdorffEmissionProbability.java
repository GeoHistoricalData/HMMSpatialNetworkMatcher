package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.ep;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IEmissionProbablityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.FeatHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.FeatObservation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Distances;

public class HausdorffEmissionProbability implements IEmissionProbablityStrategy{

  public double compute(IObservation obs, IHiddenState state) {
    // TODO Auto-generated method stub
    if(!(obs instanceof FeatObservation) || !(state instanceof FeatHiddenState)) {
      throw new RuntimeException("Parameters must extends Observation and HiddenState to"
          + "compute HausdorffEmissionProbability");
    }
        
    ILineString l1 = (ILineString)((FeatObservation)obs).getGeom().clone();
    ILineString l2 = (ILineString)((FeatHiddenState)state).getGeom().clone();
    double distance = Distances.hausdorff(l1, l2);
    return -distance;
  }
  
}
