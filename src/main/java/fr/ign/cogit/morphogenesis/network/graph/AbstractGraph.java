package fr.ign.cogit.morphogenesis.network.graph;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import Jama.Matrix;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.uci.ics.jung.algorithms.importance.RandomWalkBetweenness;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.AttributeType;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.FeatureType;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.morphogenesis.network.graph.io.FormatExport;
import fr.ign.cogit.morphogenesis.network.graph.io.GraphWriter;
import fr.ign.cogit.morphogenesis.network.graph.io.LocalFeature;
import fr.ign.cogit.morphogenesis.network.graph.randomWalk.RandomWalk;
import fr.ign.cogit.morphogenesis.network.utils.MatrixUtils;

public abstract class AbstractGraph<E, V> extends
    UndirectedSparseMultigraph<E, V> {

  /**
   * 
   */
  private static final long serialVersionUID = 3859605662361043818L;
  private Logger logger = Logger.getLogger(AbstractGraph.class);
  private IFeatureCollection<IFeature> popOrigine;
  private String id;

  /**
   * Constructeur par défaut
   */
  public AbstractGraph() {
    super();
  }

  // ***************************************************************************
  // ******************** Méthodes de calcul abstraites ***********************
  // ***************************************************************************

  /**
   * Calcul de la centralité de degré
   * @return
   */
  public DegreeScorer<E> getDegreeCentrality() {
    DegreeScorer<E> ds = new DegreeScorer<E>(this);
    return ds;
  }

  /**
   * Calcul de la centralité de degré
   * @return
   */
  public Map<E, Double> getKDegreeCentrality(int k) {
    Map<E, Double> result = new HashMap<E, Double>();
    if (k < 1) {
      logger.error("KDegreeCentrality : k must be 1 or more.");
      System.exit(-1);
    } else if (k == 1) {
      DegreeScorer<E> d = this.getDegreeCentrality();
      for (E n : this.getVertices()) {
        result.put(n, (double) d.getVertexScore(n));
      }
      return result;
    }
    DoubleMatrix2D A = this.getKSUMAdjacencyMatrix(k);

    for (E node : this.getVertices()) {
      List<E> kneighbor = this.getNeighbors(node, A);
      kneighbor.add(node);
      int cpt = 0;
      for (E neighbor : kneighbor) {
        cpt += this.getIncidentEdges(neighbor).size();
      }
      result.put(node, ((double) cpt) / ((double) kneighbor.size()));
    }

    return result;

  }

  /**
   * Calcul de la mesure de controle
   * @return
   */
  public Map<E, Double> getControlMetric() {
    Map<E, Double> cm = new HashMap<E, Double>();
    DegreeScorer<E> v = this.getDegreeCentrality();
    for (E geom : this.getVertices()) {
      double centrality = 0;
      for (E neighbor : this.getNeighbors(geom)) {
        centrality += 1.0 / (double) v.getVertexScore(neighbor);
      }
      cm.put(geom, centrality);
    }
    return cm;
  }

  /**
   * Calcul de la centralité de proximité
   * @return
   */
  public abstract ClosenessCentrality<E, V> getClosenessCentrality();

  /**
   * Calcul de la centralité de proximité locales
   * @return
   */
  public abstract Map<E, Double> getLocaleClosenessCentrality(int depth);

  /**
   * Calcul de la centralité de proximité intermédiare
   * @return
   */
  public abstract Map<E, Double> getLocaleBetweennessCentrality(int depth);

  /**
   * Calcul de la centralité intermédiaire
   * @return
   */
  public abstract BetweennessCentrality<E, V> getBetweennessCentrality();

  /**
   * Calcul de la centralité intermédiaire random graph
   * @return
   */
  public RandomWalkBetweenness<E, V> getRandomBetweennessCentrality() {
    RandomWalkBetweenness<E, V> b = new RandomWalkBetweenness<E, V>(this);
    b.setRemoveRankScoresOnFinalize(false);
    b.evaluate();
    return b;
  }

  /**
   * Calcul de l'éloignement moyen
   * @return
   */
  public Map<E, Double> getMeanDistance() {
    Map<E, Double> cm = new HashMap<E, Double>();
    ClosenessCentrality<E, V> c = this.getClosenessCentrality();
    for (E e : this.getVertices()) {
      cm.put(e, 1. / c.getVertexScore(e));
    }
    return cm;
  }

  /**
   * Centralité spectrale
   * @return
   */
  public Map<E, Double> getEigenVectorCentrality() {

    Map<E, Double> result = new HashMap<E, Double>();

    for (E e : this.getVertices()) {
      result.put(e, 1.);
    }

    double epsilon = 0.00000001;

    int cpt = 0;
    while (true) {
      Map<E, Double> oldCentrlaity = new HashMap<E, Double>(result);

      Map<E, Double> w = new HashMap<E, Double>();
      for (E e : this.getVertices()) {
        w.put(e, 0.);
      }

      for (E e : this.getVertices()) {
        for (E neighbor : this.getNeighbors(e)) {
          w.put(neighbor, w.get(neighbor) + result.get(e));
        }
      }

      for (E e : this.getVertices()) {
        result.put(e, w.get(e));
      }

      double sum = 0;
      for (E e : this.getVertices()) {
        sum += result.get(e);
      }

      for (E e : this.getVertices()) {
        result.put(e, result.get(e) / (double) sum);
      }

      boolean stop = true;
      for (E e : this.getVertices()) {
        if (Math.abs(result.get(e) - oldCentrlaity.get(e)) > epsilon) {
          stop = false;
          break;
        }
      }
      if (stop) {
        break;
      }
      cpt++;
    }

    return result;
  }

  /**
   * Calcul de page rank
   * @param d
   * @return
   */
  public Map<E, Double> getPageRank(double d) {
    PageRank<E, V> pr = new PageRank<E, V>(this, d);
    pr.evaluate();
    Map<E, Double> PR = new HashMap<E, Double>();
    for (E e : this.getVertices()) {
      PR.put(e, pr.getVertexScore(e));
    }

    return PR;

  }

  /**
   * Katz centrality alpha must be in [0, 1/lambdaMax] A priori, alpha>>0 =>
   * facteur d'atténuation faible => considération globale alpha <<1 => facteur
   * d'atténuation élevé => considérations locales Bonnes valeurs : Min (1,
   * 1.0001 * 1/lambda) => global Min (1, 1.5 * 1/lambda) => local Au dela de
   * 1.5*1/L, l'histogramme des fréquences des centralités n'est plus très
   * interessant
   * @param d
   * @return
   */
  public Map<E, Double> getKatzCentrality(double fact) {
    Map<E, Double> result = new HashMap<E, Double>();

    // matrice d'adjacence
    int n = this.getVertexCount();
    double adj[][] = new double[n][n];
    Map<Integer, E> mapping = new HashMap<Integer, E>();
    int cpt = 0;
    for (E e : this.getVertices()) {
      mapping.put(cpt, e);
      cpt++;
    }
    for (int i = 0; i < n - 1; i++) {
      for (int j = i + 1; j < n; j++) {
        if (this.isNeighbor(mapping.get(i), mapping.get(j))) {
          adj[i][j] = 1;
        } else {
          adj[i][j] = 0;
        }
        adj[j][i] = adj[i][j];
      }
      adj[i][i] = 0;
    }
    Matrix A = new Matrix(adj);

    // alpha doit etre copris entre 0 et la 1 / valeur propre max de la matrice
    // d'adjacence

    // Méthode puissance itérées
    Matrix w0 = new Matrix(n, 1);
    double[][] W0 = w0.getArray();
    W0[0][0] = 1;
    for (int i = 1; i < n; i++) {
      W0[i][0] = 0;
    }
    Matrix zk = w0.times(1. / w0.norm2());
    while (true) {
      Matrix wk = A.times(zk);
      Matrix zkk = wk.times(1. / wk.norm2());
      if (zkk.minus(zk).norm2() < 0.00001) {
        break;
      }
      zk = zkk;
    }
    Matrix lambdaMaxbis = zk.transpose().times(A).times(zk)
        .times((zk.transpose().times(zk)).inverse());

    double alpha = Math.min(1., 1. / (fact * lambdaMaxbis.get(0, 0)));

    Matrix I = new Matrix(n, n);
    double[][] X = I.getArray();
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        X[i][j] = (i == j ? 1.0 : 0.0);
      }
    }
    Matrix e = new Matrix(n, 1);
    double[][] E = e.getArray();
    for (int i = 0; i < n; i++) {
      E[i][0] = 1;
    }
    Matrix katzCentrality = (I.minus(A.times(alpha))).inverse().times(e);

    for (int i = 0; i < this.getVertexCount(); i++) {
      result.put(mapping.get(i), katzCentrality.get(i, 0));
    }

    return result;
  }

  public abstract Map<E, Double> getFlowBetweennessCentrality();

  /**
   * Alpha centrality
   * @param d
   * @return
   */
  public Map<E, Double> getAlphaCentrality(double alpha,
      Transformer<E, Double> weights) {
    Map<E, Double> result = new HashMap<E, Double>();

    // matrice d'adjacence
    int n = this.getVertexCount();
    double adj[][] = new double[n][n];
    Map<Integer, E> mapping = new HashMap<Integer, E>();
    int cpt = 0;
    for (E e : this.getVertices()) {
      mapping.put(cpt, e);
      cpt++;
    }
    Matrix e = new Matrix(n, 1);
    double[][] E = e.getArray();
    for (int i = 0; i < n; i++) {
      if (weights == null) {
        E[i][0] = 1;
      } else {
        E[i][0] = weights.transform(mapping.get(i));
      }

    }

    for (int i = 0; i < n - 1; i++) {
      for (int j = i + 1; j < n; j++) {
        if (this.isNeighbor(mapping.get(i), mapping.get(j))) {
          adj[i][j] = 1;
        } else {
          adj[i][j] = 0;
        }
        adj[j][i] = adj[i][j];
      }

      adj[i][i] = 0;
    }
    Matrix A = new Matrix(adj);

    // alpha doit etre copris entre 0 et la 1 / valeur propre max de la matrice
    // d'adjacence

    Matrix I = new Matrix(n, n);
    double[][] X = I.getArray();
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        X[i][j] = (i == j ? 1.0 : 0.0);
      }
    }

    Matrix katzCentrality = (I.minus(A.times(alpha))).inverse().times(e);

    for (int i = 0; i < this.getVertexCount(); i++) {
      result.put(mapping.get(i), katzCentrality.get(i, 0));
    }

    return result;
  }

  // TODO : revoir code, car définition ambigue ...
  /**
   * Calcul de weighted page rank
   * @param d
   * @return
   */
  public Map<E, Double> getWeightedPageRank(double d) {
    Map<E, Double> WPR = new HashMap<E, Double>();

    double epsilon = 0.000001;
    int n = this.getVertexCount();
    // initialisation
    for (E e : this.getVertices()) {
      double sum2 = 0;
      Collection<E> neighbors = this.getNeighbors(e);
      for (E ne : neighbors) {
        sum2 += this.getIncidentEdges(ne).size();

      }
      sum2 = 1. / sum2;
      WPR.put(e, this.getIncidentEdges(e).size() * sum2);
    }
    // boucle
    int cpt = 0;
    while (true) {
      Map<E, Double> oldWPR = new HashMap<E, Double>(WPR);
      for (E e : this.getVertices()) {
        // nombre de voisin du sommet e
        Collection<E> neighbors = this.getNeighbors(e);
        double sum = 0;
        double sum2 = 0;
        for (E ne : neighbors) {
          sum2 += this.getIncidentEdges(ne).size();

        }
        sum2 = 1. / sum2;
        for (E ne : neighbors) {
          // nombre de liens vers ne
          int NE = this.getIncidentEdges(ne).size();
          double Wij = NE * sum2;
          sum += WPR.get(ne) * Wij;
        }
        sum *= d;
        sum += (1. - d) / (double) n;
        WPR.put(e, sum);
      }

      boolean stop = true;
      for (E e : this.getVertices()) {
        if (Math.abs(WPR.get(e) - oldWPR.get(e)) > epsilon) {
          stop = false;
          break;
        }
      }
      cpt++;
      if (stop) {
        break;
      }
    }

    return WPR;
  }

  public RandomWalk<E, V> getRandomWalk(int nb_troopers) {
    RandomWalk<E, V> rw = new RandomWalk<E, V>(this, nb_troopers);
    rw.run();
    return rw;
  }

  public double getCharacteristicPathLength() {
    double sum = 0;

    UnweightedShortestPath<E, V> sp = new UnweightedShortestPath<E, V>(this);
    for (E v : this.getVertices()) {
      Map<E, Number> m = sp.getDistanceMap(v);
      double sum2 = 0;
      for (E vv : m.keySet()) {
        sum2 += Double.parseDouble(m.get(vv).toString());
      }
      sum2 /= (double) m.size();

      sum += sum2;
      sp.reset(v);
    }
    sum /= (double) this.getVertexCount();
    return sum;
  }

  public double getClusteringCoefficient() {
    double sum = 0;
    for (E v : this.getVertices()) {
      if (this.getNeighborCount(v) <= 1) {
        continue;
      }
      // les voisins de v
      Collection<E> neighbors = this.getNeighbors(v);
      Set<V> edges = new HashSet<V>();
      for (E e : neighbors) {
        // on ajoute le nombre d'arc connecté à e
        edges.addAll(this.getIncidentEdges(e));
      }
      // on supprime les arcs connectés à v
      edges.removeAll(this.getIncidentEdges(v));
      double l = 0.;
      for (V e : edges) {
        if (neighbors.contains(this.getEndpoints(e).getFirst())
            && neighbors.contains(this.getEndpoints(e).getSecond())) {
          l++;
        }
      }

      double m = this.getNeighborCount(v);
      sum += (2. * l) / (m * (m - 1));
    }
    sum /= (double) this.getVertices().size();
    return sum;
  }

  /**
   * Calcul de la centralité des coeef de clustering
   * @return
   */
  public Map<E, Double> getClusteringCentrality() {
    Map<E, Double> cc = new HashMap<E, Double>();
    for (E v : this.getVertices()) {
      if (this.getNeighborCount(v) <= 1) {
        cc.put(v, 0.);
        continue;
      }
      Collection<E> neighbors = this.getNeighbors(v);
      Set<V> edges = new HashSet<V>();
      for (E e : neighbors) {
        for (V ee : this.getIncidentEdges(e)) {
          // on ajoute le nombre d'arc connecté à e
          if (!edges.contains(ee)) {
            edges.add(ee);
          }
        }
      }
      // on supprime les arcs connectés à v
      edges.removeAll(this.getIncidentEdges(v));
      double l = 0.;
      for (V e : edges) {
        if (neighbors.contains(this.getEndpoints(e).getFirst())
            && neighbors.contains(this.getEndpoints(e).getSecond())) {
          l++;
        }
      }

      double m = this.getNeighborCount(v);
      double centrality = (2. * l) / (m * (m - 1));
      if ((new Double(centrality)).equals(Double.NaN)
          || (new Double(centrality)).equals(Double.POSITIVE_INFINITY)
          || (new Double(centrality)).equals(Double.NEGATIVE_INFINITY)) {
        centrality = 0.;
      }
      cc.put(v, centrality);
    }
    return cc;
  }

  public Map<E, Double> getKClusteringCentrality(int k) {
    Map<E, Double> cc = new HashMap<E, Double>();
    DoubleMatrix2D A = this.getKSUMAdjacencyMatrix(k);

    for (E v : this.getVertices()) {
      List<E> kneighbor = this.getNeighbors(v, A);
      Set<V> edges = new HashSet<V>();
      for (E e : kneighbor) {
        // on ajoute le nombre d'arc connecté à e
        for (V ee : this.getIncidentEdges(e)) {
          if (!edges.contains(ee)) {
            edges.add(ee);
          }
        }
      }
      edges.removeAll(this.getIncidentEdges(v));
      double l = 0.;
      for (V e : edges) {
        if (kneighbor.contains(this.getEndpoints(e).getFirst())
            && kneighbor.contains(this.getEndpoints(e).getSecond())) {
          l++;
        }
      }
      double m = this.getNeighborCount(v);
      double centrality = (2. * l) / (m * (m - 1));
      if ((new Double(centrality)).equals(Double.NaN)
          || (new Double(centrality)).equals(Double.POSITIVE_INFINITY)
          || (new Double(centrality)).equals(Double.NEGATIVE_INFINITY)) {
        centrality = 0.;
      }
      cc.put(v, centrality);
    }

    return cc;
  }

  /**
   * 
   * @return [0] => LRandom, [1] => CRandom
   */
  public double[] getSmallWorldProperties() {

    // nombre de sommets
    int n = this.getVertices().size();
    // nombre moyens d'arcs par sommet
    int m = 0;
    for (E v : this.getVertices()) {
      m += this.getIncidentEdges(v).size();
    }
    m /= (double) this.getVertexCount();

    double[] result = new double[2];
    result[0] = Math.log(n) / Math.log(m);
    result[1] = (double) m / (double) n;

    return result;
  }

  public List<E> getKNeighbors(E vertex, int depth) {
    List<E> result = new ArrayList<E>();
    result.addAll(this.getNeighbors(vertex));
    List<E> newNeighbor = new ArrayList<E>(result);
    result.add(vertex);
    for (int i = 1; i < depth; i++) {
      List<E> newNeighbor2 = new ArrayList<E>();
      for (E neighbor : newNeighbor) {
        for (E n : this.getNeighbors(neighbor)) {
          if (!result.contains(n)) {
            newNeighbor2.add(n);
          }
        }
      }
      newNeighbor = new ArrayList<E>(newNeighbor2);
      result.addAll(newNeighbor);

    }
    result.remove(vertex);

    return result;
  }

  /**
   * Récupère les voisins à partir de la matrice d'adjacence Si on passe une
   * puissance k de la matrice, on récupère les k-voisins ...
   * @param A
   * @return
   */
  public List<E> getNeighbors(E vertex, DoubleMatrix2D A) {
    List<E> result = new ArrayList<E>();
    Map<Integer, E> mapping = new HashMap<Integer, E>();
    int cpt = 0;
    int ind = 0;
    for (E e : this.getVertices()) {
      mapping.put(cpt, e);
      if (e.equals(vertex)) {
        ind = cpt;
      }
      cpt++;

    }
    for (int j = 0; j < this.getVertexCount(); j++) {
      if (ind == j) {
        continue;
      }
      if (A.getQuick(ind, j) != 0) {
        result.add(mapping.get(j));
      }
    }
    result.remove(vertex);

    return result;
  }

  public DoubleMatrix2D getUnweightedAdjacencyMatrix() {
    int n = this.getVertexCount();
    SparseDoubleMatrix2D A = new SparseDoubleMatrix2D(n, n);
    Map<Integer, E> mapping = new HashMap<Integer, E>();
    int cpt = 0;
    for (E e : this.getVertices()) {
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
    DoubleMatrix2D A = this.getUnweightedAdjacencyMatrix();
    DoubleMatrix2D Ak = A;
    DoubleMatrix2D ASUM = A;

    Algebra al = new Algebra();

    for (int i = 1; i < k; i++) {
      Ak = al.mult(Ak, A);
      ASUM = MatrixUtils.sum(ASUM, Ak);
    }

    return ASUM;
  }

  // ***************************************************************************
  // ******************** Méthodes d'export abstraites ************************
  // ***************************************************************************

  public abstract void exportCentrality(String centrality, String file,
      int format);

  /**
   * Méthode générique d'export d'un indicateur
   */
  protected void exportIndicator(Map<Edge, Double> values, String file,
      int format) {
    // création du schéma de sortie
    FeatureType featT = new FeatureType();
    AttributeType att = new AttributeType();
    att.setMemberName("INDICATOR");
    att.setNomField("INDICATOR");
    att.setValueType("Double");
    featT.addFeatureAttribute(att);

    IFeatureCollection<IFeature> col = new Population<IFeature>();

    for (Edge node : values.keySet()) {
      IDirectPositionList l = new DirectPositionList();
      for (Point2D.Double n : node.coords()) {
        l.add(new DirectPosition(n.getX(), n.getY()));
      }
      IGeometry geom = new GM_LineString(l);
      LocalFeature feat = new LocalFeature(geom);
      feat.setFeatureType(featT);
      double value = values.get(node);
      feat.setAttribute(featT.getFeatureAttributeByName("INDICATOR"), value);
      col.add(feat);
    }

    switch (format) {
      case FormatExport.SHAPEFILE:
        GraphWriter.graph2Shp(col, file);
        break;
      case FormatExport.PNG:
        GraphWriter.graph2Img(col, file, format);
        break;
    }
    if (logger.isInfoEnabled()) {
      logger.info("Export terminé.");
    }

  }

  public void setPop(IFeatureCollection<IFeature> popOrigine) {
    this.popOrigine = popOrigine;
  }

  public IFeatureCollection<IFeature> getPop() {
    return popOrigine;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
