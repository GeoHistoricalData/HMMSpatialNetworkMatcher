package fr.ign.cogit.morphogenesis.network.strokes.binJiang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.apache.log4j.Logger;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.util.algo.geomstructure.Vector2D;

/**
 * Strokes builder (aka Natural Roads applied on roads networks), from Bin
 * Jiang, Sijian Zhao and Junjun Yin : "Self-organized Natural Roads for
 * predicting Traffic Flow : A Sensitivity Study",2008
 * @author BDumenieu
 * 
 */
public class StrokesBuilder {

  public static final Logger logger = Logger.getLogger(StrokesBuilder.class);

  private static final double min_length = 0.1;
  static double prec = 8;

  /**
   * Convenience method that build strokes from a set of linear objects.
   * @param objects
   * @return
   */
  public static List<ILineString> buildStroke(List<ILineString> objects,
      double linear_threshold, double merge_tolerance, int type) {
    List<ILineString> roads = new ArrayList<ILineString>();
    CarteTopo t = StrokesBuilder.buildPlanarGraph(objects);
    List<List<Arc>> roads_arcs = StrokesBuilder.buildLinesClusters(t,
        linear_threshold, type);
    for (List<Arc> road_arcs : roads_arcs) {
      ILineString stroke = StrokesBuilder.mergeStroke(road_arcs,
          merge_tolerance);
      roads.add(stroke);
    }
    return roads;
  }

  /**
   * Build the planar graph from the network.
   * @param road_set the network.
   * @return the planar graph
   */
  public static CarteTopo buildPlanarGraph(Collection<ILineString> road_set) {
    CarteTopo ct = new CarteTopo("void"); //$NON-NLS-1$
    IPopulation<Arc> arcs = ct.getPopArcs();
    logger.debug("Creating the edges..."); //$NON-NLS-1$
    for (ILineString feature : road_set) {
      Arc arc = arcs.nouvelElement();
      try {
        arc.setGeometrie(feature);
        arc.addCorrespondant((IFeature) feature);
      } catch (ClassCastException e) {
        e.printStackTrace();
      }
    }
    logger.debug("CarteTopo operations..."); //$NON-NLS-1$
    ct.creeNoeudsManquants(prec);
    ct.fusionNoeuds(prec);
    ct.filtreArcsDoublons();
    ct.filtreNoeudsIsoles();
    ct.rendPlanaire(prec);
    ct.fusionNoeuds(prec);
    ct.filtreArcsDoublons();
    logger.debug("Planar graph created"); //$NON-NLS-1$
    return ct;
  }

  /**
   * Build the strokes accorded to the desired principle : <br/>
   * 1 : Every Best Fit 2 : Self Best Fit 3 : Self Fit This method first build
   * the planar graph from a given network
   * @param roads the network
   * @param threshold : the angle from which we "break" a stroke.
   * @param principle
   * @return
   */
  public static List<List<Arc>> buildLinesClusters(
      Collection<ILineString> roads, double threshold, int principle) {
    logger.debug("Buidling planar graph..."); //$NON-NLS-1$
    CarteTopo t = buildPlanarGraph(roads);
    return buildLinesClusters(t, threshold, principle);
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
      double threshold, int principle) {
    List<List<Arc>> result = new ArrayList<List<Arc>>();
    List<Arc> arcs = t.getListeArcs();
    Stack<Arc> processed = new Stack<Arc>();
    Arc random = arcs.get(new Random().nextInt(arcs.size()));
    while (random != null) {
      if (!processed.contains(random)) {
        logger.debug(processed.size() * 100 / t.getListeArcs().size()
            + "% edges processed"); //$NON-NLS-1$
        //System.out.println("CHOSEN : " + random); //$NON-NLS-1$
        processed.add(random);
        List<Arc> clusterFrom = new ArrayList<Arc>();
        if (principle == 1) {
          StrokesBuilder.search_ebf(random, -1, processed, threshold,
              clusterFrom);
        } else if (principle == 2) {
          StrokesBuilder.search_sbf(random, -1, processed, threshold,
              clusterFrom);
        } else {
          StrokesBuilder.search_sf(random, -1, processed, threshold,
              clusterFrom);
        }
        List<Arc> clusterTo = new ArrayList<Arc>();
        if (principle == 1) {
          StrokesBuilder.search_ebf(random, 1, processed, threshold, clusterTo);
        } else if (principle == 2) {
          StrokesBuilder.search_sbf(random, 1, processed, threshold, clusterTo);
        } else {
          StrokesBuilder.search_sf(random, 1, processed, threshold, clusterTo);
        }
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
    }
    return result;
  }

  /**
   * Build a stroke based on self fit
   * @param old_s
   * @param direction
   * @param processed
   * @param threshold
   * @param cluster
   */
  private static void search_sf(Arc old_s, int direction, Stack<Arc> processed,
      double threshold, List<Arc> cluster) {
    Noeud search_point = null;
    if (direction == 1) {
      search_point = old_s.getNoeudFin();
    } else {
      search_point = old_s.getNoeudIni();
    }
    List<Arc> searched = new ArrayList<Arc>();
    List<Arc> selected = new ArrayList<Arc>();
    searched.addAll(search_point.getEntrants());
    searched.addAll(search_point.getSortants());
    searched.remove(old_s);
    searched.removeAll(processed);
    if (searched.isEmpty()) {
      return;
    }
    double[] old_remain_angles = new double[searched.size()];
    int i = 0;
    for (Arc remain : searched) {
      old_remain_angles[i] = deflectionAngle(old_s, remain);
      i++;
    }
    for (Arc a : searched) {
      int id = searched.indexOf(a);
      if (old_remain_angles[id] < threshold) {
        selected.add(a);
      }
    }
    if (selected.isEmpty()) {
      return;
    }
    if (selected.size() == 1) {
      search_sf(selected.get(0), direction, processed, threshold, cluster);
    } else {
      Arc choice = selected.get(new Random().nextInt(selected.size() - 1));
      search_sf(choice, direction, processed, threshold, cluster);

    }
  }

  /**
   * Build a stroke based on self best fit
   * @param old_s
   * @param direction
   * @param processed
   * @param threshold
   * @param cluster
   */
  private static void search_sbf(Arc old_s, int direction,
      Stack<Arc> processed, double threshold, List<Arc> cluster) {
    Noeud search_point = null;
    if (direction == 1) {
      search_point = old_s.getNoeudFin();
    } else {
      search_point = old_s.getNoeudIni();
    }
    List<Arc> searched = new ArrayList<Arc>();
    searched.addAll(search_point.getEntrants());
    searched.addAll(search_point.getSortants());
    searched.remove(old_s);
    searched.removeAll(processed);
    if (searched.isEmpty()) {
      return;
    }
    double[] old_remain_angles = new double[searched.size()];
    int i = 0;
    for (Arc remain : searched) {
      old_remain_angles[i] = deflectionAngle(old_s, remain);
      i++;
    }
    double min = Double.MAX_VALUE;
    Arc choice = null;
    for (Arc a : searched) {
      int id = searched.indexOf(a);
      if (old_remain_angles[id] < min) {
        min = old_remain_angles[id];
        choice = a;
      }
    }
    if (min < threshold && choice != null) {
      cluster.add(choice);
      processed.add(choice);
      if (old_s.getNoeudFin().equals(choice.getNoeudFin())
          || old_s.getNoeudIni().equals(choice.getNoeudIni())) {
        Noeud n = choice.getNoeudFin();
        choice.setNoeudFin(choice.getNoeudIni());
        choice.setNoeudIni(n);
        choice.setGeometrie(choice.getGeometrie().reverse());
      }
      search_sbf(choice, direction, processed, threshold, cluster);
    }
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
      Stack<Arc> processed, double threshold, List<Arc> cluster) {
    Noeud search_point = null;
    if (direction == 1) {
      search_point = old_s.getNoeudFin();
    } else {
      search_point = old_s.getNoeudIni();
    }
    List<Arc> searched = new ArrayList<Arc>();
    List<Arc> selected = new ArrayList<Arc>();
    searched.addAll(search_point.getEntrants());
    searched.addAll(search_point.getSortants());
    searched.remove(old_s);
    searched.removeAll(processed);
    if (searched.isEmpty()) {
      return;
    }
    double[] old_remain_angles = new double[searched.size()];
    int i = 0;
    for (Arc remain : searched) {
      old_remain_angles[i] = deflectionAngle(old_s, remain);
      i++;
    }
    if (searched.size() > 1) {
      for (Arc pair_a : searched) {
        boolean isMin = true;
        for (Arc pair_b : searched) {
          if (!pair_a.equals(pair_b)) {
            double angle = deflectionAngle(pair_a, pair_b);
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
    if (min < threshold && choice != null) {
      cluster.add(choice);
      processed.add(choice);
      if (old_s.getNoeudFin().equals(choice.getNoeudFin())
          || old_s.getNoeudIni().equals(choice.getNoeudIni())) {
        Noeud n = choice.getNoeudFin();
        choice.setNoeudFin(choice.getNoeudIni());
        choice.setNoeudIni(n);
        choice.setGeometrie(choice.getGeometrie().reverse());
      }
      search_ebf(choice, direction, processed, threshold, cluster);
    }
  }

  private static double deflectionAngle(Arc old_s, Arc remain) {
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
        logger.error("cannot compute the deflection angle"); //$NON-NLS-1$
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

}
