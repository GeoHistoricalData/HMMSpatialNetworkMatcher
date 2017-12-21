package fr.ign.cogit.morphogenesis.network.strokes.binJiang;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class StrokesBuilderRun {

  public static void main(String args[]) {

    IPopulation<IFeature> features = ShapefileReader
        .read("/media/Data/Benoit/these/analyses/centralites/FILAIRES_L93/1885_ALPHAND_POUBELLE_emprise.shp");

    IFeatureCollection<IFeature> inputFeatureCollectionCorrected = new Population<IFeature>();
    // On simplifie les multi line string et on passe par des localFeature
    // pour conserver les attributs
    for (IFeature feat : features) {
      if (feat.getGeom() instanceof IMultiCurve) {
        for (int i = 0; i < ((IMultiCurve<?>) feat.getGeom()).size(); i++) {
          inputFeatureCollectionCorrected.add(new DefaultFeature(
              ((IMultiCurve<?>) feat.getGeom()).get(i)));
        }
      } else {
        inputFeatureCollectionCorrected.add(new DefaultFeature(feat.getGeom()));
      }
    }

    List<ILineString> linesString = new ArrayList<ILineString>();
    for (IFeature f : inputFeatureCollectionCorrected) {
      linesString.add((ILineString) f.getGeom());
    }
    List<ILineString> strokes = StrokesBuilder.buildStroke(linesString,
        Math.PI / 3., 1, 1);

    IPopulation<IFeature> col = new Population<IFeature>();
    for (ILineString l : strokes) {
      col.add(new DefaultFeature(l));
    }

    ShapefileWriter
        .write(
            col,
            "/media/Data/Benoit/these/analyses/centralites/strokes_L93_ebf/1885_ALPHAND_POUBELLE_emprise2.shp");

  }
}
