package fr.ign.cogit.v2.indicators.local;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;
import fr.ign.cogit.v2.utils.ColtUtils;

public class AlphaCentrality extends ILocalIndicator {

  private double alpha = 0.4;
  private Transformer<GraphEntity, Double> externalWeights;

  public AlphaCentrality() {
    this.name = "Alpha";
    this.externalWeights = new Transformer<GraphEntity, Double>() {
      public Double transform(GraphEntity input) {
        return 1.;
      }
    };
  }

  /**
   * 
   * @param _d entre 0 et 1, ratio de 1/lambdamax (laphamx = 1/lambdamax)
   * @param _externalWeights
   */
  public AlphaCentrality(double _d,
      Transformer<GraphEntity, Double> _externalWeights) {
    this.name = "Alpha Centrality";
    if (_d > 1 || _d < 0) {
      _d = this.alpha;
    }
    this.alpha = _d;
    this.externalWeights = _externalWeights;
  }

  public AlphaCentrality(double _d) {
    this.name = "Alpha Centrality";
    if (_d > 1 || _d < 0) {
      _d = this.alpha;
    }
    this.alpha = _d;
    this.externalWeights = new Transformer<GraphEntity, Double>() {
      public Double transform(GraphEntity input) {
        return 1.;
      }
    };
  }

  @Override
  public Map<GraphEntity, Double> calculateNodeCentrality(JungSnapshot graph,  boolean normalize) {
    // matrice d'adjacence
    int n = graph.getVertexCount();

    // la matrice des sources extérieures
    DoubleMatrix2D e = new SparseDoubleMatrix2D(n, 1);
    for (int i = 0; i < n; i++) {
      e.setQuick(i, 0, this.externalWeights.transform(graph.getNodeByIndex(i)));
    }
    // matrice d'adjacence
    DoubleMatrix2D A = graph.getWeightedAdjacencyMatrix();

    double realAlpha = 0;
    // alpha doit etre copris entre 0 et la 1 / valeur propre max de la
    // matrice
    // d'adjacence
    double[] b = new double[A.rows()];
    for (int k = 0; k < b.length; k++) {
      b[k] = 1;
    }
    for (int p = 0; p < 100; p++) {
      // calculate the matrix-by-vector product Ab
      double[] tmp = new double[A.rows()];
      for (int i = 0; i < A.rows(); i++) {
        tmp[i] = 0;
        for (int j = 0; j < A.columns(); j++) {
          tmp[i] += A.getQuick(i, j) * b[j]; // dot product of ith col in A
                                             // with
                                             // b
        }
      }

      // calculate the length of the resultant vector
      double norm_sq = 0;
      for (int k = 0; k < b.length; k++) {
        norm_sq += tmp[k] * tmp[k];
      }
      realAlpha = Math.sqrt(norm_sq);
      // normalize b to unit vector for next iteration
      for (int k = 0; k < b.length; k++) {
        b[k] = tmp[k] / realAlpha;
      }
    }
    // on en rajoute un peu pour etre sur de pouvoir inverser la matrice
    realAlpha += 5.;
    // on en prend le ration renseigné par l'utilisateur
    realAlpha = alpha * (1. / realAlpha);

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
    tmp = ColtUtils.multScalar(tmp, realAlpha);
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
  public Map<GraphEntity, Double> calculateEdgeCentrality(JungSnapshot graph,  boolean normalize) {
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
      JungSnapshot graph, int k,  boolean normalize) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<GraphEntity, Double> calculateNeighborhoodNodeCentrality(
      JungSnapshot graph, int k,  boolean normalize) {
    // TODO Auto-generated method stub
    return null;
  }

}
