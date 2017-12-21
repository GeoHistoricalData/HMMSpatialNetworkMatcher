package fr.ign.cogit.v2.patterns.trust;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.patterns.AppearanceSTPattern;
import fr.ign.cogit.v2.patterns.ReincarnationSTPattern;
import fr.ign.cogit.v2.patterns.STPattern;
import fr.ign.cogit.v2.patterns.STPattern.PATTERN_TYPE;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;

public class InconsitenciesEvaluator {
    
    private static final Logger logger = Logger.getLogger(InconsitenciesEvaluator.class);
    
    
    private double[] evaluate(STGraph stgraph, STEntity edge, STPattern.PATTERN_TYPE type){
        double[] criterion = new double[5];
        MatchingInconsistency matchEvaluator = new MatchingInconsistency();
        CompletnessInconsistency completnessEvaluator = new CompletnessInconsistency();
        TemporalityInconsistency temporalityEvaluator = new TemporalityInconsistency();
        ProjectInconsistency projectEvaluator = new ProjectInconsistency();
        RealAppearanceCase realCaseEvaluator = new RealAppearanceCase();
        
        matchEvaluator.evaluate(stgraph, edge, type);
        criterion[0] = matchEvaluator.getTrust();
        completnessEvaluator.evaluate(stgraph, edge, type);
        criterion[1] = completnessEvaluator.getTrust();
        temporalityEvaluator.evaluate(stgraph, edge, type);
        criterion[2] = temporalityEvaluator.getTrust();
        projectEvaluator.evaluate(stgraph, edge, type);
        criterion[3] = projectEvaluator.getTrust();
        realCaseEvaluator.evaluate(stgraph, edge, type);
        criterion[4] = realCaseEvaluator.getTrust();
        return criterion;
    }
    
    public Map<STEntity, double[]> evaluate(STGraph stgraph){
        Map<STEntity, double[]> result = new HashMap<STEntity, double[]>();
        Set<STEntity> appearances = new HashSet<STEntity>();
        Set<STEntity> reincarnations = new HashSet<STEntity>();
        AppearanceSTPattern pattern1 = new AppearanceSTPattern();
        ReincarnationSTPattern pattern2 = new ReincarnationSTPattern();
        for(STEntity edge : stgraph.getEdges()){
            if(pattern1.find(edge.getTimeSerie())){
                appearances.add(edge);
                continue;
            }
            if(pattern2.find(edge.getTimeSerie())){
                reincarnations.add(edge);
            }
        }
        //apparitions
        for(STEntity edge: appearances){
            double[] criterion = this.evaluate(stgraph, edge, PATTERN_TYPE.APPEARANCE);
            result.put(edge, criterion);
        }
        //réincarnations
        for(STEntity edge: reincarnations){
            double[] criterion = this.evaluate(stgraph, edge, PATTERN_TYPE.REINCARNATION);
            result.put(edge, criterion);
        }
        return result;
    }
    
    public void writeEvaluation(STGraph stgraph, String repository){
        File r = new File(repository);
        if(!r.isDirectory()){
            return;
        }
        if(!r.exists()){
            r.mkdirs();
        }
        Set<STEntity> appearances = new HashSet<STEntity>();
        Set<STEntity> reincarnations = new HashSet<STEntity>();
        AppearanceSTPattern pattern1 = new AppearanceSTPattern();
        ReincarnationSTPattern pattern2 = new ReincarnationSTPattern();
        for(STEntity edge : stgraph.getEdges()){
            if(pattern1.find(edge.getTimeSerie())){
                appearances.add(edge);
                continue;
            }
            if(pattern2.find(edge.getTimeSerie())){
                reincarnations.add(edge);
            }
        }
        //apparitions
        String s ="";
        for(STEntity edge: appearances){
            s+=edge.getId()+";";
            double[] criterion = this.evaluate(stgraph, edge, PATTERN_TYPE.APPEARANCE);
            for(double d: criterion){
                s+= d + ";";
            }
            s = s.substring(0, s.length()-1);
            s += "\n";
        }       
        try {
            FileWriter fr = new FileWriter(repository+File.separator + "appearances.txt");
            BufferedWriter br = new BufferedWriter(fr);
            br.write(s);
            br.flush();
            br.close();
            fr.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //réincarnations
        s ="";
        for(STEntity edge: reincarnations){
            s+=edge.getId()+";";
            double[] criterion = this.evaluate(stgraph, edge, PATTERN_TYPE.REINCARNATION);
            for(double d: criterion){
                s+= d + ";";
            }
            s = s.substring(0, s.length()-1);
            s += "\n";
        }
        try {
            FileWriter fr = new FileWriter(repository+File.separator + "reincarnations.txt");
            BufferedWriter br = new BufferedWriter(fr);
            br.write(s);
            br.flush();
            br.close();
            fr.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        STGraph stg = TAGIoManager.deserialize("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v5/tag.tag");
        
        InconsitenciesEvaluator evaluator = new InconsitenciesEvaluator();
        evaluator.writeEvaluation(stg, "/media/bcostes/Data/Benoit/these/analyses/TAG/analyse_patterns/v2/test_qualification_automatique");

    }

}
