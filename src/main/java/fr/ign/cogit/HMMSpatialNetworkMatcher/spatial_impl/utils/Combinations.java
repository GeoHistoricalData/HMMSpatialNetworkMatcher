package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;

/**
 * @author bcostes
 *
 * @param <T>
 */
public class Combinations<T extends IFeature> {
  
  public List<List<T>> getAllCombinations(Collection<T> lines){
    
    List<List<T>> result = new ArrayList<>();
    

    if(lines.isEmpty()) {
      return result;
    }

    if(!(lines.iterator().next().getGeom() instanceof ILineString)) {
      // TODO : logging ?
      return result;
    }

    Stack<T> copy = new Stack<>();
    copy.addAll(lines);

    while(!copy.isEmpty()){
      // Cette boucle sert à gérer les cas ou il y a plusieurs composantes connexes
      List<List<T>> clusters = new ArrayList<>();    
      //arcs déja traités
      Set<T> processed = new HashSet<>();
      // queue de traitement
      List<T> queue = new ArrayList<>();
      queue.add(copy.pop());
      while(!queue.isEmpty()){
        // on récupère l'arc courant
        T current = queue.remove(0);
        // on le déclare comme traité
        processed.add(current);
        Set<T> neighbors = getNeighbors(current, lines);
        neighbors.removeAll(processed);
        neighbors.removeAll(queue);
        for(T a : new ArrayList<>(neighbors)){
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
        for(List<T> previousCluster : new ArrayList<>(clusters)){
          List<T> newCluster = new ArrayList<>();
          newCluster.add(current);
          newCluster.addAll(previousCluster);
          if(lineMergePossible(newCluster)){
            // nouveau cluster validé
            clusters.add(newCluster);
          }
        }
        List<T> newCluster = new ArrayList<>();
        newCluster.add(current);
        clusters.add(newCluster);
      }
      copy.removeAll(processed);
      result.addAll(clusters);
    }

    return result;
  }

  /**
   *  Méthode qui teste si un ensemble de linestring peut être aggrégé.
   * @param lines linestring to merge
   * @return true if merge possible
   */
  private boolean lineMergePossible(List<T> lines) {
    if(lines.size() == 1){
      return true;
    }
    for(T s : lines) {
      if(((ILineString)s.getGeom()).startPoint().equals(((ILineString)s.getGeom()).endPoint())) {
        // pour éviter les boucles
        return false;
      }
    }
    Map<IDirectPosition, Integer> pts = new HashMap<>();
    for(T a: lines){
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

  private Set<T> getNeighbors(T current,
      Collection<T> lines) {
    ILineString lcurrent = (ILineString) current.getGeom();
    Set<T> result = new HashSet<>();
    for(T s : lines) {
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
