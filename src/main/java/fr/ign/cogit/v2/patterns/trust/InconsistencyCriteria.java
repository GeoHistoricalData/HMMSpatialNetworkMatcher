package fr.ign.cogit.v2.patterns.trust;

import fr.ign.cogit.v2.patterns.STPattern;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;

public abstract class InconsistencyCriteria {
    
    /**
     * Paramètre de confiance dans la cause de l'incohérence
     */
    protected double trust;
    
    public InconsistencyCriteria(){
        this.trust = 0.;
    }
    
    public double getTrust() {
        return trust;
    }

    public void setTrust(double trust) {
        this.trust = trust;
    }

    public abstract void evaluate(STGraph stgraph,STEntity edge, STPattern.PATTERN_TYPE type);

}
