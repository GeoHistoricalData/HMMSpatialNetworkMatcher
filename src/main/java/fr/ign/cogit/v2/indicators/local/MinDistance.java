package fr.ign.cogit.v2.indicators.local;

import java.util.HashMap;
import java.util.Map;

import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

/**
 * Eloignemement min
 * @author bcostes
 * 
 */
public class MinDistance extends ILocalIndicator {

  public MinDistance() {
    this.name = "MinD";
  }

  @Override
  public Map<GraphEntity, Double> calculateNodeCentrality(JungSnapshot graph, boolean normalize) {
    // ppc
    if (graph.getDistances() == null) {
      graph.cacheShortestPaths();
    }
    double MAX = Double.MIN_VALUE;
    Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
    for (GraphEntity v : graph.getVertices()) {
      double dmin = Double.MAX_VALUE;
      int index1 = graph.getNodeIndex(v);
      for (GraphEntity w : graph.getVertices()) {
        if (v.equals(w)) {
          continue;
        }
        int index2 = graph.getNodeIndex(w);
        double d = graph.getDistance(index1, index2);
        if (d < dmin) {
          dmin = d;
        }
      }
      if(dmin>MAX){
          MAX = dmin;
      }
      result.put(v, dmin);
    }
    if(normalize){
        for (GraphEntity v : graph.getVertices()) {
            result.put(v, result.get(v) / MAX);
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
    Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
    Map<GraphEntity, Double> nodesD = this.calculateNeighborhoodNodeCentrality(
        graph, k, normalize);
    for (GraphEntity e : graph.getEdges()) {
      result.put(e, (nodesD.get(graph.getEndpoints(e).getFirst()) + nodesD
          .get(graph.getEndpoints(e).getSecond())) / 2.);
    }
    return result;
  }

  @Override
  public Map<GraphEntity, Double> calculateNeighborhoodNodeCentrality(
      JungSnapshot graph, int k, boolean normalize) {
    Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
    double MAX = Double.MIN_VALUE;

    for (GraphEntity v : graph.getVertices()) {
      double dmin = Double.MAX_VALUE;
      int index1 = graph.getNodeIndex(v);
      for (GraphEntity w : graph.getKNeighborhood(v, k)) {
        if (v.equals(w)) {
          continue;
        }
        int index2 = graph.getNodeIndex(w);
        double d = graph.getDistance(index1, index2);
        if (d < dmin) {
          dmin = d;
        }
      }
      result.put(v, dmin);
      if(dmin>MAX){
          MAX = dmin;
      }
    }

    if(normalize){
        for (GraphEntity v : graph.getVertices()) {
            result.put(v, result.get(v) / MAX);
        }
    }
    return result;
  }
}
