package fr.ign.cogit.morphogenesis.network.strokes.ebf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
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
  static double prec = 1;

  /**
   * Convenience method that build strokes from a set of linear objects.
   * @param objects
   * @param mapping : if not null, build the mapping between the sets of arcs
   *          and the strokes
   * @return
   */
  public static List<ILineString> buildStroke(IPopulation<IFeature> objects,
      Map<ILineString, List<IFeature>> mapping, double linear_threshold,
      double global_angle_threshold, double merge_tolerance, int type) {
    List<ILineString> roads = new ArrayList<ILineString>();
    CarteTopo t = StrokesBuilder.buildGraph(objects);
    List<List<Arc>> roads_arcs = StrokesBuilder.buildLinesClusters(t,
        linear_threshold, global_angle_threshold, type);
    // logger.error("BUILD LINE CLUSTERS="+(System.currentTimeMillis()-time));
    // time= System.currentTimeMillis();
    for (List<Arc> road_arcs : roads_arcs) {
      ILineString stroke = StrokesBuilder.mergeStroke(road_arcs,
          merge_tolerance);
      if (stroke == null) {
        throw new NullPointerException("A STROKE WAS BUILT NULL");
      }
      if (mapping != null) {
        Set<IFeature> stroke_objects = new HashSet<IFeature>();
        for (Arc a : road_arcs) {
          for (IFeature f : a.getCorrespondants()) {
            stroke_objects.add(f);
          }
        }
        List<IFeature> list_obj = new ArrayList<IFeature>();
        list_obj.addAll(stroke_objects);
        mapping.put(stroke, list_obj);
      }
      roads.add(stroke);
    }
    // logger.error("BUILD ROADS="+(System.currentTimeMillis()-time));
    // time= System.currentTimeMillis();
    t.nettoyer();
    // logger.error("CLEAN MAP="+(System.currentTimeMillis()-time));
    t = null;
    return roads;
  }

  /**
   * Build the graph from the Observation.
   * @param road_set the Observation.
   * @return the graph
   */
  public static CarteTopo buildGraph(Collection<IFeature> road_set) {
    CarteTopo ct = new CarteTopo("void"); //$NON-NLS-1$
    IPopulation<Arc> arcs = ct.getPopArcs();
    logger.debug("Creating the edges..."); //$NON-NLS-1$
    for (IFeature feature : road_set) {
      Arc arc = arcs.nouvelElement();
      try {
        GM_LineString line = new GM_LineString(
            ((ILineString) ((IFeature) feature).getGeom()).getControlPoint());
        arc.setGeometrie(line);
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
   * the planar graph from a given Observation
   * @param roads the Observation
   * @param threshold : the angle from which we "break" a stroke.
   * @param principle
   * @return
   */
  public static List<List<Arc>> buildLinesClusters(Collection<IFeature> roads,
      double threshold, double global_angle_threshold, int principle) {
    logger.debug("Buidling planar graph..."); //$NON-NLS-1$
    CarteTopo t = buildGraph(roads);
    return buildLinesClusters(t, threshold, global_angle_threshold, principle);
  }

  /**
   * Sort and merge linestrings
   */
  public static ILineString mergeStroke(List<Arc> arcs, double tolerance) {
    List<ILineString> road = new ArrayList<ILineString>();
    for (Arc a : arcs) {
      road.add(a.getGeometrie());
    }
    System.out.println();
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

  @SuppressWarnings("unused")
  private static void sort(List<ILineString> roads) {
    Collections.sort(roads, new Comparator<ILineString>() {
      @Override
      public int compare(ILineString o1, ILineString o2) {
        if (o1.startPoint().equals(o2.endPoint())) {
          return 1;
        } else if (o1.endPoint().equals(o2.startPoint())) {
          return -1;
        }
        return 0;
      }
    });
  }

  /**
   * Build the strokes accorded to the desired principle : <br/>
   * 1 : Every Best Fit 2 : Self Best Fit 3 : Self Fit
   * @param t the planar graph of the Observation
   * @param threshold : the angle from which we "break" a stroke.
   * @param principle
   * @return
   */
  public static List<List<Arc>> buildLinesClusters(CarteTopo t,
      double threshold, double global_angle_threshold, int principle) {
    List<List<Arc>> result = new ArrayList<List<Arc>>();
    List<Arc> arcs = t.getListeArcs();
    Set<Arc> processed = new HashSet<Arc>();
    Arc random = arcs.get(new Random().nextInt(arcs.size()));
    int step = arcs.size() / 100;
    int cpt = 0;
    while (random != null) {
      if (!processed.contains(random)) {
        //System.out.println("CHOSEN : " + random); //$NON-NLS-1$
        processed.add(random);
        if (processed.size() / step != cpt) {
          logger.info(cpt + " % edges processed");
          cpt++;
        }
        List<Arc> clusterFrom = new ArrayList<Arc>();
        if (principle == 1) {
          StrokesBuilder.search_ebf(random, -1, processed, threshold,
              global_angle_threshold, clusterFrom);
        } else if (principle == 2) {
          StrokesBuilder.search_sbf(random, -1, processed, threshold,
              global_angle_threshold, clusterFrom);
        } else {
          StrokesBuilder.search_sf(random, -1, processed, threshold,
              global_angle_threshold, clusterFrom);
        }
        List<Arc> clusterTo = new ArrayList<Arc>();
        if (principle == 1) {
          StrokesBuilder.search_ebf(random, 1, processed, threshold,
              global_angle_threshold, clusterTo);
        } else if (principle == 2) {
          StrokesBuilder.search_sbf(random, 1, processed, threshold,
              global_angle_threshold, clusterTo);
        } else {
          StrokesBuilder.search_sf(random, 1, processed, threshold,
              global_angle_threshold, clusterTo);
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
  private static void search_sf(Arc old_s, int direction, Set<Arc> processed,
      double threshold, double global_angle_threshold, List<Arc> cluster) {
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
      search_sf(selected.get(0), direction, processed, threshold,
          global_angle_threshold, cluster);
    } else {
      Arc choice = selected.get(new Random().nextInt(selected.size() - 1));
      search_sf(choice, direction, processed, threshold,
          global_angle_threshold, cluster);

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
  private static void search_sbf(Arc old_s, int direction, Set<Arc> processed,
      double threshold, double global_angle_threshold, List<Arc> cluster) {
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
      search_sbf(choice, direction, processed, threshold,
          global_angle_threshold, cluster);
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
  private static void search_ebf(Arc old_s, int direction, Set<Arc> processed,
      double threshold, double global_angle_threshold, List<Arc> cluster) {
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
    i = 0;
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

    if (selected.size() == 1 && searched.size() == 1) {
      // qu'un noeud, on le prend
      choice = selected.get(0);
      cluster.add(choice);
      processed.add(choice);
      if (old_s.getNoeudFin().equals(choice.getNoeudFin())
          || old_s.getNoeudIni().equals(choice.getNoeudIni())) {
        Noeud n = choice.getNoeudFin();
        choice.setNoeudFin(choice.getNoeudIni());
        choice.setNoeudIni(n);
        choice.setGeometrie(choice.getGeometrie().reverse());
      }
      search_ebf(choice, direction, processed, threshold,
          global_angle_threshold, cluster);
    } else {
      for (Arc a : selected) {
        int id = searched.indexOf(a);
        if (old_remain_angles[id] < min) {
          min = old_remain_angles[id];
          choice = a;
        }
      }
      if (min < threshold && choice != null
          && globalAngle(choice, old_s) < global_angle_threshold) {
        cluster.add(choice);
        processed.add(choice);
        if (old_s.getNoeudFin().equals(choice.getNoeudFin())
            || old_s.getNoeudIni().equals(choice.getNoeudIni())) {
          Noeud n = choice.getNoeudFin();
          choice.setNoeudFin(choice.getNoeudIni());
          choice.setNoeudIni(n);
          choice.setGeometrie(choice.getGeometrie().reverse());
        }
        search_ebf(choice, direction, processed, threshold,
            global_angle_threshold, cluster);
      }
    }
  }

  private static double deflectionAngle(Arc old_s, Arc remain) {
    ILineString p = old_s.getGeometrie();
    ILineString q = remain.getGeometrie();
    if (p.startPoint().equals(q.startPoint(), 0.5)
        || p.endPoint().equals(q.endPoint(), 0.5)) {
      q = q.reverse();
    }
    // TODO Il faudrait utiliser un lissage, mais celui ci a tendance à creer
    // des effets de bord en simplifiant trop les linestrings "coudées" et
    // influe trop sur l'angle final.

    Vector2D v1 = null;
    Vector2D v2 = null;
    try {
      if (q.endPoint().equals(p.startPoint(), 0.5)) {
        v1 = new Vector2D(p.coord().get(0), p.coord().get(1));
        v2 = new Vector2D(q.coord().get(q.coord().size() - 2), q.coord().get(
            q.coord().size() - 1));
      } else if (p.endPoint().equals(q.startPoint(), 0.5)) {
        v1 = new Vector2D(p.coord().get(p.coord().size() - 2), p.coord().get(
            p.coord().size() - 1));
        v2 = new Vector2D(q.coord().get(0), q.coord().get(1));
      } else {
        logger.error("cannot compute the deflection angle"); //$NON-NLS-1$
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    if (v1 == null) {
      return Double.POSITIVE_INFINITY;
    }
    return v1.angleVecteur(v2).getValeur();

  }

  private static double globalAngle(Arc old_s, Arc remain) {

    IDirectPosition p1 = null, p2 = null;
    IDirectPositionList l1 = old_s.getCoord();
    IDirectPositionList l2 = remain.getCoord();

    if (l1.get(0).equals(l2.get(0)) || l1.get(l1.size() - 1).equals(l2.get(0))) {
      p2 = l2.get(0);
    } else {
      p2 = l2.get(l2.size() - 1);
    }
    if (l2.get(0).equals(l1.get(0)) || l2.get(l2.size() - 1).equals(l1.get(0))) {
      p1 = l1.get(0);
    } else {
      p1 = l1.get(l1.size() - 1);
    }
    if (p1.equals(l1.get(0))) {
      l1 = l1.reverse();
    }
    if (p2.equals(l2.get(l2.size() - 1))) {
      l2 = l2.reverse();
    }

    Angle angle1 = Operateurs.directionPrincipaleOrientee(Operateurs
        .echantillone(new GM_LineString(l1), 10).coord());
    Angle angle2 = Operateurs.directionPrincipaleOrientee(Operateurs
        .echantillone(new GM_LineString(l2), 10).coord());
    return Angle.ecart(angle1, angle2).getValeur();
  }

}
