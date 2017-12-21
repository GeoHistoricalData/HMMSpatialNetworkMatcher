package fr.ign.cogit.morphogenesis.network.strokes.places;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.apache.log4j.Logger;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopoFactory;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Groupe;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.util.algo.geomstructure.Vector2D;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

/**
 * Strokes builder (aka Natural Roads applied on roads networks), from Bin
 * Jiang, Sijian Zhao and Junjun Yin : "Self-organized Natural Roads for
 * predicting Traffic Flow : A Sensitivity Study",2008
 * @author BDumenieu
 * 
 */
public class StrokesBuilder2 {

  public static final Logger logger = Logger.getLogger(StrokesBuilder2.class);

  private static final double min_length = 0.1;
  static double prec = 1;

  /**
   * Convenience method that build strokes from a set of linear objects.
   * @param objects
   * @return
   */
  public static List<ILineString> buildStroke(CarteTopo map,
      IFeatureCollection<IFeature> pop, double linear_threshold,
      double merge_tolerance) {
    List<ILineString> roads = new ArrayList<ILineString>();
    List<PlaceEnrichment> places = PlaceEnrichment.detect(pop, map,
        "nom_g1888", "pl");
    for (Arc a : map.getListeArcs()) {
      a.setPoids(a.longueur());
      a.setOrientation(2);
    }
    List<List<Arc>> roads_arcs = StrokesBuilder2.buildLinesClusters(map,
        linear_threshold, places);
    for (List<Arc> road_arcs : roads_arcs) {
      ILineString stroke = StrokesBuilder2.mergeStroke(road_arcs,
          merge_tolerance);
      roads.add(stroke);
    }
    return roads;
  }

  /**
   * Merge arcs in a single stroke The list MUST be sorted
   */
  public static ILineString mergeStroke(List<Arc> arcs, double tolerance) {
    List<ILineString> road = new ArrayList<ILineString>();
    for (Arc a : arcs) {
      road.add(a.getGeometrie());
    }
    ILineString line = Operateurs.compileArcs(road, tolerance);
    if (line == null) {
      logger.warn("Merge not possible"); //$NON-NLS-1$
    } else if (line.length() < min_length) {
      logger.warn(" LINE TOO SHORT : lenght = " + line.length()); //$NON-NLS-1$
    } else {
      logger.debug("New natural road"); //$NON-NLS-1$
    }
    return line;
  }

  /**
   * Build the strokes accorded to the desired principle : <br/>
   * 1 : Every Best Fit 2 : Self Best Fit 3 : Self Fit
   * @param t the planar graph of the network
   * @param threshold : the angle from which we "break" a stroke.
   * @param principle
   * @return
   */
  public static List<List<Arc>> buildLinesClusters(CarteTopo t,
      double threshold, List<PlaceEnrichment> places) {
    List<List<Arc>> result = new ArrayList<List<Arc>>();
    List<Arc> arcs = t.getListeArcs();
    Stack<Arc> processed = new Stack<Arc>();
    Arc random = arcs.get(new Random().nextInt(arcs.size()));
    while (true) {
      PlaceEnrichment place = null;
      for (PlaceEnrichment p : places) {
        if (p.getEdges().contains(random)) {
          place = p;
          break;
        }
      }
      if (place != null) {
        // on ne commence pas la construcvtion des stroke sur une place
        random = arcs.get(new Random().nextInt(arcs.size()));
      } else {
        break;
      }
    }
    boolean placeDone = false;
    while (random != null) {
      if (!processed.contains(random)) {
        logger.debug(processed.size() * 100 / t.getListeArcs().size()
            + "% edges processed"); //$NON-NLS-1$
        //System.out.println("CHOSEN : " + random); //$NON-NLS-1$
        processed.add(random);
        List<Arc> clusterFrom = new ArrayList<Arc>();
        StrokesBuilder2.search_ebf(random, -1, processed, threshold,
            clusterFrom, places);

        List<Arc> clusterTo = new ArrayList<Arc>();

        StrokesBuilder2.search_ebf(random, 1, processed, threshold, clusterTo,
            places);

        List<Arc> road = new ArrayList<Arc>();
        Collections.reverse(clusterFrom);
        road.addAll(clusterFrom);
        road.add(random);
        road.addAll(clusterTo);
        result.add(road);
      }
      List<Arc> untagged = new ArrayList<Arc>();
      untagged.addAll(arcs);
      untagged.removeAll(processed);
      if (!untagged.isEmpty()) {
        random = untagged.get(new Random().nextInt(untagged.size()));
      } else {
        random = null;
      }
      if (!placeDone) {
        int cpt = 0;
        while (true) {
          cpt++;
          PlaceEnrichment place = null;
          for (PlaceEnrichment p : places) {
            if (p.getEdges().contains(random)) {
              place = p;
              break;
            }
          }
          if (place != null) {
            // on ne commence pas la construcvtion des stroke sur une place
            if (!untagged.isEmpty()) {
              random = untagged.get(new Random().nextInt(untagged.size()));
            } else {
              random = null;
            }
          } else {
            break;
          }
          if (cpt > 1000) {
            placeDone = true;
            break;
          }
        }
      }

    }
    return result;
  }

  /**
   * Build a stroke based on every best fit -1 = FROM 1 = TO
   * @param old_s
   * @param _dir
   * @param clusterFromprocessed
   * @param threshold
   * @param cluster
   * @param old_point
   */
  private static void search_ebf(Arc old_s, int direction,
      Stack<Arc> processed, double threshold, List<Arc> cluster,
      List<PlaceEnrichment> places) {
    Noeud search_point = null;
    if (direction == 1) {
      search_point = old_s.getNoeudFin();
    } else {
      search_point = old_s.getNoeudIni();
    }

    PlaceEnrichment place = null;
    for (PlaceEnrichment p : places) {
      if (p.getNodes().contains(search_point)) {
        place = p;
        break;
      }
    }
    List<Arc> searched = new ArrayList<Arc>();
    List<Arc> selected = new ArrayList<Arc>();
    if (place != null) {
      // le noeud tombe sur une place
      // onrécupère les arcs en sortie de la place
      searched.addAll(place.getOtherConnectedEdges(old_s));
    } else {
      searched.addAll(search_point.getEntrants());
      searched.addAll(search_point.getSortants());
      searched.remove(old_s);
    }

    searched.removeAll(processed);
    if (searched.isEmpty()) {
      return;
    }
    double[] old_remain_angles = new double[searched.size()];
    int i = 0;
    for (Arc remain : searched) {
      old_remain_angles[i] = deflectionAngle(old_s, remain, places);
      i++;
    }
    if (searched.size() > 1) {
      for (Arc pair_a : searched) {
        boolean isMin = true;
        for (Arc pair_b : searched) {
          if (!pair_a.equals(pair_b)) {
            double angle = deflectionAngle(pair_a, pair_b, places);
            if (old_remain_angles[searched.indexOf(pair_a)] > angle) {
              isMin = false;
              break;
            }
          }
        }
        if (isMin) {
          selected.add(pair_a);
        }
      }
    } else if (searched.size() == 1) {
      selected.add(searched.get(0));
    } else {
      return;
    }
    double min = Double.MAX_VALUE;
    Arc choice = null;
    for (Arc a : selected) {
      int id = searched.indexOf(a);
      if (old_remain_angles[id] < min) {
        min = old_remain_angles[id];
        choice = a;
      }
    }
    if (choice != null) {
      if (place != null) {
        // il y avait une place
        // on sélectionne également le plus court chemin a travers la place
        Noeud end = null;
        if (place.getNodes().contains(choice.getNoeudIni())) {
          end = choice.getNoeudIni();
        } else {
          end = choice.getNoeudFin();
        }
        Groupe G = search_point.plusCourtChemin(end, 0);
        for (Arc a : G.getListeArcs()) {
          cluster.add(a);
          processed.add(a);
        }
      }
      cluster.add(choice);
      processed.add(choice);
      if (old_s.getNoeudFin().equals(choice.getNoeudFin())
          || old_s.getNoeudIni().equals(choice.getNoeudIni())) {
        Noeud n = choice.getNoeudFin();
        choice.setNoeudFin(choice.getNoeudIni());
        choice.setNoeudIni(n);
        choice.setGeometrie(choice.getGeometrie().reverse());
      }
      search_ebf(choice, direction, processed, threshold, cluster, places);
    }
  }

  private static double deflectionAngle(Arc old_s, Arc remain,
      List<PlaceEnrichment> places) {
    ILineString p = old_s.getGeometrie();
    ILineString q = remain.getGeometrie();
    if (p.startPoint().equals(q.startPoint(), prec)
        || p.endPoint().equals(q.endPoint(), prec)) {
      q = q.reverse();
    }
    // TODO Il faudrait utiliser un lissage, mais celui ci a tendance à creer
    // des effets de bord en simplifiant trop les linestrings "coudées" et
    // influe trop sur l'angle final.
    // DescriptiveStatistics s = new DescriptiveStatistics();
    // for (int i = 1; i < p.coord().size(); i++) {
    // s.addValue(p.coord().get(i - 1).distance(p.coord().get(i)));
    // }
    // DescriptiveStatistics t = new DescriptiveStatistics();
    // for (int i = 1; i < q.coord().size(); i++) {
    // t.addValue(q.coord().get(i - 1).distance(q.coord().get(i)));
    // }
    // p = GaussianFilter.gaussianFilter(p, s.getStandardDeviation(),
    // s.getStandardDeviation());
    // q = GaussianFilter.gaussianFilter(q, t.getStandardDeviation(),
    // t.getStandardDeviation());
    Vector2D v1 = null;
    Vector2D v2 = null;
    // Vector2D v3 = null;
    // Vector2D v4 = null;
    try {
      if (q.endPoint().equals(p.startPoint(), prec)) {
        v1 = new Vector2D(p.coord().get(0), p.coord().get(1));
        v2 = new Vector2D(q.coord().get(q.coord().size() - 2), q.coord().get(
            q.coord().size() - 1));
        // v3 = new Vector2D(p.coord().get(0), p.coord().get(p.coord().size() -
        // 1));
        // v4 = new Vector2D(q.coord().get(0), q.coord().get(q.coord().size() -
        // 1));
      } else if (p.endPoint().equals(q.startPoint(), prec)) {
        v1 = new Vector2D(p.coord().get(p.coord().size() - 2), p.coord().get(
            p.coord().size() - 1));
        v2 = new Vector2D(q.coord().get(0), q.coord().get(1));
        // v3 = new Vector2D(p.coord().get(0), p.coord().get(p.coord().size() -
        // 1));
        // v4 = new Vector2D(q.coord().get(0), q.coord().get(q.coord().size() -
        // 1));
      } else {
        // on a affaire à une place
        PlaceEnrichment place = null;
        for (PlaceEnrichment pp : places) {
          if (pp.getConnectedEdges().contains(remain)) {
            place = pp;
            break;
          }
        }
        IDirectPosition n1, n2, n3, n4 = null;
        if (place.getNodes().contains(old_s.getNoeudIni())) {
          n1 = old_s.getCoord().get(1);
          n2 = old_s.getNoeudIni().getCoord();
        } else {
          n1 = old_s.getCoord().get(old_s.getCoord().size() - 2);
          n2 = old_s.getNoeudFin().getCoord();
        }
        if (place.getNodes().contains(remain.getNoeudIni())) {
          n3 = remain.getCoord().get(1);
          n4 = remain.getCoord().get(remain.getCoord().size() - 2);
        } else {
          n3 = remain.getCoord().get(remain.getCoord().size() - 2);
          n4 = remain.getCoord().get(1);
        }
        // translation
        v1 = new Vector2D(n1, n2);
        double tx = n4.getX() - n2.getX();
        double ty = n4.getY() - n2.getY();
        v2 = new Vector2D(n2,
            new DirectPosition(n3.getX() + tx, n3.getY() + ty));

      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    if (v1 == null) {
      return Double.POSITIVE_INFINITY;
    }
    double localAngle = v1.angleVecteur(v2).getValeur();
    // double globalAngle = v3.angleVecteur(v4).getValeur();

    // return ((localAngle + globalAngle) / 2.);

    return localAngle;

  }

  /*
   * private static double deflectionAngle(Arc old_s, Arc remain) { ILineString
   * p = old_s.getGeometrie(); ILineString q = remain.getGeometrie(); if
   * (p.startPoint().equals(q.startPoint(), prec) ||
   * p.endPoint().equals(q.endPoint(), prec)) { q = q.reverse(); } // TODO Il
   * faudrait utiliser un lissage, mais celui ci a tendance à creer // des
   * effets de bord en simplifiant trop les linestrings "coudées" et // influe
   * trop sur l'angle final. // DescriptiveStatistics s = new
   * DescriptiveStatistics(); // for (int i = 1; i < p.coord().size(); i++) { //
   * s.addValue(p.coord().get(i - 1).distance(p.coord().get(i))); // } //
   * DescriptiveStatistics t = new DescriptiveStatistics(); // for (int i = 1; i
   * < q.coord().size(); i++) { // t.addValue(q.coord().get(i -
   * 1).distance(q.coord().get(i))); // } // p =
   * GaussianFilter.gaussianFilter(p, s.getStandardDeviation(), //
   * s.getStandardDeviation()); // q = GaussianFilter.gaussianFilter(q,
   * t.getStandardDeviation(), // t.getStandardDeviation()); Vector2D v1 = null;
   * Vector2D v2 = null; try { if (q.endPoint().equals(p.startPoint(), prec)) {
   * v1 = new Vector2D(p.coord().get(0), p.coord().get(1)); v2 = new
   * Vector2D(q.coord().get(q.coord().size() - 2), q.coord().get(
   * q.coord().size() - 1)); } else if (p.endPoint().equals(q.startPoint(),
   * prec)) { v1 = new Vector2D(p.coord().get(p.coord().size() - 2),
   * p.coord().get( p.coord().size() - 1)); v2 = new Vector2D(q.coord().get(0),
   * q.coord().get(1)); } else {
   * logger.error("cannot compute the deflection angle"); //$NON-NLS-1$ }
   * 
   * } catch (Exception e) { e.printStackTrace(); } if (v1 == null) { return
   * Double.POSITIVE_INFINITY; } return v1.angleVecteur(v2).getValeur(); }
   */

  public static void main(String agrs[]) {
    IPopulation<IFeature> pop = ShapefileReader
        .read("/media/Data/Benoit/these/analyses/centralites/FILAIRES_L93/1789_verniquet.shp");

    IFeatureCollection<IFeature> inputFeatureCollectionCorrected = new Population<IFeature>();
    for (IFeature feat : pop) {
      if (feat.getGeom() instanceof IMultiCurve) {
        for (int i = 0; i < ((IMultiCurve<?>) feat.getGeom()).size(); i++) {
          inputFeatureCollectionCorrected.add(new DefaultFeature(
              ((IMultiCurve<?>) feat.getGeom()).get(i)));
        }
      } else {
        inputFeatureCollectionCorrected.add(new DefaultFeature(feat.getGeom()));
      }
    }

    CarteTopo map = CarteTopoFactory
        .newCarteTopo(inputFeatureCollectionCorrected);
    map.creeTopologieArcsNoeuds(0);
    map.rendPlanaire(0);

    List<ILineString> strokes = StrokesBuilder2.buildStroke(map, pop,
        Math.PI / 4, 0);
    IFeatureCollection<IFeature> col = new Population<IFeature>();
    for (ILineString p : strokes) {

      col.add(new DefaultFeature(p));

    }

    ShapefileWriter.write(col, "/home/bcostes/Bureau/test_generalisation.shp");

  }

}
