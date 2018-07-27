package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.pathbuilder;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservationCollection;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.Path;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.PathBuilder;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.FeatObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.ObservationPopulation;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.algo.geomstructure.Vector2D;

import java.util.*;

public class StrokePathBuilder implements PathBuilder{

  private static final double LINEAR_THRESOLD = Math.PI/6.;

  public List<Path<IObservation>> buildPaths(IObservationCollection observations, Random generator) {
    if(!(observations instanceof ObservationPopulation)) {
      throw new RuntimeException("observations type must extends ObservationPopulation to"
          + "compute strokes");
    }
    ObservationPopulation observationsPop = (ObservationPopulation)observations;
    CarteTopo t = new CarteTopo("");
    IPopulation<Arc> arcsT = t.getPopArcs();
    Map<Arc,FeatObservation> mappingArcs = new HashMap<>();
    for(FeatObservation a : observationsPop.getElements()){
      Arc newa = arcsT.nouvelElement();
      newa.setGeometrie(new GM_LineString(a.getGeom().coord()));
      mappingArcs.put(newa, a);
      newa.setPoids(newa.longueur());
    }
    t.creeTopologieArcsNoeuds(0);
    t.rendPlanaire(0);

//    Random generator = new Random(seed);

    List<List<Arc>> roads_arcs = this.buildLinesClusters(t, generator);
    List<Path<IObservation>> result = new ArrayList<>();
    for(List<Arc> road: roads_arcs){
      List<IObservation> newR = new ArrayList<>();
      for(Arc a : road){
        newR.add(mappingArcs.get(a));
      }
      result.add(new Path<>(newR));
    }
    return result;
  }

  private List<List<Arc>> buildLinesClusters(CarteTopo t, Random generator) {
    List<List<Arc>> result = new ArrayList<>();
    List<Arc> arcs = t.getListeArcs();
    Stack<Arc> processed = new Stack<>();
    Arc random = arcs.get(generator.nextInt(arcs.size()));
    while (random != null) {
      if (!processed.contains(random)) {
        processed.add(random);
        List<Arc> clusterFrom = new ArrayList<>();
        this.search_ebf(random, -1, processed, clusterFrom);

        List<Arc> clusterTo = new ArrayList<>();
        this.search_ebf(random, 1, processed, clusterTo);

        Collections.reverse(clusterFrom);
        List<Arc> road = new ArrayList<>(clusterFrom);
        road.add(random);
        road.addAll(clusterTo);
        result.add(road);
      }
      List<Arc> untagged = new ArrayList<>(arcs);
      untagged.removeAll(processed);
      if (!untagged.isEmpty()) {
        random = untagged.get(generator.nextInt(untagged.size()));
      } else {
        random = null;
      }
    }
    return result;
  }

  private void search_ebf(Arc old_s, int direction, Stack<Arc> processed,
      List<Arc> cluster) {
    Noeud search_point;
    if (direction == 1) {
      search_point = old_s.getNoeudFin();
    } else {
      search_point = old_s.getNoeudIni();
    }
    List<Arc> searched = new ArrayList<>();
    List<Arc> selected = new ArrayList<>();
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
    if (min < LINEAR_THRESOLD && choice != null) {
      cluster.add(choice);
      processed.add(choice);
      if (old_s.getNoeudFin().equals(choice.getNoeudFin())
          || old_s.getNoeudIni().equals(choice.getNoeudIni())) {
        Noeud n = choice.getNoeudFin();
        choice.setNoeudFin(choice.getNoeudIni());
        choice.setNoeudIni(n);
        choice.setGeometrie(choice.getGeometrie().reverse());
      }
      search_ebf(choice, direction, processed, cluster);
    }

  }

  private double deflectionAngle(Arc old_s, Arc remain) {
    ILineString p = old_s.getGeometrie();
    ILineString q = remain.getGeometrie();
    if (p.startPoint().equals(q.startPoint(), 0.01)
        || p.endPoint().equals(q.endPoint(), 0.01)) {
      q = q.reverse();
    }
    // TODO Il faudrait utiliser un lissage, mais celui ci a tendance à creer
    // des effets de bord en simplifiant trop les linestrings "coudées" et
    // influe trop sur l'angle final.

    Vector2D v1 = null;
    Vector2D v2 = null;
    try {
      if (q.endPoint().equals(p.startPoint(), 0.01)) {
        v1 = new Vector2D(p.coord().get(0), p.coord().get(1));
        v2 = new Vector2D(q.coord().get(q.coord().size() - 2), q.coord().get(
            q.coord().size() - 1));
      } else if (p.endPoint().equals(q.startPoint(), 0.01)) {
        v1 = new Vector2D(p.coord().get(p.coord().size() - 2), p.coord().get(
            p.coord().size() - 1));
        v2 = new Vector2D(q.coord().get(0), q.coord().get(1));
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    if (v1 == null) {
      return Double.POSITIVE_INFINITY;
    }
    assert v2 != null;
    return v1.angleVecteur(v2).getValeur();
  }

}
