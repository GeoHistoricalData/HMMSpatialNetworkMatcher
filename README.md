# HMMSpatialNetworkMatcher

### Status
[![Build Status](https://travis-ci.org/GeoHistoricalData/HMMSpatialNetworkMatcher.png)](https://travis-ci.org/GeoHistoricalData/HMMSpatialNetworkMatcher)

Contributors : Benoit Costes, Julien Perret.

This project provides :
* a library for matching data based on a Hidden Markov Model
* the Spatial Network Matcher, an implementation of the library adapted for matching linear spatial networks such as roads, streets, rivers, railways.

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

// use parallelization or not    
boolean parallel = false;

// Launcher
HMMMatchingLauncher matchingLauncher = new HMMMatchingLauncher(fileNetwork1, fileNetwork2, epStrategy,
        tpStrategy, pathBuilder, postProcressStrategy, parallel);

// Execute the HMM matching algorithm
matchingLauncher.lauchMatchingProcess();
    
// Export result
matchingLauncher.exportMatchingResults("/home/bcostes/Bureau/test");
```
## Emission probability strategies
* HausdorffEmissionProbability computes Hausdorff distance
* FrechetEmissionProbability computes Frechet distance
* LineMedianDIstanceEmissionProbability computes median of the distances between each vertex of the lines and their closest point on the other line. It gives a median distance between two lines. The distances are only computed from the smallest line points to the longest line, to avoid biases due to a line much longer than the other
* DirectionDifferenceEmissionProbability computes global orientation difference betweeen lines.

## Transition probability strategies

* AngularTransitionProbability computes angles difference at junction points.

## Path builders

* StrokePathBuilder builds strokes based on Bin Jiang "every best fit" algorithm
* RandomShortestPathBuilder builds paths calculated as shortest paths between random couples of nodes
* RandomPathBuilder(boolean fixedLength) builds pseudo-randoms paths. If fixedLength == True, length of each path is exactly PATH_MIN_LENGTH. Facultative and False by default.

## Post process strategies

Are used to remove unexpected matched entities. Because the algorithme uses almost no parameters, almost every edge of the first network are matched at the end of the process. Use a post process strategy to keep only the best matches.

* OptimizationPostStratregy use lp optimization to determine matchings when n:m cardinality links occur.
* ReverseHMMPostStrategy match network1 with network2, then network2 with network1 and only keeps links that exist in both matchings.

## Parameters

* SELECTION_THRESHOLD : geometrical threshold used to speed up the process by selecting only matching candidates that are closer than the threshold.
* NETWORK_PROJECTION : resample the networks by projecting nodes of network 1 on the edges of network 2 and reciprocally.
* PATH_MIN_LENGTH : only usefull if RandomShortestPathBuilder or RandomPathBuilder is used. Determine the minimum size of generated paths in terms of number of edges.

## Exporting results

Calling exportMatchingResults generates two files in provided directory : 
* xxx.shp contains matching links
* xxx_simplified.shp contains matching links filtered by the post process strategy.
