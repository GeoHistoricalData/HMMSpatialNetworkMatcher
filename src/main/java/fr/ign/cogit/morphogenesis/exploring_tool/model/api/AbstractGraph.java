package fr.ign.cogit.morphogenesis.exploring_tool.model.api;

import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.AbstractIndicator;

public abstract class AbstractGraph<E, V> extends
    UndirectedSparseMultigraph<E, V> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  protected IFeatureCollection<IFeature> popFeatures;
  protected String name;
  protected String date;
  protected int id;
  protected Map<String, Double> globalMorphologicalIndicators;

  /**
   * Default Constructor
   */
  public AbstractGraph() {
    super();
    this.name = "";
    this.date = "";
    this.globalMorphologicalIndicators = new HashMap<String, Double>();
  }

  public void setPop(IFeatureCollection<IFeature> popFeatures) {
    this.popFeatures = popFeatures;
  }

  public IFeatureCollection<IFeature> getPop() {
    return popFeatures;
  }

  public abstract void accept(AbstractIndicator v);

  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getDate() {
    return date;
  }

  public void delete() {
    this.popFeatures.clear();
    this.popFeatures = null;
  }

  public Map<String, Double> getGlobalMorphologicalIndicators() {
    return globalMorphologicalIndicators;
  }

  public void setGlobalMorphologicalIndicators(
      Map<String, Double> morphologicalIndicators) {
    this.globalMorphologicalIndicators = morphologicalIndicators;
  }

  public abstract double[] getLocalMorphologicalIndicator(String ind);

}
