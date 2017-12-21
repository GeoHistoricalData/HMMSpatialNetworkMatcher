package fr.ign.cogit.v2.indicators.local;

import java.util.HashMap;
import java.util.Map;

import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

public class MeanDistance extends ILocalIndicator {

  public MeanDistance() {
    this.name = "MeanD";
  }

  @Override
  public Map<GraphEntity, Double> calculateNodeCentrality(JungSnapshot g, boolean normalize) {
    // résultat
    Map<GraphEntity, Double> values = new HashMap<GraphEntity, Double>();
    // nombre de sommets
    // ppc
    if (g.getDistances() == null) {
      g.cacheShortestPaths();
    }

    for (GraphEntity node : g.getVertices()) {
      double sum = 0;
    //  double normalize = 0;
      int index1 = g.getNodeIndex(node);
      for (GraphEntity node2 : g.getVertices()) {
        if (node.equals(node2)) {
          continue;
        }
        int index2 = g.getNodeIndex(node2);
        // Prise en compte de l'éventuelle pondération des sommets
//        if (g.getNodesWeights() != null) {
//          // calcul des poids de la relation i j
//          double pi = g.getNodesWeights().transform(node);
//          double pj = g.getNodesWeights().transform(node2);
//          double pij = (pi * pj / (1. - pi)) + (pi * pj / (1. - pj));
//          normalize += pij;
//          sum += pij * g.getDistance(index1, index2);
//        } else {
         // normalize++;
          sum += g.getDistance(index1, index2);
       // }
      }
      if(normalize){
          values.put(node, sum / ((double)g.getVertexCount()-1.));
      }
      else{
          values.put(node, sum);
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
      JungSnapshot g, int k, boolean normalize) {
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
    // nombre de sommets
    // ppc
    if (g.getDistances() == null) {
      g.cacheShortestPaths();
    }

    for (GraphEntity node : g.getVertices()) {
      double sum = 0;
      //double normalize = 0;
      int index1 = g.getNodeIndex(node);
      for (GraphEntity node2 : g.getKNeighborhood(node, k)) {
        if (node.equals(node2)) {
          continue;
        }
        int index2 = g.getNodeIndex(node2);
        // Prise en compte de l'éventuelle pondération des sommets
//        if (g.getNodesWeights() != null) {
//          // calcul des poids de la relation i j
//          double pi = g.getNodesWeights().transform(node);
//          double pj = g.getNodesWeights().transform(node2);
//          double pij = (pi * pj / (1. - pi)) + (pi * pj / (1. - pj));
//          normalize += pij;
//          sum += pij * g.getDistance(index1, index2);
//        } else {
         // normalize++;
          sum += g.getDistance(index1, index2);
       // }
      }
      if(normalize){
          System.out.println(sum +" "+ g.getKNeighborhood(node, k).size());
          values.put(node, sum / ((double)g.getKNeighborhood(node, k).size()));
      }
      else{
          values.put(node, sum);
      }    }
    return values;
  }

}
