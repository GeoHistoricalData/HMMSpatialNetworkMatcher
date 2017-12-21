package fr.ign.cogit.morphogenesis.network.graph.matching;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.contrib.appariement.EnsembleDeLiens;
import fr.ign.cogit.geoxygene.contrib.appariement.Lien;
import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.AppariementIO;
import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.ParametresApp;
import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.topologie.ReseauApp;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class TestMatching {
  public static void main(String args[]) {

    IPopulation<IFeature> popRef = ShapefileReader
        .read("/media/Data/Benoit/these/analyses/centralites/FILAIRES_L93/1810_1836_VASSEROT.shp");
    IPopulation<IFeature> popComp = ShapefileReader
        .read("/media/Data/Benoit/these/analyses/centralites/FILAIRES_L93/1885_ALPHAND_POUBELLE_emprise.shp");

    IFeatureCollection<IFeature> inputFeatureCollectionCorrected1 = new Population<IFeature>();
    for (IFeature feat : popRef) {
      if (feat.getGeom() instanceof IMultiCurve) {
        for (int i = 0; i < ((IMultiCurve<?>) feat.getGeom()).size(); i++) {
          inputFeatureCollectionCorrected1.add(new DefaultFeature(
              ((IMultiCurve<?>) feat.getGeom()).get(i)));
        }
      } else {
        inputFeatureCollectionCorrected1
            .add(new DefaultFeature(feat.getGeom()));
      }
    }

    IFeatureCollection<IFeature> inputFeatureCollectionCorrected2 = new Population<IFeature>();
    for (IFeature feat : popComp) {
      if (feat.getGeom() instanceof IMultiCurve) {
        for (int i = 0; i < ((IMultiCurve<?>) feat.getGeom()).size(); i++) {
          inputFeatureCollectionCorrected2.add(new DefaultFeature(
              ((IMultiCurve<?>) feat.getGeom()).get(i)));
        }
      } else {
        inputFeatureCollectionCorrected2
            .add(new DefaultFeature(feat.getGeom()));
      }
    }

    ParametresApp paramApp = new ParametresApp();
    paramApp.populationsArcs1.add(inputFeatureCollectionCorrected1);
    paramApp.populationsArcs2.add(inputFeatureCollectionCorrected2);

    paramApp.distanceNoeudsMax = 20;
    paramApp.distanceNoeudsImpassesMax = 30;
    paramApp.distanceArcsMax = 20;
    paramApp.distanceArcsMin = 1;
    paramApp.topologieFusionArcsDoubles1 = true;
    paramApp.topologieFusionArcsDoubles2 = true;
    paramApp.topologieGraphePlanaire1 = true;
    paramApp.topologieGraphePlanaire2 = true;
    paramApp.varianteFiltrageImpassesParasites = true;
    paramApp.topologieSeuilFusionNoeuds1 = 2;
    paramApp.topologieSeuilFusionNoeuds2 = 2;

    paramApp.projeteNoeuds1SurReseau2 = true;
    paramApp.projeteNoeuds1SurReseau2DistanceNoeudArc = 20; // 25
    paramApp.projeteNoeuds1SurReseau2DistanceProjectionNoeud = 20; // 50
    paramApp.projeteNoeuds1SurReseau2ImpassesSeulement = false;

    paramApp.projeteNoeuds2SurReseau1 = true;
    paramApp.projeteNoeuds2SurReseau1DistanceNoeudArc = 20; // 25
    paramApp.projeteNoeuds2SurReseau1DistanceProjectionNoeud = 20; // 50
    paramApp.projeteNoeuds2SurReseau1ImpassesSeulement = false;

    paramApp.varianteForceAppariementSimple = true;

    /*
     * paramApp.varianteRedecoupageArcsNonApparies = false;
     * paramApp.varianteRedecoupageNoeudsNonApparies = true;
     * paramApp.varianteRedecoupageNoeudsNonApparies_DistanceProjectionNoeud =
     * 600; paramApp.varianteChercheRondsPoints = false;
     */

    List<ReseauApp> l = new ArrayList<ReseauApp>();

    EnsembleDeLiens liens = AppariementIO.appariementDeJeuxGeo(paramApp, l);
    liens.creeGeometrieDesLiensEntreLignesEtLignes();

    IFeatureCollection<IFeature> networkNotMached = new Population<IFeature>();
    b: for (IFeature fComp : inputFeatureCollectionCorrected2) {
      for (Lien lien : liens) {
        if (lien.getObjetsComp().contains(fComp)) {
          continue b;
        }
      }
      networkNotMached.add(fComp);
    }

    System.out.println(networkNotMached.size() + " / "
        + inputFeatureCollectionCorrected2.size());

    /*
     * IFeatureCollection<IFeature> col = new Population<IFeature>(); for (Lien
     * lien : liens) { if (lien.getGeom().coord().size() == 1) { continue; } for
     * (IFeature f1 : lien.getObjetsRef()) { for (IFeature f2 :
     * lien.getObjetsComp()) { IDirectPosition p1 =
     * Operateurs.milieu((ILineString) f1.getGeom()); IDirectPosition p2 =
     * Operateurs.milieu((ILineString) f2.getGeom());
     * 
     * IDirectPositionList list = new DirectPositionList(); list.add(p1);
     * list.add(p2); col.add(new DefaultFeature(new GM_LineString(list))); } } }
     */

    ShapefileWriter.write(networkNotMached,
        "/home/bcostes/Bureau/poubelle_notmatched.shp");
  }
}
