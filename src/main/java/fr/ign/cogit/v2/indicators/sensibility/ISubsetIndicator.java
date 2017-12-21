package fr.ign.cogit.v2.indicators.sensibility;

import java.util.Map;

import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

/**
 * graph2 est un sous-graphe de graphe1
 * @author bcostes
 *
 */
public abstract class ISubsetIndicator {

    protected String name;

    public String getName() {
      return this.name;
    }

    public ISubsetIndicator() {
    }

    public abstract Map<GraphEntity, Double> calculateSubsetNodeCentrality(
        JungSnapshot graph1,  JungSnapshot graph2, boolean normalize);

    public abstract Map<GraphEntity, Double> calculateSubsetEdgeCentrality(
            JungSnapshot graph1,  JungSnapshot graph2, boolean normalize);

   /* public abstract Map<GraphEntity, Double> calculateNeighborhoodEdgeCentrality(
        JungSnapshot graph, int k,  boolean normalize);

    public abstract Map<GraphEntity, Double> calculateNeighborhoodNodeCentrality(
        JungSnapshot graph, int k,  boolean normalize);*/
}
