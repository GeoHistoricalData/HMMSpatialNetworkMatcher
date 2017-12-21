package fr.ign.cogit.v2.indicators.sensibility;

import java.util.Map;

import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;


public abstract class ILocalSubsetIndicator {
    protected String name;

    public String getName() {
      return this.name;
    }

    public ILocalSubsetIndicator() {
    }

    public abstract Map<GraphEntity, Double> calculateSubsetNodeCentrality(
        JungSnapshot graph1,  JungSnapshot graph2, double radius, boolean normalize);

    public abstract Map<GraphEntity, Double> calculateSubsetEdgeCentrality(
            JungSnapshot graph1,  JungSnapshot graph2, double radius, boolean normalize);
}
