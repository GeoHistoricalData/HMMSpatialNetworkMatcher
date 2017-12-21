package fr.ign.cogit.v2.indicators.global;

import java.util.Map;

import fr.ign.cogit.v2.indicators.IGlobalIndicator;
import fr.ign.cogit.v2.indicators.local.ClusteringCentrality;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

public class ClusteringCoefficient extends IGlobalIndicator {

  public ClusteringCoefficient() {
    this.name = "Clustering Coefficient";
  }

  @Override
  public double calculate(JungSnapshot graph) {
    Map<GraphEntity, Double> clustering = (new ClusteringCentrality())
        .calculateNodeCentrality(graph, true);
    double moy = 0;
    for (GraphEntity v : graph.getVertices()) {
      moy += clustering.get(v);
    }
    moy /= ((double) graph.getVertexCount());
    return moy;
  }

}
