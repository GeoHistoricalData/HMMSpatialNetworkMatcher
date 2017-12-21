package fr.ign.cogit.morphogenesis.network.graph.rewriting;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.algorithms.importance.RandomWalkBetweenness;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
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
import fr.ign.cogit.morphogenesis.network.graph.rewriting.io.FormatExport;
import fr.ign.cogit.morphogenesis.network.graph.rewriting.io.GraphWriter;
import fr.ign.cogit.morphogenesis.network.graph.rewriting.io.LocalFeature;
import fr.ign.cogit.morphogenesis.network.graph.rewriting.randomWalk.RandomWalk;

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
  public abstract EigenvectorCentrality<E, V> getEigenVectorCentrality();

  /**
   * Calcul de page rank
   * @param d
   * @return
   */
  public Map<E, Double> getPageRank(double d) {
    Map<E, Double> PR = new HashMap<E, Double>();
    double epsilon = 0.000001;
    int n = this.getVertexCount();
    // initialisation
    for (E e : this.getVertices()) {
      PR.put(e, 1. / n);
    }
    // boucle
    int cpt = 0;
    while (true) {
      Map<E, Double> oldPR = new HashMap<E, Double>(PR);
      for (E e : this.getVertices()) {
        // nombre de voisin du sommet e
        Collection<E> neighbors = this.getNeighbors(e);
        double sum = 0;
        for (E ne : neighbors) {
          // nombre de liens vers ne
          int NE = this.getIncidentEdges(ne).size();
          sum += PR.get(ne) / NE;
        }
        sum *= d;
        sum += (1. - d) / (double) n;
        PR.put(e, sum);
      }

      boolean stop = true;
      for (E e : this.getVertices()) {
        if (Math.abs(PR.get(e) - oldPR.get(e)) > epsilon) {
          stop = false;
          break;
        }
      }
      cpt++;
      if (stop) {
        break;
      }
    }

    return PR;
  }

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
      WPR.put(e, 1. / n);
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
          ;
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
    for (E e : this.getVertices()) {
      System.out.println(WPR.get(e));
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

    DijkstraShortestPath<E, V> sp = new DijkstraShortestPath<E, V>(this);
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
   * Calcul de la centralité des 2-coeff de clustering
   * @return
   */
  public Map<E, Double> get2ClusteringCentrality() {

    System.out.println("ok");

    Map<E, Double> cc = new HashMap<E, Double>();
    for (E v : this.getVertices()) {
      if (this.getNeighborCount(v) <= 1) {
        cc.put(v, 0.);
        continue;
      }
      Set<E> m = new HashSet<E>();

      // les voisins de v
      Collection<E> neighbors = this.getNeighbors(v);
      Set<V> edges = new HashSet<V>();
      for (E e : neighbors) {
        Collection<E> neighbors2 = this.getNeighbors(e);
        Set<V> tmp = new HashSet<V>();
        // on ajoute le nombre d'arc connecté à e
        for (E ee : neighbors2) {
          // on ajoute le nombre d'arc connecté à e
          tmp.addAll(this.getIncidentEdges(ee));
        }

        for (V ee : tmp) {
          if (neighbors2.contains(this.getEndpoints(ee).getFirst())
              && neighbors2.contains(this.getEndpoints(ee).getSecond())) {
            edges.add(ee);
          }
        }

        m.addAll(this.getNeighbors(e));
      }
      m.removeAll(this.getNeighbors(v));
      m.remove(v);

      double centrality = (2. * edges.size()) / (m.size() * (m.size() - 1));
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
