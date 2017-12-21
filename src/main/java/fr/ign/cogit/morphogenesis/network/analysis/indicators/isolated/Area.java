package fr.ign.cogit.morphogenesis.network.analysis.indicators.isolated;

import fr.ign.cogit.geoxygene.contrib.cartetopo.Face;

public class Area {
  public static double mesure(Face face) {
    return face.getSurface();
  }
}
