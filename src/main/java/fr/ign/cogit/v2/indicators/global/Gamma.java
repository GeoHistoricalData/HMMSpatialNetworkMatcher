package fr.ign.cogit.v2.indicators.global;

import fr.ign.cogit.v2.indicators.IGlobalIndicator;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

/**
 * Version standardisée de Beta : rapport nombre d'arc / nombre d'arc max
 * possible
 * @author bcostes
 * 
 */
public class Gamma extends IGlobalIndicator {

  public Gamma() {
    this.name = "Gamma";
  }

  @Override
  public double calculate(JungSnapshot graph) {
    double max = 3 * ((double)graph.getVertexCount() -2.);
    return ((double) graph.getEdgeCount()) / max;
  }

}
