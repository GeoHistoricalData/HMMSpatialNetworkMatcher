package fr.ign.cogit.morphogenesis.network.analysis.indicators.isolated;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;

public class Orientation {

  public static double mesure(IGeometry geom) {

    ILineString arcEchantillone = Operateurs.echantillonePasVariable(
        new GM_LineString(geom.coord()), 10);
    // Calcul des directions principales des droites de regression des nuages de
    // points
    Angle angleArc = Operateurs.directionPrincipaleOrientee(arcEchantillone
        .coord());
    double angle = angleArc.getValeur();
    if (angle >= Math.PI) {
      angle = angle - Math.PI;
    }
    return angle;
  }

}
