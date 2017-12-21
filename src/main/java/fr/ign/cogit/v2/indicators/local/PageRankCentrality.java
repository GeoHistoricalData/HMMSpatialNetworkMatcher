package fr.ign.cogit.v2.indicators.local;

import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.jung.algorithms.scoring.PageRank;
import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

public class PageRankCentrality extends ILocalIndicator {

  private double d = 0.85;

  public PageRankCentrality() {
    this.name = "PR";
  }

  public PageRankCentrality(double _d) {
    this.name = "PageRank";
    this.d = _d;
  }

  @Override
  public Map<GraphEntity, Double> calculateNodeCentrality(JungSnapshot graph, boolean normalize) {
    Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
    PageRank<GraphEntity, GraphEntity> pr = new PageRank<GraphEntity, GraphEntity>(
        graph, graph.getEdgesWeights(), this.d);
    pr.evaluate();
    for (GraphEntity v : graph.getVertices()) {
      result.put(v, pr.getVertexScore(v));
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
