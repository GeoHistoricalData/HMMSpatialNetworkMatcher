package fr.ign.cogit.v2.weightings;

import java.util.Map;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.v2.snapshot.GraphEntity;

public abstract class INodeWeighting {
  protected String name;

  public String getName() {
    return this.name;
  }

  public abstract void weight(Map<IGeometry, GraphEntity> geometries);
}
