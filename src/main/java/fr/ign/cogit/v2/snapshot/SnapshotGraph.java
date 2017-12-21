package fr.ign.cogit.v2.snapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.contrib.delaunay.TriangulationJTS;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.v2.indicators.IGlobalIndicator;
import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.io.SnapshotIOManager;
import fr.ign.cogit.v2.weightings.IEdgeWeighting;

/**
 * Un snapshot de réseau
 * @author bcostes
 *
 */
public class SnapshotGraph extends JungSnapshot {

    
    public static enum NORMALIZERS{
        NONE, MINMAX, CONVENTIONAL;
    }
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**
   * Indicateurs globaux de graphe
   */
  private Map<String, Double> globalIndicators;
  /**
   * Indicateurs locaux calculés pour les sommets
   */
  private List<String> nodesLocalIndicators;
  /**
   * Indicateurs locaux calculés pour les arcs
   */
  private List<String> edgesLocalIndicators;

  // **********************************************************************
  // *************************** Constructeurs ****************************
  // **********************************************************************

  public SnapshotGraph() {
    this.globalIndicators = new HashMap<String, Double>();
    this.setNodesLocalIndicators(new ArrayList<String>());
    this.setEdgesLocalIndicators(new ArrayList<String>());
  }

  // **********************************************************************
  // ************************** getteurs, setteurs ************************
  // **********************************************************************

  public void setGlobalIndicators(Map<String, Double> globalIndicators) {
    this.globalIndicators = globalIndicators;
  }

  public Map<String, Double> getGlobalIndicators() {
    return globalIndicators;
  }

  public void setNodesLocalIndicators(List<String> nodesLocalIndicators) {
    this.nodesLocalIndicators = nodesLocalIndicators;
  }

  public List<String> getNodesLocalIndicators() {
    return nodesLocalIndicators;
  }

  public void setEdgesLocalIndicators(List<String> edgesLocalIndicators) {
    this.edgesLocalIndicators = edgesLocalIndicators;
  }

  public List<String> getEdgesLocalIndicators() {
    return edgesLocalIndicators;
  }

  // ********************************************************************
  // *********************** indicateurs ********************************
  // ********************************************************************

  public void graphGlobalIndicator(IGlobalIndicator indicator) {
    this.globalIndicators.put(indicator.getName(),
        this.calculateGraphGlobalIndicator(indicator));
  }

  /**
   * Calcul de l'indicateur pour les sommets
   * @param indicator
   */
  public void nodeLocalIndicator(ILocalIndicator indicator, SnapshotGraph.NORMALIZERS normalize) {
    if (this.nodesLocalIndicators.contains(indicator.getName())) {
      return;
    }
    this.nodesLocalIndicators.add(indicator.getName());
    Map<GraphEntity, Double> values = this.calculateNodeCentrality(indicator,
        normalize);
    for (GraphEntity node : this.getVertices()) {
      node.getLocalIndicators().put(indicator.getName(), values.get(node));
    }
  }

  /**
   * Calcul de l'indicateur pour les arcs
   * @param indicator
   */
  public void edgeLocalIndicator(ILocalIndicator indicator, SnapshotGraph.NORMALIZERS normalize) {
    if (this.edgesLocalIndicators.contains(indicator.getName())) {
      return;
    }
    this.edgesLocalIndicators.add(indicator.getName());

    Map<GraphEntity, Double> values = this.calculateEdgeCentrality(indicator,
        normalize);
    for (GraphEntity edge : this.getEdges()) {
      edge.getLocalIndicators().put(indicator.getName(), values.get(edge));
    }
  }

  /**
   * Calcul de l'indicateur pour les sommets
   * @param indicator
   */
  public void neighborhoodNodeLocalIndicator(ILocalIndicator indicator, int k,
          SnapshotGraph.NORMALIZERS normalize) {
    if (this.nodesLocalIndicators.contains("local_" + indicator.getName())) {
      return;
    }
    this.nodesLocalIndicators.add("local_" + indicator.getName());

    Map<GraphEntity, Double> values = this.calculateNeighborhoodNodeCentrality(
        indicator, k, normalize);
    for (GraphEntity node : this.getVertices()) {
      node.getLocalIndicators().put("local_" + indicator.getName(),
          values.get(node));
    }
  }

  /**
   * Calcul de l'indicateur pour les arcs
   * @param indicator
   */
  public void neighborhoodEdgeLocalIndicator(ILocalIndicator indicator, int k,
          SnapshotGraph.NORMALIZERS normalize) {
    if (this.edgesLocalIndicators.contains("local_" + indicator.getName())) {
      return;
    }
    this.edgesLocalIndicators.add("local_" + indicator.getName());

    Map<GraphEntity, Double> values = this.calculateNeighborhoodEdgeCentrality(
        indicator, k, normalize);
    for (GraphEntity edge : this.getEdges()) {
      edge.getLocalIndicators().put("local_" + indicator.getName(),
          values.get(edge));
    }
  }

  /**
   * Suppression d'un indicateur local pour les sommets
   * @param indicator
   */
  public void deleteNodesLocalIndicator(String indicator) {
    if (this.nodesLocalIndicators.contains(indicator)) {
      this.nodesLocalIndicators.remove(indicator);
      for (GraphEntity node : this.getVertices()) {
        node.getLocalIndicators().remove(indicator);
      }
    }
  }

  /**
   * Suppression d'un indicateur local pour les arcs
   * @param indicator
   */
  public void deleteEdgesLocalIndicator(String indicator) {
    if (this.edgesLocalIndicators.contains(indicator)) {
      this.edgesLocalIndicators.remove(indicator);
      for (GraphEntity edge : this.getEdges()) {
        edge.getLocalIndicators().remove(indicator);
      }
    }
  }

  // **********************************************************************
  // ************************** Autre fonctions ***************************
  // **********************************************************************

  /**
   * Donne l'arbre couvrant de poids minimum
   */
  public SnapshotGraph minimumSpanningTree(IEdgeWeighting edges_weights) {
    SnapshotGraph j = SnapshotIOManager.graph2JungSnapshot(
        this.arbitraryMinimumSpanningTree(), (edges_weights));
    return j;
  }

  /**
   * Réalise une triangulation de Delaunay sur le réseau. Attention, les sommets
   * et les arcz doivent porter une géométrie (donc ici être de type Graphentity
   * par exemple)
   * @return
   */
  public SnapshotGraph delaunayTriangulation(IEdgeWeighting edgesWeighting) {

    TriangulationJTS jts = new TriangulationJTS("");
    List<IFeature> listeFeatures = new ArrayList<IFeature>();
    for (GraphEntity v : this.getVertices()) {
      listeFeatures.add(new DefaultFeature(v.getGeometry().toGeoxGeometry()));
    }
    jts.importAsNodes(listeFeatures);
    try {
      jts.triangule();
    } catch (Exception e) {
      e.printStackTrace();
    }

    SnapshotGraph gg = SnapshotIOManager.topo2Snapshot(jts, edgesWeighting,
        null);

    jts = null;

    return gg;
  }
}
