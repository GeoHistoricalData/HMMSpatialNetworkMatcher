package fr.ign.cogit.morphogenesis.network.analysis.indicators.aggregated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class SegmentAngleAggregated {

  public static double[] mesure(IFeatureCollection<IFeature> pop) {

    pop.initSpatialIndex(Tiling.class, true);

    List<Double> angles = new ArrayList<Double>();

    for (IFeature f : pop) {
      Collection<IFeature> candidates = pop.select(f.getGeom(), 0);
      if (candidates.isEmpty()) {
        continue;
      } else {
        for (IFeature ff : candidates) {
          if (f.getGeom().equals(ff.getGeom())) {
            continue;
          }
          IDirectPosition intersection = null;
          for (IDirectPosition p1 : f.getGeom().coord()) {
            for (IDirectPosition p2 : ff.getGeom().coord()) {
              if (p1.equals(p2)) {
                intersection = p1;
                break;
              }
            }
            if (intersection != null) {
              break;
            }

          }
          IDirectPosition p1 = null, p2 = null;
          if (intersection.equals(f.getGeom().coord().get(0))) {
            p1 = f.getGeom().coord().get(1);
          } else {
            p1 = f.getGeom().coord().get(f.getGeom().coord().size() - 2);
          }
          if (intersection.equals(ff.getGeom().coord().get(0))) {
            p2 = ff.getGeom().coord().get(1);
          } else {
            p2 = ff.getGeom().coord().get(ff.getGeom().coord().size() - 2);
          }
          Angle angleij = Angle.angleAPiPres(Angle.angleTroisPoints(p1,
              intersection, p2));
          angles.add(angleij.getValeur());
        }
      }
    }

    double[] result = new double[angles.size()];
    for (int i = 0; i < angles.size(); i++) {
      result[i] = angles.get(i);
    }
    return result;
  }
}
