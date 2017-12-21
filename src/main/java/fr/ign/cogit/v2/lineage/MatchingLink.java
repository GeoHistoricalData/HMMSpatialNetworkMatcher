package fr.ign.cogit.v2.lineage;

import java.io.Serializable;

import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;

public class MatchingLink implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MatchingLink(FuzzyTemporalInterval tsource, FuzzyTemporalInterval ttarget) {
        this.sources = new STGroupe();
        this.targets = new STGroupe();
//        this.tsource = tsource;
//        this.ttarget = ttarget;
    }
    
//    public MatchingLink(FuzzyTemporalInterval ttarget) {
//        this.sources = new STGroupe();
//        this.targets = new STGroupe();
//        this.ttarget = ttarget;
//    }
    
    public MatchingLink() {
        this.sources = new STGroupe();
        this.targets = new STGroupe();
    }


    private STGroupe sources;
    private STGroupe targets;
//    private FuzzyTemporalInterval tsource;
//    private FuzzyTemporalInterval ttarget;

    public void setTargets(STGroupe targets) {
        this.targets = targets;
    }

    public STGroupe getTargets() {
        return targets;
    }

    public void setSources(STGroupe sources) {
        this.sources = sources;
    }

    public STGroupe getSources() {
        return sources;
    }
//    public void setDateTarget(FuzzyTemporalInterval ttarget) {
//        this.ttarget = ttarget;
//    }

   /* public FuzzyTemporalInterval getDateTarget() {
        return ttarget;
    }*/

//    public void setDateSource(FuzzyTemporalInterval tsource) {
//        this.tsource = tsource;
//    }
//
//    public FuzzyTemporalInterval getDateSource() {
//        return tsource;
//    }

    public boolean isSingleSourceSingleTarget() {
        return (this.sources.size() == 1 && this.targets.size() == 1);
    }

    public boolean isSingleSourceMultipleTarget() {
        return (this.sources.size() == 1 && this.targets.size() > 1);
    }

    public boolean isMultipleSourceSingleTarget() {
        return (this.sources.size() > 1 && this.targets.size() == 1);
    }

    public boolean isMultipleSourceMultipleTarget() {
        return (this.sources.size() > 1 && this.targets.size() > 1);
    }

    @Override
    public String toString() {
        String s = "";
        s += "LINK " + this.sources.size() + " : " + this.targets.size();
        s += "\n";
        s += "*** SOURCES ***\n";
        for (STEntity node : this.sources.getNodes()) {
            s += "NODE " + node.getId() + "\n";
        }
        for (STEntity edge : this.sources.getEdges()) {
            s += "EDGE " + edge.getId() + "\n";
        }
        s += "*** TARGETS ***\n";
        for (STEntity node : this.targets.getNodes()) {
            s += "NODE " + node.getId() + "\n";
        }
        for (STEntity edge : this.targets.getEdges()) {
            s += "EDGE " + edge.getId() + "\n";
        }
        return s;
    }
    

}
