package fr.ign.cogit.v2.manual.corrections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.v1.tag.STAttribute;
import fr.ign.cogit.v1.tag.TimeSerie;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.utils.JungUtils;

public class Desintegration {

    public static void desintagrate(STGraph stg, STEntity edge, FuzzyTemporalInterval t){
        //on vérifie que edge existe à t
        if(!edge.existsAt(t)){
            return;
        }
        //récupération des extrémités
        STEntity node1 = stg.getEndpoints(edge).getFirst();
        STEntity node2 = stg.getEndpoints(edge).getSecond();
        
        
        //on va récupérer tous les tronçons connectés par des sommets fictifs et ces sommets
        Set<STEntity> edges  = new HashSet<STEntity>();
        edges.add(edge);
        Set<STEntity> nodes  = new HashSet<STEntity>();
        if(node1.isFictive()){
            nodes.add(node1);
        }
        if(node2.isFictive()){
            nodes.add(node2);
        }
        //processus de sélection de sommets fictifs
        Set<STEntity> oldsNodes = new HashSet<STEntity>();
        oldsNodes.addAll(nodes);
        while(!oldsNodes.isEmpty()){
            Set<STEntity> newNodes = new HashSet<STEntity>();
            for(STEntity node:oldsNodes){
                //pour chaque sommet fictif
                for(STEntity e : stg.getIncidentEdges(node)){
                    //pour chaque arc incident au sommet fictif
                    if(!edges.contains(e)){
                        edges.add(e);
                        if(node.equals(stg.getEndpoints(e).getFirst())){
                            if(stg.getEndpoints(e).getSecond().isFictive() && !nodes.contains(stg.getEndpoints(e).getSecond())){
                                newNodes.add(stg.getEndpoints(e).getSecond());
                            }
                        }
                        else{
                            if(stg.getEndpoints(e).getFirst().isFictive() && !nodes.contains(stg.getEndpoints(e).getFirst())){
                                newNodes.add(stg.getEndpoints(e).getFirst());
                            }
                        }
                    }
                }
            }
            oldsNodes.clear();
            oldsNodes.addAll(newNodes);
            nodes.addAll(newNodes);
            newNodes.clear();          
        }
        

        List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>(edge.getTimeSerie().getValues().keySet());
        times.remove(null);
        Collections.sort(times);
        int indexT = times.indexOf(t);
        
        //on va désagréger chaque arc de edges et chaque sommet (fictif) de nodes
        for(STEntity e: edges){
            Map<FuzzyTemporalInterval, Boolean> _timeSerie = new HashMap<FuzzyTemporalInterval, Boolean>();
            for(int i=0; i< times.size(); i++){
                if(i<indexT){
                    _timeSerie.put(times.get(i), false);
                }
                else{
                    _timeSerie.put(times.get(i), true);
                }
            }
            TimeSerie ts = new TimeSerie(_timeSerie);
            STEntity.setCurrentType(STEntity.EDGE);
            STEntity newEdge = new STEntity(ts);
            for(FuzzyTemporalInterval tt: times){
                if(newEdge.existsAt(tt)){
                    //géométries
                    newEdge.setGeometryAt(tt, e.getGeometryAt(tt));
                    e.setGeometryAt(tt, null);
                    //poids
                    newEdge.putWeightAt(tt, e.getWeightAt(tt));
                    e.putWeightAt(tt, -1.);
                    // indicateurs
                    newEdge.getTLocalIndicators().put(tt, e.getTLocalIndicators().get(tt));
                    e.getTLocalIndicators().put(tt, new HashMap<String, Double>());
                    e.existsAt(tt, false);
                }
            }
            //attributs
            for(String attName : e.getTAttributes().keySet()){
                STAttribute att = new STAttribute(new ArrayList<FuzzyTemporalInterval>(times));
                for(FuzzyTemporalInterval tt: times){
                    if(newEdge.existsAt(tt)){ 
                        att.setValueAt(tt, e.getTAttributes().get(attName).getValueAt(tt));
                        e.getTAttributes().get(attName).setValueAt(tt, null);
                    }
                }
            }
            //on l'ajoute au graphe
            stg.addEdge(newEdge,stg.getEndpoints(e));
        }
        //on dasagrége les sommet fictifs
        for(STEntity node: new ArrayList<STEntity>(nodes)){
            // on crée un nouveau sommet
            Map<FuzzyTemporalInterval, Boolean> _timeSerie = new HashMap<FuzzyTemporalInterval, Boolean>();
            for(int i=0; i< times.size(); i++){
                if(i<indexT){
                    _timeSerie.put(times.get(i), false);
                }
                else{
                    _timeSerie.put(times.get(i), true);
                }
            }
            TimeSerie ts = new TimeSerie(_timeSerie);
            STEntity.setCurrentType(STEntity.NODE);
            STEntity newNode = new STEntity(ts);
            for(FuzzyTemporalInterval tt: times){
                if(newNode.existsAt(tt)){
                    //géométries
                    newNode.setGeometryAt(tt, node.getGeometryAt(tt));
                    node.setGeometryAt(tt, null);
                    //poids
                    newNode.putWeightAt(tt, node.getWeightAt(tt));
                    node.putWeightAt(tt, -1.);
                    // indicateurs
                    newNode.getTLocalIndicators().put(tt, node.getTLocalIndicators().get(tt));
                    node.getTLocalIndicators().put(tt, new HashMap<String, Double>());
                    node.existsAt(tt, false);
                }
            }
            //attributs
            for(String attName : node.getTAttributes().keySet()){
                STAttribute att = new STAttribute(new ArrayList<FuzzyTemporalInterval>(times));
                for(FuzzyTemporalInterval tt: times){
                    if(newNode.existsAt(tt)){ 
                        att.setValueAt(tt, node.getTAttributes().get(attName).getValueAt(tt));
                        node.getTAttributes().get(attName).setValueAt(tt, null);
                    }
                }
            }
            //on lmet à jour le graphe
            JungUtils<STEntity, STEntity> JU = new JungUtils<STEntity, STEntity>();
            JU.replaceNode(stg, node, newNode);           
            // on  regarde si node est complétement fictif
            boolean fullyFictive = true;
            for(FuzzyTemporalInterval tt: times){
                if(node.existsAt(tt)){
                    fullyFictive = false;
                    break;
                }
            }
            if(fullyFictive){
                Desintegration.mergeEdges(stg, node);
            }
            // on  regarde si newNode est complétement fictif
            fullyFictive = true;
            for(FuzzyTemporalInterval tt: times){
                if(newNode.existsAt(tt)){
                    fullyFictive = false;
                    break;
                }
            }
            if(fullyFictive){
                Desintegration.mergeEdges(stg, newNode);
            }
        }

    }

    private static void mergeEdges(STGraph stg, STEntity node) {
        List<FuzzyTemporalInterval> times= new ArrayList<FuzzyTemporalInterval>(node.getTimeSerie().asList().keySet());
        times.remove(null);
        for(FuzzyTemporalInterval t: new ArrayList<FuzzyTemporalInterval>(times)){
            if(node.getGeometryAt(t) == null){
             times.remove(t);   
            }
        }
        // pour chaque date pour laquel le sommet fictif a une géométrie
        Collections.sort(times);
        //création d'un nouvel arc
        Map<FuzzyTemporalInterval, Boolean> _timeSerie = new HashMap<FuzzyTemporalInterval, Boolean>();
        for(int i=0; i< times.size(); i++){
            
        }
        TimeSerie ts = new TimeSerie(_timeSerie);
        STEntity.setCurrentType(STEntity.EDGE);
        STEntity newEdge = new STEntity(ts);
        for(FuzzyTemporalInterval t : times){
            // on va récupérer les arcs incident pour cette dateµ. Il y en a forcément 2 et uniquement
            // 2 vu la définition d'un sommet fictif
            List<STEntity> incidents = stg.getIncidentEdgessAt(node, t);
            STEntity edge1 = incidents.get(0);
            STEntity edge2 = incidents.get(1);
            //
        }
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
