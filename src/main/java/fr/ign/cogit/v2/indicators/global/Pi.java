package fr.ign.cogit.v2.indicators.global;

import fr.ign.cogit.v2.indicators.IGlobalIndicator;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

/**
 * ratio longueur du graphe / diametre Faible valeur de Pi => réseau peu
 * developpé
 * @author bcostes
 * 
 */
public class Pi extends IGlobalIndicator {

  public Pi() {
    this.name = "Pi";
  }

  @Override
  public double calculate(JungSnapshot graph) {
    double diameter = (new Diameter()).calculate(graph);
    double lon = (new TotalEdgeLength()).calculate(graph);
    return lon / diameter;
  }

}
