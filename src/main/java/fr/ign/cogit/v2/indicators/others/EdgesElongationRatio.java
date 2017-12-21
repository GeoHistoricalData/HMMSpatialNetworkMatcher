package fr.ign.cogit.v2.indicators.others;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.v2.indicators.IOtherGeometricalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

public class EdgesElongationRatio extends IOtherGeometricalIndicator {

  public EdgesElongationRatio() {
    this.name = "Elongation Ratio";
  }

  @Override
  public List<Double> calculateGeometricalIndicator(JungSnapshot graph) {

    List<Double> result = new ArrayList<Double>();
    for (GraphEntity g : graph.getEdges()) {
      IGeometry geom = g.getGeometry().toGeoxGeometry();
      double l = geom.envelope().maxX() - geom.envelope().minX();
      double h = geom.envelope().maxY() - geom.envelope().minY();
      result.add((Math.min(l, h)) / (Math.max(l, h)));
    }
    return result;
  }
}
