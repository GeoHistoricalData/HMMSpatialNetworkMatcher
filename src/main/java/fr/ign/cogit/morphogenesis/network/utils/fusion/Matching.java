package fr.ign.cogit.morphogenesis.network.utils.fusion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;

public class Matching {

  public Matching() {
    this.map = new HashMap<ILineString, List<ILineString>>();
  }

  public Map<ILineString, List<ILineString>> map;

  // TODO : pour le moment, les linestring de match sont considérée comme
  // orientée toujours dans le sens graph1 -> graph2
  public static Matching buildMatching(Graph graph, Graph graph2,
      IPopulation<IFeature> match) {
    Matching m = new Matching();

    for (IFeature feature : match) {
      ILineString line = new GM_LineString(feature.getGeom().coord());
      ILineString feat1 = null, feat2 = null;
      for (ILineString line1 : graph.getEdges()) {
        if (line.getControlPoint(0).toGM_Point().distance(line1) < 0.0025) {
          feat1 = line1;
          for (ILineString line2 : graph2.getEdges()) {
            if (line.getControlPoint(line.coord().size() - 1).toGM_Point()
                .distance(line2) < 0.0025) {
              feat2 = line2;
              break;
            }
          }
          if (feat2 != null) {
            break;
          }
        }
      }

      if (feat1 != null && feat2 != null) {
        if (m.map.containsKey(feat1)) {
          m.map.get(feat1).add(feat1);
        } else {
          List<ILineString> l = new ArrayList<ILineString>();
          l.add(feat1);
          m.map.put(feat1, l);
        }
      }

    }

    return m;
  }
}
