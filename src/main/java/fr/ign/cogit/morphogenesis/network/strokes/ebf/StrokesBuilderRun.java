package fr.ign.cogit.morphogenesis.network.strokes.ebf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
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
        .read("/media/Data/Benoit/these/donnees/vecteur/FILAIRES_L93_OK/1810_1836_VASSEROT_accentsOK_topologieOk.shp");

    IPopulation<IFeature> inputFeatureCollectionCorrected = new Population<IFeature>();
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

    Map<ILineString, List<IFeature>> mapping = new HashMap<ILineString, List<IFeature>>();
    List<ILineString> strokes = StrokesBuilder.buildStroke(
        inputFeatureCollectionCorrected, mapping, Math.PI / 3., Math.PI / 3, 1,
        1);

    IPopulation<IFeature> col = new Population<IFeature>();
    for (ILineString l : strokes) {
      DefaultFeature df = new DefaultFeature(l);
      col.add(df);
    }

    ShapefileWriter
        .write(col,
            "/media/Data/Benoit/these/donnees/vecteur/STROKES_L93/vasserot_strokes.shp");

  }
}
