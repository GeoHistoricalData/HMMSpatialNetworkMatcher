package fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.global;

import fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.AbstractIndicator;

/**
 * Indicateur mrophologique global (ex: coefficient de clustering d'un graphe,
 * degré moyen, etc.)
 * @author bcostes
 * 
 */
public abstract class GlobalMorphologicalIndicator extends AbstractIndicator {

  public GlobalMorphologicalIndicator() {
    this.isGlobal = true;
  }

}
