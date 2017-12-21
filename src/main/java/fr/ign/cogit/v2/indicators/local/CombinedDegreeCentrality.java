package fr.ign.cogit.v2.indicators.local;

import java.util.HashMap;
import java.util.Map;

import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

public class CombinedDegreeCentrality extends ILocalIndicator {

  private double alpha = 0.5;

  public CombinedDegreeCentrality() {
    this.name = "Combi";
  }

  public CombinedDegreeCentrality(double _alpha) {
    this.name = "Combined Centrality";
    this.alpha = _alpha;
  }

  @Override
  public Map<GraphEntity, Double> calculateNodeCentrality(JungSnapshot graph, boolean normalize) {
    Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
    Map<GraphEntity, Double> degree = (new DegreeCentrality())
        .calculateNodeCentrality(graph, normalize);
    Map<GraphEntity, Double> wdegree = (new WeightedDegreeCentrality())
        .calculateNodeCentrality(graph, normalize);
    for (GraphEntity v : graph.getVertices()) {
      double value = Math.pow(degree.get(v), 1 - alpha)
          * Math.pow(wdegree.get(v), alpha);
      result.put(v, value);
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
    Map<GraphEntity, Double> degree = (new DegreeCentrality())
        .calculateNeighborhoodNodeCentrality(graph, k, normalize);
    Map<GraphEntity, Double> wdegree = (new WeightedDegreeCentrality())
        .calculateNeighborhoodNodeCentrality(graph, k, normalize);
    for (GraphEntity v : graph.getVertices()) {
      double value = Math.pow(degree.get(v), 1 - alpha)
          * Math.pow(wdegree.get(v), alpha);
      result.put(v, value);
    }
    return result;
  }

}
