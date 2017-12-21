package fr.ign.cogit.v2.indicators.local;

import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.jung.algorithms.importance.RandomWalkBetweenness;
import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

public class RWBetweennessCentrality extends ILocalIndicator {

  public RWBetweennessCentrality() {
    this.name = "RWBetw";
  }

  @Override
  public Map<GraphEntity, Double> calculateNodeCentrality(JungSnapshot graph, boolean normalize) {
    RandomWalkBetweenness<GraphEntity, GraphEntity> ranker = new RandomWalkBetweenness<GraphEntity, GraphEntity>(
        graph);
    ranker.evaluate();
    Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
    for (GraphEntity v : graph.getVertices()) {
      result.put(v, ranker.getVertexRankScore(v));
    }
    return result;
  }

  @Override
  public Map<GraphEntity, Double> calculateEdgeCentrality(JungSnapshot graph, boolean normalize) {
    RandomWalkBetweenness<GraphEntity, GraphEntity> ranker = new RandomWalkBetweenness<GraphEntity, GraphEntity>(
        graph);
    ranker.evaluate();
    Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
    for (GraphEntity e : graph.getEdges()) {
      result.put(e, ranker.getEdgeRankScore(e));
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
