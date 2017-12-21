package fr.ign.cogit.morphogenesis.exploring_tool.model.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.uci.ics.jung.algorithms.filters.Filter;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter.EdgeType;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.AbstractIndicator;
import fr.ign.cogit.morphogenesis.network.utils.MatrixUtils;

public class GeometricalGraph extends AbstractGraph<Node, Edge> {

  private static final long serialVersionUID = 1L;
  private static int GRAPH_COUNT = 0;

  public GeometricalGraph() {
    super();
    this.id = GRAPH_COUNT;
    GRAPH_COUNT++;
  }

  public void accept(AbstractIndicator v) {
    v.calculate(this);
  }

  public void updateCentralityValues(String string, Map<Node, Double> values) {
    for (Node n : this.getVertices()) {
      n.getCentralityValues().put(string, values.get(n));
    }
    Map<Edge, Double> result = new HashMap<Edge, Double>();
    for (Edge edge : this.getEdges()) {
      result.put(edge,
          (values.get(edge.first()) + values.get(edge.last())) / 2.);
    }
    // result = (new Statistics<Edge>()).centrerRéduire(result);
    for (Edge edge : this.getEdges()) {
      edge.getCentralityValues().put(string, result.get(edge));
    }
    result.clear();
    result = null;
    System.gc();
  }

  public void updateCentralityValuesEdgesOnly(String string,
      Map<Edge, Double> values) {
    for (Edge edge : this.getEdges()) {
      edge.getCentralityValues().put(string, values.get(edge));
    }
    System.gc();
  }

  public double[] getLocalMorphologicalIndicator(String ind) {
    double[] values = new double[this.getVertexCount()];
    int cpt = 0;
    for (Node n : this.getVertices()) {
      values[cpt] = n.getCentralityValues().get(ind);
      cpt++;
    }
    return values;
  }

  /**
   * Récupère les voisins à partir de la matrice d'adjacence Si on passe une
   * puissance k de la matrice, on récupère les k-voisins ...
   * @param A
   * @return
   */
  public List<Node> getKNeighbors(Node vertex, int k) {
    UndirectedSparseMultigraph<Node, Edge> neighborhood = this
        .getKNeighborhood(vertex, k);
    List<Node> l = new ArrayList<Node>();
    l.addAll(neighborhood.getVertices());
    l.remove(vertex);
    return l;
  }

  public UndirectedSparseMultigraph<Node, Edge> getKNeighborhood(Node n, int k) {
    Filter<Node, Edge> filter = new KNeighborhoodFilter<Node, Edge>(n, k,
        EdgeType.IN_OUT);
    UndirectedSparseMultigraph<Node, Edge> neighborhood = (UndirectedSparseMultigraph<Node, Edge>) filter
        .transform(this);
    return neighborhood;
  }

  public SparseDoubleMatrix2D getUnweightedAdjacencyMatrix() {
    int n = this.getVertexCount();
    SparseDoubleMatrix2D A = new SparseDoubleMatrix2D(n, n);
    Map<Integer, Node> mapping = new HashMap<Integer, Node>();
    int cpt = 0;
    for (Node e : this.getVertices()) {
      mapping.put(cpt, e);
      cpt++;
    }
    for (int i = 0; i < n - 1; i++) {
      for (int j = i + 1; j < n; j++) {
        if (this.isNeighbor(mapping.get(i), mapping.get(j))) {
          A.setQuick(i, j, 1);
        } else {
          A.setQuick(i, j, 0);
        }
        A.setQuick(j, i, A.getQuick(i, j));
      }
      A.setQuick(i, i, 0);
    }
    return A;
  }

  public DoubleMatrix2D getKSUMAdjacencyMatrix(int k) {
    SparseDoubleMatrix2D A = this.getUnweightedAdjacencyMatrix();
    DoubleMatrix2D Ak = A;
    DoubleMatrix2D ASUM = A;

    Algebra al = new Algebra();

    for (int i = 1; i < k; i++) {
      Ak = al.mult(Ak, A);
      ASUM = MatrixUtils.sum(ASUM, Ak);
    }

    return ASUM;
  }

  public DoubleMatrix2D getKSUMAdjacencyMatrix(SparseDoubleMatrix2D A, int k) {
    DoubleMatrix2D Ak = A;
    DoubleMatrix2D ASUM = A;

    Algebra al = new Algebra();

    for (int i = 1; i < k; i++) {
      Ak = al.mult(Ak, A);
      ASUM = MatrixUtils.sum(ASUM, Ak);
    }

    return ASUM;
  }

}
