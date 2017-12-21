package fr.ign.cogit.v2.patterns.trust;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;


import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.patterns.AppearanceSTPattern;
import fr.ign.cogit.v2.patterns.DeathSTPattern;
import fr.ign.cogit.v2.patterns.LifeSTPattern;
import fr.ign.cogit.v2.patterns.ReincarnationSTPattern;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.STProperty;
import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;


public class STPatternLevelOfDetail {

    public static List<String> NAMES = new ArrayList<String>(
            Arrays.asList("passage","impasse","pas", "imp", "cul", "place",
                    "pl", "ruelle","allee","chemin","champ","c.", "cloitre",
                    "carreau","cour", "petit","petite","port","preau","cul-de-sac",
                    "all","cite","crs","gal","galerie","i","rle","sq"));    
    public static void main(String args[]){

        STGraph stg = TAGIoManager.deserialize("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v4/with_indicators/tag_ind.tag");

        //        Map<FuzzyTemporalInterval, Double> mappingTimes = STPatternLevelOfDetail.getNumberOfPassages(stg);
        //        Map<FuzzyTemporalInterval, Double> mappingCreation = STPatternLevelOfDetail.getDensification2(stg);
        //
        //        List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>(mappingTimes.keySet());
        //        Collections.sort(times);
        //        for(FuzzyTemporalInterval t : times){
        //            System.out.println(t.getX(1)+ " : " + mappingTimes.get(t));
        //        }
        //
        //        System.out.println();
        //
        //        for(FuzzyTemporalInterval t : times){
        //            System.out.println(t.getX(1)+ " : " + mappingCreation.get(t));
        //        }
        //
        //
        //        STPatternLevelOfDetail.getLevelOfDetail(stg);
        STPatternLevelOfDetail.getLevelOfDetail(stg);


    }

    /**
     * Nombre de passages et impasses
     * @return
     */
    public static Map<FuzzyTemporalInterval, Double> getNumberOfPassages(STGraph stg){
        Map<FuzzyTemporalInterval, Double> mapping = new HashMap<FuzzyTemporalInterval, Double>();
        for(FuzzyTemporalInterval t : stg.getTemporalDomain().asList()){
            mapping.put(t, 0.);
            for(STEntity edge : stg.getEdgesAt(t)){
                if(stg.getIncidentEdgessAt(stg.getEndpoints(edge).getFirst(),t).size() == 1 
                        || stg.getIncidentEdgessAt(stg.getEndpoints(edge).getSecond(),t).size() == 1){
                    mapping.put(t, mapping.get(t)+1.);
                    continue;
                }
                String name = edge.getTAttributeByName("name").getValueAt(t);
                if(name == null || name.equals(" ")){
                    continue;
                }
                name = name.toLowerCase();
                name = name.trim();
                String norm1 = Normalizer.normalize(name, Normalizer.Form.NFD);
                Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
                name = pattern.matcher(norm1).replaceAll("");
                StringTokenizer tokenizer = new StringTokenizer(name, " ");
                if(tokenizer.hasMoreTokens()){
                    String firstToken = tokenizer.nextToken();
                    if(NAMES.contains(firstToken)){
                        mapping.put(t, mapping.get(t)+1.);
                    }
                }

            }
        }
        return mapping;
    }

    /**
     * Calcul de la densification comme le rapport entre le nombre de nouveau arc
     *  et le nombre d'arc détruit entre deux dates consécutives
     *  Attention : on ne considère que les arcs créés et existant jusqu'à la dernière date, et les arcs
     *  détruit définitivement. Donc ne sont pas prises en compte si les apparitions,
     *  ni les réincarnation. 
     *  Il s'agit donc d'une valeur de densification approximative
     * @return
     */
    public static Map<FuzzyTemporalInterval, Double> getDensification(STGraph stg){
        Map<FuzzyTemporalInterval, Double> mapping = new HashMap<FuzzyTemporalInterval, Double>();
        Map<FuzzyTemporalInterval, Double> birth = new HashMap<FuzzyTemporalInterval, Double>();
        Map<FuzzyTemporalInterval, Double> death = new HashMap<FuzzyTemporalInterval, Double>();
        // initialisation
        for(FuzzyTemporalInterval t : stg.getTemporalDomain().asList()){
            birth.put(t, 0.);
            death.put(t, 0.);
        }
        for(STEntity edge : stg.getEdges()){
            STProperty<Boolean> ts = edge.getTimeSerie();
            LifeSTPattern lifePattern = new LifeSTPattern();
            DeathSTPattern deatPattern = new DeathSTPattern();
            if(lifePattern.find(ts)){
                FuzzyTemporalInterval tpattern = lifePattern.findEvent(ts);
                birth.put(tpattern,birth.get(tpattern)+1.);
                continue;
            }
            if(deatPattern.find(ts)){
                FuzzyTemporalInterval tpattern = deatPattern.findEvent(ts);
                death.put(tpattern,death.get(tpattern)+1.);
            }
        }
        //densification
        for(FuzzyTemporalInterval t : stg.getTemporalDomain().asList()){
            mapping.put(t, birth.get(t) / death.get(t));
        }

        return mapping;
    }

    public static Map<FuzzyTemporalInterval, Double> getDensification2(STGraph stg){
        Map<FuzzyTemporalInterval, Double> mapping = new HashMap<FuzzyTemporalInterval, Double>();
        Map<FuzzyTemporalInterval, Double> birth = new HashMap<FuzzyTemporalInterval, Double>();
        Map<FuzzyTemporalInterval, Double> death = new HashMap<FuzzyTemporalInterval, Double>();
        // initialisation

        List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>(stg.getEdges().iterator().next().getTimeSerie().getValues().keySet());
        times.remove(null);
        Collections.sort(times);
        for(FuzzyTemporalInterval t : times){
            birth.put(t, 0.);
            death.put(t, 0.);
            int index = times.indexOf(t);
            for(STEntity edge: stg.getEdges()){
                //création
                if(index != 0){
                    if(edge.existsAt(t) && !edge.existsAt(times.get(index -1))){
                        birth.put(t, birth.get(t)+1.);
                    }
                    //destruction
                    if(!edge.existsAt(t) && edge.existsAt(times.get(index -1))){
                        death.put(t, death.get(t)+1.);
                    }
                }
            }

        }
        //densification
        for(FuzzyTemporalInterval t : stg.getTemporalDomain().asList()){
            mapping.put(t, birth.get(t) / death.get(t));
        }

        return mapping;
    }

    public static Map<FuzzyTemporalInterval, Double> getLevelOfDetail(STGraph stg){
        List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>(stg.getTemporalDomain().asList());
        Collections.sort(times);

        
        try {
            FuzzyTemporalInterval tvasserot = new FuzzyTemporalInterval(new double[]{1808,1810,1836,1853},new double[]{0,1,1,0}, 4);
            times.remove(tvasserot);
           } catch (XValuesOutOfOrderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (YValueOutOfRangeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        Map<FuzzyTemporalInterval, Set<STEntity>> edges = new HashMap<FuzzyTemporalInterval, Set<STEntity>>();
        for(FuzzyTemporalInterval t : times){
            Set<STEntity> set = new HashSet<STEntity>(stg.getEdgesAt(t));
            edges.put(t, set);
        }
        for(STEntity edge : stg.getEdges()){
            boolean remove = false;

            AppearanceSTPattern appearancePattern = new AppearanceSTPattern();
            if(appearancePattern.find(edge.getTimeSerie())){
                //on supprime l'arc
                remove = true;
            }
            if(remove){
                for(FuzzyTemporalInterval t :edges.keySet()){
                    edges.get(t).remove(edge);
                }
                continue;
            }
            ReincarnationSTPattern reincPattern = new ReincarnationSTPattern();
            if(reincPattern.find(edge.getTimeSerie())){
                //on supprime l'arc
                remove = true;
            }
            if(remove){
                for(FuzzyTemporalInterval t :edges.keySet()){
                    edges.get(t).remove(edge);
                }
                continue;
            }

            for(FuzzyTemporalInterval t :edge.getTimeSerie().getValues().keySet()){
                if(stg.getIncidentEdgessAt(stg.getEndpoints(edge).getFirst(),t).size() == 1 
                        || stg.getIncidentEdgessAt(stg.getEndpoints(edge).getSecond(),t).size() == 1){
                    remove = true;
                    break;
                }
                String name = edge.getTAttributeByName("name").getValueAt(t);
                if(name == null || name.equals(" ")){
                    continue;
                }
                name = name.toLowerCase();
                name = name.trim();
                StringTokenizer tokenizer = new StringTokenizer(name, " ");
                if(tokenizer.hasMoreTokens()){
                    String firstToken = tokenizer.nextToken();
                    if(NAMES.contains(firstToken)){
                        remove = true;
                        break;
                    }
                }
            }
            if(remove){
                for(FuzzyTemporalInterval t :edges.keySet()){
                    edges.get(t).remove(edge);
                }
            }
        }

        
        for(FuzzyTemporalInterval t : times){
            System.out.println(edges.get(t).size());
        }

        //calcul des patterns
        Map<FuzzyTemporalInterval, Double> birthPattern = new HashMap<FuzzyTemporalInterval, Double>();
        Map<FuzzyTemporalInterval, Double> deathPattern = new HashMap<FuzzyTemporalInterval, Double>();
        // initialisation
        for(FuzzyTemporalInterval t : times){
            birthPattern.put(t, 0.);
            deathPattern.put(t, 0.);
        }
        for(FuzzyTemporalInterval t : times){
            int index = times.indexOf(t);
            for(STEntity edge: edges.get(t)){
                if(index != 0){
                    if(!edge.existsAt(times.get(index-1))){
                        birthPattern.put(t, birthPattern.get(t)+1);
                        continue;
                    }
                }
                if(index != times.size()-1){
                    if(!edge.existsAt(times.get(index+1))){
                        deathPattern.put(times.get(index+1), deathPattern.get(times.get(index+1))+1);
                        continue;
                    }
                }
            }
        }




        //        for(FuzzyTemporalInterval t : stg.getTemporalDomain().asList()){
        //            birthPattern.put(t, 0.);
        //            deathPattern.put(t, 0.);
        //        }
        //        for(STEntity edge : stg.getEdges()){
        //            TimeSerie ts = edge.getTimeSerie();
        //            LifeSTPattern lifePattern = new LifeSTPattern();
        //            DeathSTPattern deatPattern = new DeathSTPattern();
        //            if(lifePattern.find(ts)){
        //                FuzzyTemporalInterval tpattern = lifePattern.findEvent(ts);
        //                birthPattern.put(tpattern,birthPattern.get(tpattern)+1.);
        //                continue;
        //            }
        //            if(deatPattern.find(ts)){
        //                FuzzyTemporalInterval tpattern = deatPattern.findEvent(ts);
        //                deathPattern.put(tpattern,deathPattern.get(tpattern)+1.);
        //            }
        //        }        

        // on rempli la matrice de densifications
        double[][] densifications = new double[times.size()][times.size()];
        for(int i=0; i< times.size()-1; i++){
            for(int j=i+1; j< times.size(); j++){
                double birth = 0., death = 0.;
                for(int k=i+1; k<=j; k++){
                    birth += birthPattern.get(times.get(k));
                    death += deathPattern.get(times.get(k));
                }

                double ni = edges.get(times.get(i)).size();
                
                densifications[i][j] = (ni + birth - death) / ni; 
                
                System.out.println(i+" " +j+" "+ densifications[i][j]);
                
                densifications[j][i] = 1./densifications[i][j] ; 
            }
            densifications[i][i] = 1.;
        }
        densifications[times.size()-1][times.size()-1] = 1.;

        //matrice de comparaisons entre le nombre d'arcs attendue et le nombre
        // d'arcs observés

        double[][] comparison = new double[times.size()][times.size()];
        for(int i=0; i< times.size()-1; i++){
            //nombre d'arcs à ti
            //double ni = edges.get(times.get(i)).size();
            double ni = stg.getEdgesAt(times.get(i)).size();

            for(int j=i+1; j< times.size(); j++){
                //nombre d'arcs observés à tj
                //double nj = edges.get(times.get(j)).size();    
                double nj = stg.getEdgesAt(times.get(j)).size();                
                //nombre d'arc attendus à tj
                // double xj = ni * densifications[i][j];
                //nombre d'arc attendus à ti
                //double xi = nj * densifications[j][i];
                // le rappport
                // comparison[i][j] = (ni / xi + nj / xj)/2.; 
                System.out.println(nj + " " + ni * densifications[i][j]);
                comparison[i][j] = nj / (ni * densifications[i][j]); 
                comparison[j][i] = ni / (nj * densifications[j][i]); 
            }
            comparison[i][i] = 1.;
        }
        comparison[times.size()-1][times.size()-1] = 1.;
        for(int i=0; i< times.size(); i++){
            for(int j=0; j< times.size(); j++){
                System.out.print(comparison[i][j] + ";");
            }
            System.out.println();
        }
        PairWiseComparisonMatrix matrix = new PairWiseComparisonMatrix(comparison);
        PriorityVector v = matrix.getProrityVector();
        System.out.println(v.toString());
        System.out.println(matrix.getInconsistency());
        return null;
    }
}
