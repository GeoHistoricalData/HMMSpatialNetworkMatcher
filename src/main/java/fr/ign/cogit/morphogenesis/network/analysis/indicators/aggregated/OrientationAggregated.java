package fr.ign.cogit.morphogenesis.network.analysis.indicators.aggregated;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.morphogenesis.network.analysis.indicators.isolated.Orientation;

public class OrientationAggregated {

  public static double[] mesure(IFeatureCollection<IFeature> features) {
    double[] result = new double[features.size()];

    int cpt = 0;
    for (IFeature f : features) {
      result[cpt] = Orientation.mesure(f.getGeom());
      cpt++;
    }

    return result;
  }

}
