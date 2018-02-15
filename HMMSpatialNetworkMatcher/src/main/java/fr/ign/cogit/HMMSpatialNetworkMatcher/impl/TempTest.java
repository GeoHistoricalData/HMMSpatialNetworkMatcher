package fr.ign.cogit.HMMSpatialNetworkMatcher.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.Path;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.ep.HausdorffEmissionProbability;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.tp.AngularTransitionProbability;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.HmmMatchingIteration;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class TempTest {
  
  public static void ma(Collection<? extends IFeature> d) {
    
  }

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    
    String fileNetwork1 ="/home/bcostes/Documents/IGN/these/donnees/vecteur/filaires/filaires_corriges"
        + "/verniquet_l93_utf8_corr.shp";
    IPopulation<IFeature> inRef = ShapefileReader.read(fileNetwork1);

    CarteTopo netRef = new CarteTopo("");
    IPopulation<Arc> popArcRef = netRef.getPopArcs();

    for (IFeature f : inRef) {
      Arc a = popArcRef.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
    }

    netRef.creeTopologieArcsNoeuds(1);
    netRef.creeNoeudsManquants(1);
    netRef.rendPlanaire(1);
    netRef.filtreDoublons(1);
    netRef.filtreArcsNull(1);
    netRef.filtreArcsDoublons();
    netRef.filtreNoeudsSimples();

    IPopulation<Observation> observations = new Population<Observation>();
    IPopulation<HiddenState> states = new Population<HiddenState>();
    for(Arc a : netRef.getPopArcs()) {
      Observation obs = new Observation(a.getGeom());
      obs.setEmissionProbaStrategy(new HausdorffEmissionProbability());
      observations.add(obs);
    }
    for(Arc a : netRef.getPopArcs()) {
      HiddenState s = new HiddenState(a.getGeom());
      s.setTransitionProbaStrategy(new AngularTransitionProbability());
      states.add(s);
    }
    observations.initSpatialIndex(Tiling.class, true);
    states.initSpatialIndex(Tiling.class, true);

    List<IObservation> l = new ArrayList<IObservation>();
    l.addAll(observations);
    Path p = new Path(l);
    
    HiddenStatePopulation col = new HiddenStatePopulation();
    col.addAll(states);
    col.initSpatialIndex(Tiling.class, false);
    
    Collection<IHiddenState> ssss = new  ArrayList<IHiddenState>();
    HmmMatchingIteration hmmi = new HmmMatchingIteration(p,col);
    
    hmmi.match();
  }

}
