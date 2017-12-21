package fr.ign.cogit.v2.indicators.local;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.io.SnapshotIOManager;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;
import fr.ign.cogit.v2.weightings.LengthEdgeWeighting;

public class ProximalBypass  extends ILocalIndicator {
  public ProximalBypass() {
    this.name = "PByPass";
  }

  @Override
  public Map<GraphEntity, Double> calculateNodeCentrality(JungSnapshot graph,
      boolean normalize) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<GraphEntity, Double> calculateEdgeCentrality(JungSnapshot graph,
      boolean normalize) {
    Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
    // ppc sur le réseau normal
    DijkstraShortestPath<GraphEntity, GraphEntity> path = new DijkstraShortestPath<GraphEntity, GraphEntity>(graph, graph.getEdgesWeights());
    //rayon proximal des arretes
    Map<GraphEntity, Set<Double>> proximalRadiusSet = new HashMap<GraphEntity, Set<Double>>();
    Stack<GraphEntity> nodes  = new Stack<GraphEntity>();
    nodes.addAll(graph.getVertices());
    while(!nodes.isEmpty()){
      GraphEntity node1 = nodes.pop();
      for(GraphEntity node2: nodes){
        List<GraphEntity> ppc = path.getPath(node1, node2);
        for(GraphEntity edge: ppc){
          //extrémité
          GraphEntity extremity1 = graph.getEndpoints(edge).getFirst();
          GraphEntity extremity2 = graph.getEndpoints(edge).getSecond();
          double d1 = Math.min(path.getDistance(extremity1, node1).doubleValue(), path.getDistance(extremity1, node2).doubleValue());
          double d2 = Math.min(path.getDistance(extremity2, node1).doubleValue(), path.getDistance(extremity2, node2).doubleValue());
          if(proximalRadiusSet.containsKey(edge)){
            proximalRadiusSet.get(edge).add(Math.min(d1, d2));
          }
          else{
            Set<Double> s = new HashSet<Double>();
            s.add(Math.min(d1,d2));
            proximalRadiusSet.put(edge, s);
          }
        }
      }
    }
    Map<GraphEntity, Double> proximalRadius = new HashMap<GraphEntity, Double>();
    for(GraphEntity e: proximalRadiusSet.keySet()){
      double somme = 0;
      for(Double d: proximalRadiusSet.get(e)){
        somme+= d;
      }
      proximalRadius.put(e, somme/((double)(proximalRadiusSet.get(e).size())));
      System.out.println(somme/((double)(proximalRadiusSet.get(e).size())));
    }
    System.out.println("proximal radius calculated");
    int cpt2=0;
    for(GraphEntity edge: graph.getEdges()){
      cpt2++;
      System.out.println(cpt2);
      if(!proximalRadius.containsKey(edge)){
        continue;
      }
      //rayon proximal de l'arrret
      double pRadius = proximalRadius.get(edge);
      //suppresion de l'arrete
      JungSnapshot newG = new JungSnapshot();
      for(GraphEntity e: graph.getEdges()){
        newG.addEdge(e,graph.getEndpoints(e));
      }
      newG.setEdgesWeights(graph.getEdgesWeights());
      newG.removeEdge(edge);
      // récupération des sommets proximaux des extrémités de l'arrete
      Set<GraphEntity> nodesPropximal = new HashSet<GraphEntity>();
      GraphEntity extremity1 = graph.getEndpoints(edge).getFirst();
      GraphEntity extremity2 = graph.getEndpoints(edge).getSecond();
      for(GraphEntity n: graph.getVertices()){
        if(path.getDistance(extremity1, n).doubleValue() <= pRadius || path.getDistance(extremity2, n).doubleValue() <= pRadius ){
          nodesPropximal.addAll(graph.getNeighbors(n));
        }
      }
      nodesPropximal.remove(extremity1);
      nodesPropximal.remove(extremity2);
      
      DijkstraShortestPath<GraphEntity, GraphEntity> path2 = new DijkstraShortestPath<GraphEntity, GraphEntity>(newG, newG.getEdgesWeights());
      //calcul des ppc
      double d =0;
      int cpt=0;
      nodes = new Stack<GraphEntity>();
      nodes.addAll(nodesPropximal);
      while(!nodes.isEmpty()){
        GraphEntity node1 = nodes.pop();
        for(GraphEntity node2: nodes){
          double d1 =path.getDistance(node1, node2).doubleValue();
          Number n = path2.getDistance(node1, node2);
          if(n== null){
            continue;
          }
          double d2 =n.doubleValue();
          d += d2 - d1;
          cpt++;
        }
      }
      d /= ((double)cpt);
      result.put(edge, d);
    }
    return result;
  }

  @Override
  public Map<GraphEntity, Double> calculateNeighborhoodEdgeCentrality(
      JungSnapshot graph, int k, boolean normalize) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<GraphEntity, Double> calculateNeighborhoodNodeCentrality(
      JungSnapshot graph, int k, boolean normalize) {
    // TODO Auto-generated method stub
    return null;
  }

  
  public static void main(String args[]){
    JungSnapshot snap= SnapshotIOManager.shp2Snapshot("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/construction/etape0/snapshot_1784.0_1791.0_edges.shp", new LengthEdgeWeighting(), null, false);
    
    ILocalIndicator i = new ProximalBypass();
    Map<GraphEntity, Double> r = i.calculateEdgeCentrality(snap, false);
    IPopulation<IFeature> out = new Population<IFeature>();
    for(GraphEntity g: r.keySet()){
      IFeature f = new DefaultFeature(g.getGeometry().toGeoxGeometry());
      AttributeManager.addAttribute(f, "bypass", r.get(g), "Double");
      out.add(f);
    }
    ShapefileWriter.write(out,"/home/bcostes/Bureau/bypassP.shp");
  }
}
