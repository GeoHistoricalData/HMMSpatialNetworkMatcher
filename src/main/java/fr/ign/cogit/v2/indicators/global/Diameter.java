package fr.ign.cogit.v2.indicators.global;

import java.util.Stack;

import fr.ign.cogit.v2.indicators.IGlobalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

/**
 * Diamètre : plus grande distance entre deux sommets selon les ppc, i.e plus
 * long des ppc
 * @author bcostes
 * 
 * @param <V>
 * @param <E>
 */
public class Diameter extends IGlobalIndicator {

  public Diameter() {
    this.name = "Diameter";
  }

  @Override
  public double calculate(JungSnapshot graph) {
    double dmax = -1;
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
        double d = graph.getDistance(index, index2);
        if (d > dmax) {
          dmax = d;
        }
      }
    }
    return dmax;
  }

}
