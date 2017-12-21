package fr.ign.cogit.v2.indicators.global;

import fr.ign.cogit.v2.indicators.IGlobalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

public class MeanNodeDegree extends IGlobalIndicator {

  public MeanNodeDegree() {
    this.name = "Mean Degree";
  }

  @Override
  public double calculate(JungSnapshot graph) {
    double moy = 0;
    for (GraphEntity v : graph.getVertices()) {
      moy += graph.getDegre(v);
    }
    moy /= ((double) graph.getVertexCount());
    return moy;
  }

}
