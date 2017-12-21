package fr.ign.cogit.v2.weightings;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.v2.snapshot.GraphEntity;

/**
 * Classe abstraite pour les pondérations des noeuds et arcs
 * @author bcostes
 * 
 */
public abstract class IEdgeWeighting {

  protected String name;

  public String getName() {
    return this.name;
  }

  public void weight(GraphEntity entity, IGeometry geom) {

  }

}
