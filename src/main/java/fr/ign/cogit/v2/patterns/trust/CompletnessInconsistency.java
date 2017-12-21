package fr.ign.cogit.v2.patterns.trust;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.patterns.AppearanceSTPattern;
import fr.ign.cogit.v2.patterns.ReincarnationSTPattern;
import fr.ign.cogit.v2.patterns.STPattern.PATTERN_TYPE;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;

public class CompletnessInconsistency extends InconsistencyCriteria{
    
    private Map<FuzzyTemporalInterval, Double> accuracies;
    private final List<String> NAMES = new ArrayList<String>(
            Arrays.asList("passage","impasse","pas", "imp", "cul", "place",
                    "pl", "ruelle","allee","chemin","champ","c.", "cloitre",
                    "carreau","cour", "petit","petite","port","preau","cul-de-sac",
                    "all","cite","crs","gal","galerie","i","rle","sq"));  
    
    public CompletnessInconsistency(){
        super();
        this.accuracies  = new HashMap<FuzzyTemporalInterval, Double>();
        try {
            this.accuracies.put(new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4),
                    0.667);
            this.accuracies.put(new FuzzyTemporalInterval(new double[]{1808,1810,1836,1853},new double[]{0,1,1,0}, 4),
                    0.825);
            this.accuracies.put(new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4),
                    0.807);
            this.accuracies.put(new FuzzyTemporalInterval(new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4),
                    0.807);
            this.accuracies.put(new FuzzyTemporalInterval(new double[]{1870,1871,1871,1872},new double[]{0,1,1,0}, 4),
                    0.439);

            //this.accuracies.put(new FuzzyTemporalInterval(new double[]{1887,1888,1888,1889},new double[]{0,1,1,0}, 4),
            //         0.0);
        } catch (XValuesOutOfOrderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (YValueOutOfRangeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    


    public void evaluate(STGraph stgraph, STEntity edge, PATTERN_TYPE type) {
        
        //on ne travaille que sur les passages, cours, etc.
        boolean ok = false;
        for(FuzzyTemporalInterval t :edge.getTimeSerie().getValues().keySet()){
            if(stgraph.getIncidentEdgessAt(stgraph.getEndpoints(edge).getFirst(),t).size() == 1 
                    || stgraph.getIncidentEdgessAt(stgraph.getEndpoints(edge).getSecond(),t).size() == 1){
                ok = true;
                break;
            }
            String name = edge.getTAttributeByName(",name").getValueAt(t);
            if(name == null || name.equals(" ")){
                continue;
            }
            name = name.toLowerCase();
            name = name.trim();
            StringTokenizer tokenizer = new StringTokenizer(name, " ");
            if(tokenizer.hasMoreTokens()){
                String firstToken = tokenizer.nextToken();
                if(NAMES.contains(firstToken)){
                    ok = true;
                    break;
                }
            }
        }
        if(!ok){
            return;
        }
        
        Map<Integer, FuzzyTemporalInterval> indexes = new HashMap<Integer, FuzzyTemporalInterval>();
        List<FuzzyTemporalInterval> l = new ArrayList<FuzzyTemporalInterval>();
        l.addAll(edge.getTimeSerie().getValues().keySet());
        l.remove(null);
        Collections.sort(l);
        String sequence = "";
        double ones =0.;
        for (int i=0; i< l.size(); i++) {
            sequence += (edge.getTimeSerie().getValues().get(l.get(i)) ? "1" : "0");
            if(edge.existsAt(l.get(i))){
                ones++;
            }
            indexes.put(i, l.get(i));
        }
        ones /= ((double)sequence.length());
        if(type == PATTERN_TYPE.APPEARANCE){
            int lifeIndex = sequence.indexOf("01");
            int deathIndex = sequence.indexOf("10",lifeIndex) +1;   
            int firstZero = sequence.indexOf("0");
            int lastZero = sequence.lastIndexOf("0");
            double a1=1., a2=1.;
            for(int i=firstZero; i<=lifeIndex; i++){
                a1 *= (1. - this.accuracies.get(indexes.get(i)));
            }
            for(int i=deathIndex; i<=lastZero; i++){
                a2 *= (1. - this.accuracies.get(indexes.get(i)));
            }
            this.trust = Math.max(a1, a2) * ones;
        }   
        if(type == PATTERN_TYPE.REINCARNATION){
            int deathIndex = sequence.indexOf("10")+1;
            int lifeIndex = sequence.indexOf("01",deathIndex);   
            double a = 1.;
            for(int i=deathIndex; i<=lifeIndex; i++){
                a *= (1. - this.accuracies.get(indexes.get(i)));
            }
            this.trust = a * ones;
        } 

    }
    
    public static void main(String[] args) {
        STGraph stg = TAGIoManager.deserialize("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v5/tag.tag");
        ReincarnationSTPattern app = new ReincarnationSTPattern();
        for(STEntity e : stg.getEdges()){
            if(app.find(e.getTimeSerie())){
                CompletnessInconsistency criteria = new CompletnessInconsistency();
                criteria.evaluate(stg, e, PATTERN_TYPE.REINCARNATION);
            }
        }

    }

}
