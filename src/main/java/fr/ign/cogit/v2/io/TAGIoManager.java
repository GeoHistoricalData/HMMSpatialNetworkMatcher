package fr.ign.cogit.v2.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.snapshot.JungSnapshot;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;


public class TAGIoManager {
    public static int NODE_ONLY = 1;
    public static int EDGE_ONLY = 2;
    public static int NODE_AND_EDGE = 3;

    /**
     * Exporte le STAG dans un shapefile
     * @param stgraph
     * @param shp
     */
    public static void exportTAG(STGraph stgraph, String shp) {
        IPopulation<IFeature> out = new Population<IFeature>();
        IPopulation<IFeature> outE = new Population<IFeature>();
        // IPopulation<IFeature> outL = new Population<IFeature>();

        // les sommets
        for (STEntity node : stgraph.getVertices()) {

            DefaultFeature f = new DefaultFeature(node.getGeometry().toGeoxGeometry());
            AttributeManager.addAttribute(f, "ID",
                node.getId(), "String");
            
            for (FuzzyTemporalInterval t : stgraph.getTemporalDomain().asList()) {
                String tt = (int) t.getX(0) + "-" + (int) t.getX(t.size()-1);
                AttributeManager.addAttribute(f, tt,
                        Boolean.toString(node.existsAt(t)), "String");
            }


            for(String s: stgraph.getNodesLocalIndicators()){
                for (FuzzyTemporalInterval t : stgraph.getTemporalDomain().asList()) {
                    String tt = Integer.toString((int) t.getX(0));
                    if(node.getTIndicatorByName(s) != null && 
                            node.getTIndicatorByName(s).getValueAt(t) != null){
                        AttributeManager.addAttribute(f, s + "_" + tt,
                                node.getTIndicatorByName(s).getValueAt(t), "Double");
                    } else {
                        AttributeManager.addAttribute(f, s + "_" + tt, null, "Double");
                    }
                }
            }

            out.add(f);
        }

        
        // les arcs
        for (STEntity edge : stgraph.getEdges()) {

            DefaultFeature f = new DefaultFeature(edge.getGeometry().toGeoxGeometry());

            AttributeManager.addAttribute(f, "ID",
                    edge.getId(), "String");

            for (FuzzyTemporalInterval t : stgraph.getTemporalDomain().asList()) {
      
            String tt = (int) t.getX(0) + "-" + (int) t.getX(t.size()-1);
            AttributeManager.addAttribute(f, tt,
                    Boolean.toString(edge.existsAt(t)), "String");
        }
        for (String attName : stgraph.getAttributes()) {
            for (FuzzyTemporalInterval t : stgraph.getTemporalDomain().asList()) {
                String tt = attName + "_" + (int) t.getX(0) + "-" + (int) t.getX(t.size()-1);
                AttributeManager.addAttribute(f, tt,
                        edge.getTAttributeByName(attName).getValueAt(t), "String");
            }
        }
        for(String s: stgraph.getEdgesLocalIndicators()){
            for (FuzzyTemporalInterval t : stgraph.getTemporalDomain().asList()) {
                String tt = Integer.toString((int) t.getX(0));
                if(edge.getTIndicatorByName(s) != null &&
                        edge.getTIndicatorByName(s).getValueAt(t) != null){
                    AttributeManager.addAttribute(f, s + "_" + tt,
                            edge.getTIndicatorByName(s).getValueAt(t), "Double");
                } else {
                    AttributeManager.addAttribute(f, s + "_" + tt, null, "Double");
                }
            }
        }
        outE.add(f);

    }
    //les st links
    //        for (STLink link : stgraph.getStLinks()) {
    //            for (STEntity node : link.getSources().getNodes()) {
    //                if (!stgraph.containsVertex(node)) {
    //                    continue;
    //                }
    //                IDirectPosition p1 = ((LightDirectPosition) node.getGeometry())
    //                        .toGeoxDirectPosition();
    //                for (STEntity edge2 : link.getTargets().getEdges()) {
    //                    IDirectPosition p2 = Operateurs.milieu(((LightLineString) edge2
    //                            .getGeometry()).toGeoxGeometry());
    //                    IDirectPositionList l = new DirectPositionList();
    //                    l.add(p1);
    //                    l.add(p2);
    //                    outL.add(new DefaultFeature(new GM_LineString(l)));
    //                }
    //                for (STEntity node2 : link.getTargets().getNodes()) {
    //                    if (!stgraph.containsVertex(node2)) {
    //                        continue;
    //                    }
    //                    IDirectPosition p2 = ((LightDirectPosition) node2.getGeometry())
    //                            .toGeoxDirectPosition();
    //                    IDirectPositionList l = new DirectPositionList();
    //                    l.add(p1);
    //                    l.add(p2);
    //                    outL.add(new DefaultFeature(new GM_LineString(l)));
    //                }
    //            }
    //            for (STEntity edge : link.getSources().getEdges()) {
    //                IDirectPosition p1 = Operateurs.milieu(((LightLineString) edge
    //                        .getGeometry()).toGeoxGeometry());
    //                for (STEntity edge2 : link.getTargets().getEdges()) {
    //                    IDirectPosition p2 = Operateurs.milieu(((LightLineString) edge2
    //                            .getGeometry()).toGeoxGeometry());
    //                    IDirectPositionList l = new DirectPositionList();
    //                    l.add(p1);
    //                    l.add(p2);
    //                    outL.add(new DefaultFeature(new GM_LineString(l)));
    //                }
    //                for (STEntity node2 : link.getTargets().getNodes()) {
    //                    IDirectPosition p2 = node2.getGeometry().toGeoxGeometry().coord()
    //                            .get(0);
    //                    IDirectPositionList l = new DirectPositionList();
    //                    l.add(p1);
    //                    l.add(p2);
    //                    outL.add(new DefaultFeature(new GM_LineString(l)));
    //                }
    //            }
    //        }
    String localShp = shp.substring(0, shp.length() - 4);

    ShapefileWriter.write(out, shp);
    ShapefileWriter.write(outE, localShp + "_edges.shp");
    // ShapefileWriter.write(outL, localShp + "_stlinks.shp");
}

/**
 * Exporte les snapshots du STAG avec leur géométries initiales
 * @param stgraph
 * @param shp
 * @param entityToStore
 */
public static void exportSnapshots(STGraph stgraph, String shp,
        int entityToStore) {
    if (entityToStore != TAGIoManager.NODE_AND_EDGE
            && entityToStore != TAGIoManager.NODE_ONLY
            && entityToStore != TAGIoManager.EDGE_ONLY) {
        return;
    }
    int cpt = 0;
    String localShp = shp.substring(0, shp.length() - 4);
    for (FuzzyTemporalInterval t : stgraph.getTemporalDomain().asList()) {
        JungSnapshot s = stgraph.getSnapshotAt(t);
        SnapshotIOManager.snapshot2Shp(s, localShp + "_" + cpt + ".shp", entityToStore);
        cpt++;
    }
}

/**
 * Exporte les snapshots du STAG avec leur géométries initiales
 * @param stgraph
 * @param shp
 * @param entityToStore
 */
public static void exportTAGSnapshots(STGraph stgraph, String shp) {

    int cpt = 0;
    String localShp = shp.substring(0, shp.length() - 4);

    for (FuzzyTemporalInterval t : stgraph.getTemporalDomain().asList()) {
        IPopulation<IFeature> outE = new Population<IFeature>();
        for(STEntity edge : stgraph.getEdges()){
            if(!edge.existsAt(t)){
                continue;
            }
            DefaultFeature f = new DefaultFeature(edge.getGeometry().toGeoxGeometry());

            AttributeManager.addAttribute(f, "ID",
                    edge.getId(), "String");

            for (String attName : stgraph.getAttributes()) {
                String tt = attName + "_" + (int) t.getX(0) + "-" + (int) t.getX(2);
                AttributeManager.addAttribute(f, tt,
                        edge.getTAttributeByName(attName).getValueAt(t), "String");

            }
            String tt = (int) t.getX(0) + "-" + (int) t.getX(2);
            for (String s : stgraph.getEdgesLocalIndicators()) {
                if (edge.getIndicatorAt(s, t) == -1) {
                    AttributeManager.addAttribute(f, s + "_" + tt, null, "Double");
                } else {
                    AttributeManager.addAttribute(f, s + "_" + tt,
                            edge.getIndicatorAt(s, t), "Double");
                }
            }

            outE.add(f);
        }
        ShapefileWriter.write(outE, localShp + "_" + cpt + ".shp");
        cpt++;
    }
}


/**
 * Sauvegarde le STAG sous une forme sérialisée binaire
 * @param stg
 * @param file
 */
public static void serializeBinary(STGraph stg, String file) {
    ObjectOutputStream oos = null;
    try {
        final FileOutputStream fichier = new FileOutputStream(file);
        oos = new ObjectOutputStream(fichier);
        oos.writeObject(stg);
        oos.flush();
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        try {
            if (oos != null) {
                oos.flush();
                oos.close();
            }
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }
}

/**
 * Charge le STAG à partir d'un format sérialisé binaire
 * @param file
 * @return
 */
public static STGraph deserialize(String file) {
    ObjectInputStream ois = null;
    try {
        final FileInputStream fichier = new FileInputStream(file);
        ois = new ObjectInputStream(fichier);
        STGraph stg = (STGraph) ois.readObject();
        ois.close();      
        //stg.updateGeometries();
        int idEmax = 0;
        int idNmax = 0;
        for (STEntity e : stg.getEdges()) {
            if (e.getId() > idEmax) {
                idEmax = e.getId();
            }
        }
        for (STEntity e : stg.getVertices()) {
            if (e.getId() > idNmax) {
                idNmax = e.getId();
            }
        }
        
        //renumérotation des arcs et des sommets
        //stg.updateIds();
        STEntity.updateIDS(idNmax, idEmax);
        stg.updateGeometries();
        
        return stg;
    } catch (IOException e) {
        e.printStackTrace();
    } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    return null;
}
//
//
///**
// * Sauvegarde le STAG sous une forme sérialisée binaire
// * @param 
// * @return
// */
//public static void serializeXml(STGraph stGraph, String file) {
//    XStream xstream = new XStream(new DomDriver()); 
//    xstream.alias("STAttribute", STAttribute.class);
//    xstream.alias("STEntity", STEntity.class);
//    xstream.alias("STGraph", STGraph.class);
//    xstream.alias("STLink", STLink.class);
//    xstream.alias("TimeSerie", TimeSerie.class);
//    xstream.alias("TemporalDomain", TemporalDomain.class);
//    xstream.alias("FuzzyTemporalInterval", FuzzyTemporalInterval.class);
//    xstream.alias("LightDirectPosition", LightDirectPosition.class);
//    xstream.alias("LightLineString", LightLineString.class);
//    xstream.alias("LightMultipleGeometry", LightMultipleGeometry.class);
//    xstream.alias("LightGeometry", LightGeometry.class);
//    xstream.alias("LightDirectPosition", LightDirectPosition.class);
//    xstream.alias("STGroupe", STGroupe.class);
//
//    String xml = xstream.toXML(stGraph);
//    try {
//        FileWriter fw = new FileWriter(file);
//        BufferedWriter bf = new BufferedWriter(fw);
//        bf.write(xml);
//        bf.flush();
//        bf.close();
//        fw.close();
//    } catch (IOException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//    }
//
//}
//
//
///**
// * Charge le STAG à partir d'un format sérialisé xml
// * @param 
// * @return
// */
//public static STGraph deserializeXml(String file) {
//    XStream xstream = new XStream(new DomDriver()); // does not require XPP3 //
//    // library
//    xstream.alias("STAttribute", STAttribute.class);
//    xstream.alias("STEntity", STEntity.class);
//    xstream.alias("STGraph", STGraph.class);
//    xstream.alias("STLink", STLink.class);
//    xstream.alias("TimeSerie", TimeSerie.class);
//    xstream.alias("TemporalDomain", TemporalDomain.class);
//    xstream.alias("FuzzyTemporalInterval", FuzzyTemporalInterval.class);
//    xstream.alias("LightDirectPosition", LightDirectPosition.class);
//    xstream.alias("LightLineString", LightLineString.class);
//    xstream.alias("LightMultipleGeometry", LightMultipleGeometry.class);
//    xstream.alias("LightGeometry", LightGeometry.class);
//    xstream.alias("LightDirectPosition", LightDirectPosition.class);
//    xstream.alias("STGroupe", STGroupe.class);
//
//    try {
//
//        FileInputStream fis = new FileInputStream(
//                file);
//        InputStreamReader ipsr = new InputStreamReader(fis);
//        BufferedReader br = new BufferedReader(ipsr);
//        String line = "";
//        StringBuilder sb = new StringBuilder();
//        while ((line = br.readLine()) != null) {
//            sb.append(line);
//        }
//        br.close();
//
//        // on récupère le contenu du fichier xml pour l'insérer dans l'objet
//        // parametersCol
//        String xml = sb.toString();
//        sb = null;
//        STGraph stg = new STGraph();
//        xstream.fromXML(xml, stg);
//        xml = "";
//        return stg;
//    } catch (FileNotFoundException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//    } //$NON-NLS-1$
//    catch (IOException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//    }
//    return null;
//
//}



}
