package fr.ign.cogit.morphogenesis.exploring_tool.model.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ign.cogit.morphogenesis.exploring_tool.model.api.AbstractGraph;
import fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.IndicatorVisitorBuilder;
import fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.local.LocalMorphologicalIndicator;
import fr.ign.cogit.morphogenesis.exploring_tool.utils.Parameters;
import fr.ign.cogit.morphogenesis.exploring_tool.view.legend.Legend;
import fr.ign.cogit.morphogenesis.exploring_tool.view.legend.NodesLegend;

public class GraphLayer {

  protected int id;
  protected AbstractGraph<?, ?> g;
  protected Legend legendEdges; // légende globale d'affichage du graphe (arcs)
  protected NodesLegend legendNodes;// légende globale d'affichage du graphe
                                    // (noeuds)
  protected Map<String, Legend> legendsEdges; // légende pour chaque indicateur
                                              // (arcs)
  // (morpho local)
  // (noeuds)
  protected Map<String, NodesLegend> legendsNodes; // légende pour chaque
                                                   // indicateur (noeuds)
  // (morpho local)
  protected List<String> localMorphologicalIndicators;
  protected List<String> globalMorphologicalIndicators;
  private String drawable_entities = Parameters.EDGES_ONLY;

  public GraphLayer(AbstractGraph<?, ?> g) {
    this.g = g;
    this.id = g.getId();
    this.legendEdges = new Legend();
    this.legendNodes = new NodesLegend();
    this.localMorphologicalIndicators = new ArrayList<String>();
    this.globalMorphologicalIndicators = new ArrayList<String>();
    this.legendsNodes = new HashMap<String, NodesLegend>();
    this.legendsEdges = new HashMap<String, Legend>();
  }

  public AbstractGraph<?, ?> getG() {
    return g;
  }

  public void setG(AbstractGraph<?, ?> g) {
    this.g = g;
  }

  public void updateLocalMorphologicalIndicator(String centrality) {
    if (this.localMorphologicalIndicators.contains(centrality)) {
      return;
    }
    LocalMorphologicalIndicator indicator = IndicatorVisitorBuilder
        .createLocalMorphologicalIndicator(centrality);
    this.getG().accept(indicator);
    this.localMorphologicalIndicators.add(centrality);
    this.legendsEdges.put(centrality, new Legend());
    if (!indicator.isEdgesOnly()) {
      NodesLegend ll = new NodesLegend();
      ll.setPropotional(true);
      this.legendsNodes.put(centrality, ll);
    }
  }

  public void updateMorphologicalIndicator(String indicator) {
    if (this.globalMorphologicalIndicators.contains(indicator)) {
      return;
    }
    this.getG().accept(
        IndicatorVisitorBuilder.createGlobalMorphologicalIndicator(indicator));
    this.globalMorphologicalIndicators.add(indicator);
  }

  public List<String> getLocalMorphologicalIndicators() {
    return localMorphologicalIndicators;
  }

  public void setLocalMorphologicalIndicators(List<String> centralities) {
    this.localMorphologicalIndicators = centralities;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setLegendEdges(Legend legendEdges) {
    this.legendEdges = legendEdges;
  }

  public Legend getLegendEdges() {
    return this.legendEdges;
  }

  public NodesLegend getLegendNodes() {
    return this.legendNodes;
  }

  public void setLLegendNodes(NodesLegend legendNodes) {
    this.legendNodes = legendNodes;
  }

  public Map<String, NodesLegend> getLegendNodesIndicators() {
    return this.legendsNodes;
  }

  public Map<String, Legend> getLegendEdgesIndicators() {
    return this.legendsEdges;
  }

  public int getId() {
    return this.id;
  }

  public void setDrawableEntities(String drawable_entities) {
    this.drawable_entities = drawable_entities;
  }

  public String getDrawableEntities() {
    return drawable_entities;
  }

  public void delete() {
    this.localMorphologicalIndicators.clear();
    this.localMorphologicalIndicators = null;
    this.globalMorphologicalIndicators.clear();
    this.globalMorphologicalIndicators = null;
    this.g.delete();
    this.g = null;
  }

  public List<String> getGlobalMorphologicalIndicators() {
    return globalMorphologicalIndicators;
  }

  public void setGlobalMorphologicalIndicators(
      List<String> globalMorphologicalIndicators) {
    this.globalMorphologicalIndicators = globalMorphologicalIndicators;
  }

}
