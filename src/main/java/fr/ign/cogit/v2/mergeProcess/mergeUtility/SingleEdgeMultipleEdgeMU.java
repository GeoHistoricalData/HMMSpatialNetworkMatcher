package fr.ign.cogit.v2.mergeProcess.mergeUtility;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.ign.cogit.v2.lineage.MatchingLink;
import fr.ign.cogit.v2.mergeProcess.MergingProcess;
import fr.ign.cogit.v2.tag.STEntity;

/**
 * Réalise la fusion d'un arc avec plusieurs autres arcs à partir d'un lien
 *  d'appariement 1 : n
 * @author bcostes
 *
 */
public class SingleEdgeMultipleEdgeMU extends MergeUtility{

    private final Logger logger = Logger.getLogger(SingleEdgeMultipleEdgeMU.class);

    public SingleEdgeMultipleEdgeMU(MatchingLink link) {
        super(link);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void merge(MergingProcess p) {
      
        // on récupère le lien seul et les liens appariés
        Set<STEntity> edgesSource = new HashSet<STEntity>();
        Set<STEntity> edgesTarget = new HashSet<STEntity>();
        if (link.getSources().getEdges().size() == 1) {
            edgesSource.add(link.getSources().getEdges().iterator().next());
            edgesTarget.addAll(link.getTargets().getEdges());
        } else {
            edgesSource.addAll(link.getSources().getEdges());
            edgesTarget.add(link.getTargets().getEdges().iterator().next());
        }
        
        p.getMatchingLinks().remove(this.link);


        

        // on split
       // boolean splitOk = SplitUtils.split(edgesSource, edgesTarget, g1, g2);
        boolean splitOk = SplitUtils.splitCurviligne(edgesSource, edgesTarget, p.getStGraph());

        
        if (!splitOk) {
            String idSources = "[";
            for(STEntity e : link.getSources().getEdges()){
                idSources+= e.getId()+" , ";
            }
            idSources= idSources.substring(0, idSources.length()-3);
            idSources+= "]";
            String idTargets = "[";
            for(STEntity e : link.getTargets().getEdges()){
                idTargets+= e.getId()+" , ";
            }
            idTargets= idTargets.substring(0, idTargets.length()-3);
            idTargets+= "]";
            logger.info("Merge not possible between edge(s) " + idSources +" and edge(s) " + idTargets);
            return;
        }
        Set<MatchingLink> newLinks = SplitUtils.splitLinks(edgesSource, edgesTarget, p.getStGraph()/*, link.getDateTarget()*/);
        if (newLinks == null) {
            return;
        }

          // on a plus qu'a fusionner classiquement les nouveaux liens 1:1
          // élémentaires
          for (MatchingLink l : newLinks) {
            MergeUtils.mergeEdges( p.getStGraph(), l.getSources().getEdges().iterator().next(),
                    l.getTargets().getEdges().iterator().next());
          }
        
    }

}
