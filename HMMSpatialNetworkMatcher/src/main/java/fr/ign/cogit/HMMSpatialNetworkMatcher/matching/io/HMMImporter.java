package fr.ign.cogit.HMMSpatialNetworkMatcher.matching.io;

import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.HiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.HiddenStatePopulation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.Observation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.ObservationPopulation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.core.ParametersSet;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.util.Resampler;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class HMMImporter{
  
  private ObservationPopulation observations;
  private HiddenStatePopulation states;
  public ObservationPopulation getObservations() {
    return observations;
  }
  public void setObservations(ObservationPopulation observations) {
    this.observations = observations;
  }
  public HiddenStatePopulation getStates() {
    return states;
  }
  public void setStates(HiddenStatePopulation states) {
    this.states = states;
  }
  
  public void loadAndPrepareNetworks(String fileNetwork1, String fileNetwork2) {
    // lecture des SHP
    IPopulation<IFeature> inRef = ShapefileReader.read(fileNetwork1);
    IPopulation<IFeature> inComp = ShapefileReader.read(fileNetwork2);
    
    /*
     * Création des réseaux
     */
    CarteTopo netRef = new CarteTopo("ref");
    CarteTopo netComp = new CarteTopo("comp");
    IPopulation<Arc> popArcRef = netRef.getPopArcs();
    for (IFeature f : inRef) {
      Arc a = popArcRef.nouvelElement();
      a.setGeom(Resampler.resample(new GM_LineString(f.getGeom().coord()), 30.0));
    }
    IPopulation<Arc> popArcComp = netComp.getPopArcs();
    for (IFeature f : inComp) {
      Arc a = popArcComp.nouvelElement();
      a.setGeom(Resampler.resample(new GM_LineString(f.getGeom().coord()), 30.0));
    }
    netRef.creeTopologieArcsNoeuds(1);
    netRef.creeNoeudsManquants(1);
    netRef.rendPlanaire(1);
    netRef.filtreDoublons(1);
    netRef.filtreArcsNull(1);
    netRef.filtreArcsDoublons();
    netRef.filtreNoeudsSimples();

    netComp.creeTopologieArcsNoeuds(1);
    netComp.creeNoeudsManquants(1);
    netComp.rendPlanaire(1);
    netComp.filtreDoublons(1);
    netComp.filtreArcsNull(1);
    netComp.filtreArcsDoublons();
    netComp.filtreNoeudsSimples();
    
    if (ParametersSet.get().NETWORK_PROJECTION) {
      // Si demandé, on rééchantillonne en projetant les réseaux les uns sur les autres.
      // A éviter ...
      if (netRef.getPopArcs().size() > netComp.getPopArcs().size()) {
        netComp.projete(netRef, ParametersSet.get().SELECTION_THRESHOLD, ParametersSet.get().SELECTION_THRESHOLD,
            false);
      } else {
        netRef.projete(netComp, ParametersSet.get().SELECTION_THRESHOLD,ParametersSet.get().SELECTION_THRESHOLD,
            false);
      }
    }

    this.observations = new ObservationPopulation();
    this.states = new HiddenStatePopulation();
    for(Arc a : netRef.getPopArcs()) {
      Observation obs = new Observation(a.getGeometrie());
      obs.addCorrespondant(a);
      observations.add(obs);
    }
    for(Arc a : netComp.getPopArcs()) {
      HiddenState s = new HiddenState(a.getGeometrie());
      s.addCorrespondant(a);
      states.add(s);
    }
   this.observations.initSpatialIndex(Tiling.class, true);
   this.states.initSpatialIndex(Tiling.class, true);
   
  }
  

}
