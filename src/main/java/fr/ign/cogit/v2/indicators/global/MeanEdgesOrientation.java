package fr.ign.cogit.v2.indicators.global;

import java.util.List;

import fr.ign.cogit.v2.indicators.IGlobalIndicator;
import fr.ign.cogit.v2.indicators.others.EdgesOrientation;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

public class MeanEdgesOrientation extends IGlobalIndicator {

  public MeanEdgesOrientation() {
    this.name = "Mean Edges Orientation";
  }

  @Override
  public double calculate(JungSnapshot graph) {
    List<Double> or = (new EdgesOrientation())
        .calculateGeometricalIndicator(graph);
    double sum = 0;
    for (Double ori : or) {
      sum += ori;
    }
    sum /= (double) graph.getEdgeCount();
    return sum;
  }

}
