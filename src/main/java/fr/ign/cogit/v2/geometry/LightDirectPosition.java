package fr.ign.cogit.v2.geometry;

import java.io.Serializable;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IPoint;
import fr.ign.cogit.geoxygene.contrib.geometrie.Distances;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;

/**
 * Implémentation légère d'un point
 * @author bcostes
 *
 */
public class LightDirectPosition implements LightGeometry, Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private double x, y;

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public LightDirectPosition(IDirectPosition p) {
    this.x = p.getX();
    this.y = p.getY();
  }

  @Override
  public double distance(LightGeometry g) {
    return this.toGeoxGeometry().distance(g.toGeoxGeometry());
  }

  @Override
  public IPoint toGeoxGeometry() {
    return new GM_Point(this.toGeoxDirectPosition());
  }

  public IDirectPosition toGeoxDirectPosition() {
    return new DirectPosition(this.x, this.y);
  }

  @Override
  public String toString() {
    String s = "LightDirectPosition : [x = " + this.x + " , y = " + this.y
        + "]";
    return s;
  }
}
