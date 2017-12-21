package fr.ign.cogit.v2.mergeProcess.mergeUtility;

import fr.ign.cogit.v2.lineage.MatchingLink;
import fr.ign.cogit.v2.mergeProcess.MergingProcess;
import fr.ign.cogit.v2.tag.STEntity;

/**
 * Réalise la fusion de deux sommets à partir d'un lien d'appariement 1 : 1
 * @author bcostes
 *
 */
public class SingleNodeSingleNodeMU extends MergeUtility{

    public SingleNodeSingleNodeMU(MatchingLink link) {
        super(link);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void merge(MergingProcess p) {
        // TODO Auto-generated method stub
        // récupération des deux sommets 
        STEntity node1 = link.getSources().getNodes().iterator().next();
        STEntity node2 = link.getTargets().getNodes().iterator().next();
        // on va modifier node1 et supprimer node2
        // Modification de node1
        MergeUtils.mergeNodes(p.getStGraph(), node1, node2);
        p.getMatchingLinks().remove(this.link);
    }

}
