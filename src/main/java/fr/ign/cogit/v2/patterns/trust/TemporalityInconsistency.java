package fr.ign.cogit.v2.patterns.trust;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nrc.fuzzy.FuzzySet;
import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.patterns.AppearanceSTPattern;
import fr.ign.cogit.v2.patterns.ReincarnationSTPattern;
import fr.ign.cogit.v2.patterns.STPattern.PATTERN_TYPE;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;

public class TemporalityInconsistency extends InconsistencyCriteria{

    @Override
    public void evaluate(STGraph stgraph, STEntity edge, PATTERN_TYPE type) {      
        // Pour les apparitions, a priori le critère de temporalité ne joue pas
        if(type == PATTERN_TYPE.APPEARANCE){
            return;
        }        
        Map<Integer, FuzzyTemporalInterval> indexes = new HashMap<Integer, FuzzyTemporalInterval>();
        List<FuzzyTemporalInterval> l = new ArrayList<FuzzyTemporalInterval>();
        l.addAll(edge.getTimeSerie().getValues().keySet());
        l.remove(null);
        Collections.sort(l);
        String sequence = "";
        for (int i=0; i< l.size(); i++) {
            sequence += (edge.getTimeSerie().getValues().get(l.get(i)) ? "1" : "0");
            indexes.put(i, l.get(i));
        }
        //pour les reincarnations
        int deathIndex = sequence.indexOf("10");
        int lifeIndex = sequence.indexOf("01",deathIndex) +1;   
        int firstOne = sequence.indexOf("1");
        int lastOne = sequence.lastIndexOf("1");
        
        
        double a1=0, a2 =0;
        FuzzySet tint = indexes.get(lifeIndex-1);
        FuzzySet tunion = indexes.get(lifeIndex-1);
        
        for(int i=firstOne; i<=deathIndex; i++){
            tunion = tunion.fuzzyUnion(indexes.get(i));
            tint = tint.fuzzyIntersection(indexes.get(i));
        }
        a1 = tint.getArea() / tunion.getArea();
        
        FuzzySet tint2 = indexes.get(deathIndex+1);
        FuzzySet tunion2 = indexes.get(deathIndex+1);

        for(int i=lifeIndex; i<=lastOne; i++){
            tunion2 = tunion2.fuzzyUnion(indexes.get(i));
            tint2 = tint2.fuzzyIntersection(indexes.get(i));
        }
        a2 = tint2.getArea() / tunion2.getArea();
        
        this.trust =  Math.max(a1, a2);
    }
    
    
    public static void main(String args[]){
        
        
//        try {
//            FuzzyTemporalInterval t1 = new FuzzyTemporalInterval
//                    (new double[]{1808,1810,1836,1853},new double[]{0,1,1,0}, 4);
//            FuzzyTemporalInterval t2 = new FuzzyTemporalInterval
//                    (new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4);
//            
//            System.out.println(t1.fuzzyIntersection(t2).getArea());
//            System.out.println(t2.fuzzyIntersection(t1).getArea());
//
//            
//        } catch (XValuesOutOfOrderException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (YValueOutOfRangeException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        
        STGraph stg = TAGIoManager.deserialize("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v5/tag.tag");
        ReincarnationSTPattern app = new ReincarnationSTPattern();
        for(STEntity e : stg.getEdges()){
            if(app.find(e.getTimeSerie())){
                TemporalityInconsistency criteria = new TemporalityInconsistency();
                criteria.evaluate(stg, e, PATTERN_TYPE.REINCARNATION);
            }
        }
    }

}
