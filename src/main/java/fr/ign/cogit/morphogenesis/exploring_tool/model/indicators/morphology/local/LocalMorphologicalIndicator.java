package fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.local;

import fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.AbstractIndicator;

/**
 * Indicateur morphologique local (ex: centralité de proximité, orientation des
 * arcs,etc.) ATTENTION: certains indicateurs locaux peuvent s'interpréter
 * globalement (ex : centralité intermédiaire) ou localement (ex: centarlité de
 * degré)
 * @author bcostes
 * 
 */
public abstract class LocalMorphologicalIndicator extends AbstractIndicator {

  /**
   * true si indicateur calculé uniquement sur les arcs d'un graphe. Faux par
   * défaut
   */
  protected boolean edgesOnly;

  public LocalMorphologicalIndicator() {
    this.isGlobal = false;
    this.edgesOnly = false;
  }

  public void setEdgesOnly(boolean edgesOnly) {
    this.edgesOnly = edgesOnly;
  }

  public boolean isEdgesOnly() {
    return edgesOnly;
  }
}
