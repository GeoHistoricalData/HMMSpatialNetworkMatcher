package fr.ign.cogit.v2.indicators.local;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

/**
 * Calcul de l'empan TODO : attention : il s'agit d'une approximation de
 * l'empan, car si plusieurs ppc existent entre deux sommets, un seul
 * contribuera à l'empan. Il faudrait identifier les cas ou plusieurs ppc
 * existent, peut-être en repartant de l'algo de Brandes pour la centralité
 * intermédiaire
 * @author bcostes
 * 
 * @param <GraphEntity>
 * @param <GraphEntity>
 */
public class Empan extends ILocalIndicator {

  public Empan() {
    this.name = "Emp";
  }

  @Override
  public Map<GraphEntity, Double> calculateNodeCentrality(JungSnapshot graph, boolean normalize) {
    Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
   // Map<GraphEntity, Double> normalizers = new HashMap<GraphEntity, Double>();
    for (GraphEntity v : graph.getVertices()) {
      result.put(v, 0.);
    }
//    for (GraphEntity v : graph.getVertices()) {
//      normalizers.put(v, 0.);
//    }
    // ppc
    if (graph.getDistances() == null) {
      graph.cacheShortestPaths();
    }

    Stack<GraphEntity> stack = new Stack<GraphEntity>();
    stack.addAll(graph.getVertices());
    while (!stack.isEmpty()) {
      // pour chaque sommet v1
      GraphEntity v1 = stack.pop();
      for (GraphEntity v2 : stack) {
//        double pij = -1;
        double dist = graph.getDistance(v1, v2);
//        if (graph.getNodesWeights() != null) {
//          // calcul des poids de la relation 1 2
//          double pi = graph.getNodesWeights().transform(v1);
//          double pj = graph.getNodesWeights().transform(v2);
//          pij = (pi * pj / (1. - pi)) + (pi * pj / (1. - pj));
//        }
        // pour chaque sommet v2 non déja traité
        // on récupère tous les ppc entre v1 et v2
        List<List<GraphEntity>> sps = graph.getShortestPaths(v1, v2);
        // nombre de ppc entre v1 et v2
        int nbppc = sps.size();
        for (List<GraphEntity> sp : sps) {
          // pour chaque ppc entre v1 et v2
          Set<GraphEntity> vertices = new HashSet<GraphEntity>();
          for (GraphEntity e : sp) {
            // on prend en compte les extrémité par définition de
            // l'empan
            vertices.add(graph.getEndpoints(e).getFirst());
            vertices.add(graph.getEndpoints(e).getSecond());
          }
          for (GraphEntity w : vertices) {
            // pour chaque sommet sur le ppc entre v1 et v2,
            // on ajoute la contribution du ppc
//            if (graph.getNodesWeights() != null) {
//              normalizers.put(w, normalizers.get(w) + pij / (double) nbppc);
//              result.put(w, result.get(w) + pij * dist / (double) nbppc);
//            } else {
             // normalizers.put(w, normalizers.get(w) + 1. / (double) nbppc);
              result.put(w, result.get(w) + dist / (double) nbppc);
            //}
          }
        }
      }
    }
//    for (GraphEntity v : graph.getVertices()) {
//      if (normalizers.get(v) == 0) {
//        // cas des arrêtes paralelles
//        result.put(v, 0.);
//      } else {
//        result.put(v, result.get(v) / normalizers.get(v));
//      }
//    }
    if(normalize){
        for (GraphEntity w : graph.getVertices()) {
            result.put(w, 2.* result.get(w)  / (((double) graph.getVertexCount() -1.)* ((double) graph.getVertexCount() -2.)));
        }
    }
    return result;
  }

  @Override
  public Map<GraphEntity, Double> calculateEdgeCentrality(JungSnapshot graph, boolean normalize) {
    Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();

   // Map<GraphEntity, Double> normalizers = new HashMap<GraphEntity, Double>();

    for (GraphEntity e : graph.getEdges()) {
      result.put(e, 0.);
    }
//    for (GraphEntity e : graph.getEdges()) {
//      normalizers.put(e, 0.);
//    }
    // ppc
    if (graph.getDistances() == null) {
      graph.cacheShortestPaths();
    }
    Stack<GraphEntity> stack = new Stack<GraphEntity>();
    stack.addAll(graph.getVertices());
    while (!stack.isEmpty()) {
      GraphEntity v1 = stack.pop();
      for (GraphEntity v2 : stack) {
       // double pij = -1;
        double dist = graph.getDistance(v1, v2);
//        if (graph.getNodesWeights() != null) {
//          // calcul des poids de la relation 1 2
//          double pi = graph.getNodesWeights().transform(v1);
//          double pj = graph.getNodesWeights().transform(v2);
//          pij = (pi * pj / (1. - pi)) + (pi * pj / (1. - pj));
//        }
        List<List<GraphEntity>> sps = graph.getShortestPaths(v1, v2);
        int nbppc = sps.size();
        for (List<GraphEntity> sp : sps) {
          for (GraphEntity edge : sp) {
            // pour chaque arc sur le ppc entre v1 et v2
//            if (graph.getNodesWeights() != null) {
//              normalizers.put(edge, normalizers.get(edge) + pij
//                  / (double) nbppc);
//              result.put(edge, result.get(edge) + pij * dist / (double) nbppc);
//            } else {
             // normalizers
              //    .put(edge, normalizers.get(edge) + 1. / (double) nbppc);
              result.put(edge, result.get(edge) + dist / (double) nbppc);
            //}
          }
        }
      }
    }
//    for (GraphEntity e : graph.getEdges()) {
//      if (normalizers.get(e) == 0) {
//        // cas des arrêtes parallèles
//        result.put(e, 0.);
//      } else {
//        result.put(e, result.get(e) / normalizers.get(e));
//      }
//    }
    if(normalize){
        for (GraphEntity w : graph.getEdges()) {
            result.put(w, result.get(w)  / (((double) graph.getEdgeCount() -1.)* ((double) graph.getEdgeCount() -2.)));
        }
    }
    return result;
  }

  @Override
  public Map<GraphEntity, Double> calculateNeighborhoodEdgeCentrality(
      JungSnapshot graph, int k, boolean normalize) {
    Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();

    //Map<GraphEntity, Double> normalizers = new HashMap<GraphEntity, Double>();

    for (GraphEntity e : graph.getEdges()) {
      result.put(e, 0.);
    }
//    for (GraphEntity e : graph.getEdges()) {
//      normalizers.put(e, 0.);
//    }
    // ppc
    if (graph.getDistances() == null) {
      graph.cacheShortestPaths();
    }
    Stack<GraphEntity> stack = new Stack<GraphEntity>();
    stack.addAll(graph.getVertices());
    while (!stack.isEmpty()) {
      GraphEntity v1 = stack.pop();
      for (GraphEntity v2 : graph.getKNeighborhood(v1, k)) {
      //  double pij = -1;
        double dist = graph.getDistance(v1, v2);
//        if (graph.getNodesWeights() != null) {
//          // calcul des poids de la relation 1 2
//          double pi = graph.getNodesWeights().transform(v1);
//          double pj = graph.getNodesWeights().transform(v2);
//          pij = (pi * pj / (1. - pi)) + (pi * pj / (1. - pj));
//        }
        List<List<GraphEntity>> sps = graph.getShortestPaths(v1, v2);
        int nbppc = sps.size();
        for (List<GraphEntity> sp : sps) {
          for (GraphEntity edge : sp) {
            // pour chaque arc sur le ppc entre v1 et v2
//            if (graph.getNodesWeights() != null) {
//              normalizers.put(edge, normalizers.get(edge) + pij
//                  / (double) nbppc);
//              result.put(edge, result.get(edge) + pij * dist / (double) nbppc);
//            } else {
//              normalizers
//                  .put(edge, normalizers.get(edge) + 1. / (double) nbppc);
              result.put(edge, result.get(edge) + dist / (double) nbppc);
           // }
          }
        }
      }
    }
//    for (GraphEntity e : graph.getEdges()) {
//      if (normalizers.get(e) == 0) {
//        // cas des arrêtes parallèles
//        result.put(e, 0.);
//      } else {
//        result.put(e, result.get(e) / normalizers.get(e));
//      }
//    }
    if(normalize){
        for (GraphEntity v : graph.getEdges()) {
            int c =graph.getKNeighborhood(v, k).size();
            result.put(v, result.get(v) / (((double)c-1.)*((double)c-2.)));
        }
    }
    return result;
  }

  @Override
  public Map<GraphEntity, Double> calculateNeighborhoodNodeCentrality(
      JungSnapshot graph, int k, boolean normalize) {
    Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
   // Map<GraphEntity, Double> normalizers = new HashMap<GraphEntity, Double>();
    for (GraphEntity v : graph.getVertices()) {
      result.put(v, 0.);
    }
//    for (GraphEntity v : graph.getVertices()) {
//      normalizers.put(v, 0.);
//    }
    // ppc
    if (graph.getDistances() == null) {
      graph.cacheShortestPaths();
    }

    Stack<GraphEntity> stack = new Stack<GraphEntity>();
    stack.addAll(graph.getVertices());
    while (!stack.isEmpty()) {
      // pour chaque sommet v1
      GraphEntity v1 = stack.pop();
      for (GraphEntity v2 : graph.getKNeighborhood(v1, k)) {
       // double pij = -1;
        double dist = graph.getDistance(v1, v2);
//        if (graph.getNodesWeights() != null) {
//          // calcul des poids de la relation 1 2
//          double pi = graph.getNodesWeights().transform(v1);
//          double pj = graph.getNodesWeights().transform(v2);
//          pij = (pi * pj / (1. - pi)) + (pi * pj / (1. - pj));
//        }
        // pour chaque sommet v2 non déja traité
        // on récupère tous les ppc entre v1 et v2
        List<List<GraphEntity>> sps = graph.getShortestPaths(v1, v2);
        // nombre de ppc entre v1 et v2
        int nbppc = sps.size();
        for (List<GraphEntity> sp : sps) {
          // pour chaque ppc entre v1 et v2
          Set<GraphEntity> vertices = new HashSet<GraphEntity>();
          for (GraphEntity e : sp) {
            // on prend en compte les extrémité par définition de
            // l'empan
            vertices.add(graph.getEndpoints(e).getFirst());
            vertices.add(graph.getEndpoints(e).getSecond());
          }
          for (GraphEntity w : vertices) {
            // pour chaque sommet sur le ppc entre v1 et v2,
            // on ajoute la contribution du ppc
//            if (graph.getNodesWeights() != null) {
//              normalizers.put(w, normalizers.get(w) + pij / (double) nbppc);
//              result.put(w, result.get(w) + pij * dist / (double) nbppc);
//            } else {
//              normalizers.put(w, normalizers.get(w) + 1. / (double) nbppc);
              result.put(w, result.get(w) + dist / (double) nbppc);
           // }
          }
        }
      }
    }
//    for (GraphEntity v : graph.getVertices()) {
//      if (normalizers.get(v) == 0) {
//        // cas des arrêtes paralelles
//        result.put(v, 0.);
//      } else {
//        result.put(v, result.get(v) / normalizers.get(v));
//      }
//    }
    if(normalize){
        for (GraphEntity v : graph.getVertices()) {
            int c =graph.getKNeighborhood(v, k).size();
            result.put(v, 2. * result.get(v) / (((double)c-1.)*((double)c-2.)));
        }
    }
    return result;
  }
}
