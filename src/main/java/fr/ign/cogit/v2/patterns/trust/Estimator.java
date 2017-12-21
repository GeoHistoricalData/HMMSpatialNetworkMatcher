package fr.ign.cogit.v2.patterns.trust;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;

public class Estimator {
    //nombre de tirage
    private static final int N = 10000;

    public static List<String> NAMES = new ArrayList<String>(
            Arrays.asList("passage","impasse","pas", "imp", "cul", "place",
                    "pl", "ruelle","allee","chemin","champ","c.", "cloitre",
                    "carreau","cour", "petit","petite","port","preau","cul-de-sac",
                    "all","cite","crs","gal","galerie","i","rle","sq"));  

    /**
     * Sélection d'un population de N individus
     * Un individu est un k-uplet de STentity
     * La population d'individu représente l'ensemble des k-uplets de STEntity possibles
     * sans répétition,
     * k variant entre 1 et max = stg.getEdgesAt(t).size().
     * La population est donc de taille sum(k=1,n)C(n,k) avec C(n,k) = n!/(k!(n-k!)) = (2^n) -1
     * @param stg
     * @param t
     * @return
     */
    private static Set<Set<STEntity>> getSegmentation(STGraph stg, FuzzyTemporalInterval t){
        Set<Set<STEntity>> segmentation = new HashSet<Set<STEntity>>();
        List<STEntity> edges = new ArrayList<STEntity>(stg.getEdgesAt(t));
        // on va retirer les passages, impasses, places...
//        List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>(stg.getTemporalDomain().asList());
//        Collections.sort(times);
//
//        
//        try {
//            FuzzyTemporalInterval tvasserot = new FuzzyTemporalInterval(new double[]{1808,1810,1836,1853},new double[]{0,1,1,0}, 4);
//            times.remove(tvasserot);
//           } catch (XValuesOutOfOrderException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (YValueOutOfRangeException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        
//        
//
//        for(STEntity edge : stg.getEdges()){
//            boolean remove = false;
//
//            AppearanceSTPattern appearancePattern = new AppearanceSTPattern();
//            if(appearancePattern.find(edge.getTimeSerie())){
//                //on supprime l'arc
//                remove = true;
//            }
//            if(remove){
//                edges.remove(edge);
//                continue;
//            }
//            ReincarnationSTPattern reincPattern = new ReincarnationSTPattern();
//            if(reincPattern.find(edge.getTimeSerie())){
//                //on supprime l'arc
//                remove = true;
//            }
//            if(remove){
//                edges.remove(edge);
//                continue;
//            }
//
//            for(FuzzyTemporalInterval tt :edge.getTimeSerie().asList().keySet()){
//                if(stg.getIncidentEdgessAt(stg.getEndpoints(edge).getFirst(),tt).size() == 1 
//                        || stg.getIncidentEdgessAt(stg.getEndpoints(edge).getSecond(),tt).size() == 1){
//                    remove = true;
//                    break;
//                }
//                String name = edge.getTAttributes().get("name").getValueAt(tt);
//                if(name == null || name.equals(" ")){
//                    continue;
//                }
//                name = name.toLowerCase();
//                name = name.trim();
//                StringTokenizer tokenizer = new StringTokenizer(name, " ");
//                if(tokenizer.hasMoreTokens()){
//                    String firstToken = tokenizer.nextToken();
//                    if(NAMES.contains(firstToken)){
//                        remove = true;
//                        break;
//                    }
//                }
//            }
//            if(remove){
//                edges.remove(edge);
//            }
//        }
//        
//        System.out.println(edges.size());

        int min =edges.size() / 10;
        int max = edges.size();
        while(segmentation.size() < Estimator.N){
            int k = min  + (int)(Math.random() * (max-min));
            Set<STEntity> selection = new HashSet<STEntity>();
            //sélection de k STentiy, tirage sans remise
            if(k == max){
                selection.addAll(edges);
                segmentation.add(selection);
                continue;
            }
            for(int i=0; i< k; i++){
                while(true){
                    int j = (int)(Math.random() * (max-1));
                    if(selection.contains(edges.get(j))){
                        continue;
                    }
                    selection.add(edges.get(j));
                    break;
                }
            }
            segmentation.add(selection);
        }
        System.out.println("Segmentation done");
        return segmentation;
    }

    /**
     * Donne une estimation de la moyenne de la densification et de sa variance
     * @param stg
     * @param t
     * @return
     */
    public static double[] getEstimator(STGraph stg, FuzzyTemporalInterval ti,FuzzyTemporalInterval tj){
        //segmentation
        Set<Set<STEntity>> segmentation = Estimator.getSegmentation(stg, tj);
        // table des densifications pour chaque population
        double[] observations = new double[segmentation.size()];
        int cpt=0;
        for(Set<STEntity> selection : segmentation){
            // taille de l'échantillon à t
            double nj = (double)selection.size();
            // nombre d'arc créé entre ti et tj
            int birth = 0;
            // nombre d'arc détruits entre ti et tj
            int death = 0;
            for(STEntity edge : selection){
                if(edge.existsAt(ti) && !edge.existsAt(tj)){
                    death ++;
                }
                else if(!edge.existsAt(ti) && edge.existsAt(tj)){
                    birth ++;
                }
            }
            // calcul de la taille de la selction de STEntity à ti
            //calcul de la densification 
            double dji = (nj + death - birth) / nj;
            observations[cpt] = 1./dji;
            cpt++;
        }
        // estimation de la moyenne
        double d = 0;
        for(int i=0; i< observations.length; i++){
            d += observations[i];
        }
        d /= (double)(observations.length);
        //estimationd e l'écart type
       double sigma = 0;
       for(int i=0; i< observations.length; i++){
           sigma += Math.pow(observations[i] - d,2);
       }
       sigma /= ((double)(observations.length-1));
       sigma = Math.sqrt(sigma);
       double result[] = {d, sigma};
       return result;
    }   

    
    public static void main(String args[]){
        STGraph stg = TAGIoManager.deserialize("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v4/with_indicators/tag_ind.tag");
        List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>(stg.getEdges().iterator().next().getTimeSerie().getValues().keySet());
        times.remove(null);
        Collections.sort(times);
        
        FuzzyTemporalInterval t1 = times.get(2);
        FuzzyTemporalInterval t2 = times.get(3);
        
        double densification[] = Estimator.getEstimator(stg, t1, t2);
        System.out.println(densification[0] +" "+ densification[1]);
        

    }

}
