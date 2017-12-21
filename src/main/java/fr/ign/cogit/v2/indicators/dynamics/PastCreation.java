package fr.ign.cogit.v2.indicators.dynamics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.conversion.JtsGeOxygene;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;

/**
 * Donne pour une date t, la densité d'arcs nouvellement crées(donc crées entre t-1 et t)
 * La densité se calcul en utilisant les diagrammes de voronoi
 * @author bcostes
 *
 */
public class PastCreation {


    public static  Map<GraphEntity, Double> calculateNodeIndicator(STGraph stgraph, FuzzyTemporalInterval t1, FuzzyTemporalInterval t2,  boolean normalize) {       
        Map<GraphEntity, Double> result = new HashMap<GraphEntity, Double>();
        //récupération des nouveaux arcs entre t-1 et t
        List<IGeometry> edgesTransfo = new ArrayList<IGeometry>();
        IFeatureCollection<IFeature> pop = new Population<IFeature>();
        for(STEntity e : stgraph.getEdges()){
            if(!e.existsAt(t1) && e.existsAt(t2)){
                edgesTransfo.add(e.getGeometry().toGeoxGeometry());
                pop.add(new DefaultFeature(e.getGeometry().toGeoxGeometry()));
            }
        }

        //calcul des cellules de voronoi
        pop = new Population<IFeature>();
        VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
        Set<Coordinate> coords = new HashSet<Coordinate>();
        for(STEntity n : stgraph.getVertices()){
            if(n.existsAt(t2)){
                pop.add(new DefaultFeature(n.getGeometry().toGeoxGeometry()));
                IDirectPosition p = n.getGeometry().toGeoxGeometry().coord().get(0);
                Coordinate c= new Coordinate(p.getX(), p.getY());
                coords.add(c);
            }
        }
        
        Map<STEntity, IGeometry> voronoi = new HashMap<STEntity, IGeometry>();
        IGeometry envC = pop.getGeomAggregate().convexHull().buffer(100);    

        try {
            Polygon envJTS = (Polygon)JtsGeOxygene.makeJtsGeom(envC);
            builder.setSites(coords);
            GeometryFactory geomfactx = new GeometryFactory();
            GeometryCollection diagram  = (GeometryCollection)builder.getDiagram(geomfactx);
            pop = new Population<IFeature>();
            for(int i=0; i< diagram.getNumGeometries(); i++){
                pop.add(new DefaultFeature(JtsGeOxygene.makeGeOxygeneGeom(diagram.getGeometryN(i))));
            }            
            for(STEntity n : stgraph.getVertices()){
                if(n.existsAt(t2)){
                    for(int i=0; i< diagram.getNumGeometries(); i++){
                        try {
                            Geometry p = diagram.getGeometryN(i);
                            if(JtsGeOxygene.makeGeOxygeneGeom(p).contains(n.getGeometry().toGeoxGeometry())){
                                if(!envJTS.contains(p)){
                                    p = p.intersection(envJTS);
                                }
                                voronoi.put(n, JtsGeOxygene.makeGeOxygeneGeom(p));
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
            if(voronoi.keySet().size() != pop.size()){
                System.out.println("AIE AIE AIE");
                return null;
            }

        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // calcul de la densité d'objets
        double lengthTot = 0;
        for(STEntity n : voronoi.keySet()){
            IGeometry cell = voronoi.get(n);
            double area = cell.area();
            double weight = 0;
            for(IGeometry transfo : edgesTransfo){
                if(cell.contains(transfo)){
                    lengthTot += transfo.length();
                    weight += transfo.length();
                    continue;
                }
                if(transfo.intersects(cell)){
                    IGeometry intersection = transfo.intersection(cell);
                    weight += intersection.length();
                    lengthTot += transfo.length();
                }
            }
            //weight /= lengthTot;
            //inversemment proportionnel à l'aire des cellules de voronoi
            weight *= 1./ Math.pow(area,1./2.);
            result.put(n.toGraphEntity(t2), weight);
        }
//        for(GraphEntity n : result.keySet()){
//            result.put(n, result.get(n) / tot);
//        }
        return result;
    }
}
