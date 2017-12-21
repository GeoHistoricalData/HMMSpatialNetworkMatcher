package fr.ign.cogit.v2.indicators.local;

import java.util.HashMap;
import java.util.Map;

import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

public class WeightedDegreeCentrality extends ILocalIndicator {

    public WeightedDegreeCentrality() {
        this.name = "WDeg";
    }

    @Override
    public Map<GraphEntity, Double> calculateNodeCentrality(JungSnapshot graph, boolean normalize) {
        Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
        double cpt = 0.;
        for (GraphEntity v : graph.getVertices()) {
            double sum = 0;
            for (GraphEntity e : graph.getIncidentEdges(v)) {
                sum += graph.getEdgesWeights().transform(e);
                cpt+=  graph.getEdgesWeights().transform(e);
            }
            result.put(v, sum);
        }
        if(normalize){
            for (GraphEntity v : graph.getVertices()) {
                result.put(v, result.get(v)  / cpt);
            }
        }
        return result;
    }

    @Override
    public Map<GraphEntity, Double> calculateEdgeCentrality(JungSnapshot graph, boolean normalize) {
        Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
        Map<GraphEntity, Double> nodesD = this.calculateNodeCentrality(graph, normalize);
        for (GraphEntity e : graph.getEdges()) {
            result.put(e, (nodesD.get(graph.getEndpoints(e).getFirst()) + nodesD
                    .get(graph.getEndpoints(e).getSecond())) / 2.);
        }
        return result;
    }

    @Override
    public Map<GraphEntity, Double> calculateNeighborhoodEdgeCentrality(
            JungSnapshot graph, int k, boolean normalize) {
        Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
        Map<GraphEntity, Double> nodesD = this.calculateNeighborhoodNodeCentrality(
                graph, k, normalize);
        for (GraphEntity e : graph.getEdges()) {
            result.put(e, (nodesD.get(graph.getEndpoints(e).getFirst()) + nodesD
                    .get(graph.getEndpoints(e).getSecond())) / 2.);
        }
        return result;
    }

    @Override
    public Map<GraphEntity, Double> calculateNeighborhoodNodeCentrality(
            JungSnapshot graph, int k, boolean normalize) {
        Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
        double cpt = 0;
        for (GraphEntity v : graph.getVertices()) {
            double sum = 0;
            for (GraphEntity v2 : graph.getKNeighborhood(v, k)) {
                for (GraphEntity e : graph.getIncidentEdges(v2)) {
                    sum += graph.getEdgesWeights().transform(e);
                    cpt+=graph.getEdgesWeights().transform(e);
                }
            }
            result.put(v, sum / (double) graph.getKNeighborhood(v, k).size());
        }
        if(normalize){
            for (GraphEntity v : graph.getVertices()) {
                result.put(v, result.get(v)
                        / cpt);
            }
        }  
        return result;
    }
}
