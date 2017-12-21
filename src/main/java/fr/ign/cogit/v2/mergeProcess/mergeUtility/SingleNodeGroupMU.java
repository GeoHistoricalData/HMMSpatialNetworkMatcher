package fr.ign.cogit.v2.mergeProcess.mergeUtility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.ign.cogit.v2.lineage.MatchingLink;
import fr.ign.cogit.v2.mergeProcess.AcceptableMatchingPattern;
import fr.ign.cogit.v2.mergeProcess.MergingProcess;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.utils.JungUtils;

/**
 * Réalise la fusion d'un sommet et d'un groupe {somemt ,arc , sommet} à partir d'un
 * lien d'appariement 1 : n     
 * @author bcostes
 *
 */
public class SingleNodeGroupMU extends MergeUtility{

    public SingleNodeGroupMU(MatchingLink link) {
        super(link);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void merge(MergingProcess p) {
        // TODO Auto-generated method stub
        // récupération du sommet et du groupe
        STEntity node =  null;
        Set<STEntity> group = new HashSet<STEntity>();
        if(AcceptableMatchingPattern.SINGLENODE_GROUP.isMatchedBy(link)){
            // lien 1 : n
            node = link.getSources().getNodes().iterator().next();
//            if(link.getTargets().getNodes().size() != link.getTargets().getEdges().size()+1){
//              p.getMatchingLinks().remove(this.link);
//              return;
//            }
            group.addAll(link.getTargets().getNodes());
            group.addAll(link.getTargets().getEdges());
            
            
            List<STEntity> edges = new ArrayList<STEntity>();
            List<STEntity> nodes = new ArrayList<STEntity>();

            for (STEntity entity : group) {
              if (entity.getType() == STEntity.NODE) {
                nodes.add(entity);
              } else {
                edges.add(entity);
              }
            }
//            Set<FuzzyTemporalInterval> times = new HashSet<FuzzyTemporalInterval>();
//            for (STEntity e : edges) {
//              for (FuzzyTemporalInterval t : e.getTimeSerie().getValues().keySet()) {
//                if (e.existsAt(t)) {
//                  times.add(t);
//                }
//              }
//            }
//            for(STEntity n: nodes){
//              for(FuzzyTemporalInterval t: times){
//                if(!n.existsAt(t)){
//                  p.getMatchingLinks().remove(this.link);
//                  return;
//                }
//              }
//            }

        }
        else{
            // lien n : 1
            node = link.getTargets().getNodes().iterator().next();
//            if(link.getSources().getNodes().size() != link.getSources().getEdges().size()+1){
//              p.getMatchingLinks().remove(this.link);
//              return;
//            }
            group.addAll(link.getSources().getNodes());
            group.addAll(link.getSources().getEdges());
            List<STEntity> edges = new ArrayList<STEntity>();
            List<STEntity> nodes = new ArrayList<STEntity>();

            for (STEntity entity : group) {
              if (entity.getType() == STEntity.NODE) {
                nodes.add(entity);
              } else {
                edges.add(entity);
              }
            }
//            Set<FuzzyTemporalInterval> times = new HashSet<FuzzyTemporalInterval>();
//            for (STEntity e : edges) {
//              for (FuzzyTemporalInterval t : e.getTimeSerie().getValues().keySet()) {
//                if (e.existsAt(t)) {
//                  times.add(t);
//                }
//              }
//            }
//            for(STEntity n: nodes){
//              for(FuzzyTemporalInterval t: times){
//                if(!n.existsAt(t)){
//                  p.getMatchingLinks().remove(this.link);
//                  return;
//                }
//              }
//            }
        }
        // on va modifier node et supprimer le groupe
        // Modification de node1
        MergeUtils.mergeNodes(p.getStGraph(), node, group);
        p.getMatchingLinks().remove(this.link);
        
    }

}
