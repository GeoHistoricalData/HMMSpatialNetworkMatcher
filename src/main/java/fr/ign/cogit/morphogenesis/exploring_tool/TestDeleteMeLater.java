package fr.ign.cogit.morphogenesis.exploring_tool;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;

public class TestDeleteMeLater {

  /**
   * @param args
   */
  public static void main(String[] args) {

    /*
     * IFeature f = new DefaultFeature(new GM_Point(new DirectPosition(2.336524,
     * 48.836478))); IFeatureCollection<IFeature> col = new
     * Population<IFeature>(); col.add(f); ShapefileWriter.write(col,
     * "/home/bcostes/Bureau/test_georef_banlieue/obsWGS84.shp");
     */

    IDirectPositionList l = new DirectPositionList();
    l.add(new DirectPosition(0, 0));
    l.add(new DirectPosition(1, 1));
    l.add(new DirectPosition(2, 2));
    l.add(new DirectPosition(3, 3));

    ILineString line = new GM_LineString(l);

    System.out.println(line.reverse());
    line.setControlPoint(0, new DirectPosition(-1, -1));
    System.out.println(line.toString());

  }
}
