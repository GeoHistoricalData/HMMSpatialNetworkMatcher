package fr.ign.cogit.morphogenesis.network.utils.fusion;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;

public class Graph extends
    UndirectedSparseMultigraph<IDirectPosition, ILineString> {

  public Graph() {
    super();
  }

  /**
   * 
   */
  private static final long serialVersionUID = -4919323731503296222L;

  public static Graph buildGraph(IPopulation<IFeature> pop) {

    Graph g = new Graph();
    for (IFeature f : pop) {
      ILineString l = new GM_LineString(f.getGeom().coord());
      Pair<IDirectPosition> p = new Pair<IDirectPosition>(l.getControlPoint(0),
          l.getControlPoint(l.coord().size() - 1));
      g.addEdge(l, p);
    }

    return g;
  }
}
