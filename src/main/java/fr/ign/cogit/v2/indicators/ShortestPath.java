package fr.ign.cogit.v2.indicators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import edu.uci.ics.jung.algorithms.util.MapBinaryHeap;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

/**
 * Modifications locales de la classe BetweennessCentrality de Jung 2.0 Source :
 * utilisation de l'algorithme de Brandes pour calculer les ppc, le nombre de
 * ppc, ainsi que les sommets et arcs précedéents dans les ppc
 * @author bcostes, jung project
 * @see "Ulrik Brandes: A Faster Algorithm for Betweenness Centrality. Journal of Mathematical Sociology 25(2):163-177, 2001."
 * @see JUNG Project and the Regents of the University
 * 
 * @param <GraphEntity>
 * @param <GraphEntity>
 */
public class ShortestPath {

  private double[] distances; // les distances
  //private int[] nbrsp; // les nombres de shortest path
  //private int[] previousEdges; // indexes des arcs précédents selon les ppc
  // depuis un sommet vers un autre sommet
  //private int[] previousNodes; // indexes des sommet précédents selon les
                               // ppc
  // depuis un sommet vers un autre sommet
  protected Map<GraphEntity, ShortestPathData> vertex_data;

  /**
   * Calcule des structures des ppc
   * @param graph
   */
  public void calculate(JungSnapshot graph) {

    // Initialisation
    int n = graph.getVertexCount();
    int dmax = graph.getDegreMax();
    int size = n * (n - 1) / 2;
    int size2 = dmax * n * n;
    this.distances = new double[size];
    //this.nbrsp = new int[size];
    

    
    
    //this.previousEdges = new int[size2];
    //this.previousNodes = new int[size2];
    

    vertex_data = new HashMap<GraphEntity, ShortestPathData>();

    for (int i = 0; i < distances.length; i++) {
      distances[i] = -1;
      //nbrsp[i] = -1;
    }
//    for (int i = 0; i < previousNodes.length; i++) {
//      previousNodes[i] = -1;
//      previousEdges[i] = -1;
//    }
    vertex_data = new HashMap<GraphEntity, ShortestPathData>();

    // start
    for (GraphEntity v : graph.getVertices()) {
      // initialize the betweenness data for this new vertex
      for (GraphEntity s : graph.getVertices())
        this.vertex_data.put(s, new ShortestPathData());

      vertex_data.get(v).numSPs = 1;
      vertex_data.get(v).distance = 0;

      Stack<GraphEntity> stack = new Stack<GraphEntity>();
      Queue<GraphEntity> queue = new MapBinaryHeap<GraphEntity>(
          new ShortestPathComparator());
      queue.offer(v);

      while (!queue.isEmpty()) {
        GraphEntity w = queue.poll();
        stack.push(w);
        ShortestPathData w_data = vertex_data.get(w);

        for (GraphEntity e : graph.getIncidentEdges(w)) {
          GraphEntity x = graph.getOpposite(w, e);
          if (x.equals(w))
            continue;
          double wx_weight = graph.getEdgesWeights().transform(e).doubleValue();

          ShortestPathData x_data = vertex_data.get(x);
          double x_potential_dist = w_data.distance + wx_weight;

          if (x_data.distance < 0) {
            x_data.distance = x_potential_dist;
            queue.offer(x);
            ((MapBinaryHeap<GraphEntity>) queue).update(x);
            x_data.predecessors.add(w);
            x_data.incomingEdges.add(e);
            x_data.numSPs = w_data.numSPs;
          }
          // (1) this can only happen with weighted edges
          // (2) x's SP count and incoming edges are updated below
          else if (x_data.distance > x_potential_dist) {
            x_data.distance = x_potential_dist;
            // invalidate previously identified incoming edges
            // (we have a better shortest path distance to x)
            x_data.incomingEdges.clear();
            x_data.predecessors.clear();
            x_data.predecessors.add(w);
            x_data.incomingEdges.add(e);
            x_data.numSPs = w_data.numSPs;
            // update x's position in queue
            ((MapBinaryHeap<GraphEntity>) queue).update(x);
          } else if (x_data.distance == x_potential_dist) {
            // nouveau ppc
            x_data.numSPs += w_data.numSPs;
            x_data.predecessors.add(w);
            x_data.incomingEdges.add(e);
          }

        }
      }

      // System.out.println(graph.getNodeIndex(v));

      while (!stack.isEmpty()) {

        GraphEntity x = stack.pop();
        if (v.equals(x)) {
          continue;
        }
        int ind = graph.getNodeIndex(v);
        int ind2 = graph.getNodeIndex(x);

        if (ind > ind2) {
          int ind3 = ind;
          ind = ind2;
          ind2 = ind3;
        }

        // distance v - x selon les ppc
        int id = ind * (2 * n - ind - 1) / 2 + (ind2 - ind - 1);

        this.distances[id] = vertex_data.get(x).distance;
        // nombre de ppc entre v et x
       // this.nbrsp[id] = (int) vertex_data.get(x).numSPs;
        // les prédécesseur de x dans les ppc v->x
       // int cpt = 0;

//        for (GraphEntity pred : vertex_data.get(x).predecessors) {
//          this.previousNodes[graph.getNodeIndex(v) * dmax * n + dmax
//              * graph.getNodeIndex(x) + cpt] = graph.getNodeIndex(pred);
//          cpt++;
//        }
        // les arcs prédécesseurs de x
      //  cpt = 0;
//        for (GraphEntity epred : vertex_data.get(x).incomingEdges) {
//          this.previousEdges[graph.getNodeIndex(v) * dmax * n + dmax
//              * graph.getNodeIndex(x) + cpt] = graph.getEdgeIndex(epred);
//          cpt++;
//        }

      }
    }

    vertex_data.clear();
  }

  /**
   * Déstructions des structures
   */
  public void reset() {
    this.distances = null;
    //this.nbrsp = null;
    //this.previousEdges = null;
    //this.previousNodes = null;
  }

  private class ShortestPathComparator implements Comparator<GraphEntity> {
    public int compare(GraphEntity v1, GraphEntity v2) {
      return vertex_data.get(v1).distance > vertex_data.get(v2).distance ? 1
          : -1;
    }
  }

  private class ShortestPathData {
    double distance;
    double numSPs;
    List<GraphEntity> incomingEdges;
    List<GraphEntity> predecessors;

    ShortestPathData() {
      distance = -1;
      numSPs = 0;
      incomingEdges = new ArrayList<GraphEntity>();
      predecessors = new ArrayList<GraphEntity>();
    }
  }

  public double[] getDistances() {
    return distances;
  }

//  public int[] getNbrsp() {
//    return nbrsp;
//  }
//
//  public int[] getPreviousEdges() {
//    return previousEdges;
//  }
//
//  public int[] getPreviousNodes() {
//    return previousNodes;
//  }

}
