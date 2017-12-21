package fr.ign.cogit.v2.mergeProcess.hierarchicalMatching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineSegment;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.algo.geomstructure.Vector2D;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class HMMCluster {

  public ILineString getGeom() {
    return geom;
  }

  public void setGeom(ILineString geom) {
    this.geom = geom;
  }

  CarteTopo network;

  public HMMCluster(){
    this.clust = new ArrayList<Arc>();
  }

  private List<Arc> clust;

  public List<Arc> getCluster() {
    return clust;
  }

  public void setCluster(List<Arc> clust) {
    this.clust = clust;
  }
  
  ILineString geom;

  public double length(){
    double l = 0;
    for(Arc f: this.clust){
      l += f.getGeom().length();
    }
    return l;
  }



  static double prec = 2;

  /**
   * Convenience method that build strokes from a set of linear objects.
   * @param objects
   * @param mapping : if not null, build the mapping between the sets of arcs
   *          and the strokes
   * @return
   */
  public static List<HMMCluster> buildStroke(List<Arc> objects, double linear_threshold) {
    List<HMMCluster> roads = new ArrayList<HMMCluster>();
    List<List<Arc>> roads_arcs = HMMCluster.buildLinesClusters(objects,
        linear_threshold);
    // logger.error("BUILD LINE CLUSTERS="+(System.currentTimeMillis()-time));
    // time= System.currentTimeMillis();
    for (List<Arc> road_arcs : roads_arcs) {
      HMMCluster stroke = HMMCluster.mergeStroke(road_arcs);
      if (stroke == null) {
        throw new NullPointerException("A STROKE WAS BUILT NULL");
      }
      roads.add(stroke);
    }
    return roads;
  }


  /**
   * Sort and merge linestrings
   */
  private static HMMCluster mergeStroke(List<Arc> arcs) {
    HMMCluster.sort(arcs);
    List<ILineString> road = new ArrayList<ILineString>();
    for (Arc a : arcs) {
      road.add(a.getGeometrie());
    }
    ILineString line = Operateurs.compileArcs(road);
    HMMCluster clust = new HMMCluster();
    clust.getCluster().addAll(arcs);
    clust.setGeom(line);
    return clust;
  }


  /**
   * Build the strokes accorded to the desired principle : <br/>
   * 1 : Every Best Fit 2 : Self Best Fit 3 : Self Fit
   * @param t the planar graph of the Observation
   * @param threshold : the angle from which we "break" a stroke.
   * @param principle
   * @return
   */
  private static List<List<Arc>> buildLinesClusters(List<Arc> arcs,
      double threshold) {
    List<List<Arc>> result = new ArrayList<List<Arc>>();
    Stack<Arc> processed = new Stack<Arc>();
    Arc random = arcs.get(new Random().nextInt(arcs.size()));
    while (random != null) {
      if (!processed.contains(random)) {
        processed.add(random);
        List<Arc> clusterFrom = new ArrayList<Arc>();
        HMMCluster.search_ebf(random, -1, processed, threshold,
            clusterFrom);
        List<Arc> clusterTo = new ArrayList<Arc>();
        HMMCluster.search_ebf(random, 1, processed, threshold, clusterTo);
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

    Vector2D v1 = null;
    Vector2D v2 = null;
    try {
      if (q.endPoint().equals(p.startPoint(), prec)) {
        v1 = new Vector2D(p.coord().get(0), p.coord().get(1));
        v2 = new Vector2D(q.coord().get(q.coord().size() - 2), q.coord().get(
            q.coord().size() - 1));
      } else if (p.endPoint().equals(q.startPoint(), prec)) {
        v1 = new Vector2D(p.coord().get(p.coord().size() - 2), p.coord().get(
            p.coord().size() - 1));
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


  private static void sort(List<Arc> roads) {

    Collections.sort(roads, new Comparator<Arc>() {
      @Override
      public int compare(Arc o1, Arc o2) {
        if (o1.getGeometrie().startPoint().equals(o2.getGeometrie().endPoint())) {
          return 1;
        } else if (o1.getGeometrie().endPoint().equals(o2.getGeometrie().startPoint())) {
          return -1;
        }
        return 0;
      }
    });
  }
  
  public static void main(String args[]){
    
    IPopulation<IFeature> in = ShapefileReader.read("/home/bcostes/Bureau/test.shp");
    CarteTopo t = new CarteTopo("");
    IPopulation<Arc> arcs = t.getPopArcs();
    for(IFeature f : in){
      Arc a = arcs.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      a.addCorrespondant(f);
    }
    t.creeTopologieArcsNoeuds(0);
    t.creeNoeudsManquants(0);
    t.rendPlanaire(0.);
    List<HMMCluster> clusters = HMMCluster.buildStroke(t.getListeArcs(), Math.PI/4.);
    IPopulation<IFeature> out = new Population<IFeature>();
    for(HMMCluster c : clusters){
      IFeature f = new DefaultFeature(c.getGeom());
      out.add(f);
    }
    ShapefileWriter.write(out, "/home/bcostes/Bureau/strokes.shp");
  }
}
