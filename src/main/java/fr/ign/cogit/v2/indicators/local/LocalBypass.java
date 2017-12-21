package fr.ign.cogit.v2.indicators.local;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
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

public class LocalBypass  extends ILocalIndicator {
  public LocalBypass() {
    this.name = "LByPass";
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
    for(GraphEntity edge: graph.getEdges()){
      //suppresion de l'arrete
      JungSnapshot newG = new JungSnapshot();
      for(GraphEntity e: graph.getEdges()){
        newG.addEdge(e,graph.getEndpoints(e));
      }
      newG.setEdgesWeights(graph.getEdgesWeights());
      newG.removeEdge(edge);
      // récupération des sommets voisins immédiats des extrémités de l'arrete
      Set<GraphEntity> nodesP = new HashSet<GraphEntity>();
      nodesP.addAll(graph.getNeighbors(graph.getEndpoints(edge).getFirst()));
      nodesP.addAll(graph.getNeighbors(graph.getEndpoints(edge).getSecond()));
      nodesP.remove(graph.getEndpoints(edge).getFirst());
      nodesP.remove(graph.getEndpoints(edge).getSecond());
      DijkstraShortestPath<GraphEntity, GraphEntity> path2 = new DijkstraShortestPath<GraphEntity, GraphEntity>(newG, newG.getEdgesWeights());
      //calcul des ppc
      double d =0;
      int cpt=0;
      Stack<GraphEntity>nodes = new Stack<GraphEntity>();
      nodes.addAll(nodesP);
      while(!nodes.isEmpty()){
        GraphEntity node1 = nodes.pop();
        for(GraphEntity node2: nodes){
          if(node1.equals(node2)){
            continue;
          }
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
    JungSnapshot snap= SnapshotIOManager.shp2Snapshot("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/construction/etape3/snapshot_1870.0_1872.0_edges.shp", new LengthEdgeWeighting(), null, false);
    
    
    EdgeBetweennessClusterer<GraphEntity, GraphEntity>c = new EdgeBetweennessClusterer<GraphEntity, GraphEntity>(500);
    Set<Set<GraphEntity>> v = c.transform(snap);
    IPopulation<IFeature> out = new Population<IFeature>();
    int cpt =0;
    for(Set<GraphEntity> s: v){
      for(GraphEntity vv: s){
      IFeature f = new DefaultFeature(vv.getGeometry().toGeoxGeometry());
      AttributeManager.addAttribute(f, "cluster", cpt, "Integer");
      out.add(f);
      }
      cpt++;
    }
    ShapefileWriter.write(out,"/home/bcostes/Bureau/weak_cluster.shp");
    
//    ILocalIndicator i = new LocalBypass();
//    Map<GraphEntity, Double> r = i.calculateEdgeCentrality(snap, false);
//    IPopulation<IFeature> out = new Population<IFeature>();
//    for(GraphEntity g: r.keySet()){
//      IFeature f = new DefaultFeature(g.getGeometry().toGeoxGeometry());
//      AttributeManager.addAttribute(f, "bypass", r.get(g), "Double");
//      out.add(f);
//    }
//    ShapefileWriter.write(out,"/home/bcostes/Bureau/bypass2.shp");
  }
}
