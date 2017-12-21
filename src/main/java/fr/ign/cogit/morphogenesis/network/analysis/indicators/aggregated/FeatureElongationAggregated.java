package fr.ign.cogit.morphogenesis.network.analysis.indicators.aggregated;

import java.util.Collection;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopoFactory;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Face;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.morphogenesis.network.analysis.indicators.isolated.FeatureElongation;

public class FeatureElongationAggregated {
  public static double[] mesure(Collection<IFeature> features) {
    IFeatureCollection<IFeature> inputFeatureCollectionCorrected = new Population<IFeature>();
    for (IFeature feat : features) {
      if (feat.getGeom() instanceof IMultiCurve) {
        for (int i = 0; i < ((IMultiCurve<?>) feat.getGeom()).size(); i++) {
          inputFeatureCollectionCorrected.add(new DefaultFeature(
              ((IMultiCurve<?>) feat.getGeom()).get(i)));
        }
      } else {
        inputFeatureCollectionCorrected.add(new DefaultFeature(feat.getGeom()));
      }
    }
    CarteTopo map = CarteTopoFactory
        .newCarteTopo(inputFeatureCollectionCorrected);
    map.creeTopologieFaces();
    double[] result = new double[map.getListeFaces().size()];
    int cpt = 0;
    for (Face f : map.getListeFaces()) {
      double area = FeatureElongation.mesure(f.getGeom());
      result[cpt] = area;
      cpt++;
    }

    return result;
  }
}
