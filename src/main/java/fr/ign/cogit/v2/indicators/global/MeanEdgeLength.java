package fr.ign.cogit.v2.indicators.global;

import fr.ign.cogit.v2.indicators.IGlobalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

/**
 * Longueur moyenne des arcs (indicateur eta)
 * @author bcostes
 * 
 * @param <V>
 * @param <E>
 */
public class MeanEdgeLength extends IGlobalIndicator {

  public MeanEdgeLength() {
    this.name = "Edges Length";
  }

  @Override
  public double calculate(JungSnapshot graph) {
    double moy = 0;
    for (GraphEntity e : graph.getEdges()) {
      moy += graph.getEdgesWeights().transform(e);
    }
    moy /= ((double) graph.getEdgeCount());
    return moy;
  }

}
