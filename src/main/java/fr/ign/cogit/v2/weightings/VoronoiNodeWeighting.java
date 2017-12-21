package fr.ign.cogit.v2.weightings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IEnvelope;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.v2.snapshot.GraphEntity;

public class VoronoiNodeWeighting extends INodeWeighting {

  /**
   * Nombre de lancés de points
   */
  private static final int iterations = 750000;

  private static final Logger logger = Logger
      .getLogger(VoronoiNodeWeighting.class);

  public VoronoiNodeWeighting() {
    this.name = "Voronoi";
  }

  public void weight(Map<IGeometry, GraphEntity> geometries) {

    // vérification préalabe
    for (GraphEntity t : geometries.values()) {
      if (t.getType() != GraphEntity.NODE) {
        return;
      }

    }

    if (logger.isInfoEnabled()) {
      logger.info("Estimating voronoi cells surfaces ... ");
    }

    // récupération de l'emprise
    Iterator<IGeometry> itG = geometries.keySet().iterator();
    IGeometry union = itG.next();
    while (itG.hasNext()) {
      union = union.union(itG.next());
    }
    IGeometry enveloppe = union.envelope().getGeom(); // enveloppe convexe

    // structure pour stocker les poids
    Map<GraphEntity, Double> weights = new HashMap<GraphEntity, Double>();
    for (GraphEntity t : geometries.values()) {
      weights.put(t, 0.);
    }
    IEnvelope env = enveloppe.envelope();
    double minx = env.minX();
    double miny = env.minY();
    double maxx = env.maxX();
    double maxy = env.maxY();

    for (int i = 0; i < iterations; i++) {
      // tirage aléatoire d'un point
      IDirectPosition newPos = null;
      while (true) {
        double x = minx + Math.random() * (maxx - minx);
        double y = miny + Math.random() * (maxy - miny);

        // On vérifie si le point est dans le polygone
        newPos = new DirectPosition(x, y);
        if (enveloppe.contains(new GM_Point(newPos))) {
          break;
        }
      }

      // on récupère le point le plus proche
      double dmin = Double.MAX_VALUE;
      IGeometry fmin = null;
      for (IGeometry f : geometries.keySet()) {
        double d = f.coord().get(0).distance(newPos);
        if (d < dmin) {
          dmin = d;
          fmin = f;
        }
      }
      weights.put(geometries.get(fmin), weights.get(geometries.get(fmin)) + 1.);
    }
    // on normalise
    for (GraphEntity t : weights.keySet()) {
      weights.put(t, weights.get(t) / ((double) iterations));
    }

    // on associe à chaque objet son poids
    for (GraphEntity t : weights.keySet()) {
      t.setWeight(weights.get(t));
    }

    if (logger.isInfoEnabled()) {
      logger.info("Voronoi cells surfaces estimation done.");
    }

  }

}
