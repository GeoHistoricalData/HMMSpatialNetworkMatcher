package fr.ign.cogit.v2.utils;

import org.apache.commons.collections15.Transformer;

import fr.ign.cogit.v2.snapshot.GraphEntity;

/**
 * Gestion du système de pondération des arcs
 * @author bcostes
 *
 */
public class WeightingUtils {
    
    public static enum WEIGHTING{
        UNIFORM, WEIGHT, RANDOM;
    }
    
    public static Transformer<GraphEntity, Double> getTransformer(WEIGHTING w){
        if(w.equals(WEIGHTING.RANDOM)){
            return WeightingUtils.getRandomTransformer();
        }
        if(w.equals(WEIGHTING.UNIFORM)){
            return WeightingUtils.getUniformTransformer();
        }
        if(w.equals(WEIGHTING.WEIGHT)){
            return WeightingUtils.getWeightTransformer();
        }
        return null;
    } 

    private static Transformer<GraphEntity, Double> getUniformTransformer() {
        Transformer<GraphEntity, Double> t = new Transformer<GraphEntity, Double>() {
            public Double transform(GraphEntity input) {
               return 1.;
            }
        };
        return t;
    }
    
    private static Transformer<GraphEntity, Double> getWeightTransformer() {
        Transformer<GraphEntity, Double> t = new Transformer<GraphEntity, Double>() {
            public Double transform(GraphEntity input) {
               return input.getWeight();
            }
        };
        return t;
    }
    
    private static Transformer<GraphEntity, Double> getRandomTransformer() {
        Transformer<GraphEntity, Double> t = new Transformer<GraphEntity, Double>() {
            public Double transform(GraphEntity input) {
               return Math.random();
            }
        };
        return t;
    }

}
