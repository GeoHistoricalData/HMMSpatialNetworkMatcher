package fr.ign.cogit.HMMSpatialNetworkMatcher.impl.pathbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservationCollection;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.Path;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.PathBuilder;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.Observation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.ObservationPopulation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.core.ParametersSet;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;


/**
 * Generates pseudo random paths that cover the whole network
 * Each edge is likely to be used multiple time ....
 * Each path is not exactly random : we try to guide the process by weighting the random selection of edges
 * by the distance bewteen initial and final nodes
 * TODO : does not work for undirected graph
 * @author bcostes
 *
 */
public class RandomPathBuilder implements PathBuilder{

  @Override
  public List<Path> buildPaths(IObservationCollection observations) {
    if(!(observations instanceof ObservationPopulation)) {
      throw new RuntimeException("observations type must extends ObservationPopulation to"
          + "compute random paths");
    }

    ObservationPopulation obsPop = (ObservationPopulation) observations;

    // graph generation
    UndirectedSparseMultigraph<IDirectPosition, Observation> graph = new UndirectedSparseMultigraph<>();
    for(Observation o : obsPop) {
      graph.addEdge(o, o.getGeom().startPoint(), o.getGeom().endPoint());
    }


    List<Path> result = new ArrayList<>();

    Random r = new Random();

    Stack<Observation> remainingEdges = new Stack<>();
    remainingEdges.addAll(graph.getEdges());
    List<Observation> edges = new ArrayList<>(graph.getEdges());
    while(!remainingEdges.isEmpty()) {
      
      Observation e1 = remainingEdges.pop();
      IDirectPosition p1 = e1.getGeom().startPoint();
      IDirectPosition backupIni = p1;

      // random choice of second node
      while(true) {
        Observation e2 = edges.get(r.nextInt(edges.size()));
        if(e1.equals(e2)) {
          continue;
        }
        IDirectPosition p2 = e2.getGeom().startPoint();
        List<IObservation> p = new LinkedList<>();

        // p1 neighbors
        List<Observation> incidents = new ArrayList<>(graph.getIncidentEdges(p1));
        List<org.apache.commons.math3.util.Pair<Observation,Double>> itemWeights = new ArrayList<>();
        Map<Observation, Double> distances = new HashMap<Observation,Double>();
        for(Observation possibleNext : incidents){
          IDirectPosition other = graph.getOpposite(p1, possibleNext);
          double d= other.distance(p2);
          distances.put(possibleNext, d);
        }
        double dmin = Double.MAX_VALUE;
        for(Double d: distances.values()){
          if(d < dmin){
            dmin = d;
          }
        }
        for(Observation possibleNext : distances.keySet()){
          if(distances.get(possibleNext) == 0){
            itemWeights.add(new Pair<>(possibleNext, Double.MAX_VALUE));
          }
          else{
            itemWeights.add(new Pair<>(possibleNext, Math.exp(-distances.get(possibleNext) /dmin)));
          }
        }
        Observation nextEdge = new EnumeratedDistribution<Observation>(itemWeights).sample();
        IDirectPosition next = graph.getOpposite(p1, nextEdge);
        p.add(nextEdge);

        while(!next.equals(p2)){
          
          p1 = next;
          incidents = new ArrayList<>(graph.getIncidentEdges(p1));
          if(incidents.size() != 1){
            incidents.remove(nextEdge);
          }
          

          // we try to guide the process by weighting the random selection of edges
          // by the distance bewteen initial and final nodes
          itemWeights = new ArrayList<>();
          distances = new HashMap<Observation,Double>();
          for(Observation possibleNext : incidents){
            IDirectPosition other = graph.getOpposite(p1, possibleNext);
            double d= other.distance(p2);
            distances.put(possibleNext, d);
          }
          dmin = Double.MAX_VALUE;
          for(Double d: distances.values()){
            if(d < dmin){
              dmin = d;
            }
          }
          for(Observation possibleNext : distances.keySet()){
            if(distances.get(possibleNext) == 0){
              itemWeights.add(new Pair<>(possibleNext, Double.MAX_VALUE));
            }
            else{
              itemWeights.add(new Pair<>(possibleNext, Math.exp(-distances.get(possibleNext) /dmin)));
            }
          }
          nextEdge = new EnumeratedDistribution<Observation>(itemWeights).sample();

          //      random = new Random();
          //      r = random.nextInt(incidents.size());
          //      nextEdge = incidents.get(
          //          r );
          next = graph.getOpposite(p1, nextEdge);
          p.add(nextEdge);
        }
        
        if(!p.contains(e1)) {
          p.add(0,e1);
        }
        if(!p.contains(e2)) {
          p.add(e2);
        }
        
        if(p.size() < ParametersSet.get().PATH_MIN_LENGTH) {
          continue;
        }

        //on veut un chemin "simple"
        if((new HashSet<>(p)).size() != p.size()){
          // if the randm path contains the same edge multiple times, a quite complex operation is proceeded.
          // we consider the partial subgraph generated by the edges and vertices of the random path.
          // Then we keep the edges of the shortest path between p1 and p2.
          // This is NOT the same as considering the shortest path between p1 and p2 in the initial graph !


          // partial subgraph
          UndirectedSparseMultigraph<IDirectPosition, Observation> ssg = new UndirectedSparseMultigraph<>();
          for(IObservation a : (new HashSet<>(p))){
            ssg.addEdge((Observation)a, new edu.uci.ics.jung.graph.util.Pair<IDirectPosition>(((Observation)a).getGeom().startPoint(),
                ((Observation)a).getGeom().endPoint()));
          }
          Transformer<Observation, Double> wtTransformer = new Transformer<Observation, Double>() {
            @Override
            public Double transform(Observation a) {
              return a.getGeom().length();                
            }            
          };
          DijkstraShortestPath<IDirectPosition, Observation> sp = new DijkstraShortestPath<>(ssg, wtTransformer);
          // shortest path between p1 and p2 in the partial subgraph
          p.clear(); 
          p.addAll(sp.getPath(backupIni, p2));

        }
        if(p.size() < ParametersSet.get().PATH_MIN_LENGTH) {
          // the path is too small
          continue;
        }
        if(!p.contains(e1)) {
          p.add(0,e1);
        }
        if(!p.contains(e2)) {
          p.add(e2);
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