package fr.ign.cogit.v2.indicators.sensibility;

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
import fr.ign.cogit.v2.io.SnapshotIOManager;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.weightings.LengthEdgeWeighting;

/**
 * Dans l'hypothèse ou snap2 est un sous-réseau de snap1,  on calcul
 * dans quelle mesure les arcs de snap1 qui ne sont pas dans snap2 "absorbent" les 
 * ppc entre éléments de snap1, et dans quelle mesure les entités de snap2 sollicités
 * par les ppc entre éléments de snap2 perdent en sollicitation lorsque l'on considère les
 * arcs de snap1 qui ne sont pas dans snap2
 * @author bcostes
 *
 */
public class DeviationShortestPath {


    public static Map<GraphEntity, Double> deviationShortestPath(JungSnapshot snap1, JungSnapshot snap2){
        //on établi la correspondance entre les éléments
        if(snap1.getEdgeCount() < snap2.getEdgeCount()){
            return DeviationShortestPath.deviationShortestPath(snap2, snap1);
        }
        //snap1 doit contenir snap2
        //mapping entre sommets de snap2 et de snap1
        Map<GraphEntity, GraphEntity> mappingDirect = new HashMap<GraphEntity, GraphEntity>();
        Map<GraphEntity, GraphEntity> mappingDirectEdges = new HashMap<GraphEntity, GraphEntity>();

        //mapping entre arcs de snap1 et de snap2
        Map<GraphEntity, GraphEntity> mappingIndirect = new HashMap<GraphEntity, GraphEntity>();

        for(GraphEntity v2: snap2.getVertices()){
            //récupération de la géométrie
            IDirectPosition p2 = (IDirectPosition)v2.getGeometry().toGeoxGeometry().coord().get(0);
            for(GraphEntity v1: snap1.getVertices()){
                IDirectPosition p1 =  (IDirectPosition)v1.getGeometry().toGeoxGeometry().coord().get(0);
                if(p1.equals(p2,0.05)){
                    //c'est bon
                    mappingDirect.put(v2, v1);
                    break;
                }

            }
        }


        for(GraphEntity e1: snap1.getEdges()){
            //récupération de la géométrie
            ILineString l1 = (ILineString)e1.getGeometry().toGeoxGeometry();
            for(GraphEntity e2: snap2.getEdges()){
                ILineString l2 =  (ILineString)e2.getGeometry().toGeoxGeometry();
                if(l1.startPoint().equals(l2.startPoint()) && l1.endPoint().equals(l2.endPoint())
                        || l1.startPoint().equals(l2.endPoint()) && l1.endPoint().equals(l2.startPoint())){
                    if(l1.buffer(0.005).contains(l2)){
                        //c'est bon
                        mappingIndirect.put(e1, e2);
                        mappingDirectEdges.put(e2, e1);
                        break;
                    }
                }

            }
        }


        System.out.println(mappingIndirect.keySet().size()+"/"+snap1.getEdgeCount());
        System.out.println(mappingDirectEdges.keySet().size()+"/"+snap2.getEdgeCount());


        //calcul des ppc pour snap 1
        DijkstraShortestPath<GraphEntity, GraphEntity> ppc1 = new DijkstraShortestPath<GraphEntity, GraphEntity>(snap1, snap1.getEdgesWeights());
        //calcul des ppc pour snap 1
        DijkstraShortestPath<GraphEntity, GraphEntity> ppc2 = new DijkstraShortestPath<GraphEntity, GraphEntity>(snap2, snap2.getEdgesWeights());
        // on regarde les ppc de snap2
        Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
        for(GraphEntity e: snap1.getEdges()){
            result.put(e,0.);
        }
        int size = mappingDirect.keySet().size()/100;
        int cpt =0;
        int cptGlobal = 0;
        Stack<GraphEntity> vertices = new Stack<GraphEntity>();
        vertices.addAll(mappingDirect.keySet());
        while(!vertices.isEmpty()){
            GraphEntity v1 = vertices.pop();
            Stack<GraphEntity> vertices2 = new Stack<GraphEntity>();
            vertices2.addAll(vertices);
            GraphEntity vX1 = mappingDirect.get(v1);
            while(!vertices2.isEmpty()) {
                GraphEntity v2 = vertices2.pop();
                Set<GraphEntity> path = new HashSet<GraphEntity>(ppc2.getPath(v1, v2));
                //on calcul le ppc entre v1 et v2 mais sur snap1
                GraphEntity vX2 = mappingDirect.get(v2);
                Set<GraphEntity> pathX = new HashSet<GraphEntity>(ppc1.getPath(vX1, vX2));
                //on sélectionne les éléments de pathX qui ne sont pas dans path
                for(GraphEntity eX1: pathX){
                    GraphEntity e1 = mappingIndirect.get(eX1);
                    if(!path.contains(e1)){
                        result.put(eX1, result.get(eX1)+1.);
                    }
                }
                for(GraphEntity e1: path){
                    GraphEntity eX1 = mappingDirectEdges.get(e1);
                    if(eX1 == null){
                    }
                    else if(!pathX.contains(eX1)){
                        result.put(eX1, result.get(eX1)-1.);
                    }
                }
            }
            ppc1.reset(vX1);
            cpt++;
            if(cpt % size == 0){
                cptGlobal ++;
                System.out.println(cptGlobal+"%");
                cpt = 0;
            }
            ppc2.reset(v1);
        }
        for(GraphEntity e: snap1.getEdges()){
            result.put(e, 2.*result.get(e));
        }
        return result;
    }


    /**
     * cherche les arcs d'un ppc qui utilise l'arc d'identifiant id comme support dans snap1 mais plus dnas snap2
     * @param snap1
     * @param snap2
     * @param id
     * @return
     */
    public static Map<GraphEntity, Double> deviationShortestPathForId(JungSnapshot snap1, JungSnapshot snap2, GraphEntity edgeId){
        //snap1 doit contenir snap2
        //mapping entre sommets de snap2 et de snap1
        Map<GraphEntity, GraphEntity> mappingDirect = new HashMap<GraphEntity, GraphEntity>();
        Map<GraphEntity, GraphEntity> mappingDirectEdges = new HashMap<GraphEntity, GraphEntity>();

        //mapping entre arcs de snap1 et de snap2
        Map<GraphEntity, GraphEntity> mappingIndirect = new HashMap<GraphEntity, GraphEntity>();

        for(GraphEntity v2: snap2.getVertices()){
            //récupération de la géométrie
            IDirectPosition p2 = (IDirectPosition)v2.getGeometry().toGeoxGeometry().coord().get(0);
            for(GraphEntity v1: snap1.getVertices()){
                IDirectPosition p1 =  (IDirectPosition)v1.getGeometry().toGeoxGeometry().coord().get(0);
                if(p1.equals(p2,0.05)){
                    //c'est bon
                    mappingDirect.put(v2, v1);
                    break;
                }

            }
        }


        for(GraphEntity e1: snap1.getEdges()){
            //récupération de la géométrie
            ILineString l1 = (ILineString)e1.getGeometry().toGeoxGeometry();
            for(GraphEntity e2: snap2.getEdges()){
                ILineString l2 =  (ILineString)e2.getGeometry().toGeoxGeometry();
                if(l1.startPoint().equals(l2.startPoint()) && l1.endPoint().equals(l2.endPoint())
                        || l1.startPoint().equals(l2.endPoint()) && l1.endPoint().equals(l2.startPoint())){
                    if(l1.buffer(0.005).contains(l2)){
                        //c'est bon
                        mappingIndirect.put(e1, e2);
                        mappingDirectEdges.put(e2, e1);
                        break;
                    }
                }

            }
        }




        GraphEntity edgeIdSnap2 = mappingIndirect.get(edgeId);
        System.out.println("ok :"+ edgeIdSnap2.getGeometry().toGeoxGeometry() );

        //calcul des ppc pour snap 1
        DijkstraShortestPath<GraphEntity, GraphEntity> ppc1 = new DijkstraShortestPath<GraphEntity, GraphEntity>(snap1, snap1.getEdgesWeights());
        //calcul des ppc pour snap 1
        DijkstraShortestPath<GraphEntity, GraphEntity> ppc2 = new DijkstraShortestPath<GraphEntity, GraphEntity>(snap2, snap2.getEdgesWeights());
        // on regarde les ppc de snap2
        Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
        for(GraphEntity e: snap1.getEdges()){
            result.put(e,0.);
        }
        int size = mappingDirect.keySet().size()/100;
        int cpt =0;
        int cptGlobal = 0;
        Stack<GraphEntity> vertices = new Stack<GraphEntity>();
        vertices.addAll(mappingDirect.keySet());
        while(!vertices.isEmpty()){
            GraphEntity v1 = vertices.pop();
            Stack<GraphEntity> vertices2 = new Stack<GraphEntity>();
            vertices2.addAll(vertices);
            GraphEntity vX1 = mappingDirect.get(v1);
            while(!vertices2.isEmpty()) {
                GraphEntity v2 = vertices2.pop();
                Set<GraphEntity> path = new HashSet<GraphEntity>(ppc2.getPath(v1, v2));
                //on calcul le ppc entre v1 et v2 mais sur snap1
                GraphEntity vX2 = mappingDirect.get(v2);
                Set<GraphEntity> pathX = new HashSet<GraphEntity>(ppc1.getPath(vX1, vX2));
                if(pathX.contains(edgeId)){
                    continue;
                }
                if(!path.contains(edgeIdSnap2)){
                    continue;
                }


                for(GraphEntity eX1: pathX){
                    GraphEntity e1 = mappingIndirect.get(eX1);
                    if(e1 == null || !path.contains(e1)){
                        result.put(eX1, result.get(eX1)+1.);
                    }
                }



            }
            ppc1.reset(vX1);
            cpt++;
            if(cpt % size == 0){
                cptGlobal ++;
                System.out.println(cptGlobal+"%");
                cpt = 0;
            }
            ppc2.reset(v1);
        }
        for(GraphEntity e: snap1.getEdges()){
            result.put(e, 2.*result.get(e));
        }
        return result;
    }

    public static Map<GraphEntity, Double> deviationShortestPathForIdReverse(JungSnapshot snap1, JungSnapshot snap2, GraphEntity edgeId){
        //snap1 doit contenir snap2
        //mapping entre sommets de snap2 et de snap1
        Map<GraphEntity, GraphEntity> mappingDirect = new HashMap<GraphEntity, GraphEntity>();
        Map<GraphEntity, GraphEntity> mappingDirectEdges = new HashMap<GraphEntity, GraphEntity>();

        //mapping entre arcs de snap1 et de snap2
        Map<GraphEntity, GraphEntity> mappingIndirect = new HashMap<GraphEntity, GraphEntity>();

        for(GraphEntity v2: snap2.getVertices()){
            //récupération de la géométrie
            IDirectPosition p2 = (IDirectPosition)v2.getGeometry().toGeoxGeometry().coord().get(0);
            for(GraphEntity v1: snap1.getVertices()){
                IDirectPosition p1 =  (IDirectPosition)v1.getGeometry().toGeoxGeometry().coord().get(0);
                if(p1.equals(p2,0.05)){
                    //c'est bon
                    mappingDirect.put(v2, v1);
                    break;
                }

            }
        }


        for(GraphEntity e1: snap1.getEdges()){
            //récupération de la géométrie
            ILineString l1 = (ILineString)e1.getGeometry().toGeoxGeometry();
            for(GraphEntity e2: snap2.getEdges()){
                ILineString l2 =  (ILineString)e2.getGeometry().toGeoxGeometry();
                if(l1.startPoint().equals(l2.startPoint()) && l1.endPoint().equals(l2.endPoint())
                        || l1.startPoint().equals(l2.endPoint()) && l1.endPoint().equals(l2.startPoint())){
                    if(l1.buffer(0.005).contains(l2)){
                        //c'est bon
                        mappingIndirect.put(e1, e2);
                        mappingDirectEdges.put(e2, e1);
                        break;
                    }
                }

            }
        }




        GraphEntity edgeIdSnap2 = mappingIndirect.get(edgeId);
        System.out.println("ok :"+ edgeIdSnap2.getGeometry().toGeoxGeometry() );

        //calcul des ppc pour snap 1
        DijkstraShortestPath<GraphEntity, GraphEntity> ppc1 = new DijkstraShortestPath<GraphEntity, GraphEntity>(snap1, snap1.getEdgesWeights());
        //calcul des ppc pour snap 1
        DijkstraShortestPath<GraphEntity, GraphEntity> ppc2 = new DijkstraShortestPath<GraphEntity, GraphEntity>(snap2, snap2.getEdgesWeights());
        // on regarde les ppc de snap2
        Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
        for(GraphEntity e: snap1.getEdges()){
            result.put(e,0.);
        }
        int size = mappingDirect.keySet().size()/100;
        int cpt =0;
        int cptGlobal = 0;
        Stack<GraphEntity> vertices = new Stack<GraphEntity>();
        vertices.addAll(mappingDirect.keySet());
        while(!vertices.isEmpty()){
            GraphEntity v1 = vertices.pop();
            Stack<GraphEntity> vertices2 = new Stack<GraphEntity>();
            vertices2.addAll(vertices);
            GraphEntity vX1 = mappingDirect.get(v1);
            while(!vertices2.isEmpty()) {
                GraphEntity v2 = vertices2.pop();
                Set<GraphEntity> path = new HashSet<GraphEntity>(ppc2.getPath(v1, v2));
                //on calcul le ppc entre v1 et v2 mais sur snap1
                GraphEntity vX2 = mappingDirect.get(v2);
                Set<GraphEntity> pathX = new HashSet<GraphEntity>(ppc1.getPath(vX1, vX2));
                if(!pathX.contains(edgeId)){
                    continue;
                }
                if(path.contains(edgeIdSnap2)){
                    continue;
                }


                for(GraphEntity e1: path){
                    GraphEntity eX1 = mappingDirectEdges.get(e1);
                    if(eX1 == null ||!pathX.contains(eX1)){
                        result.put(eX1, result.get(eX1)+1.);
                    }
                }



            }
            ppc1.reset(vX1);
            cpt++;
            if(cpt % size == 0){
                cptGlobal ++;
                System.out.println(cptGlobal+"%");
                cpt = 0;
            }
            ppc2.reset(v1);
        }
        for(GraphEntity e: snap1.getEdges()){
            result.put(e, 2.*result.get(e));
        }
        return result;
    }


    public static void main(String[] args) {
        String shp1 = "/home/bcostes/Bureau/bdtopo/ROUTE_grph_sansbois.shp";
        String shp2 = "/home/bcostes/Bureau/bdtopo/ROUTE_grph_sansbois_simpli.shp";

        int id = 18913;

        //String shp1 = "/home/bcostes/Bureau/bdtopo/ROUTE_charles5.shp";
        // String shp2 = "/home/bcostes/Bureau/bdtopo/ROUTE_ferme.shp";
        JungSnapshot snap1 = SnapshotIOManager.shp2Snapshot
                (shp1,  new LengthEdgeWeighting(), null, false);

        JungSnapshot snap2 = SnapshotIOManager.shp2Snapshot
                (shp2,  new LengthEdgeWeighting(), null, false);


        IFeature fId = null;
        IPopulation<IFeature> pop = ShapefileReader.read(shp2);

        for(IFeature f: pop){
            if(Integer.parseInt(f.getAttribute("ID").toString()) == id){
                fId = f;
                break;
            }
        }
        GraphEntity edgeId = null;
        for(GraphEntity e: snap1.getEdges()){
            if(e.getGeometry().toGeoxGeometry().equals(fId.getGeom())){
                edgeId = e;
                break;
            }
        }
        System.out.println(edgeId.toString()+" "+ edgeId.getGeometry());

        Map<GraphEntity, Double> deviations = DeviationShortestPath.deviationShortestPathForId(snap1, snap2,edgeId);
        IPopulation<IFeature> out = new Population<IFeature>();
        for(GraphEntity e :deviations.keySet()){
            IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
            //   AttributeManager.addAttribute(f, "degree", bet.get(e), "Double");
            //  AttributeManager.addAttribute(f, "clust", meand.get(e), "Double");
            //  AttributeManager.addAttribute(f, "ctrl", str.get(e), "Double");
            //  AttributeManager.addAttribute(f, "meanD5", local.get(e), "Double");
            AttributeManager.addAttribute(f, "deviation", deviations.get(e), "Double");
            out.add(f);
        }

        ShapefileWriter.write(out, "/home/bcostes/Bureau/deviations_18913.shp");

    }

}
