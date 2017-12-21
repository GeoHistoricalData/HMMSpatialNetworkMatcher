package fr.ign.cogit.morphogenesis.network.analysis.indicators.aggregated;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.morphogenesis.network.analysis.indicators.isolated.Length;

public class LengthAggregated {
  public static double[] mesure(IFeatureCollection<IFeature> pop) {
    double[] result = new double[pop.size()];
    int cpt = 0;
    for (IFeature feat : pop) {
      result[cpt] = Length.mesure(feat.getGeom());
      cpt++;
    }

    return result;
  }

}
