package fr.ign.cogit.v2.indicators.sensibility;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;
import fr.ign.cogit.v2.io.SnapshotIOManager;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;
import fr.ign.cogit.v2.weightings.LengthEdgeWeighting;

public class SubsetBetweennessCentrality extends ISubsetIndicator{
    public SubsetBetweennessCentrality() {
        this.name = "Betw";
    }
    @Override
    public Map<GraphEntity, Double> calculateSubsetNodeCentrality(
            JungSnapshot graph1, JungSnapshot graph2, boolean normalize) {
        
      return null;
    }

    @Override
    public Map<GraphEntity, Double> calculateSubsetEdgeCentrality(
            JungSnapshot graph1, JungSnapshot graph2, boolean normalize) {
        // TODO Auto-generated method stub
      //on établi la correspondance entre les éléments
        if(graph1.getEdgeCount() < graph2.getEdgeCount()){
            return this.calculateSubsetEdgeCentrality(graph2, graph1, normalize);
        }
        //snap1 doit contenir snap2
        //mapping entre sommets de snap2 et de snap1
        Map<GraphEntity, GraphEntity> mappingDirect = new HashMap<GraphEntity, GraphEntity>();
        //mapping entre arcs de snap1 et de snap2
        Map<GraphEntity, GraphEntity> mappingIndirect = new HashMap<GraphEntity, GraphEntity>();

        for(GraphEntity v2: graph2.getVertices()){
            //récupération de la géométrie
            IDirectPosition p2 = (IDirectPosition)v2.getGeometry().toGeoxGeometry().coord().get(0);
            for(GraphEntity v1: graph1.getVertices()){
                IDirectPosition p1 =  (IDirectPosition)v1.getGeometry().toGeoxGeometry().coord().get(0);
                if(p1.equals(p2,0.05)){
                    //c'est bon
                    mappingDirect.put(v2, v1);
                    break;
                }

            }
        }
        
        for(GraphEntity e1: graph1.getEdges()){
            //récupération de la géométrie
            ILineString l1 = (ILineString)e1.getGeometry().toGeoxGeometry();
            for(GraphEntity e2: graph2.getEdges()){
                ILineString l2 =  (ILineString)e2.getGeometry().toGeoxGeometry();
                if(l1.startPoint().equals(l2.startPoint()) && l1.endPoint().equals(l2.endPoint())
                         || l1.startPoint().equals(l2.endPoint()) && l1.endPoint().equals(l2.startPoint())){
                    if(l1.buffer(0.005).contains(l2)){
                        //c'est bon
                        mappingIndirect.put(e1, e2);
                        break;
                    }
                }

            }
        }
        
        
        Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
        for (GraphEntity v : graph2.getEdges()) {
          result.put(v, 0.);
        }
        DijkstraShortestPath<GraphEntity, GraphEntity> ppc = new DijkstraShortestPath<GraphEntity, GraphEntity>(graph1, graph1.getEdgesWeights());
   
        Stack<GraphEntity> stack = new Stack<GraphEntity>();
        stack.addAll(graph2.getVertices());
        while (!stack.isEmpty()) {
          // pour chaque sommet v1
          GraphEntity v1 = stack.pop();
          GraphEntity vX1 = mappingDirect.get(v1);
          for (GraphEntity v2 : stack) {
              GraphEntity vX2 = mappingDirect.get(v2);
            // pour chaque sommet v2 non déja traité
            // on récupère tous les ppc entre v1 et v2
            Set<GraphEntity> pathX= new HashSet<GraphEntity>(ppc.getPath(vX1, vX2));
              for (GraphEntity eX : pathX) {
                  if(mappingIndirect.containsKey(eX) && mappingIndirect.get(eX) != null){
                      GraphEntity e = mappingIndirect.get(eX);
                    // pour chaque sommet sur le ppc entre v1 et v2,
                      result.put(e, result.get(e) + 1. / 1.);
                  }
              }
            }
          ppc.reset(vX1);
          }
        
        if(normalize){
            for (GraphEntity w : graph2.getEdges()) {
                result.put(w, 2.*result.get(w)  / (((double) graph2.getVertexCount())* ((double) graph2.getVertexCount() -1.)));
            }
        }
        return result;

    }
    
    
    
    public static void main(String args[]){
        String shp1 = "/home/bcostes/Bureau/bdtopo/ROUTE_grph_sansbois.shp";
        String shp2 = "/home/bcostes/Bureau/bdtopo/ROUTE_grph_sansbois_simpli.shp";

        JungSnapshot snap1 = SnapshotIOManager.shp2Snapshot
                (shp1,  new LengthEdgeWeighting(), null, false);

        JungSnapshot snap2 = SnapshotIOManager.shp2Snapshot
                (shp2,  new LengthEdgeWeighting(), null, false);
        
        
        Map<GraphEntity, Integer> ids = new HashMap<GraphEntity, Integer>();
        IPopulation<IFeature> pop = ShapefileReader.read(shp2);
        pop.initSpatialIndex(Tiling.class,false);
        
        for(GraphEntity e : snap2.getEdges()){
            Collection<IFeature> can = pop.select(e.getGeometry().toGeoxGeometry(),1);
            IFeature f = null;
            for(IFeature ff: can){
                if(ff.getGeom().equals(e.getGeometry().toGeoxGeometry())){
                    f =ff;
                    break;
                }
            }
            if(f == null){
                System.out.println("WAT");
                return;
            }
            ids.put(e, Integer.parseInt(
                    f.getAttribute(f.getFeatureType().getFeatureAttributeByName("ID")).toString()));
        }
        
        
        ISubsetIndicator ind = new SubsetBetweennessCentrality();

        Map<GraphEntity, Double> indValues = ind.calculateSubsetEdgeCentrality(snap1, snap2, true);
        IPopulation<IFeature> out = new Population<IFeature>();
        for(GraphEntity e :indValues.keySet()){
            IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
            AttributeManager.addAttribute(f, "ID", ids.get(e), "Integer");
            //   AttributeManager.addAttribute(f, "degree", bet.get(e), "Double");
            //  AttributeManager.addAttribute(f, "clust", meand.get(e), "Double");
            //  AttributeManager.addAttribute(f, "ctrl", str.get(e), "Double");
            //  AttributeManager.addAttribute(f, "meanD5", local.get(e), "Double");
            AttributeManager.addAttribute(f, "betw", indValues.get(e), "Double");
            out.add(f);
        }

        ShapefileWriter.write(out, "/home/bcostes/Bureau/subsetBetw.shp");
    }

}
