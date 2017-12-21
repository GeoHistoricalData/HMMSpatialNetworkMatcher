package fr.ign.cogit.morphogenesis.network.utils.topologie;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IEnvelope;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Face;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class DetectionVoiesMultiples {

  /**
   * @param args
   */
  public static void main(String[] args) {

    String input = "/media/Data/Benoit/these/donnees/vecteur/filaires/V2_corrections_topologieOk/start_deletemeWhenDone/poubelle/v2_sans_voies_multiples/poubelle_l93.shp";
    String output = "/home/bcostes/Bureau/voies.shp";

    IPopulation<IFeature> pop = ShapefileReader.read(input);
    CarteTopo topo = new CarteTopo("");
    IPopulation<Arc> arcs = topo.getPopArcs();
    for (IFeature feature : pop) {
      Arc arc = arcs.nouvelElement();
      try {
        GM_LineString line = new GM_LineString(feature.getGeom().coord());
        arc.setGeometrie(line);
        arc.addCorrespondant(feature);
      } catch (ClassCastException e) {
        e.printStackTrace();
      }
    }
    topo.creeTopologieArcsNoeuds(0);
    topo.creeNoeudsManquants(0);
    topo.rendPlanaire(0);
    topo.creeTopologieFaces();

    IFeatureCollection<IFeature> col = new Population<IFeature>();
    for (Face f : topo.getListeFaces()) {
      IEnvelope env = f.getGeom().envelope();
      if (env.width() > 3 * env.length() || env.length() > 3 * env.width()) {
        col.add(f);
      }
    }

    ShapefileWriter.write(col, output);

  }

}
