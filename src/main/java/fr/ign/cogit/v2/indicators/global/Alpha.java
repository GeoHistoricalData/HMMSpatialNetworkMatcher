package fr.ign.cogit.v2.indicators.global;

import fr.ign.cogit.v2.indicators.IGlobalIndicator;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

/**
 * RAtio nombre cyclomatique / nombre maximum de cycles possible
 * @author bcostes
 * 
 * @param <V>
 * @param <E>
 */
public class Alpha extends IGlobalIndicator {

  public Alpha() {
    this.name = "Alpha";
  }

  @Override
  public double calculate(JungSnapshot graph) {
    double mu = (new Mu()).calculate(graph);
    return mu / (2 * graph.getVertexCount() - 5);
  }

}
