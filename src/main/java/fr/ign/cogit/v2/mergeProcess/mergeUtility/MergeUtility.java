package fr.ign.cogit.v2.mergeProcess.mergeUtility;

import fr.ign.cogit.v2.lineage.MatchingLink;
import fr.ign.cogit.v2.mergeProcess.MergingProcess;

public abstract class MergeUtility {

    public MergeUtility(MatchingLink link){
        this.link = link;
    }
    protected MatchingLink link;
    public abstract void merge(MergingProcess p);
}
