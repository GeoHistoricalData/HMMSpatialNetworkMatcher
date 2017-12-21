package hmmmatching.impl;

import java.util.Arrays;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.index.Tiling;

/**
 * TODO : ne marche que si les réseaux sont connexes
 * 
 * @author bcostes
 */
public class HMMMatchingPreProcess {

  public HMMMatchingPreProcess() {
  }

  public void loadAndPrepareNetworks(CarteTopo netRef, String fileNetwork1, CarteTopo netComp, String fileNetwork2, HMMParameters parameters) {
    /*
     * Lectures des shapsfiles
     */
    IPopulation<IFeature> inRef = ShapefileReader.read(fileNetwork1);
    IPopulation<IFeature> inComp = ShapefileReader.read(fileNetwork2);
    /*
     * Création des réseaux
     */
    CarteTopo netRefTmp = new CarteTopo("ref");
    CarteTopo netCompTmp = new CarteTopo("comp");
    IPopulation<Arc> popArcRef = netRefTmp.getPopArcs();
    for (IFeature f : inRef) {
      Arc a = popArcRef.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      a.setCorrespondants(Arrays.asList(f));
    }
    IPopulation<Arc> popArcComp = netCompTmp.getPopArcs();
    for (IFeature f : inComp) {
      Arc a = popArcComp.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      a.setCorrespondants(Arrays.asList(f));
    }
    netRefTmp.creeTopologieArcsNoeuds(1);
    netRefTmp.creeNoeudsManquants(1);
    netRefTmp.rendPlanaire(1);
    netRefTmp.filtreDoublons(1);
    netRefTmp.filtreArcsNull(1);
    netRefTmp.filtreArcsDoublons();
    netRefTmp.filtreNoeudsSimples();

    netCompTmp.creeTopologieArcsNoeuds(1);
    netCompTmp.creeNoeudsManquants(1);
    netCompTmp.rendPlanaire(1);
    netCompTmp.filtreDoublons(1);
    netCompTmp.filtreArcsNull(1);
    netCompTmp.filtreArcsDoublons();
    netCompTmp.filtreNoeudsSimples();
    // netRef = new CarteTopo("ref");
    // netComp = new CarteTopo("comp");
    popArcRef = netRef.getPopArcs();
    for (Arc f : netRefTmp.getPopArcs()) {
      Arc a = popArcRef.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      a.setCorrespondants(f.getCorrespondants());
    }
    popArcComp = netComp.getPopArcs();
    for (Arc f : netCompTmp.getPopArcs()) {
      Arc a = popArcComp.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      a.setCorrespondants(f.getCorrespondants());
    }
    netRef.creeNoeudsManquants(0);
    netRef.creeTopologieArcsNoeuds(1);
    netComp.creeNoeudsManquants(0);
    netComp.creeTopologieArcsNoeuds(1);
    netRefTmp = null;
    netCompTmp = null;

    if (parameters.resampling) {
      // Si demandé, on rééchantillonne en projetant les réseaux les uns sur les autres.
      // A éviter ...
      if (netRef.getPopArcs().size() > netComp.getPopArcs().size()) {
        netComp.projete(netRef, parameters.selection, parameters.selection, false);
      } else {
        netRef.projete(netComp, parameters.selection, parameters.selection, false);
      }
    }
    netRef.getPopArcs().initSpatialIndex(Tiling.class, false);
    for (Arc a : netRef.getPopArcs()) {
      a.setPoids(a.longueur());
      a.setOrientation(2);
    }
    netComp.getPopArcs().initSpatialIndex(Tiling.class, false);
    for (Arc a : netComp.getPopArcs()) {
      a.setPoids(a.longueur());
      a.setOrientation(2);
    }
  }
}
