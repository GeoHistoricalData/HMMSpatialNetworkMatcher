package fr.ign.cogit.v2.indicators.global;

import fr.ign.cogit.v2.indicators.IGlobalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

public class TotalEdgeLength extends IGlobalIndicator {

  public TotalEdgeLength() {
    this.name = "Total Edge Length";
  }

  @Override
  public double calculate(JungSnapshot graph) {
    double sum = 0;
    for (GraphEntity e : graph.getEdges()) {
      sum += graph.getEdgesWeights().transform(e);
    }
    return sum;
  }

}
