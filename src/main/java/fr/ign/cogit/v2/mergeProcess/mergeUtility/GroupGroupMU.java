package fr.ign.cogit.v2.mergeProcess.mergeUtility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import fr.ign.cogit.v2.lineage.MatchingLink;
import fr.ign.cogit.v2.mergeProcess.AcceptableMatchingPattern;
import fr.ign.cogit.v2.mergeProcess.MergingProcess;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;

public class GroupGroupMU extends MergeUtility{

  public GroupGroupMU(MatchingLink link) {
    super(link);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void merge(MergingProcess p) {
    // TODO Auto-generated method stub
    // récupération du sommet et du groupe
    Set<STEntity> group1 = new HashSet<STEntity>();
    Set<STEntity> group2 = new HashSet<STEntity>();
    if(AcceptableMatchingPattern.GROUP_GROUP.isMatchedBy(link)){
      // lien n : n
      //on vérifie que les deux noeuds sont les extrémité de l'arc
      Iterator<STEntity> itN = link.getSources().getNodes().iterator();
      STEntity node1 = itN.next();
      STEntity node2 = itN.next();
      STEntity edge1 = link.getSources().getEdges().iterator().next();
      if(!p.getStGraph().getEndpoints(edge1).getFirst().equals(node1) && !p.getStGraph().getEndpoints(edge1).getFirst().equals(node2) ||
          !p.getStGraph().getEndpoints(edge1).getSecond().equals(node1) && !p.getStGraph().getEndpoints(edge1).getSecond().equals(node2)){
        p.getMatchingLinks().remove(this.link);
        return;
      }
      itN = link.getTargets().getNodes().iterator();
      node1 = itN.next();
      node2 = itN.next();
      edge1 = link.getTargets().getEdges().iterator().next();
      if(!p.getStGraph().getEndpoints(edge1).getFirst().equals(node1) && !p.getStGraph().getEndpoints(edge1).getFirst().equals(node2) ||
          !p.getStGraph().getEndpoints(edge1).getSecond().equals(node1) && !p.getStGraph().getEndpoints(edge1).getSecond().equals(node2)){
        p.getMatchingLinks().remove(this.link);
        return; 
      }
      group1.addAll(link.getSources().getNodes());
      group1.addAll(link.getSources().getEdges());

      group2.addAll(link.getTargets().getNodes());
      group2.addAll(link.getTargets().getEdges());
      
      List<STEntity> edges = new ArrayList<STEntity>();
      List<STEntity> nodes = new ArrayList<STEntity>();

      for (STEntity entity : group1) {
        if (entity.getType() == STEntity.NODE) {
          nodes.add(entity);
        } else {
          edges.add(entity);
        }
      }
      Set<FuzzyTemporalInterval> times = new HashSet<FuzzyTemporalInterval>();
      for (STEntity e : edges) {
        for (FuzzyTemporalInterval t : e.getTimeSerie().getValues().keySet()) {
          if (e.existsAt(t)) {
            times.add(t);
          }
        }
      }
      for(STEntity n: nodes){
        for(FuzzyTemporalInterval t: times){
          if(!n.existsAt(t)){
            p.getMatchingLinks().remove(this.link);
            return;
          }
        }
      }
      
       edges = new ArrayList<STEntity>();
      nodes = new ArrayList<STEntity>();

      for (STEntity entity : group2) {
        if (entity.getType() == STEntity.NODE) {
          nodes.add(entity);
        } else {
          edges.add(entity);
        }
      }
       times = new HashSet<FuzzyTemporalInterval>();
      for (STEntity e : edges) {
        for (FuzzyTemporalInterval t : e.getTimeSerie().getValues().keySet()) {
          if (e.existsAt(t)) {
            times.add(t);
          }
        }
      }
      for(STEntity n: nodes){
        for(FuzzyTemporalInterval t: times){
          if(!n.existsAt(t)){
            p.getMatchingLinks().remove(this.link);
            return;
          }
        }
      }

      // on va modifier node et supprimer le groupe
      // Modification de node1
      MergeUtils.mergeNodes(p.getStGraph(), group1, group2);
    }

    p.getMatchingLinks().remove(this.link);
  }

}

