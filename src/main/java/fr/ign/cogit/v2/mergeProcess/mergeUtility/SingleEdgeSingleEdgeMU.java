package fr.ign.cogit.v2.mergeProcess.mergeUtility;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.ign.cogit.v2.lineage.MatchingLink;
import fr.ign.cogit.v2.mergeProcess.MergingProcess;
import fr.ign.cogit.v2.tag.STEntity;

/**
 * Réalise la fusion de deux arcs à partir d'un lien d'appariement 1 : 1
 * @author bcostes
 *
 */
public class SingleEdgeSingleEdgeMU extends MergeUtility{
    
    private final Logger logger = Logger.getLogger(SingleEdgeSingleEdgeMU.class);


    public SingleEdgeSingleEdgeMU(MatchingLink link) {
        super(link);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void merge(MergingProcess p) {
        // TODO Auto-generated method stub
        // récupération des deux sommets 
        STEntity edgeSource = link.getSources().getEdges().iterator().next();
        STEntity edgeTarget = link.getTargets().getEdges().iterator().next();
        
        p.getMatchingLinks().remove(this.link);
        
        
        // *************************************************************************
        // ****************************** Cas 1 ************************************
        // *************************************************************************
        if (p.getStGraph().getEndpoints(edgeSource).getFirst()
            .equals(p.getStGraph().getEndpoints(edgeTarget).getFirst())
            && p.getStGraph().getEndpoints(edgeSource).getSecond()
                .equals(p.getStGraph().getEndpoints(edgeTarget).getSecond())
            || p.getStGraph().getEndpoints(edgeSource).getFirst()
                .equals(p.getStGraph().getEndpoints(edgeTarget).getSecond())
            && p.getStGraph().getEndpoints(edgeSource).getSecond()
                .equals(p.getStGraph().getEndpoints(edgeTarget).getFirst())) {
          // les deux arcs ont les même extrémités
          // c'est donc déja un lien 1:1 élémentaire, on ne fait rien
        MergeUtils.mergeEdges(p.getStGraph(), edgeSource, edgeTarget);
        }
        else{
            // Sinon on split
            Set<STEntity> edgesSource = new HashSet<STEntity>();
            Set<STEntity> edgesTarget = new HashSet<STEntity>();
            edgesSource.add(edgeSource);
            edgesTarget.add(edgeTarget);

            //boolean splitOk = SplitUtils.split(edgesSource, edgesTarget, g1, g2);
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

}
