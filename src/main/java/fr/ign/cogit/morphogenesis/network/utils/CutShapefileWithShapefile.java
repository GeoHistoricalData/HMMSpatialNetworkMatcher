package fr.ign.cogit.morphogenesis.network.utils;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class CutShapefileWithShapefile {

  static String shp2cut = "/media/Data/Benoit/these/analyses/centralites/FILAIRES_L93/1999_GEOROUTE.shp";
  static String shpBoundary = "/home/bcostes/Bureau/aaa.shp";

  static String shpOutput = "/media/Data/Benoit/these/analyses/centralites/FILAIRES_L93/1999_GEOROUTE_emprise.shp";

  public static void main(String[] args) {

    IPopulation<IFeature> pop2Cut = ShapefileReader.read(shp2cut);
    IPopulation<IFeature> popBoundary = ShapefileReader.read(shpBoundary);
    IPopulation<IFeature> ouput = new Population<IFeature>();

    // popBoundary.getGeomAggregate().convexHull();
    IGeometry boundary = popBoundary.get(0).getGeom();
    for (IFeature feat : pop2Cut) {
      if (boundary.contains(feat.getGeom())) {
        ouput.add(feat);
      } /*
         * else if (boundary.intersects(feat.getGeom())) { ouput.add(new
         * DefaultFeature(boundary.intersection(feat.getGeom()))); }
         */
    }

    ShapefileWriter.write(ouput, shpOutput);

  }
}
