package fr.ign.cogit.v2.tag;


public class STLink {
    public STLink(FuzzyTemporalInterval tsource, FuzzyTemporalInterval ttarget) {
        this.tsource = tsource;
        this.ttarget = ttarget;
    }


    private STEntity source;
    private STEntity target;

    private FuzzyTemporalInterval tsource;
    private FuzzyTemporalInterval ttarget;

    public void setTargets(STEntity target) {
        this.target = target;
    }

    public STEntity getTarget() {
        return target;
    }

    public void setSource(STEntity source) {
        this.source = source;
    }

    public STEntity getSource() {
        return source;
    }

    public void setDateTarget(FuzzyTemporalInterval ttarget) {
        this.ttarget = ttarget;
    }

    public FuzzyTemporalInterval getDateTarget() {
        return ttarget;
    }

    public void setDateSource(FuzzyTemporalInterval tsource) {
        this.tsource = tsource;
    }

    public FuzzyTemporalInterval getDateSource() {
        return tsource;
    }

    public void updateFuzzyTemporalInterval(FuzzyTemporalInterval told,
            FuzzyTemporalInterval tnew) {
        if (this.tsource != null && this.tsource.equals(told)) {
            this.tsource = tnew;
        } else if (this.ttarget != null && this.ttarget.equals(told)) {
            this.ttarget = tnew;
        }
    }
}
