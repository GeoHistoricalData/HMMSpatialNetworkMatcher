package fr.ign.cogit.v2.indicators.global;

import fr.ign.cogit.v2.indicators.IGlobalIndicator;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

/**
 * rapport entre le nombre d’arcs et le nombre de sommets du graphe ([Berge,
 * 1973]). C’est une mesure de la densité du réseau : plus le nombre de liens
 * est important, plus la densité augmente si le nombre de sommet est fixe
 */
public class Beta extends IGlobalIndicator {

  public Beta() {
    this.name = "Beta";
  }

  @Override
  public double calculate(JungSnapshot graph) {
    return ((double) graph.getEdgeCount()) / ((double) graph.getVertexCount());
  }

}
