package fr.ign.cogit.v2.mergeProcess.mergeGeometries.snapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Jama.Matrix;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;

public class HelmertSnapping extends AbstractSnappingAlgorithm{

  public HelmertSnapping(Set<ILineString> lines) {
    super(lines);
    // TODO Auto-generated constructor stub
  }
  public HelmertSnapping(Set<ILineString> lines, IDirectPosition p1, IDirectPosition p2){
    super(lines, p1, p2);
  }

  @Override
  public Set<ILineString> transformAll() {
    Set<ILineString> result = new HashSet<ILineString>();
    if (this.p1.equals(this.p2)) {
      // simple translation
      for (ILineString l : this.lines) {
        double tx = this.p1.getX() - l.getControlPoint(0).getX();
        double ty = this.p1.getY() - l.getControlPoint(0).getY();
        for (int i = 0; i < l.coord().size(); i++) {
          l.setControlPoint(i, new DirectPosition(l.getControlPoint(i).getX()
              + tx, l.getControlPoint(i).getY() + ty));
        }
      }
      return this.lines;
    }
    for (ILineString l : this.lines) {
      if (l.getControlPoint(0).equals(this.p1)
          && l
          .getControlPoint(l.getControlPoint().size() - 1)
          .equals(this.p2)) {
        result.add(l);
        continue;
      }

      IDirectPosition pp1 = l.getControlPoint(0);
      IDirectPosition pp2 = l.getControlPoint(
          l.getControlPoint().size() - 1);

      Matrix X = new Matrix(4, 4);
      X.set(0, 0, pp1.getX());
      X.set(1, 0, pp1.getY());
      X.set(2, 0, 1);
      X.set(3, 0, 0);
      X.set(0, 1, pp1.getY());
      X.set(1, 1, -pp1.getX());
      X.set(2, 1, 0);
      X.set(3, 1, 1);
      X.set(0, 2, pp2.getX());
      X.set(1, 2, pp2.getY());
      X.set(2, 2, 1);
      X.set(3, 2, 0);
      X.set(0, 3, pp2.getY());
      X.set(1, 3, -pp2.getX());
      X.set(2, 3, 0);
      X.set(3, 3, 1);
      Matrix Y = new Matrix(1, 4);
      Y.set(0, 0, this.p1.getX());
      Y.set(0, 1, this.p1.getY());
      Y.set(0, 2, this.p2.getX());
      Y.set(0, 3, this.p2.getY());
      Matrix A = Y.times(X.inverse());
      double a = A.get(0, 0);
      double b = A.get(0, 1);
      double c = A.get(0, 2);
      double d = A.get(0, 3);
      IDirectPositionList ll = new DirectPositionList();
      for (IDirectPosition p : l.getControlPoint()) {
        ll.add(new DirectPosition(a * p.getX() + b * p.getY() + c, -b
            * p.getX() + a * p.getY() + d));
      }
      ILineString newLine = new GM_LineString(ll);
      this.snap(newLine);
      result.add(newLine);

    }

    return result;

  }

  @Override
  public ILineString transform(ILineString lineMerged) {
    // TODO Auto-generated method stub
    if (this.p1.equals(this.p2)) {
      // simple translation
      double tx = this.p1.getX() - lineMerged.getControlPoint(0).getX();
      double ty = this.p1.getY() - lineMerged.getControlPoint(0).getY();
      for (int i = 0; i < lineMerged.coord().size(); i++) {
        lineMerged.setControlPoint(i, new DirectPosition(lineMerged.getControlPoint(i).getX()
            + tx, lineMerged.getControlPoint(i).getY() + ty));

      }
      return lineMerged;
    }
    if (lineMerged.getControlPoint(0).equals(this.p1)
        && lineMerged
        .getControlPoint(lineMerged.getControlPoint().size() - 1)
        .equals(this.p2)) {
      return lineMerged;
    }

    IDirectPosition pp1 = lineMerged.getControlPoint(0);
    IDirectPosition pp2 = lineMerged.getControlPoint(
        lineMerged.getControlPoint().size() - 1);

    Matrix X = new Matrix(4, 4);
    X.set(0, 0, pp1.getX());
    X.set(1, 0, pp1.getY());
    X.set(2, 0, 1);
    X.set(3, 0, 0);
    X.set(0, 1, pp1.getY());
    X.set(1, 1, -pp1.getX());
    X.set(2, 1, 0);
    X.set(3, 1, 1);
    X.set(0, 2, pp2.getX());
    X.set(1, 2, pp2.getY());
    X.set(2, 2, 1);
    X.set(3, 2, 0);
    X.set(0, 3, pp2.getY());
    X.set(1, 3, -pp2.getX());
    X.set(2, 3, 0);
    X.set(3, 3, 1);
    Matrix Y = new Matrix(1, 4);
    Y.set(0, 0, this.p1.getX());
    Y.set(0, 1, this.p1.getY());
    Y.set(0, 2, this.p2.getX());
    Y.set(0, 3, this.p2.getY());
    Matrix A = Y.times(X.inverse());
    double a = A.get(0, 0);
    double b = A.get(0, 1);
    double c = A.get(0, 2);
    double d = A.get(0, 3);
    IDirectPositionList ll = new DirectPositionList();
    for (IDirectPosition p : lineMerged.getControlPoint()) {
      ll.add(new DirectPosition(a * p.getX() + b * p.getY() + c, -b
          * p.getX() + a * p.getY() + d));
    }
    ILineString newLine = new GM_LineString(ll);
    this.snap(newLine);
    return newLine;  
  }


}
