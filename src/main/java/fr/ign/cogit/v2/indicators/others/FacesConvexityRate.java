package fr.ign.cogit.v2.indicators.others;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Face;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.v2.indicators.IOtherGeometricalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

public class FacesConvexityRate extends IOtherGeometricalIndicator {

  public FacesConvexityRate() {
    this.name = "Convexity Rate";
  }

  @Override
  public List<Double> calculateGeometricalIndicator(JungSnapshot graph) {
    List<Double> result = new ArrayList<Double>();
    // création de la carte top
    CarteTopo map = new CarteTopo("void");
    IPopulation<Arc> popArcs = map.getPopArcs();
    for (GraphEntity g : graph.getEdges()) {
      Arc arc = popArcs.nouvelElement();
      try {
        GM_LineString line = new GM_LineString(g.getGeometry().toGeoxGeometry()
            .coord());
        arc.setGeometrie(line);
      } catch (ClassCastException r) {
        r.printStackTrace();
      }
    }
    map.creeTopologieArcsNoeuds(0);
    map.creeNoeudsManquants(0);
    map.rendPlanaire(0);
    map.creeTopologieFaces();

    for (Face f : map.getListeFaces()) {
      result.add(f.getSurface() / f.getGeom().convexHull().area());
    }
    return result;
  }

}
