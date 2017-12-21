package hmmmatching.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.feature.Population;
import twitter4j.Logger;

/**
 * Collection de cluster d'arcs correspondant à toutes les combinaisons possibles pouvant être 
 * fusionées, crées à partir d'un ensemble d'arcs
 * @author bcostes
 *
 */
public class AClusterCollection extends Population<ACluster>{
  
  private Logger logger = Logger.getLogger(AClusterCollection.class);


  public AClusterCollection(List<Arc> arcs){
    super();

    if(arcs.size()>=25 && logger.isWarnEnabled()){
      logger.warn("Candidates size : " + arcs.size() + " , computation may takes some time ...");
    }
    List<List<Arc>> combinations = this.getAllPossibleCombinations(arcs);
    //combinations = this.filter(combinations);
    for(List<Arc> la: combinations){
      ACluster acluster = new ACluster(la);
      this.add(acluster);
    }

  }

  /**
   * On donne les sous-ensembles possibles mais en filtrant au fur et à mesure
   * Attention : peu être très long ...
   * @param arcs
   * @return
   */
  @SuppressWarnings("unused")
  private List<List<Arc>> enumerateWithFilter(List<Arc> arcs){
    List<List<Arc>> result = new ArrayList<>();
    int length = arcs.size();
    int[] bitVector = new int[length + 2];
    for (int i = 0; i <= length + 1; i++) {
      bitVector[i] = 0;
    }
    while(bitVector[length + 1] != 1){
      List<Arc> currentSubSet = new ArrayList<>();
      for (int index = 1; index <= length; index++) {
        if (bitVector[index] == 1) {
          Arc value = arcs.get(index - 1);
          currentSubSet.add(value);
        }
      }
      int i = 1;
      while (bitVector[i] == 1) {
        bitVector[i] = 0;
        i++;
      }
      bitVector[i] = 1;
      if(this.lineMergePossible(currentSubSet)){
        result.add(currentSubSet);
      }
    }
    return result;    
  }


  /**
   * On ne retient que les arcs pouvant être fusionnés
   */
  @SuppressWarnings("unused")
  private List<List<Arc>> filter(List<List<Arc>> arcs){
    List<List<Arc>> combinationsf = new ArrayList<List<Arc>>();
    for(List<Arc> la: arcs){
      if(this.lineMergePossible(la)){
        combinationsf.add(la);
      }
    }
    return combinationsf;
  }
  /**
   * Méthode qui teste si un ensemble de linestring peut être aggrégé
   * @param lines
   * @return
   */
  private boolean lineMergePossible(List<Arc> lines){
    if(lines.size() == 1){
      return true;
    }
    Map<IDirectPosition, Integer> pts = new HashMap<IDirectPosition, Integer>();
    for(Arc a: lines){
      ILineString l = a.getGeometrie();
      if(pts.containsKey(l.startPoint())){
        pts.put(l.startPoint(), pts.get(l.startPoint())+1);
      }
      else{
        pts.put(l.startPoint(),1);
      }
      if(pts.containsKey(l.endPoint())){
        if(l.endPoint() == null){
          System.out.println("aie");
        }
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


  private List<List<Arc>> getAllPossibleCombinations(List<Arc> lines){
    List<List<Arc>> result = new ArrayList<>();

    Stack<Arc> copy = new Stack<>();
    copy.addAll(lines);

    while(!copy.isEmpty()){
      // Cette boucle sert à gérer les cas ou il y a plusieurs composantes connexes
      List<List<Arc>> clusters = new ArrayList<>();    
      //arcs déja traités
      Set<Arc> processed = new HashSet<>();
      // queue de traitement
      List<Arc> queue = new ArrayList<>();
      queue.add(copy.pop());
      while(!queue.isEmpty()){
        // on récupère l'arc courant
        Arc current = queue.remove(0);
        // on le déclare comme traité
        processed.add(current);
        Set<Arc> neighbors = new HashSet<>();
        neighbors.addAll(current.getNoeudIni().getEntrants());
        neighbors.addAll(current.getNoeudIni().getSortants());
        neighbors.addAll(current.getNoeudFin().getEntrants());
        neighbors.addAll(current.getNoeudFin().getSortants());
        neighbors.removeAll(processed);
        neighbors.removeAll(queue);
        for(Arc a : new ArrayList<>(neighbors)){
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
        for(List<Arc> previousCluster : new ArrayList<>(clusters)){
          List<Arc> newCluster = new ArrayList<>();
          newCluster.add(current);
          newCluster.addAll(previousCluster);
          if(this.lineMergePossible(newCluster)){
            // nouveau cluster validé
            clusters.add(newCluster);
          }
        }
        List<Arc> newCluster = new ArrayList<>();
        newCluster.add(current);
        clusters.add(newCluster);
      }
      copy.removeAll(processed);
      result.addAll(clusters);
    }

    return result;
  }
  


}
