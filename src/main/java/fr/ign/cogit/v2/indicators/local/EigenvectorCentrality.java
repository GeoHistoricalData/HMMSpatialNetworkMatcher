package fr.ign.cogit.v2.indicators.local;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

/**
 * Implémentation d'un algorithme d'itérations de puissances pour calculer le
 * vecteur propre associé à la plus grande valeur absolue de la matrice
 * d'adjacence
 * @author bcostes
 * 
 * @param <GraphEntity>
 * @param <GraphEntity>
 */
public class EigenvectorCentrality extends ILocalIndicator {

  public EigenvectorCentrality() {
    this.name = "Eig";
  }

  /**
   * Précisions de la convergence de l'algorithme itératif
   */
  private static final double epsilon = 0.0000001;
  /**
   * Nombre maximal d'itérations
   */
  private static final int MAX_ITERATIONS = 1000;

  @Override
  public Map<GraphEntity, Double> calculateNodeCentrality(JungSnapshot graph, boolean normalize) {
    Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
    for (GraphEntity v : graph.getVertices()) {
      result.put(v, 1.);
    }
    int cpt = 0;
    while (true) {
      Map<GraphEntity, Double> w = new HashMap<GraphEntity, Double>();
      for (GraphEntity v : graph.getVertices()) {
        w.put(v, 0.);
      }
      for (GraphEntity v : graph.getVertices()) {
        for (GraphEntity neighbor : graph.getNeighbors(v)) {
          Collection<GraphEntity> edges = graph.findEdgeSet(v, neighbor);
          // on prend le plus court
          double dmin = Double.MAX_VALUE;
          for (GraphEntity e : edges) {
            if (graph.getEdgesWeights().transform(e) < dmin) {
              dmin = graph.getEdgesWeights().transform(e);
            }
          }
          w.put(v, w.get(v) + dmin * result.get(neighbor));
        }
      }
      double sum = 0;
      for (GraphEntity v : graph.getVertices()) {
        sum += w.get(v) * w.get(v);
      }
      sum /= Math.sqrt(sum);
      for (GraphEntity v : graph.getVertices()) {
        w.put(v, w.get(v) / (double) sum);
      }
      boolean stop = true;
      // Ya t'il eu convergence ?
      for (GraphEntity v : graph.getVertices()) {
        if (Math.abs(result.get(v) - w.get(v)) > epsilon) {
          stop = false;
          break;
        }
      }
      if (stop) {
        break;
      }
      cpt++;
      for (GraphEntity v : graph.getVertices()) {
        result.put(v, w.get(v));
      }
      // Si on a atteint le nombre max d'itérations
      if (cpt >= MAX_ITERATIONS) {
        break;
      }
    }
    return result;
  }

  @Override
  public Map<GraphEntity, Double> calculateEdgeCentrality(JungSnapshot graph, boolean normalize) {
    Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
    Map<GraphEntity, Double> nodesD = this.calculateNodeCentrality(graph, normalize);
    for (GraphEntity e : graph.getEdges()) {
      result.put(e, (nodesD.get(graph.getEndpoints(e).getFirst()) + nodesD
          .get(graph.getEndpoints(e).getSecond())) / 2.);
    }
    return result;
  }

  @Override
  public Map<GraphEntity, Double> calculateNeighborhoodEdgeCentrality(
      JungSnapshot graph, int k, boolean normalize) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<GraphEntity, Double> calculateNeighborhoodNodeCentrality(
      JungSnapshot graph, int k, boolean normalize) {
    // TODO Auto-generated method stub
    return null;
  }
}
