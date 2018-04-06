package fr.ign.cogit.HMMSpatialNetworkMatcher.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenStateCollection;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.ParametersSet;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.feature.Population;

public class HiddenStatePopulation extends Population<HiddenState> implements IHiddenStateCollection{

  public Collection<IHiddenState> filter(IObservation obs) {
    Observation o = (Observation) obs;
    Collection<HiddenState> resultTmp = new ArrayList<HiddenState>();
    resultTmp.addAll(this.select(o.getGeom(), ParametersSet.get().SELECTION_THRESHOLD));
    List<List<HiddenState>> combinations = this.getAllPossibleCombinations(resultTmp);
    Collection<IHiddenState> result = new ArrayList<IHiddenState>();
    for(List<HiddenState> hdl : combinations) {
      if(hdl.size() == 1) {
        result.add(hdl.get(0));
      }
      else {
        result.add(new CompositeHiddenState(hdl));
      }
    }
    return result;
  }

  private List<List<HiddenState>> getAllPossibleCombinations(
      Collection<HiddenState> lines) {
    List<List<HiddenState>> result = new ArrayList<List<HiddenState>>();

    Stack<HiddenState> copy = new Stack<HiddenState>();
    copy.addAll(lines);

    while(!copy.isEmpty()){
      // Cette boucle sert à gérer les cas ou il y a plusieurs composantes connexes
      List<List<HiddenState>> clusters = new ArrayList<List<HiddenState>>();    
      //arcs déja traités
      Set<HiddenState> processed = new HashSet<HiddenState>();
      // queue de traitement
      List<HiddenState> queue = new ArrayList<HiddenState>();
      queue.add(copy.pop());
      while(!queue.isEmpty()){
        // on récupère l'arc courant
        HiddenState current = queue.remove(0);
        // on le déclare comme traité
        processed.add(current);
        Set<HiddenState> neighbors = this.getNeighbors(current, lines);
        neighbors.removeAll(processed);
        neighbors.removeAll(queue);
        for(HiddenState a : new ArrayList<HiddenState>(neighbors)){
          if(!lines.contains(a)){
            neighbors.remove(a);
          }
        }
        // on a récupéré les voisins de l'arc courant, non déja traités
        // et présent dans la liste d'arcs en entrée (pour éviter de parcourir toute 
        // la carte topo

        queue.addAll(neighbors);

        // on va parcourir les cluster déja validé
        // si c est l'arc courant et [a1, .., an] un cluster validé,
        // alors on va tester [c, a1, .., an]
        for(List<HiddenState> previousCluster : new ArrayList<List<HiddenState>>(clusters)){
          List<HiddenState> newCluster = new ArrayList<HiddenState>();
          newCluster.add(current);
          newCluster.addAll(previousCluster);
          if(this.lineMergePossible(newCluster)){
            // nouveau cluster validé
            clusters.add(newCluster);
          }
        }
        List<HiddenState> newCluster = new ArrayList<HiddenState>();
        newCluster.add(current);
        clusters.add(newCluster);
      }
      copy.removeAll(processed);
      result.addAll(clusters);
    }

    return result;
  }

  /**
   *  Méthode qui teste si un ensemble de linestring peut être aggrégé
   * @param lines
   * @return
   */
  private boolean lineMergePossible(List<HiddenState> lines) {
    if(lines.size() == 1){
      return true;
    }
    for(HiddenState s : lines) {
      if(s.getGeom().startPoint().equals(s.getGeom().endPoint())) {
        // pour éviter les boucles
        return false;
      }
    }
    Map<IDirectPosition, Integer> pts = new HashMap<IDirectPosition, Integer>();
    for(HiddenState a: lines){
      ILineString l = (ILineString)a.getGeom();
      if(pts.containsKey(l.startPoint())){
        pts.put(l.startPoint(), pts.get(l.startPoint())+1);
      }
      else{
        pts.put(l.startPoint(),1);
      }
      if(pts.containsKey(l.endPoint())){
        pts.put(l.endPoint(), pts.get(l.endPoint())+1);
      }
      else{
        pts.put(l.endPoint(),1);
      }
    }
    int deadend = 0;
    for(IDirectPosition p : pts.keySet()){
      int deg = pts.get(p);
      if(deg>=3){
        return false;
      }
      if(deg == 1){
        deadend ++;
      }
    }
    return (deadend == 2);
  }

  private Set<HiddenState> getNeighbors(HiddenState current,
      Collection<HiddenState> lines) {
    ILineString lcurrent = (ILineString) current.getGeom();
    Set<HiddenState> result = new HashSet<HiddenState>();
    for(HiddenState s : lines) {
      if(current.equals(s)) {
        continue;
      }
      ILineString ls = (ILineString) s.getGeom();
      if(ls.startPoint().equals(lcurrent.startPoint()) ||
          ls.startPoint().equals(lcurrent.endPoint()) ||
          ls.endPoint().equals(lcurrent.startPoint()) ||
          ls.endPoint().equals(lcurrent.endPoint()) ) {
        result.add(s);
      }
    }
    return result;
  }

}
