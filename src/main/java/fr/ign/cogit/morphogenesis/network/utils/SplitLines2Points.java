package fr.ign.cogit.morphogenesis.network.utils;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.morphogenesis.network.graph.io.LocalFeature;

public class SplitLines2Points {

  static String input = "/media/Data/Benoit/thèse/analyses/centralites/indicateurs/"
      + "1789_verniquet/betweeness_centrality/primalTopological_1789_cluster20.shp"; //$NON-NLS-N$

  static String output = "/media/Data/Benoit/thèse/analyses/centralites/indicateurs/"
      + "1789_verniquet/betweeness_centrality/segmentation/"
      + "primalTopological_1789_nocluster_points.shp"; //$NON-NLS-N$

  static String attribute = "INDICATOR"; //$NON-NLS-N$

  static int SPLIT_THRESOLD = 25;

  public static void main(String args[]) {

    IPopulation<IFeature> pop = ShapefileReader.read(input);
    IFeatureCollection<IFeature> col = new Population<IFeature>();

    for (IFeature feat : pop) {
      ILineString ls = Operateurs.echantillone(new GM_LineString(feat.getGeom()
          .coord()), SPLIT_THRESOLD);
      for (IDirectPosition pos : ls.getControlPoint()) {
        LocalFeature outFeat = new LocalFeature(new GM_Point(pos));
        outFeat.setFeatureType(feat.getFeatureType());
        outFeat.setAttribute(
            feat.getFeatureType().getFeatureAttributeByName(attribute),
            feat.getAttribute(attribute));
        col.add(outFeat);
      }
    }

    ShapefileWriter.write(col, output);

  }
}
