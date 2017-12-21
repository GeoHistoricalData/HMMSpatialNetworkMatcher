package fr.ign.cogit.v2.weightings;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.v2.snapshot.GraphEntity;

public class LengthEdgeWeighting extends IEdgeWeighting {

  public LengthEdgeWeighting() {
    this.name = "Length";
  }

  public void weight(GraphEntity entity, IGeometry geom) {
    if (entity.getType() != GraphEntity.EDGE) {
      return;
    }
    entity.setWeight(geom.length());
  }
}
