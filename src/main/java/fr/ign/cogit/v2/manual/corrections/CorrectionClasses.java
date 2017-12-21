package fr.ign.cogit.v2.manual.corrections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.stdb.time.TemporalInterval;
import fr.ign.cogit.v2.geometry.LightDirectPosition;
import fr.ign.cogit.v2.geometry.LightGeometry;
import fr.ign.cogit.v2.geometry.LightLineString;
import fr.ign.cogit.v2.geometry.LightMultipleGeometry;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.lineage.STGroupe;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STAttribute;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.STLink;
import fr.ign.cogit.v2.tag.TemporalDomain;
import fr.ign.cogit.v2.tag.TimeSerie;

/**
 * Cette classe sert juste à recréer un TAG à partir d'un autre TAG, lorsque l'on modifie la 
 * classe d'un attribut (p.e TemporalInterval => FuzzySet)
 * @author bcostes
 *
 */
public class CorrectionClasses {
    public static void main(String args[]){
        fr.ign.cogit.v2.tag.STGraph oldstg =  fr.ign.cogit.v2.io.TAGIoManager.deserialize("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v3/with_indicators/tag_ind.tag");



        Map<TemporalInterval, FuzzyTemporalInterval> mapping  = new HashMap<TemporalInterval, FuzzyTemporalInterval>();
        TemporalInterval told1 = new TemporalInterval(1784,1785,1789,1791);
        TemporalInterval told2 = new TemporalInterval(1808,1810,1836,1853);
        TemporalInterval told3 = new TemporalInterval(1825,1827,1836,1839);
        TemporalInterval told4 = new TemporalInterval(1848,1849,1849,1850); 
        TemporalInterval told5 = new TemporalInterval(1870,1871,1871,1872); 
        TemporalInterval told6 = new TemporalInterval(1884, 1885, 1888, 1889);

        FuzzyTemporalInterval t1 = new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4);
        FuzzyTemporalInterval t2 = new FuzzyTemporalInterval(new double[]{1808,1810,1836,1853},new double[]{0,1,1,0}, 4);
        FuzzyTemporalInterval t3 = new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4);
        FuzzyTemporalInterval t4 = new FuzzyTemporalInterval(new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4);
        FuzzyTemporalInterval t5 = new FuzzyTemporalInterval(new double[]{1870,1871,1871,1872},new double[]{0,1,1,0}, 4);
        FuzzyTemporalInterval t6 = new FuzzyTemporalInterval(new double[]{1884, 1885, 1888, 1889},new double[]{0,1,1,0}, 4);

        mapping.put(told1, t1);
        mapping.put(told2, t2);
        mapping.put(told3, t3);
        mapping.put(told4, t4);
        mapping.put(told5, t5);
        mapping.put(told6, t6);


        //******************** STGraph ****************************
        //domaine temporel
        List<FuzzyTemporalInterval> l = new ArrayList<FuzzyTemporalInterval>();
        l.add(t1);
        l.add(t2);
        l.add(t3);
        l.add(t4);
        l.add(t5);
        TemporalDomain dt = new TemporalDomain(l);
        STGraph stg = new STGraph(dt);
        // attributs
        stg.setAttributes(oldstg.getAttributes());
        //accuracies
        Map<FuzzyTemporalInterval, Double> acc = new HashMap<FuzzyTemporalInterval, Double>();
        for(TemporalInterval t : oldstg.getAccuracies().keySet()){
            acc.put(mapping.get(t), oldstg.getAccuracies().get(t));
        }
        stg.setAccuracies(acc);
        //global indicators
        Map<FuzzyTemporalInterval, Map<String,Double>> globalIndicators = new HashMap<FuzzyTemporalInterval, Map<String,Double>>();
        for(TemporalInterval t : oldstg.getGlobalIndicators().keySet()){
            globalIndicators.put(mapping.get(t), oldstg.getGlobalIndicators().get(t));
        }
        stg.setGlobalIndicators(globalIndicators);
        //edge local indicator
        Map<FuzzyTemporalInterval, List<String>> edgeslocal = new HashMap<FuzzyTemporalInterval, List<String>>();
        for(TemporalInterval t : oldstg.getEdgesLocalIndicators().keySet()){
            edgeslocal.put(mapping.get(t), oldstg.getEdgesLocalIndicators().get(t));
        }
        stg.setEdgesLocalIndicators(edgeslocal);
        //node local indicator
        Map<FuzzyTemporalInterval, List<String>> nodesLocal = new HashMap<FuzzyTemporalInterval, List<String>>();
        for(TemporalInterval t : oldstg.getNodesLocalIndicators().keySet()){
            nodesLocal.put(mapping.get(t), oldstg.getNodesLocalIndicators().get(t));
        }
        stg.setNodesLocalIndicators(nodesLocal);
        //********************* STEntity ****************************
        Map<fr.ign.cogit.v2.tag.STEntity,STEntity> mappingE = new HashMap<fr.ign.cogit.v2.tag.STEntity, STEntity>();
        for(fr.ign.cogit.v2.tag.STEntity node : oldstg.getVertices()){
            Map<FuzzyTemporalInterval, Boolean> map = new HashMap<FuzzyTemporalInterval, Boolean>();
            for(TemporalInterval t : node.getTimeSerie().asList().keySet()){
                map.put(mapping.get(t), node.existsAt(t));
            }
            // time serie
            TimeSerie ts = new TimeSerie(map);
            STEntity newNode = new STEntity(ts);
            //id
            newNode.setId(node.getId());
            // geometry fusion
            LightGeometry g = null;
            fr.ign.cogit.v2.geometry.LightGeometry oldg = node.getGeometry();
            if(oldg instanceof fr.ign.cogit.v2.geometry.LightDirectPosition){
                g = new LightDirectPosition(((fr.ign.cogit.v2.geometry.LightDirectPosition)oldg).toGeoxDirectPosition());
            }
            else{
                List<LightDirectPosition> l1 = new ArrayList<LightDirectPosition>();
                List<LightLineString> l2 = new ArrayList<LightLineString>();
                fr.ign.cogit.v2.geometry.LightMultipleGeometry cast = (fr.ign.cogit.v2.geometry.LightMultipleGeometry )oldg;
                for(fr.ign.cogit.v2.geometry.LightDirectPosition p: cast.getLightDirectPosition()){
                    l1.add(new LightDirectPosition(p.toGeoxDirectPosition()));
                }
                for(fr.ign.cogit.v2.geometry.LightLineString p: cast.getLightLineString()){
                    IDirectPositionList ll = new DirectPositionList();
                    ll.addAll(p.toGeoxGeometry().coord());
                    l2.add(new LightLineString(ll));
                }
                g = new LightMultipleGeometry(l2, l1);
                LightDirectPosition geoxGeom = new LightDirectPosition(cast.getGeoxGeom().toGeoxDirectPosition());
                ((LightMultipleGeometry)g).setGeoxGeom(geoxGeom);

            }
            newNode.setGeometry(g);
            //geometries
            Map<FuzzyTemporalInterval, LightGeometry> geoms = new HashMap<FuzzyTemporalInterval, LightGeometry>();
            for(TemporalInterval t : node.getTimeSerie().asList().keySet()){
                if(node.getGeometryAt(t) != null){
                    LightGeometry gg = null;
                    fr.ign.cogit.v2.geometry.LightGeometry oldg2 = node.getGeometryAt(t);
                    if(oldg2 instanceof fr.ign.cogit.v2.geometry.LightDirectPosition){
                        gg = new LightDirectPosition(((fr.ign.cogit.v2.geometry.LightDirectPosition)oldg2).toGeoxDirectPosition());
                    }
                    else{
                        List<LightDirectPosition> l1 = new ArrayList<LightDirectPosition>();
                        List<LightLineString> l2 = new ArrayList<LightLineString>();
                        fr.ign.cogit.v2.geometry.LightMultipleGeometry cast = (fr.ign.cogit.v2.geometry.LightMultipleGeometry )oldg2;
                        for(fr.ign.cogit.v2.geometry.LightDirectPosition p: cast.getLightDirectPosition()){
                            l1.add(new LightDirectPosition(p.toGeoxDirectPosition()));
                        }
                        for(fr.ign.cogit.v2.geometry.LightLineString p: cast.getLightLineString()){
                            IDirectPositionList ll = new DirectPositionList();
                            ll.addAll(p.toGeoxGeometry().coord());
                            l2.add(new LightLineString(ll));
                        }
                        gg = new LightMultipleGeometry(l2, l1);
                        if(cast.getGeoxGeom() != null){
                            LightDirectPosition geoxGeom = new LightDirectPosition(cast.getGeoxGeom().toGeoxDirectPosition());
                            ((LightMultipleGeometry)gg).setGeoxGeom(geoxGeom);
                        }

                    }
                    geoms.put(mapping.get(t), gg);
                }
            }
            newNode.setTgeometries(geoms);
            // poids
            Map<FuzzyTemporalInterval, Double> weights = new HashMap<FuzzyTemporalInterval, Double>();
            for(TemporalInterval t: node.getTimeSerie().asList().keySet()){
                if(node.getWeightAt(t) != null){
                    weights.put(mapping.get(t), node.getWeightAt(t));
                }
            }
            newNode.setTweights(weights);

            // attributs
            Map<String, STAttribute> att = new HashMap<String, STAttribute>();
            for(String s: node.getTAttributes().keySet()){
                STAttribute a = new STAttribute();
                a.setName(node.getTAttributes().get(s).getName());
                Map<FuzzyTemporalInterval, String>mapAtt = new HashMap<FuzzyTemporalInterval, String>();
                for(TemporalInterval t: node.getTAttributes().get(s).getValues().keySet()){
                    mapAtt.put(mapping.get(t),node.getTAttributes().get(s).getValueAt(t));
                }
                a.setValues(mapAtt);
                newNode.getTAttributes().put(s, a);
            }
            //local ind
            Map<FuzzyTemporalInterval, Map<String,Double>> localInd = new HashMap<FuzzyTemporalInterval,  Map<String,Double>>();
            for(TemporalInterval t: node.getTimeSerie().asList().keySet()){
                localInd.put(mapping.get(t), node.getTLocalIndicators().get(t));
            }
            newNode.setTLocalIndicators(localInd);

            mappingE.put(node, newNode);
        }


        for(fr.ign.cogit.v2.tag.STEntity node : oldstg.getEdges()){
            Map<FuzzyTemporalInterval, Boolean> map = new HashMap<FuzzyTemporalInterval, Boolean>();
            for(TemporalInterval t : node.getTimeSerie().asList().keySet()){
                map.put(mapping.get(t), node.existsAt(t));
            }
            // time serie
            TimeSerie ts = new TimeSerie(map);
            STEntity newNode = new STEntity(ts);
            //id
            newNode.setId(node.getId());
            // geometry fusion
            fr.ign.cogit.v2.geometry.LightLineString oldg = (fr.ign.cogit.v2.geometry.LightLineString)node.getGeometry();
            IDirectPositionList list = new DirectPositionList();
            list.addAll(oldg.toGeoxGeometry().coord());
            LightLineString g = new LightLineString(list);
            newNode.setGeometry(g);
            //geometries
            Map<FuzzyTemporalInterval, LightGeometry> geoms = new HashMap<FuzzyTemporalInterval, LightGeometry>();
            for(TemporalInterval t : node.getTimeSerie().asList().keySet()){
                if(node.getGeometryAt(t) != null){
                    fr.ign.cogit.v2.geometry.LightLineString oldg2 = (fr.ign.cogit.v2.geometry.LightLineString)node.getGeometryAt(t);
                    IDirectPositionList list2 = new DirectPositionList();
                    list2.addAll(oldg2.toGeoxGeometry().coord());
                    LightLineString gg = new LightLineString(list2);
                    geoms.put(mapping.get(t), gg);
                }
            }
            newNode.setTgeometries(geoms);
            // poids
            Map<FuzzyTemporalInterval, Double> weights = new HashMap<FuzzyTemporalInterval, Double>();
            for(TemporalInterval t: node.getTimeSerie().asList().keySet()){
                if(node.getWeightAt(t) != null){
                    weights.put(mapping.get(t), node.getWeightAt(t));
                }
            }
            newNode.setTweights(weights);

            // attributs
            Map<String, STAttribute> att = new HashMap<String, STAttribute>();
            for(String s: node.getTAttributes().keySet()){
                STAttribute a = new STAttribute();
                a.setName(node.getTAttributes().get(s).getName());
                Map<FuzzyTemporalInterval, String>mapAtt = new HashMap<FuzzyTemporalInterval, String>();
                for(TemporalInterval t: node.getTAttributes().get(s).getValues().keySet()){
                    mapAtt.put(mapping.get(t),node.getTAttributes().get(s).getValueAt(t));
                }
                a.setValues(mapAtt);
                newNode.getTAttributes().put(s, a);
            }
            //local ind
            Map<FuzzyTemporalInterval, Map<String,Double>> localInd = new HashMap<FuzzyTemporalInterval,  Map<String,Double>>();
            for(TemporalInterval t: node.getTimeSerie().asList().keySet()){
                localInd.put(mapping.get(t), node.getTLocalIndicators().get(t));
            }
            newNode.setTLocalIndicators(localInd);
            mappingE.put(node, newNode);
        }

        for(fr.ign.cogit.v2.tag.STEntity node : oldstg.getEdges()){
            stg.addEdge(mappingE.get(node), mappingE.get(oldstg.getEndpoints(node).getFirst()),mappingE.get(oldstg.getEndpoints(node).getSecond()));
        }


        //********************* STLinks ****************************
        Set<STLink> stLinks = new HashSet<STLink>();
        for(fr.ign.cogit.v2.tag.STLink oldl : oldstg.getStLinks()){
            STLink newL = new STLink(mapping.get(oldl.getDateSource()), mapping.get(oldl.getDateTarget()));
            STGroupe gsource = new STGroupe();
            for(fr.ign.cogit.v2.tag.STEntity oldNode : oldl.getSources().getNodes()){
                gsource.getNodes().add(mappingE.get(oldNode));
            }
            for(fr.ign.cogit.v2.tag.STEntity oldEdge : oldl.getSources().getEdges()){
                gsource.getEdges().add(mappingE.get(oldEdge));
            }
            newL.setSources(gsource);
            STGroupe gtarget = new STGroupe();
            for(fr.ign.cogit.v2.tag.STEntity oldNode : oldl.getTargets().getNodes()){
                gtarget.getNodes().add(mappingE.get(oldNode));
            }
            for(fr.ign.cogit.v2.tag.STEntity oldEdge : oldl.getTargets().getEdges()){
                gtarget.getEdges().add(mappingE.get(oldEdge));
            }
            newL.setTargets(gtarget);
            stLinks.add(newL);
        }
        stg.setStLinks(stLinks);



        TAGIoManager.exportTAG(stg, "/home/bcostes/Bureau/test/test.shp");
        TAGIoManager.exportSnapshots(stg, "/home/bcostes/Bureau/test/snapshots/test.shp", TAGIoManager.EDGE_ONLY);
        TAGIoManager.exportTAGSnapshots(stg, "/home/bcostes/Bureau/test/tag_snapshots/test.shp");


    }
}
