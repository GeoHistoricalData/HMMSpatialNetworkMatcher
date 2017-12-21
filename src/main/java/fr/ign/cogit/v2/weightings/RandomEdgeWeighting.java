package fr.ign.cogit.v2.weightings;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.v2.snapshot.GraphEntity;

public class RandomEdgeWeighting extends IEdgeWeighting {

  public RandomEdgeWeighting() {
    this.name = "Random";
  }

  public void weight(GraphEntity entity, IGeometry geom) {
    entity.setWeight(Math.random() * 1000);

  }
}
