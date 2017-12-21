package fr.ign.cogit.v2.indicators.local;

import java.util.HashMap;
import java.util.Map;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;
import fr.ign.cogit.v2.utils.ColtUtils;

public class KatzCentrality extends ILocalIndicator {

  private double alpha = 0.1;

  public KatzCentrality() {
    this.name = "Katz Centrality";
  }

  public KatzCentrality(double _d) {
    this.name = "Katz";
    this.alpha = _d;
  }

  @Override
  public Map<GraphEntity, Double> calculateNodeCentrality(JungSnapshot graph, boolean normalize) {
    // matrice d'adjacence
    int n = graph.getVertexCount();

    DoubleMatrix2D e = new SparseDoubleMatrix2D(n, 1);
    for (int i = 0; i < n; i++) {
      e.setQuick(i, 0, 1.);
    }

    // matrice d'adjacence
    DoubleMatrix2D A = graph.getWeightedAdjacencyMatrix();

    // alpha doit etre copris entre 0 et la 1 / valeur propre max de la matrice
    // d'adjacence

    // matrice identité
    DoubleMatrix2D I = new SparseDoubleMatrix2D(n, n);
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (i == j) {
          I.setQuick(i, i, 1.);
        } else {
          I.setQuick(i, j, 0.);
        }
      }
    }

    // opération
    Algebra al = new Algebra();
    DoubleMatrix2D tmp = al.transpose(A);
    tmp = ColtUtils.multScalar(tmp, alpha);
    tmp = ColtUtils.minus(I, tmp);
    tmp = al.inverse(tmp);
    DoubleMatrix2D katzCentrality = al.mult(tmp, e);

    Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
    for (int i = 0; i < graph.getVertexCount(); i++) {
      result.put(graph.getNodeByIndex(i), katzCentrality.getQuick(i, 0));
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
