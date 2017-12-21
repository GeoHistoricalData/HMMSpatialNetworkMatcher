package fr.ign.cogit.v2.geometry;

import java.io.Serializable;
import java.util.List;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;

/**
 * Implémentation légère d'une géométrie multiple
 * @author bcostes
 *
 */
public class LightMultipleGeometry implements LightGeometry, Serializable {

  private List<LightLineString> lightLineString;
  private List<LightDirectPosition> lightDirectPosition;

  // si la lightMultipleGeometry correspond à un sommet fictif issu de la
  // fusion d'une place, geoxGeom représente ce point fictif
  private LightDirectPosition geoxGeom;

  public LightDirectPosition getGeoxGeom() {
    return geoxGeom;
  }

  public void setGeoxGeom(LightDirectPosition geoxGeom) {
    this.geoxGeom = geoxGeom;
  }

  public LightMultipleGeometry(List<LightLineString> lightLineString,
      List<LightDirectPosition> lightDirectPosition) {
    this.lightDirectPosition = lightDirectPosition;
    this.lightLineString = lightLineString;
  }

  @Override
  public IGeometry toGeoxGeometry() {
    if (this.geoxGeom != null) {
      // il s'agit d'un noeud fictif représentant la fusion d'une place
      return this.geoxGeom.toGeoxGeometry();
    } else if (this.lightDirectPosition.size() != 0) {
      // sinon on va renvoyer un point qui est le centre de
      // gravité des lightDirectPosition
      double x = 0, y = 0;
      for (LightDirectPosition p : this.lightDirectPosition) {
        x += p.getX();
        y += p.getY();
      }
      x /= (double) this.lightDirectPosition.size();
      y /= (double) this.lightDirectPosition.size();

      return new GM_Point(new DirectPosition(x, y));

    } else {
      LightLineString l = this.lightLineString.get(0);
      return new GM_Point(new DirectPosition((l.first().getX() + l.last()
          .getX()) / 2., (l.first().getY() + l.last().getY()) / 2.));
    }

  }

  @Override
  public double distance(LightGeometry g) {
      return this.toGeoxGeometry().distance(g.toGeoxGeometry());

  }

  public List<LightLineString> getLightLineString() {
    return lightLineString;
  }

  public void setLightLineString(List<LightLineString> lightLineString) {
    this.lightLineString = lightLineString;
  }

  public List<LightDirectPosition> getLightDirectPosition() {
    return lightDirectPosition;
  }

  public void setLightDirectPosition(
      List<LightDirectPosition> lightDirectPosition) {
    this.lightDirectPosition = lightDirectPosition;
  }
}
