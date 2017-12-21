package fr.ign.cogit.morphogenesis.network.analysis.indicators.aggregated;

import java.util.Collection;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.morphogenesis.network.analysis.indicators.isolated.FeatureStraightness;

public class FeatureStraightnessAggregated {
  public static double[] mesure(Collection<IFeature> features) {
    double[] result = new double[features.size()];
    int cpt = 0;
    for (IFeature f : features) {
      double area = FeatureStraightness.mesure(f.getGeom());
      result[cpt] = area;
      cpt++;
    }

    return result;
  }

}
