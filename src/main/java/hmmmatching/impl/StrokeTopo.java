package hmmmatching.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.algo.geomstructure.Vector2D;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class StrokeTopo {

  static double prec = 2;

  /**
   * Convenience method that build strokes from a set of linear objects.
   * 
   * @param objects
   * @param mapping
   *          : if not null, build the mapping between the sets of arcs and the strokes
   * @return
   */
  public static List<List<Arc>> buildStroke(IPopulation<Arc> arcs, double linear_threshold, int type, Random rng) {
    CarteTopo t = new CarteTopo("");
    IPopulation<Arc> arcsT = t.getPopArcs();
    Map<Arc, Arc> mappingArcs = new HashMap<>();
    for (Arc a : arcs) {
      Arc newa = arcsT.nouvelElement();
      newa.setGeometrie(new GM_LineString(a.getGeometrie().getControlPoint()));
      mappingArcs.put(newa, a);
      newa.setPoids(newa.longueur());
    }
    t.creeTopologieArcsNoeuds(0);
    t.rendPlanaire(0);

    List<List<Arc>> roads_arcs = StrokeTopo.buildLinesClusters(t, linear_threshold, type, rng);
    List<List<Arc>> result = new ArrayList<List<Arc>>();
    for (List<Arc> road : roads_arcs) {
      List<Arc> newR = new ArrayList<Arc>();
      for (Arc a : road) {
        newR.add(mappingArcs.get(a));
      }
      result.add(newR);
    }
    return result;
  }

  /**
   * Build the strokes accorded to the desired principle : <br/>
   * 1 : Every Best Fit 2 : Self Best Fit 3 : Self Fit
   * 
   * @param t
   *          the planar graph of the Observation
   * @param threshold
   *          : the angle from which we "break" a stroke.
   * @param principle
   * @return
   */
  public static List<List<Arc>> buildLinesClusters(CarteTopo t, double threshold, int principle, Random rng) {
    List<List<Arc>> result = new ArrayList<List<Arc>>();
    List<Arc> arcs = t.getListeArcs();
    Stack<Arc> processed = new Stack<Arc>();
    Arc random = arcs.get(rng.nextInt(arcs.size()));
    while (random != null) {
      if (!processed.contains(random)) {
        processed.add(random);
        List<Arc> clusterFrom = new ArrayList<Arc>();
        if (principle == 1) {
          StrokeTopo.search_ebf(random, -1, processed, threshold, clusterFrom);
        } else if (principle == 2) {
          StrokeTopo.search_sbf(random, -1, processed, threshold, clusterFrom);
        } else {
          StrokeTopo.search_sf(random, -1, processed, threshold, clusterFrom, rng);
        }
        List<Arc> clusterTo = new ArrayList<Arc>();
        if (principle == 1) {
          StrokeTopo.search_ebf(random, 1, processed, threshold, clusterTo);
        } else if (principle == 2) {
          StrokeTopo.search_sbf(random, 1, processed, threshold, clusterTo);
        } else {
          StrokeTopo.search_sf(random, 1, processed, threshold, clusterTo, rng);
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
        random = untagged.get(rng.nextInt(untagged.size()));
      } else {
        random = null;
      }
    }
    return result;
  }

  /**
   * Build a stroke based on self fit
   * 
   * @param old_s
   * @param direction
   * @param processed
   * @param threshold
   * @param cluster
   */
  private static void search_sf(Arc old_s, int direction, Stack<Arc> processed, double threshold, List<Arc> cluster, Random rng) {
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
      /*int id = */searched.indexOf(a);
      // if (old_remain_angles[id] < threshold) {
      selected.add(a);
      // }
    }
    if (selected.isEmpty()) {
      return;
    }
    if (selected.size() == 1) {
      search_sf(selected.get(0), direction, processed, threshold, cluster, rng);
    } else {
      Arc choice = selected.get(rng.nextInt(selected.size() - 1));
      search_sf(choice, direction, processed, threshold, cluster, rng);

    }
  }

  /**
   * Build a stroke based on self best fit
   * 
   * @param old_s
   * @param direction
   * @param processed
   * @param threshold
   * @param cluster
   */
  private static void search_sbf(Arc old_s, int direction, Stack<Arc> processed, double threshold, List<Arc> cluster) {
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
      if (old_s.getNoeudFin().equals(choice.getNoeudFin()) || old_s.getNoeudIni().equals(choice.getNoeudIni())) {
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
   * 
   * @param old_s
   * @param _dir
   * @param clusterFromprocessed
   * @param threshold
   * @param cluster
   * @param old_point
   */
  private static void search_ebf(Arc old_s, int direction, Stack<Arc> processed, double threshold, List<Arc> cluster) {
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
      if (old_s.getNoeudFin().equals(choice.getNoeudFin()) || old_s.getNoeudIni().equals(choice.getNoeudIni())) {
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
    if (p.startPoint().equals(q.startPoint(), prec) || p.endPoint().equals(q.endPoint(), prec)) {
      q = q.reverse();
    }
    // TODO Il faudrait utiliser un lissage, mais celui ci a tendance à creer
    // des effets de bord en simplifiant trop les linestrings "coudées" et
    // influe trop sur l'angle final.

    Vector2D v1 = null;
    Vector2D v2 = null;
    try {
      if (q.endPoint().equals(p.startPoint(), prec)) {
        v1 = new Vector2D(p.coord().get(0), p.coord().get(1));
        v2 = new Vector2D(q.coord().get(q.coord().size() - 2), q.coord().get(q.coord().size() - 1));
      } else if (p.endPoint().equals(q.startPoint(), prec)) {
        v1 = new Vector2D(p.coord().get(p.coord().size() - 2), p.coord().get(p.coord().size() - 1));
        v2 = new Vector2D(q.coord().get(0), q.coord().get(1));
      } else {
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    if (v1 == null) {
      return Double.POSITIVE_INFINITY;
    }
    return v1.angleVecteur(v2).getValeur();
  }

  public static void main(String args[]) {
    IPopulation<IFeature> in = ShapefileReader.read("networkmatching/verniquet_l93_utf8_corr.shp");
    CarteTopo t = new CarteTopo("");
    IPopulation<Arc> arcs = t.getPopArcs();
    for (IFeature f : in) {
      Arc a = arcs.nouvelElement();
      a.setGeometrie(new GM_LineString(f.getGeom().coord()));
    }
    t.creeTopologieArcsNoeuds(0);
    t.rendPlanaire(0);

    Random rng = new Random(42L);
    List<List<Arc>> strokes = StrokeTopo.buildStroke(t.getPopArcs(), Math.PI + 6, 1, rng);
    IPopulation<IFeature> out = new Population<IFeature>();
    for (List<Arc> a : strokes) {
      List<ILineString> l = new ArrayList<ILineString>();
      for (Arc aa : a) {
        l.add(aa.getGeometrie());
      }
      ILineString ll = Operateurs.union(l);
      out.add(new DefaultFeature(ll));
    }
    ShapefileWriter.write(out, "results/test.shp");
  }

}
