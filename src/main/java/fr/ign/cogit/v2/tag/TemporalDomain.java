package fr.ign.cogit.v2.tag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Un domaine temporel de validité
 * @author bcostes
 * 
 */
public class TemporalDomain implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public TemporalDomain() {
        this.temporalDomain = new ArrayList<FuzzyTemporalInterval>();
    }

    public TemporalDomain(List<FuzzyTemporalInterval> _temporalDomain) {
        this.temporalDomain = _temporalDomain;
    }

    private List<FuzzyTemporalInterval> temporalDomain;

    public void setDomain(List<FuzzyTemporalInterval> temporalDomain) {
        this.temporalDomain = temporalDomain;
    }

    public List<FuzzyTemporalInterval> asList() {
        return temporalDomain;
    }

    @Override
    public String toString() {
        return this.temporalDomain.toString();
    }

    public void updateFuzzyTemporalInterval(FuzzyTemporalInterval told,
            FuzzyTemporalInterval tnew) {
        int index = this.temporalDomain.indexOf(told);
        this.temporalDomain.add(index, tnew);
        this.temporalDomain.remove(told);
    }

}
