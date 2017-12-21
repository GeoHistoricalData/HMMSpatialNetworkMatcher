package fr.ign.cogit.v2.patterns;

import java.util.ArrayList;
import java.util.List;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.util.Pair;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v1.tag.TimeSerie;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.STProperty;

public class PatternManager {

    /**
     * Cherche deux patterns pattern1 et pattern2 dont les noeuds extrémités sont deux a deux distants de moins de deuclidean (distance euclidienne)
     * @param stg
     * @param pattern1
     * @param pattern2
     * @param deuclidean
     * @param times
     * @param entity_type
     * @return
     */
    public static List<Pair<STEntity>> lookForClosePattern(STGraph stg, STPattern pattern1, STPattern pattern2, double deuclidean, List<FuzzyTemporalInterval> times, int entity_type){
        List<Pair<STEntity>> result = new ArrayList<Pair<STEntity>>();
        if(times == null){
            times = new ArrayList<FuzzyTemporalInterval>();
        }        
        if(entity_type == STEntity.EDGE){
            List<STEntity> matchPattern1 = new ArrayList<STEntity>();
            for(STEntity edge : stg.getEdges()){
                STProperty<Boolean> ts = edge.getTimeSerie().copy();
                for(FuzzyTemporalInterval t : times){
                    ts.getValues().remove(t);
                }
                if(pattern1.find(ts)){
                    matchPattern1.add(edge);
                }              

            }

            if(matchPattern1.isEmpty()){
                return result;
            }

            List<STEntity> matchPattern2 = new ArrayList<STEntity>();
            for(STEntity edge : stg.getEdges()){
                STProperty<Boolean> ts = edge.getTimeSerie().copy();
                for(FuzzyTemporalInterval t : times){
                    ts.getValues().remove(t);
                }
                if(pattern2.find(ts)){
                    matchPattern2.add(edge);
                }
            }
            if(matchPattern2.isEmpty()){
                return result;
            }

            for(STEntity e1 : matchPattern1){
                STEntity node11 = stg.getEndpoints(e1).getFirst();
                STEntity node12 = stg.getEndpoints(e1).getSecond();
                for(STEntity e2 : matchPattern2){
                    if(e1.equals(e2)){
                        continue;
                    }

                    STEntity node21 = stg.getEndpoints(e2).getFirst();
                    STEntity node22 = stg.getEndpoints(e2).getSecond();
                    if(node11.getGeometry().toGeoxGeometry().coord().get(0).distance(node21.getGeometry().toGeoxGeometry().coord().get(0))<deuclidean){
                        if(node12.getGeometry().toGeoxGeometry().coord().get(0).distance(node22.getGeometry().toGeoxGeometry().coord().get(0))<deuclidean){
                            //on vérifie que les séries temporelles sont disjointes
                            boolean timesok = true;
                            for(FuzzyTemporalInterval t1 : e1.getTimeSerie().getValues().keySet()){
                                if(times.contains(t1)){
                                    continue;
                                }
                                if(e1.existsAt(t1) && e2.existsAt(t1)){
                                    timesok = false;
                                    break;
                                }
                            }
                            if(timesok){
                                result.add(new Pair<STEntity>(e1, e2));
                            }
                            continue;
                        }
                    }
                    else  if(node11.getGeometry().toGeoxGeometry().coord().get(0).distance(node22.getGeometry().toGeoxGeometry().coord().get(0))<deuclidean){
                        if(node12.getGeometry().toGeoxGeometry().coord().get(0).distance(node21.getGeometry().toGeoxGeometry().coord().get(0))<deuclidean){
                            //on vérifie que les séries temporelles sont disjointes
                            boolean timesok = true;
                            for(FuzzyTemporalInterval t1 : e1.getTimeSerie().getValues().keySet()){
                                if(times.contains(t1)){
                                    continue;
                                }
                                if(e1.existsAt(t1) && e2.existsAt(t1)){
                                    timesok = false;
                                    break;
                                }
                            }
                            if(timesok){
                                result.add(new Pair<STEntity>(e1, e2));
                            }
                            continue;
                        }
                    }
                }
            }
            return result;
        }


        return result;
    }

    /**
     * Cherche deux patterns pattern1 et pattern2 dont les noeuds extrémités sont deux a deux distants de moins de dtopo (distance topologique)
     * @param stg
     * @param pattern1
     * @param pattern2
     * @param deuclidean
     * @param times
     * @param entity_type
     * @return
     */
    public static List<Pair<STEntity>> lookForClosePattern(STGraph stg, STPattern pattern1, STPattern pattern2, int dtopo,  List<FuzzyTemporalInterval> times, int entity_type){
        List<Pair<STEntity>> result = new ArrayList<Pair<STEntity>>();
        if(times == null){
            times = new ArrayList<FuzzyTemporalInterval>();
        }
        if(entity_type == STEntity.EDGE){
            List<STEntity> matchPattern1 = new ArrayList<STEntity>();
            for(STEntity edge : stg.getEdges()){
                STProperty<Boolean> ts = edge.getTimeSerie().copy();
                for(FuzzyTemporalInterval t : times){
                    ts.getValues().remove(t);
                }
                if(pattern1.find(ts)){
                    matchPattern1.add(edge);
                }
            }
            if(matchPattern1.isEmpty()){
                return result;
            }
            List<STEntity> matchPattern2 = new ArrayList<STEntity>();
            for(STEntity edge : stg.getEdges()){
                STProperty<Boolean> ts = edge.getTimeSerie().copy();
                for(FuzzyTemporalInterval t : times){
                    ts.getValues().remove(t);
                }
                if(pattern2.find(ts)){
                    matchPattern2.add(edge);
                }
            }
            if(matchPattern2.isEmpty()){
                return result;
            }
            DijkstraShortestPath<STEntity, STEntity> distances = new DijkstraShortestPath<STEntity, STEntity>(stg);
            for(STEntity e1 : matchPattern1){
                STEntity node11 = stg.getEndpoints(e1).getFirst();
                STEntity node12 = stg.getEndpoints(e1).getSecond();
                for(STEntity e2 : matchPattern2){
                    if(e1.equals(e2)){
                        continue;
                    }
                    STEntity node21 = stg.getEndpoints(e2).getFirst();
                    STEntity node22 = stg.getEndpoints(e2).getSecond();
                    if(distances.getDistance(node11, node21).intValue()<dtopo){
                        if(distances.getDistance(node12, node22).intValue()<dtopo){
                            //on vérifie que les séries temporelles sont disjointes
                            boolean timesok = true;
                            for(FuzzyTemporalInterval t1 : e1.getTimeSerie().getValues().keySet()){
                                if(times.contains(t1)){
                                    continue;
                                }
                                if(e1.existsAt(t1) && e2.existsAt(t1)){
                                    timesok = false;
                                    break;
                                }
                            }
                            if(timesok){
                                result.add(new Pair<STEntity>(e1, e2));
                            }
                            continue;
                        }
                    }
                    else  if(distances.getDistance(node11, node22).intValue()<dtopo){
                        if(distances.getDistance(node12, node21).intValue()<dtopo){
                            //on vérifie que les séries temporelles sont disjointes
                            boolean timesok = true;
                            for(FuzzyTemporalInterval t1 : e1.getTimeSerie().getValues().keySet()){
                                if(times.contains(t1)){
                                    continue;
                                }
                                if(e1.existsAt(t1) && e2.existsAt(t1)){
                                    timesok = false;
                                    break;
                                }
                            }
                            if(timesok){
                                result.add(new Pair<STEntity>(e1, e2));
                            }
                            continue;
                        }
                    }
                }
            }
            return result;
        }

        return result;
    }

    /**
     * Cherche deux patterns dont les deux extrémités sont distantes de moins de dtopo et deuclidean simultanément
     * @param stg
     * @param pattern1
     * @param pattern2
     * @param dtopo
     * @param deuclidean
     * @param times
     * @param entity_type
     * @return
     */
    public static List<Pair<STEntity>> lookForClosePattern(STGraph stg, STPattern pattern1, STPattern pattern2, int dtopo, double deuclidean, List<FuzzyTemporalInterval> times, int entity_type){
        if(dtopo <= 1){
            return PatternManager.lookForClosePattern(stg, pattern1, pattern2, dtopo, times, entity_type);
        }
        if(deuclidean == 0){
            return PatternManager.lookForClosePattern(stg, pattern1, pattern2, deuclidean, times, entity_type);
        }
        List<Pair<STEntity>> result = PatternManager.lookForClosePattern(stg, pattern1, pattern2, dtopo, times, entity_type);
        if(times == null){
            times = new ArrayList<FuzzyTemporalInterval>();
        }
        List<Pair<STEntity>> result2 = new ArrayList<Pair<STEntity>>(); 
        if(entity_type == STEntity.EDGE){
            for(Pair<STEntity> pair : result){
                STEntity e1 = pair.getFirst();
                STEntity e2 = pair.getSecond();
                STEntity node11 = stg.getEndpoints(e1).getFirst();
                STEntity node12 = stg.getEndpoints(e1).getSecond();
                STEntity node21 = stg.getEndpoints(e2).getFirst();
                STEntity node22 = stg.getEndpoints(e2).getSecond();
                if(node11.getGeometry().toGeoxGeometry().coord().get(0).distance(node21.getGeometry().toGeoxGeometry().coord().get(0))<deuclidean){
                    if(node12.getGeometry().toGeoxGeometry().coord().get(0).distance(node22.getGeometry().toGeoxGeometry().coord().get(0))<deuclidean){
                        //on vérifie que les séries temporelles sont disjointes
                        boolean timesok = true;
                        for(FuzzyTemporalInterval t1 : e1.getTimeSerie().getValues().keySet()){
                            if(times.contains(t1)){
                                continue;
                            }
                            if(e1.existsAt(t1) && e2.existsAt(t1)){
                                timesok = false;
                                break;
                            }
                        }
                        if(timesok){
                            result2.add(new Pair<STEntity>(e1, e2));
                        }
                        continue;
                    }
                }
                else  if(node11.getGeometry().toGeoxGeometry().coord().get(0).distance(node22.getGeometry().toGeoxGeometry().coord().get(0))<deuclidean){
                    if(node12.getGeometry().toGeoxGeometry().coord().get(0).distance(node21.getGeometry().toGeoxGeometry().coord().get(0))<deuclidean){
                        //on vérifie que les séries temporelles sont disjointes
                        boolean timesok = true;
                        for(FuzzyTemporalInterval t1 : e1.getTimeSerie().getValues().keySet()){
                            if(times.contains(t1)){
                                continue;
                            }
                            if(e1.existsAt(t1) && e2.existsAt(t1)){
                                timesok = false;
                                break;
                            }
                        }
                        if(timesok){
                            result2.add(new Pair<STEntity>(e1, e2));
                        }
                        continue;
                    }
                }
            }
            return result2;
        }
        return result2;
    }
    
    /**
     * Cherche deux patterns dont deux extrémités sont proches de moins de dtopo et les deux autres de deuclidean
     * @param stg
     * @param pattern1
     * @param pattern2
     * @param dtopo
     * @param deuclidean
     * @param times
     * @param entity_type
     * @return
     */
    public static List<Pair<STEntity>> lookForSemiClosePattern(STGraph stg, STPattern pattern1, STPattern pattern2, int dtopo, double deuclidean, List<FuzzyTemporalInterval> times, int entity_type){
        List<Pair<STEntity>> result = new ArrayList<Pair<STEntity>>();
        if(times == null){
            times = new ArrayList<FuzzyTemporalInterval>();
        }
        if(entity_type == STEntity.EDGE){
            List<STEntity> matchPattern1 = new ArrayList<STEntity>();
            for(STEntity edge : stg.getEdges()){
                STProperty<Boolean> ts = edge.getTimeSerie().copy();
                for(FuzzyTemporalInterval t : times){
                    ts.getValues().remove(t);
                }
                if(pattern1.find(ts)){
                    matchPattern1.add(edge);
                }
            }
            if(matchPattern1.isEmpty()){
                return result;
            }
            List<STEntity> matchPattern2 = new ArrayList<STEntity>();
            for(STEntity edge : stg.getEdges()){
                STProperty<Boolean> ts = edge.getTimeSerie().copy();
                for(FuzzyTemporalInterval t : times){
                    ts.getValues().remove(t);
                }
                if(pattern2.find(ts)){
                    matchPattern2.add(edge);
                }
            }
            if(matchPattern2.isEmpty()){
                return result;
            }
            DijkstraShortestPath<STEntity, STEntity> distances = new DijkstraShortestPath<STEntity, STEntity>(stg);
            for(STEntity e1 : matchPattern1){
                STEntity node11 = stg.getEndpoints(e1).getFirst();
                STEntity node12 = stg.getEndpoints(e1).getSecond();
                for(STEntity e2 : matchPattern2){
                    if(e1.equals(e2)){
                        continue;
                    }
                    STEntity node21 = stg.getEndpoints(e2).getFirst();
                    STEntity node22 = stg.getEndpoints(e2).getSecond();
                    if(distances.getDistance(node11, node21).intValue()<dtopo){
                        if(node12.getGeometry().toGeoxGeometry().coord().get(0).distance(node22.getGeometry().toGeoxGeometry().coord().get(0))<deuclidean){
                            //on vérifie que les séries temporelles sont disjointes
                            boolean timesok = true;
                            for(FuzzyTemporalInterval t1 : e1.getTimeSerie().getValues().keySet()){
                                if(times.contains(t1)){
                                    continue;
                                }
                                if(e1.existsAt(t1) && e2.existsAt(t1)){
                                    timesok = false;
                                    break;
                                }
                            }
                            if(timesok){
                                result.add(new Pair<STEntity>(e1, e2));
                            }
                            continue;
                        }
                    }
                    else  if(distances.getDistance(node11, node22).intValue()<dtopo){
                        if(node12.getGeometry().toGeoxGeometry().coord().get(0).distance(node21.getGeometry().toGeoxGeometry().coord().get(0))<deuclidean){
                            //on vérifie que les séries temporelles sont disjointes
                            boolean timesok = true;
                            for(FuzzyTemporalInterval t1 : e1.getTimeSerie().getValues().keySet()){
                                if(times.contains(t1)){
                                    continue;
                                }
                                if(e1.existsAt(t1) && e2.existsAt(t1)){
                                    timesok = false;
                                    break;
                                }
                            }
                            if(timesok){
                                result.add(new Pair<STEntity>(e1, e2));
                            }
                            continue;
                        }
                    }
                }
            }
            return result;
        }

        return result;
    }

    public static void closePattern2Shp(List<Pair<STEntity>> closePatterns, String shp){
        IPopulation<IFeature> out = new Population<IFeature>();
        for(Pair<STEntity> entities : closePatterns){
            IGeometry geom = (IGeometry)entities.getFirst().getGeometry().toGeoxGeometry().clone();
            geom = geom.union((IGeometry)entities.getSecond().getGeometry().toGeoxGeometry().clone());
            out.add(new DefaultFeature(geom));
        }
        ShapefileWriter.write(out, shp);
    }

}
