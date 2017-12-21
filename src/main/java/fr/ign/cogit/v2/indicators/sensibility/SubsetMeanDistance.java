package fr.ign.cogit.v2.indicators.sensibility;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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

public class SubsetMeanDistance extends ISubsetIndicator{
    public SubsetMeanDistance() {
        this.name = "Meand";
    }
    @Override
    public Map<GraphEntity, Double> calculateSubsetNodeCentrality(
            JungSnapshot graph1, JungSnapshot graph2, boolean normalize) {
        
        if(graph1.getEdgeCount() < graph2.getEdgeCount()){
            return this.calculateSubsetNodeCentrality(graph2, graph1, normalize);
        }
        //snap1 doit contenir snap2
        //mapping entre sommets de snap2 et de snap1
        Map<GraphEntity, GraphEntity> mappingDirect = new HashMap<GraphEntity, GraphEntity>();
        ////mapping entre arcs de snap1 et de snap2

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
        

        
        Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
        for (GraphEntity v : graph2.getVertices()) {
          result.put(v, 0.);
        }
        DijkstraShortestPath<GraphEntity, GraphEntity> ppc = new DijkstraShortestPath<GraphEntity, GraphEntity>(graph1, graph1.getEdgesWeights());
   
        Stack<GraphEntity> stack = new Stack<GraphEntity>();
        stack.addAll(graph2.getVertices());
        for(GraphEntity v1: graph2.getVertices()) {
          // pour chaque sommet v1
          GraphEntity vX1 = mappingDirect.get(v1);
          for (GraphEntity v2: graph2.getVertices()) {
              if(v1.equals(v2)){
                  continue;
              }
              GraphEntity vX2 = mappingDirect.get(v2);
            // pour chaque sommet v2 non déja traité
            // on récupère tous les ppc entre v1 et v2
            double d =ppc.getDistance(vX1, vX2).doubleValue();
            result.put(v1, result.get(v1) + d);

            }
          ppc.reset(vX1);
          }
        
        if(normalize){
            for (GraphEntity w : graph2.getVertices()) {
                result.put(w, result.get(w)  / ((double) graph2.getVertexCount()-1));
            }
        }
        return result;
    }

    @Override
    public Map<GraphEntity, Double> calculateSubsetEdgeCentrality(
            JungSnapshot graph1, JungSnapshot graph2, boolean normalize) {
        // TODO Auto-generated method stub
      //on établi la correspondance entre les éléments
        if(graph1.getEdgeCount() < graph2.getEdgeCount()){
            return this.calculateSubsetNodeCentrality(graph2, graph1, normalize);
        }
        Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();

        Map<GraphEntity, Double> nodesValues = this.calculateSubsetNodeCentrality(graph1, graph2, normalize);
        for (GraphEntity edge : graph2.getEdges()) {
          GraphEntity n1 = graph2.getEndpoints(edge).getFirst();
          GraphEntity n2 = graph2.getEndpoints(edge).getSecond();

          double centraltiy = (nodesValues.get(n1) + nodesValues.get(n2)) / (2.);
          result.put(edge, centraltiy);
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
        
        
        ISubsetIndicator ind = new SubsetMeanDistance();

        Map<GraphEntity, Double> indValues = ind.calculateSubsetEdgeCentrality(snap1, snap2, true);
        IPopulation<IFeature> out = new Population<IFeature>();
        for(GraphEntity e :indValues.keySet()){
            IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
            AttributeManager.addAttribute(f, "ID", ids.get(e), "Integer");
            //   AttributeManager.addAttribute(f, "degree", bet.get(e), "Double");
            //  AttributeManager.addAttribute(f, "clust", meand.get(e), "Double");
            //  AttributeManager.addAttribute(f, "ctrl", str.get(e), "Double");
            //  AttributeManager.addAttribute(f, "meanD5", local.get(e), "Double");
            AttributeManager.addAttribute(f, "meand", indValues.get(e), "Double");
            out.add(f);
        }

        ShapefileWriter.write(out, "/home/bcostes/Bureau/subsetmeand.shp");
    }

}
