package fr.ign.cogit.morphogenesis.network.analysis.indicators;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.morphogenesis.network.analysis.indicators.aggregated.AreaAggregated;
import fr.ign.cogit.morphogenesis.network.analysis.indicators.aggregated.FeatureConvexityRateAggregated;
import fr.ign.cogit.morphogenesis.network.analysis.indicators.aggregated.FeatureElongationAggregated;
import fr.ign.cogit.morphogenesis.network.analysis.indicators.aggregated.FeatureStraightnessAggregated;
import fr.ign.cogit.morphogenesis.network.analysis.indicators.aggregated.FormFactorAggregated;
import fr.ign.cogit.morphogenesis.network.analysis.indicators.aggregated.LengthAggregated;
import fr.ign.cogit.morphogenesis.network.analysis.indicators.aggregated.OrientationAggregated;
import fr.ign.cogit.morphogenesis.network.analysis.indicators.aggregated.SegmentAngleAggregated;
import fr.ign.cogit.morphogenesis.network.graph.io.Indicators;

public class MainIndicator {
  public static double[] values(String indicatorName,
      IFeatureCollection<IFeature> pop) {
    double[] values = null;
    if (indicatorName.equals(Indicators.LENGTH)) {
      values = LengthAggregated.mesure(pop);
    } else if (indicatorName.equals(Indicators.AREA)) {
      values = AreaAggregated.mesure(pop);
    } else if (indicatorName.equals(Indicators.FORMS)) {
      values = FormFactorAggregated.mesure(pop);
    } else if (indicatorName.equals(Indicators.SMALLWORLD)) {
    } else if (indicatorName.equals(Indicators.STRAIGHTNESS)) {
      values = FeatureStraightnessAggregated.mesure(pop);
    } else if (indicatorName.equals(Indicators.ORIENTATION)) {
      values = OrientationAggregated.mesure(pop);
    } else if (indicatorName.equals(Indicators.INTERSECTION)) {
      values = SegmentAngleAggregated.mesure(pop);
    } else if (indicatorName.equals(Indicators.ELONGATION)) {
      values = FeatureElongationAggregated.mesure(pop);
    } else if (indicatorName.equals(Indicators.CONVEXITY)) {
      values = FeatureConvexityRateAggregated.mesure(pop);
    }
    return values;
  }
}
