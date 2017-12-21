package fr.ign.cogit.v2.indicators.global;

import fr.ign.cogit.v2.indicators.IGlobalIndicator;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

/**
 * Nombre cyclomatique : nombre de cycles indépendants
 * @author bcostes
 * 
 */
public class Mu extends IGlobalIndicator {

  public Mu() {
    this.name = "Mu";
  }

  @Override
  public double calculate(JungSnapshot graph) {
    // on suppose le graphe connexe
    return (double) (graph.getEdgeCount() - graph.getVertexCount() + 1);
  }

}
