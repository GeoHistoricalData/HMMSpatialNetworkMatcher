package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.pathbuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservationCollection;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.Path;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.PathBuilder;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.FeatObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.ObservationPopulation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.ParametersSet;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;

/**
 * Path are built by selecting random couples of nodes and calculatinf shortest path
 * between them.
 * Each edge is likely to be on several paths (probably a function of its betweenness centrality).
 * TODO : does not work yet of unconnected graph
 * @author bcostes
 *
 */
public class RandomShortestPathBuilder implements PathBuilder{

  @Override
  public List<Path> buildPaths(IObservationCollection observations) {
    if(!(observations instanceof ObservationPopulation)) {
      throw new RuntimeException("observations type must extends ObservationPopulation to"
          + "compute random paths");
    }

    List<Path> result = new ArrayList<>();

    ObservationPopulation obsPop = (ObservationPopulation) observations;

    // graph generation
    UndirectedSparseMultigraph<IDirectPosition, FeatObservation> graph = new UndirectedSparseMultigraph<>();
    for(FeatObservation o : obsPop) {
      graph.addEdge(o, o.getGeom().startPoint(), o.getGeom().endPoint());
    }

    Stack<FeatObservation> remainingEdges = new Stack<>();
    remainingEdges.addAll(graph.getEdges());
    List<FeatObservation> edges = new ArrayList<>(graph.getEdges());

    Random r = new Random();

    Transformer<FeatObservation, Double> wtTransformer = new Transformer<FeatObservation, Double>() {
      @Override
      public Double transform(FeatObservation a) {
        return a.getGeom().length();                
      }            
    };
    DijkstraShortestPath<IDirectPosition, FeatObservation> sp = new DijkstraShortestPath<>(graph, wtTransformer);


    while(!remainingEdges.isEmpty()) {
      FeatObservation e1 = remainingEdges.pop();
      IDirectPosition p1 = e1.getGeom().startPoint();
      // random choice of second node
      while(true) {
        FeatObservation e2 = edges.get(r.nextInt(edges.size()));
        if(e1.equals(e2)) {
          continue;
        }
        IDirectPosition p2 = e2.getGeom().startPoint();
        List<IObservation> p = new LinkedList<>();
        p.addAll(sp.getPath(p1, p2));
        if(!p.contains(e1)) {
          p.add(0, e1);
        }
        if(!p.contains(e2)) {
          p.add(e2);
        }

        // shortest path
        if(p.size() < ParametersSet.get().PATH_MIN_LENGTH) {
          continue;
        }

        Path path = new Path(p);
        result.add(path);
        remainingEdges.removeAll(p);
        break;
      }
    }


    return result;
  }
  
  
  
 
}