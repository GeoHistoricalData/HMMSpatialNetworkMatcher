package fr.ign.cogit.morphogenesis.generation.barthelemy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IAggregate;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;

public class GeometryUtils {

  public static List<IDirectPosition> projection(IDirectPosition dp,
      IAggregate<IGeometry> aggr) {

    List<IDirectPosition> result = new ArrayList<IDirectPosition>();

    Iterator<IGeometry> itComposants = aggr.getList().iterator();
    IDirectPosition pt = null;
    boolean geomOK;
    while (itComposants.hasNext()) {
      IGeometry composant = itComposants.next();
      geomOK = false;
      if (composant instanceof ILineString) {
        pt = Operateurs.projection(dp, (ILineString) composant);
        geomOK = true;
      }
      if (!geomOK) {
        System.out.println("Projection - Type de géométrie non géré: "
            + composant.getClass());
        continue;
      }
      if (!result.contains(pt)) {
        result.add(pt);
      }
    }
    return result;
  }

}
