package fr.ign.cogit.v2.geometry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Distances;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;

/**
 * Implémentation légère d'une linestring
 * @author bcostes
 *
 */
public class LightLineString implements LightGeometry, Serializable {

  private List<LightDirectPosition> coords;

  public LightLineString(IDirectPositionList list) {
    this.coords = new ArrayList<LightDirectPosition>();
    for (IDirectPosition p : list) {
      this.coords.add(new LightDirectPosition(p));
    }
  }

  public void setCoords(List<LightDirectPosition> coords) {
    this.coords = coords;
  }

  public List<LightDirectPosition> coords() {
    return coords;
  }

  public LightDirectPosition first() {
    return this.coords.get(0);
  }

  public LightDirectPosition last() {
    return this.coords.get(this.coords.size() - 1);
  }

  @Override
  public ILineString toGeoxGeometry() {
    IDirectPositionList l = new DirectPositionList();
    for (LightDirectPosition p : this.coords) {
      l.add(p.toGeoxDirectPosition());
    }
    return new GM_LineString(l);
  }

  @Override
  public double distance(LightGeometry g) {
      return this.toGeoxGeometry().distance(g.toGeoxGeometry());


  }

  @Override
  public String toString() {
    String s = "LightLineString : [";
    for (LightDirectPosition p : this.coords) {
      s += p.toString() + ", ";
    }
    s = s.substring(0, s.length() - 2);
    s += "]";
    return s;
  }
}
