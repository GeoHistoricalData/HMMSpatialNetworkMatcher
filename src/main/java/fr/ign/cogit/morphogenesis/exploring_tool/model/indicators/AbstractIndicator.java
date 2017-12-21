package fr.ign.cogit.morphogenesis.exploring_tool.model.indicators;

import fr.ign.cogit.morphogenesis.exploring_tool.model.api.GeometricalGraph;

public abstract class AbstractIndicator {

  /**
   * indicateur de graphe ou d'éléments de graphe
   */
  protected boolean isGlobal;

  public abstract void calculate(GeometricalGraph g);

  public boolean isGlobalIndicator() {
    return this.isGlobal;
  }

}
