package fr.ign.cogit.v2.geometry;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;

/**
 * Implémentation légère d'une géométrie
 * @author bcostes
 *
 */
public interface LightGeometry {

  public abstract IGeometry toGeoxGeometry();

  public abstract double distance(LightGeometry g);

}
