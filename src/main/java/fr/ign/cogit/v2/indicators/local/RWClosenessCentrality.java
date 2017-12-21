package fr.ign.cogit.v2.indicators.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;
import fr.ign.cogit.v2.utils.ColtUtils;

public class RWClosenessCentrality extends ILocalIndicator {

  public RWClosenessCentrality() {
    this.name = "RWClos";
  }

  @Override
  public Map<GraphEntity, Double> calculateNodeCentrality(JungSnapshot graph, boolean normalize) {
    Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
    for (GraphEntity v : graph.getVertices()) {
      result.put(v, 0.);
    }

    int n = graph.getVertexCount();
    // Création de la matrice de transition
    DoubleMatrix2D M = new SparseDoubleMatrix2D(n, n);
    M.assign(0.);
    DoubleMatrix2D A = graph.getWeightedAdjacencyMatrix();
    for (GraphEntity v : graph.getVertices()) {
      int i = graph.getNodeIndex(v);
      List<Integer> indexesN = new ArrayList<Integer>();
      double sum = 0;
      for (GraphEntity w : graph.getNeighbors(v)) {
        int j = graph.getNodeIndex(w);
        indexesN.add(j);
        M.setQuick(i, j, A.getQuick(i, j));
        sum += A.getQuick(i, j);
      }
      for (Integer j : indexesN) {
        M.setQuick(i, j, M.getQuick(i, j) / sum);
      }
    }

    // calcul des temps de premier passage moyen
    DoubleMatrix2D I = ColtUtils.identityMatrix(n - 1);
    DoubleMatrix2D e = new DenseDoubleMatrix2D(n - 1, 1);
    e.assign(1.);
    Algebra al = new Algebra();
    for (GraphEntity vj : graph.getVertices()) {
      int j = graph.getNodeIndex(vj);
      // suppression de la jème ligne et colonne de M
      long t0 = System.currentTimeMillis();
      DoubleMatrix2D Mj = ColtUtils.delete(M, j);
      long t1 = System.currentTimeMillis() - t0;
      System.out.println("1 : " + t1);
      t0 = System.currentTimeMillis();

      DoubleMatrix2D Hj = ColtUtils.minus(I, Mj);
      t1 = System.currentTimeMillis() - t0;
      System.out.println("2 : " + t1);
      t0 = System.currentTimeMillis();

      Hj = al.inverse(Hj);
      t1 = System.currentTimeMillis() - t0;
      System.out.println("3 : " + t1);
      t0 = System.currentTimeMillis();

      Hj = al.mult(Hj, e);
      t1 = System.currentTimeMillis() - t0;
      System.out.println("4 : " + t1);
      t0 = System.currentTimeMillis();

      for (GraphEntity vi : graph.getVertices()) {
        if (!vi.equals(vj)) {
          int i = graph.getNodeIndex(vi);
          if (i > j) {
            i--;
          }
          result.put(vi, result.get(vi) + Hj.getQuick(i, 0));
        }
      }
    }

    // on normalise
    for (GraphEntity v : graph.getVertices()) {
      result.put(v, result.get(v) / ((double) n));
    }

    return null;
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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<GraphEntity, Double> calculateNeighborhoodNodeCentrality(
      JungSnapshot graph, int k, boolean normalize) {
    // TODO Auto-generated method stub
    return null;
  }

}
