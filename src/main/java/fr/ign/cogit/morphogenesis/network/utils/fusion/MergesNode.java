package fr.ign.cogit.morphogenesis.network.utils.fusion;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;

public class MergesNode {

  public MergesNode() {
    this.points = new ArrayList<IDirectPosition>();
    this.mergedPoint = null;
  }

  public IDirectPosition mergedPoint;
  public List<IDirectPosition> points;

  @Override
  public boolean equals(Object e) {
    if (!(e instanceof MergesNode)) {
      return false;
    }
    for (IDirectPosition l : ((MergesNode) e).points) {
      if (!this.points.contains(l)) {
        return false;
      }
    }
    if (this.points.size() != ((MergesNode) e).points.size()) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    String s = "";
    for (IDirectPosition l : this.points) {
      s += Integer.toString(l.hashCode());
    }
    return Integer.parseInt(s);
  }

}
