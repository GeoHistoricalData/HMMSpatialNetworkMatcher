# HMMSpatialNetworkMatcher

Contributors : Benoit Costes, Julien Perret.

This project provides :
* a library for matching data based on Hidden Markov Models
* the Spatial Network Matcher, an implementation of the library for matching linear spatial networks such as roads, streets, rivers, railways.

## Needs
Java 8, Maven.

## Specificities

The provided implementation only works with connected linear networks loaded from ESRI Shapefiles.

# Quickstart


Here is an example of HMMSpatialNetworkMatcher launcher :

```Java
// First network
String fileNetwork1 ="/home/bcostes/Documents/IGN/these/donnees/vecteur/filaires/filaires_corriges"
        + "/verniquet_l93_utf8_corr.shp";
    
// Second network
String fileNetwork2 ="/home/bcostes/Documents/IGN/these/donnees/vecteur/filaires/filaires_corriges"
        + "/jacoubet_l93_utf8.shp";
    
// Emission probability stratregy
// If you want to use more than one criteria, use CompositeEmissionProbability to wrap them
CompositeEmissionProbabilityStrategy epStrategy = new CompositeEmissionProbabilityStrategy();
epStrategy.add(new LineMedianDIstanceEmissionProbability(), 1.);
epStrategy.add(new FrechetEmissionProbability(), 1.);
epStrategy.add(new DirectionDifferenceEmissionProbability(), 1.);
    
// Transition probability Strategy
ITransitionProbabilityStrategy tpStrategy = new AngularTransitionProbability();
    
// How to build the paths of the HMM ?
PathBuilder pathBuilder = new StrokePathBuilder();

// How to manage unexpected matched entities ?
PostProcessStrategy postProcressStrategy = new OptimizationPostStratregy();
    
// Parameters of the algorithm
ParametersSet.get().SELECTION_THRESHOLD = 30;
ParametersSet.get().NETWORK_PROJECTION = false;
ParametersSet.get().PATH_MIN_LENGTH = 5;
    
// Launcher
HMMMatchingLauncher matchingLauncher = new HMMMatchingLauncher(fileNetwork1, fileNetwork2, epStrategy,
        tpStrategy, pathBuilder, postProcressStrategy);

// Execute the HMM matching algorithm
matchingLauncher.lauchMatchingProcess();
    
// Export result
matchingLauncher.exportMatchingResults("/home/bcostes/Bureau/test");
```


