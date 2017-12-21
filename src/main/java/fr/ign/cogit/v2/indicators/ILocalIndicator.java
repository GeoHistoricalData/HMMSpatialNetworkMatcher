package fr.ign.cogit.v2.indicators;

import java.util.Map;

import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

public abstract class ILocalIndicator {

  protected String name;

  public String getName() {
    return this.name;
  }

  public ILocalIndicator() {
  }

  public abstract Map<GraphEntity, Double> calculateNodeCentrality(
      JungSnapshot graph,  boolean normalize);

  public abstract Map<GraphEntity, Double> calculateEdgeCentrality(
      JungSnapshot graph, boolean normalize);

  public abstract Map<GraphEntity, Double> calculateNeighborhoodEdgeCentrality(
      JungSnapshot graph, int k,  boolean normalize);

  public abstract Map<GraphEntity, Double> calculateNeighborhoodNodeCentrality(
      JungSnapshot graph, int k,  boolean normalize);

}
