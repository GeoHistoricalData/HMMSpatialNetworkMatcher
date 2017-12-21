package fr.ign.cogit.v2.indicators.global;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IEnvelope;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.v2.indicators.IGlobalIndicator;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;

/**
 * ration longueur du graphe / surface étudiée
 * @author bcostes
 * 
 * @param <V>
 * @param <E>
 */
public class Density extends IGlobalIndicator {

  public Density() {
    this.name = "Density";
  }

  @Override
  public double calculate(JungSnapshot graph) {

    IPopulation<IFeature> pop = new Population<IFeature>();
    for (GraphEntity v : graph.getVertices()) {
      pop.add(new DefaultFeature(((GraphEntity) v).getGeometry()
          .toGeoxGeometry()));
    }
    IEnvelope env = pop.envelope();
    double lon = (new TotalEdgeLength()).calculate(graph);
    double result  = lon / env.getGeom().area();
    pop.clear();
    pop = null;
    return result;
  }
}
