package fr.ign.cogit.v2.io;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import edu.uci.ics.jung.graph.Graph;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.geometry.LightDirectPosition;
import fr.ign.cogit.v2.geometry.LightLineString;
import fr.ign.cogit.v2.geometry.LightMultipleGeometry;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;
import fr.ign.cogit.v2.snapshot.SnapshotGraph;
import fr.ign.cogit.v2.weightings.IEdgeWeighting;
import fr.ign.cogit.v2.weightings.INodeWeighting;
import fr.ign.cogit.v2.weightings.UniformEdgeWeighting;
import fr.ign.cogit.v2.weightings.VoronoiNodeWeighting;


public class SnapshotIOManager {

    private static Logger logger = Logger.getLogger(SnapshotIOManager.class);
    public static int NODE_ONLY = 1;
    public static int EDGE_ONLY = 2;
    public static int NODE_AND_EDGE = 3;

    /**
     * Construit un snapshot graph
     * @param shp
     * @param edgesWeighting pondération des arcs
     * @param nodesWeighting pondération des sommets
     * @param cacheShortestPath doit-on pré-calculer les plus courts chemins
     * @return
     */
    public static SnapshotGraph shp2Snapshot(String shp,
            IEdgeWeighting edgesWeighting, INodeWeighting nodesWeighting,
            boolean cacheShortestPath) {
        if (logger.isInfoEnabled()) {
            logger.info("Creating snapshot graph from shapefile " + shp + " ...");
        }

        IPopulation<IFeature> pop = ShapefileReader.read(shp);
        if (pop.isEmpty()) {
            logger.warn("Error loading shapefile : " + shp + ", may be empty");
            return null;
        }

        CarteTopo map = new CarteTopo("void"); //$NON-NLS-1$
        IPopulation<Arc> popArcs = map.getPopArcs();
        for (IFeature feature : pop) {
            Arc arc = popArcs.nouvelElement();
            try {
                GM_LineString line = new GM_LineString(feature.getGeom().coord());
                arc.setGeometrie(line);
                arc.addCorrespondant((IFeature) feature);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
        map.creeTopologieArcsNoeuds(0);
        map.creeNoeudsManquants(0);
        map.rendPlanaire(0);
        map.fusionNoeuds(0);
        map.filtreArcsDoublons();

        SnapshotGraph sGraph = SnapshotIOManager.topo2Snapshot(map, edgesWeighting,
                nodesWeighting);

        // Plus courts chemins
        if (cacheShortestPath) {
            // on va stocker en cache les shortest path
            sGraph.cacheShortestPaths();
        }

        // Nettoyage des structures de stockage
        pop.clear();
        pop = null;

        if (logger.isInfoEnabled()) {
            logger.info("Snapshot graph created : " + sGraph.getVertexCount()
                    + " nodes, " + sGraph.getEdgeCount() + " edges");
        }
        return sGraph;
    }

    /**
     * Transforme un graphe quelconque en SnapshotGraph
     * @param g
     * @param edges_weights poids des arcs
     * @return
     */
    public static SnapshotGraph graph2JungSnapshot(
            Graph<GraphEntity, GraphEntity> g, IEdgeWeighting edges_weights) {
        SnapshotGraph sGraph = new SnapshotGraph();

        // création des nouveaux noeuds
        Map<GraphEntity, GraphEntity> mappingNodes = new HashMap<GraphEntity, GraphEntity>();
        GraphEntity.setCurrentType(GraphEntity.NODE);
        for (GraphEntity n : g.getVertices()) {
            GraphEntity newN = new GraphEntity();
            newN.setGeometry(n.getGeometry());
            mappingNodes.put(n, newN);
        }

        // création des nouveaux arcs
        GraphEntity.setCurrentType(GraphEntity.EDGE);
        for (GraphEntity a : g.getEdges()) {
            GraphEntity newA = new GraphEntity();
            newA.setGeometry(a.getGeometry());
            // Pondération des arcs
            if (edges_weights != null) {
                edges_weights.weight(newA, newA.getGeometry().toGeoxGeometry());
            } else {
                // par défaut, pondération uniforme
                (new UniformEdgeWeighting()).weight(newA, null);
            }

            sGraph.addEdge(newA, mappingNodes.get(g.getEndpoints(a).getFirst()),
                    mappingNodes.get(g.getEndpoints(a).getSecond()));
        }

        final Map<GraphEntity, Double> weights = new HashMap<GraphEntity, Double>();
        for (GraphEntity e : sGraph.getEdges()) {
            weights.put(e, e.getWeight());
        }
        Transformer<GraphEntity, Double> edgesWeights = new Transformer<GraphEntity, Double>() {
            public Double transform(GraphEntity input) {
                return weights.get(input);
            }
        };

        sGraph.setEdgesWeights(edgesWeights);
        mappingNodes.clear();
        mappingNodes = null;
        return sGraph;
    }

    /**
     * Construit un snapshotgraph à partir d'une carte topo
     * @param pop
     * @return
     */
    public static SnapshotGraph topo2Snapshot(CarteTopo map,
            IEdgeWeighting edgesWeighting, INodeWeighting nodesWeighting) {
        SnapshotGraph sGraph = new SnapshotGraph();

        /*
         * Les sommets
         */
        GraphEntity.setCurrentType(GraphEntity.NODE);
        // mapping Noeud - entité, utilisé pour la création des arcs
        Map<Noeud, GraphEntity> mappingNodes = new HashMap<Noeud, GraphEntity>();
        // mapping graphEntity: geometries, utilisées pour la pondérations des
        // objets
        Map<IGeometry, GraphEntity> geometriesNodes = new HashMap<IGeometry, GraphEntity>();

        for (Noeud n : map.getListeNoeuds()) {
            GraphEntity node = new GraphEntity();
            node.setGeometry(new LightDirectPosition(n.getCoord()));
            geometriesNodes.put(n.getGeom(), node);
            mappingNodes.put(n, node);
        }

        /*
         * Les arcs
         */
        GraphEntity.setCurrentType(GraphEntity.EDGE);
        for (Arc a : map.getListeArcs()) {
            // création d'un nouvel arc
            GraphEntity arc = new GraphEntity();
            arc.setGeometry(new LightLineString(a.getGeom().coord()));

            // Pondération des arcs
            if (edgesWeighting != null) {
                edgesWeighting.weight(arc, a.getGeom());
            } else {
                // par défaut, pondération uniforme
                (new UniformEdgeWeighting()).weight(arc, null);
            }

            sGraph.addEdge(arc, mappingNodes.get(a.getNoeudIni()),
                    mappingNodes.get(a.getNoeudFin()));
        }

        // création du transformer pour le graphe jung
        final Map<GraphEntity, Double> weights = new HashMap<GraphEntity, Double>();
        for (GraphEntity e : sGraph.getEdges()) {
            weights.put(e, e.getWeight());
        }
        Transformer<GraphEntity, Double> edgesWeights = new Transformer<GraphEntity, Double>() {
            public Double transform(GraphEntity input) {
                return weights.get(input);
            }
        };
        sGraph.setEdgesWeights(edgesWeights);

        // ------<
        // ----> Pondération des sommets

        if (nodesWeighting != null) {
            if (nodesWeighting instanceof VoronoiNodeWeighting) { // pondération
                // surface de
                // Voronoi
                ((VoronoiNodeWeighting) nodesWeighting).weight(geometriesNodes);
            } // création du transformer pour le graphe
            final Map<GraphEntity, Double> nweights = new HashMap<GraphEntity, Double>();
            for (GraphEntity e : sGraph.getVertices()) {
                nweights.put(e, e.getWeight());
            }
            Transformer<GraphEntity, Double> nodesWeights = new Transformer<GraphEntity, Double>() {
                public Double transform(GraphEntity input) {
                    return nweights.get(input);
                }
            };
            sGraph.setNodesWeights(nodesWeights);
        }

        // Nettoyage des structures de stockage
        map.nettoyer();
        map = null;
        geometriesNodes.clear();
        geometriesNodes = null;

        return sGraph;
    }

    /**
     * Shapefile writer
     * @param graph
     * @param shp
     * @param entityToStore
     */
    public static void snapshot2Shp(JungSnapshot graph, String shp,
            int entityToStore) {
        if (entityToStore != SnapshotIOManager.NODE_AND_EDGE
                && entityToStore != SnapshotIOManager.NODE_ONLY
                && entityToStore != SnapshotIOManager.EDGE_ONLY) {
            return;
        }
        IPopulation<IFeature> out = new Population<IFeature>();
        IPopulation<IFeature> outE = new Population<IFeature>();

        if (entityToStore == SnapshotIOManager.NODE_AND_EDGE
                || entityToStore == SnapshotIOManager.NODE_ONLY) {
            for (GraphEntity node : graph.getVertices()) {
                if (node.getGeometry() == null) {
                    logger.error("Export failed : no valid geometry");
                    return;
                }
                if (node.getGeometry() instanceof LightDirectPosition) {
                    DefaultFeature f = new DefaultFeature(node.getGeometry()
                            .toGeoxGeometry());
                    // les indicateurs
                    for (String indicator : graph.getVertices().iterator().next()
                            .getLocalIndicators().keySet()) {
                        if(node.getLocalIndicators().get(indicator) != null){
                        AttributeManager.addAttribute(f, indicator, node
                                .getLocalIndicators().get(indicator).floatValue(), "Float");
                        }
                        else{
                            AttributeManager.addAttribute(f, indicator, null, "Float");
                        }
                    }
                    out.add(f);
                } else if (node.getGeometry() instanceof LightMultipleGeometry) {
                    //                    for(LightDirectPosition pp: ((LightMultipleGeometry) node.getGeometry()).getLightDirectPosition()){
                    //                        DefaultFeature f = new DefaultFeature(new GM_Point(pp.toGeoxDirectPosition()));
                    //                        // les indicateurs
                    //                        for (String indicator : graph.getVertices().iterator().next()
                    //                                .getLocalIndicators().keySet()) {
                    //                            AttributeManager.addAttribute(f, indicator, node
                    //                                    .getLocalIndicators().get(indicator).floatValue(), "Float");
                    //                        }
                    //                        out.add(f);
                    //                    }
                    //                    for(LightLineString ll: ((LightMultipleGeometry) node.getGeometry()).getLightLineString()){
                    //                        DefaultFeature f = new DefaultFeature(ll.toGeoxGeometry());
                    //                        outE.add(f);
                    //                    }
                    DefaultFeature f = new DefaultFeature(
                            ((LightMultipleGeometry) node.getGeometry()).toGeoxGeometry());
                    // les indicateurs
                    for (String indicator : graph.getVertices().iterator().next()
                            .getLocalIndicators().keySet()) {
                        if(node.getLocalIndicators().get(indicator) != null){
                            AttributeManager.addAttribute(f, indicator, node
                                    .getLocalIndicators().get(indicator).floatValue(), "Float");
                        }
                        else
                        {
                            AttributeManager.addAttribute(f, indicator, null, "Float");
                        }
                    }
                    out.add(f);
                }

            }
            ShapefileWriter.write(out, shp);
        }

        if (entityToStore == SnapshotIOManager.NODE_AND_EDGE
                || entityToStore == SnapshotIOManager.EDGE_ONLY) {
            for (GraphEntity edge : graph.getEdges()) {
                if (edge.getGeometry() == null) {
                    logger.error("Export failed : no valid geometry");
                    return;
                }
                DefaultFeature f = new DefaultFeature(edge.getGeometry()
                        .toGeoxGeometry());
                // les indicateurs
                for (String indicator : graph.getEdges().iterator().next()
                        .getLocalIndicators().keySet()) {
                    if(edge.getLocalIndicators().get(indicator) != null){
                        AttributeManager.addAttribute(f, indicator, edge.getLocalIndicators()
                                .get(indicator).floatValue(), "Float");
                    }
                    else{
                        AttributeManager.addAttribute(f, indicator, null, "Float");
                    }
                }
                for (String attName : graph.getEdges().iterator().next()
                        .getAttributes().keySet()) {
                    AttributeManager.addAttribute(f, attName,
                            edge.getAttributes().get(attName), "String");
                }

                outE.add(f);
            }

            String shp2 = shp.substring(0, shp.length() - 4);
            shp2 += "_edges.shp";
            ShapefileWriter.write(outE, shp2);
        }

    }
}
