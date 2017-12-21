package fr.ign.cogit.v2.indicators.local;

import java.util.HashMap;
import java.util.Map;

import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

public class Circuity extends ILocalIndicator {
    public Circuity() {
        this.name = "Circ";
    }

    @Override
    public Map<GraphEntity, Double> calculateNodeCentrality(JungSnapshot g, boolean normalize) {
        // résultat
        Map<GraphEntity, Double> values = new HashMap<GraphEntity, Double>();
        // ppc
        if (g.getDistances() == null) {
            g.cacheShortestPaths();
        }

        for (GraphEntity n1 : g.getVertices()) {
            double sum = 0;
            int index1 = g.getNodeIndex(n1);
            for (GraphEntity n2 : g.getVertices()) {
                int index2 = g.getNodeIndex(n2);
                if (n1.equals(n2)) {
                    continue;
                }
                double de = n1.getGeometry().distance(n2.getGeometry());
                double dr = g.getDistance(index1, index2);
                //        if (g.getNodesWeights() != null) {
                //          // calcul des poids de la relation i j
                //          double pi = g.getNodesWeights().transform(n1);
                //          double pj = g.getNodesWeights().transform(n2);
                //          double pij = (pi * pj / (1. - pi)) + (pi * pj / (1. - pj));
                //          sum += pij * (dr - de) * (dr - de);
                //        } else {
                sum += (dr - de) * (dr - de);
                //}

            }
            if(normalize){
                values.put(n1, sum / ((double) (g.getVertexCount() - 1)));
            }
            else{
                values.put(n1, sum);
            }
        }
        return values;
    }

    @Override
    public Map<GraphEntity, Double> calculateEdgeCentrality(JungSnapshot g, boolean normalize) {
        // on fait la moyenne pondérée des centralité de proximité des extrémités
        Map<GraphEntity, Double> values = new HashMap<GraphEntity, Double>();
        // on calcul d'abord la centralité des noeuds
        Map<GraphEntity, Double> nodesValues = this.calculateNodeCentrality(g, normalize);
        for (GraphEntity edge : g.getEdges()) {
            GraphEntity n1 = g.getEndpoints(edge).getFirst();
            GraphEntity n2 = g.getEndpoints(edge).getSecond();

            double centraltiy = (nodesValues.get(n1) + nodesValues.get(n2)) / (2.);
            values.put(edge, centraltiy);
        }
        return values;
    }

    @Override
    public Map<GraphEntity, Double> calculateNeighborhoodEdgeCentrality(
            JungSnapshot g, int k , boolean normalize) {
        // on fait la moyenne pondérée des centralité de proximité des extrémités
        Map<GraphEntity, Double> values = new HashMap<GraphEntity, Double>();
        // on calcul d'abord la centralité des noeuds
        Map<GraphEntity, Double> nodesValues = this
                .calculateNeighborhoodNodeCentrality(g, k, normalize);
        for (GraphEntity edge : g.getEdges()) {
            GraphEntity n1 = g.getEndpoints(edge).getFirst();
            GraphEntity n2 = g.getEndpoints(edge).getSecond();

            double centraltiy = (nodesValues.get(n1) + nodesValues.get(n2)) / (2.);
            values.put(edge, centraltiy);
        }
        return values;
    }

    @Override
    public Map<GraphEntity, Double> calculateNeighborhoodNodeCentrality(
            JungSnapshot g, int k, boolean normalize) {
        // résultat
        Map<GraphEntity, Double> values = new HashMap<GraphEntity, Double>();
        // ppc
        if (g.getDistances() == null) {
            g.cacheShortestPaths();
        }

        for (GraphEntity node : g.getVertices()) {
            double sum = 0;
            GraphEntity n1 = (GraphEntity) node;
            int index1 = g.getNodeIndex(node);
            for (GraphEntity node2 : g.getKNeighborhood(node, k)) {
                int index2 = g.getNodeIndex(node2);
                if (node.equals(node2)) {
                    continue;
                }
                GraphEntity n2 = (GraphEntity) node2;
                double de = n1.getGeometry().distance(n2.getGeometry());
                double dr = g.getDistance(index1, index2);
                //        if (g.getNodesWeights() != null) {
                //          // calcul des poids de la relation i j
                //          double pi = g.getNodesWeights().transform(node);
                //          double pj = g.getNodesWeights().transform(node2);
                //          double pij = (pi * pj / (1. - pi)) + (pi * pj / (1. - pj));
                //          sum += pij * (dr - de) * (dr - de);
                //        } else {
                sum += (dr - de) * (dr - de);
                // }

            }
            if(normalize){
                values.put(n1, sum / ((double) (g.getKNeighborhood(node, k).size() )));
            }
            else{
                values.put(n1, sum);
            }  
        }

        return values;
    }
}
