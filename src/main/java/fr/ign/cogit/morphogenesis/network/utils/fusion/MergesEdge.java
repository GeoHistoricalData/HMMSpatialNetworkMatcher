package fr.ign.cogit.morphogenesis.network.utils.fusion;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;

public class MergesEdge {

  public MergesEdge() {
    this.lines = new ArrayList<ILineString>();
    this.mergedLine = null;
  }

  public ILineString mergedLine;
  public List<ILineString> lines;

  @Override
  public boolean equals(Object e) {
    if (!(e instanceof MergesEdge)) {
      return false;
    }
    for (ILineString l : ((MergesEdge) e).lines) {
      if (!this.lines.contains(l)) {
        return false;
      }
    }
    if (this.lines.size() != ((MergesEdge) e).lines.size()) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    String s = "";
    for (ILineString l : this.lines) {
      s += Integer.toString(l.hashCode());
    }
    return Integer.parseInt(s);
  }

}
