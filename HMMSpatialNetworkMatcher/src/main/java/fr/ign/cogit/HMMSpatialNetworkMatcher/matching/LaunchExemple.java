package fr.ign.cogit.HMMSpatialNetworkMatcher.matching;

import java.util.Collection;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.CompositeEmissionProbabilityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.ep.DirectionDifferenceEmissionProbability;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.ep.FrechetEmissionProbability;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.ep.LineMedianDIstanceEmissionProbability;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.pathbuilder.RandomShortestPathBuilder;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.pathbuilder.StrokePathBuilder;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.tp.AngularTransitionProbability;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.core.ParametersSet;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.postProcessStrategy.OptimizationPostStratregy;
import fr.ign.cogit.geoxygene.api.feature.IFeature;

public class LaunchExemple {
  
  public static void ma(Collection<? extends IFeature> d) {
    
  }

  public static void main(String[] args) {
    // TODO Auto-generated method stub

      
    
    
    
    
    Logger.getRootLogger().setLevel(Level.OFF);
    //test
    
    String fileNetwork1 ="/home/bcostes/Documents/IGN/these/donnees/vecteur/filaires/filaires_corriges"
        + "/verniquet_l93_utf8_corr.shp";
    
    
    String fileNetwork2 ="/home/bcostes/Documents/IGN/these/donnees/vecteur/filaires/filaires_corriges"
        + "/jacoubet_l93_utf8.shp";
    
    CompositeEmissionProbabilityStrategy epStrategy = new CompositeEmissionProbabilityStrategy();
    epStrategy.add(new LineMedianDIstanceEmissionProbability(), 1.);
    epStrategy.add(new FrechetEmissionProbability(), 1.);
    epStrategy.add(new DirectionDifferenceEmissionProbability(), 1.);
    
    HMMMatchingLauncher matchingLauncher = new HMMMatchingLauncher(fileNetwork1, fileNetwork2, epStrategy,
        new AngularTransitionProbability(), new StrokePathBuilder(), new OptimizationPostStratregy());
    ParametersSet.get().SELECTION_THRESHOLD = 30;
    ParametersSet.get().PATH_MIN_LENGTH = 5;

    matchingLauncher.lauchMatchingProcess();
    matchingLauncher.exportMatchingResults("/home/bcostes/Bureau/test");
  }

}
