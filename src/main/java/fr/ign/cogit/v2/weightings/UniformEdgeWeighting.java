package fr.ign.cogit.v2.weightings;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.v2.snapshot.GraphEntity;

public class UniformEdgeWeighting extends IEdgeWeighting {

  public UniformEdgeWeighting() {
    this.name = "Uniform";
  }

  public void weight(GraphEntity entity, IGeometry geom) {
    if (entity.getType() != GraphEntity.EDGE) {
      return;
    }
    entity.setWeight(1.);
  }
}
