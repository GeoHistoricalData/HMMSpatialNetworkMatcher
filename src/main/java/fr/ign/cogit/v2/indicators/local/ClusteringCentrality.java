package fr.ign.cogit.v2.indicators.local;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

public class ClusteringCentrality extends ILocalIndicator {

  public ClusteringCentrality() {
    this.name = "Clust";
  }

  @Override
  public Map<GraphEntity, Double> calculateNodeCentrality(JungSnapshot graph, boolean normalize) {
    Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
    for (GraphEntity v : graph.getVertices()) {
      Collection<GraphEntity> neighbors = graph.getNeighbors(v);
      Set<GraphEntity> edges = new HashSet<GraphEntity>();
      for (GraphEntity w : neighbors) {
        // on ajoute les arcs connectés à w
        edges.addAll(graph.getIncidentEdges(w));
      }
      // on supprime les arcs connectés à v
      edges.removeAll(graph.getIncidentEdges(v));
      double l = 0.;
      for (GraphEntity e : edges) {
        if (neighbors.contains(graph.getEndpoints(e).getFirst())
            && neighbors.contains(graph.getEndpoints(e).getSecond())
            && !graph.getEndpoints(e).getFirst()
                .equals(graph.getEndpoints(e).getSecond())) {
          l++;
        }
      }

      double m = graph.getNeighborCount(v);
      if (m <= 1) {
        result.put(v, 0.);
      } else {
        double sum = (2. * l) / (m * (m - 1));
        result.put(v, sum);
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
    // TODO Auto-generated method stub
    return null;
  }

}
