package fr.ign.cogit.v2.impacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.indicators.local.BetweennessCentrality;
import fr.ign.cogit.v2.indicators.local.ClusteringCentrality;
import fr.ign.cogit.v2.indicators.local.MeanDistance;
import fr.ign.cogit.v2.io.SnapshotIOManager;
import fr.ign.cogit.v2.manual.corrections.CorrectionInterpolation;
import fr.ign.cogit.v2.patterns.AppearanceSTPattern;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;
import fr.ign.cogit.v2.snapshot.SnapshotGraph.NORMALIZERS;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.weightings.LengthEdgeWeighting;

public class InconsistenciesCentralityImpact {

    /**
     * Pour une date, calcul l'impact sur la centralité de la prise en compte des incohérences
     * @param stg
     * @param t
     * @return
     */
    public Map<STEntity, Double> CentralityImpact(STGraph stg, FuzzyTemporalInterval t, ILocalIndicator centrality){
        Map<STEntity, Double> impacts = new HashMap<STEntity, Double>();
        //on calcul la centralité sur le graphe normal à la date t

        JungSnapshot snapT = stg.getSnapshotAt(t);


        System.out.println(snapT.getEdgeCount());


        Map<GraphEntity, Double> r = snapT.calculateEdgeCentrality(centrality, NORMALIZERS.CONVENTIONAL);
        // on crée la map
        for(STEntity e: stg.getEdgesAt(t)){
            Map<Integer, List<Integer>> ids = stg.getMappingSnapshotEdgesId();
            for(GraphEntity g: snapT.getEdges()){
                if (ids.get(g.getId()).contains(e.getId())){
                    impacts.put(e, r.get(g));
                }
            }
        }
        // maintenant on va corriger les réincarnations
        CorrectionInterpolation.correctAllReincarnations(stg);
        snapT = stg.getSnapshotAt(t);
        r = snapT.calculateEdgeCentrality(new MeanDistance(), NORMALIZERS.CONVENTIONAL);




        //apparitions
        AppearanceSTPattern app = new AppearanceSTPattern();
        Set<STEntity> appearances = new HashSet<STEntity>();
        for(STEntity e: stg.getEdgesAt(t)){
            if(app.find(e.getTimeSerie())){
                appearances.add(e);
            }
        }
        snapT = stg.getSnapshotAt(t);
        System.out.println(snapT.getEdgeCount());
        //on supprime les apparitions
        for(GraphEntity g: new ArrayList<GraphEntity>(snapT.getEdges())){
            boolean remove = false;
            for(STEntity e: appearances){
                Map<Integer, List<Integer>> ids = stg.getMappingSnapshotEdgesId();
                if (ids.get(g.getId()).contains(e.getId())){
                    remove = true;
                    break;
                }
            }
            if(remove){
                snapT.removeEdge(g);
            }
        }

        //on recalcul les centralité



        System.out.println(snapT.getEdgeCount());

        r.clear();
        r = snapT.calculateEdgeCentrality(centrality, NORMALIZERS.CONVENTIONAL);
        // on crée la map
        Map<STEntity, Double> centralities = new HashMap<STEntity, Double>();
        for(STEntity e: stg.getEdgesAt(t)){
            Map<Integer, List<Integer>> ids = stg.getMappingSnapshotEdgesId();
            for(GraphEntity g: snapT.getEdges()){
                if (ids.get(g.getId()).contains(e.getId())){
                    centralities.put(e, r.get(g));
                }
            }
        }

        // impact: diminution / augmentation situation initiale / situation altérée
        for(STEntity e: new ArrayList<STEntity>(impacts.keySet())){
            if(!centralities.containsKey(e)){
                impacts.remove(e);
            }
            else{
                impacts.put(e, impacts.get(e) - centralities.get(e));
            }
        }


        return impacts;
    }



    public static void main(String args[]){
        //        STGraph stg = TAGIoManager.deserialize("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v6/tag.tag");
        //        List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>(stg.getTemporalDomain().asList());
        //        Collections.sort(times);
        //        FuzzyTemporalInterval t = times.get(2);
        //        InconsistenciesCentralityImpact iii =new InconsistenciesCentralityImpact();
        //        Map<STEntity, Double> impacts = iii.CentralityImpact(stg, t,new MeanDistance());
        //
        //
        //        IPopulation<IFeature> out = new Population<IFeature>();
        //        for(STEntity e :impacts.keySet() ){
        //            DefaultFeature f = new DefaultFeature(e.getGeometryAt(t).toGeoxGeometry());
        //            AttributeManager.addAttribute(f, "impact", impacts.get(e), "Double");
        //            out.add(f);
        //        }
        //        ShapefileWriter.write(out, "/home/bcostes/Bureau/impacts_2.shp");


        JungSnapshot snap1 = SnapshotIOManager.shp2Snapshot("/home/bcostes/Bureau/snapshot2.shp", new LengthEdgeWeighting(), null, true);
        Map<GraphEntity, Double> c1 = snap1.calculateEdgeCentrality(new ClusteringCentrality(), NORMALIZERS.NONE);

        
        JungSnapshot snap2 = SnapshotIOManager.shp2Snapshot("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v6/shp/snapshots/snapshot2_edges.shp", new LengthEdgeWeighting(), null, true);
        Map<GraphEntity, Double> c2 = snap2.calculateEdgeCentrality(new ClusteringCentrality(), NORMALIZERS.NONE);

        
        
        Map<GraphEntity, Double> c = new HashMap<GraphEntity, Double>();
        for(GraphEntity e: c1.keySet()){
            ILineString l1 = (ILineString) e.getGeometry().toGeoxGeometry();
            for(GraphEntity e2: c2.keySet()){
                ILineString l2 = (ILineString) e2.getGeometry().toGeoxGeometry();
                if(l1.equals(l2)){
                    double d =100.*(c2.get(e2)-c1.get(e))/(c1.get(e));
                    c.put(e, d);
                    break;
                }
            }
        }
        System.out.println(c.size());
        
        IPopulation<IFeature> out = new Population<IFeature>();
        for(GraphEntity e :c.keySet() ){
            DefaultFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
            AttributeManager.addAttribute(f, "impact", c.get(e), "Double");
            out.add(f);
        }
        ShapefileWriter.write(out, "/home/bcostes/Bureau/impact_clust.shp");
    }

}
