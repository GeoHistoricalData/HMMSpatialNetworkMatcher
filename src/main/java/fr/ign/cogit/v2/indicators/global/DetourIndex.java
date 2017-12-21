package fr.ign.cogit.v2.indicators.global;

import java.util.Stack;

import fr.ign.cogit.v2.indicators.IGlobalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

/**
 * Détour : ratio entre longueur totale du graphe (distance euclidienne entre
 * les sommets) et la longueur réelle du réseau (ppc)
 * @author bcostes
 * 
 */
public class DetourIndex extends IGlobalIndicator {

  public DetourIndex() {
    this.name = "Detour Index";
  }

  @Override
  public double calculate(JungSnapshot graph) {
    double dE = 0, dR = 0;
    Stack<GraphEntity> stack = new Stack<GraphEntity>();
    stack.addAll(graph.getVertices());

    if (graph.getDistances() == null) {
      graph.cacheShortestPaths();
    }
    while (!stack.isEmpty()) {
      GraphEntity v = stack.pop();
      int index = graph.getNodeIndex(v);
      for (GraphEntity w : stack) {
        int index2 = graph.getNodeIndex(w);
        dR += graph.getDistance(index, index2);
        dE += ((GraphEntity) v).getGeometry().distance(
            ((GraphEntity) w).getGeometry());
      }
    }
    return dE / dR;
  }

}
