package fr.ign.cogit.v2.patterns;

import java.util.List;

import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STProperty;

public abstract class UnstableSTPattern extends STPattern {
    public abstract List<FuzzyTemporalInterval> findEvent(STProperty<Boolean> ts);

    /**
     * Indice de confiance de la véracité historique du pattern détecté en regard des éventuelles
     * incohérences dans les temporalités des sources. 
     * Un score de 0% indique que le pattern est probablement imputable à la temporalisation floue des
     * données.
     * Un score de 100% indique qu'a priori, le pattern n'est pas une erreur due à la temporalité des sources (
     * mais ca ne veut pas dire qu'il s'agit d'un vraie cas d'apparition ou de réincarnation)
     */
    protected double trust;



    public double getTrust(){
        return this.trust;
    }  
}
