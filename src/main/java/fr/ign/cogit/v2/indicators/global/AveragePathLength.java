package fr.ign.cogit.v2.indicators.global;

import fr.ign.cogit.v2.indicators.IGlobalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

public class AveragePathLength extends IGlobalIndicator {

  public AveragePathLength() {
    this.name = "Average Path Length";
  }

  @Override
  public double calculate(JungSnapshot graph) {
    double moy = 0;

    if (graph.getDistances() == null) {
      graph.cacheShortestPaths();
    }

    for (GraphEntity v : graph.getVertices()) {
      int index1 = graph.getNodeIndex(v);
      for (GraphEntity w : graph.getVertices()) {
        if (v.equals(w)) {
          continue;
        }
        int index2 = graph.getNodeIndex(w);
        moy += graph.getDistance(index1, index2);
      }
    }
    moy /= ((double) graph.getVertexCount() * (graph.getVertexCount() - 1));
    return moy;
  }

}
