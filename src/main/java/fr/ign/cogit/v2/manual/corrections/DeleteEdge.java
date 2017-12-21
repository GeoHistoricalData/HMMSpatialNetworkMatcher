package fr.ign.cogit.v2.manual.corrections;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.patterns.AppearanceSTPattern;
import fr.ign.cogit.v2.patterns.ReincarnationSTPattern;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.STProperty;

public class DeleteEdge {


  public static void deleteEdge(STGraph stg, STEntity edge){
    // extrémité de l'arc
    STEntity node1 = stg.getEndpoints(edge).getFirst();
    STEntity node2 = stg.getEndpoints(edge).getSecond();

    //dates d'existence de l'arc
    Set<FuzzyTemporalInterval> times = new HashSet<FuzzyTemporalInterval>(edge.getTimeSerie().getValues().keySet());
    //suppression de l'arc
    stg.removeEdge(edge);
    //boucle sur les temporalités d'existence de' l'arc
    for(FuzzyTemporalInterval t : times){
      if(!edge.existsAt(t)){
        continue;
      }
      // on regarde le nombre d'arcs incident à node1 à cette date
      //le sommet a pas été supprimé avec la suppresion d l'arc (donc pas sommet isolé)
      // on compte le nombre d'arcs incidents à node1 qui existe à t
      int incidentNumber = stg.getIncidentEdgessAt(node1, t).size();
      if(incidentNumber == 0){
        //y en a pas ? edge était le seul
        // on supprime l'existence de node1 à cette date et sa geom et attributs
        node1.existsAt(t,false);
        node1.setGeometryAt(t, null);
        node1.setWeightAt(t, -1);
        for(STProperty<Double> ind : node1.getTIndicators()){
          ind.setValueAt(t, null);
        }
      }
      else if(incidentNumber == 1){
        //est-ce un sommet ficif?
        if(node1.getGeometryAt(t) == null){
          //non
          //on fait rien
        }
        else{
          //on va enlever le caractère fictif du sommet
          node1.existsAt(t, true);
        }
      }
      else if(incidentNumber>=2){
        //on ne fait iren
      }



      //le sommet a pas été supprimé avec la suppresion d l'arc (donc pas sommet isolé)
      // on compte le nombre d'arcs incidents à node1 qui existe à t
      incidentNumber = stg.getIncidentEdgessAt(node2, t).size();
      if(incidentNumber == 0){
        //y en a pas ? edge était le seul
        // on supprime l'existence de node1 à cette date et sa geom et attributs
        node2.existsAt(t,false);
        node2.setGeometryAt(t, null);
        node2.setWeightAt(t, -1);
        for(STProperty<Double> ind : node2.getTIndicators()){
          ind.setValueAt(t, null);
        }
      }
      else if(incidentNumber == 1){
        //est-ce un sommet ficif?
        if(node2.getGeometryAt(t) == null){
          //non
          //on fait rien
        }
        else{
          //on va enlever le caractère fictif du sommet
          node2.existsAt(t, true);
        }
      }
      else if(incidentNumber>=2){
        //on ne fait iren
      }


    }

    if(stg.getIncidentEdges(node1).size() == 0){
      stg.removeVertex(node1);
    }
    if(stg.getIncidentEdges(node2).size() == 0){
      stg.removeVertex(node2);
    }

  }


  public static void deleteEdge(STGraph stg, STEntity edge, FuzzyTemporalInterval t){
    if(!edge.existsAt(t)){
      // l'arc n'existe pas à t
      return;
    }
    // extrémité de l'arc
    STEntity node1 = stg.getEndpoints(edge).getFirst();
    STEntity node2 = stg.getEndpoints(edge).getSecond();
    Set<FuzzyTemporalInterval> times = new HashSet<FuzzyTemporalInterval>(edge.getTimeSerie().getValues().keySet());
    if(times.size() == 1){
      DeleteEdge.deleteEdge(stg, edge);
      return;
    }
    edge.existsAt(t, false);
    // on regarde le nombre d'arcs incident à node1 à cette date
    //le sommet a pas été supprimé avec la suppresion d l'arc (donc pas sommet isolé)
    // on compte le nombre d'arcs incidents à node1 qui existe à t
    int incidentNumber = stg.getIncidentEdgessAt(node1, t).size();
    if(incidentNumber == 0){
      //y en a pas ? edge était le seul
      // on supprime l'existence de node1 à cette date et sa geom et attributs
      node1.existsAt(t,false);
      node1.setGeometryAt(t, null);
      node1.setWeightAt(t, -1);
      for(STProperty<Double> ind : node1.getTIndicators()){
        ind.setValueAt(t, null);
      }
    }
    else if(incidentNumber == 1){
      //est-ce un sommet ficif?
      if(node1.getGeometryAt(t) == null){
        //non
        //on fait rien
      }
      else{
        //on va enlever le caractère fictif du sommet
        node1.existsAt(t, true);
      }
    }
    else if(incidentNumber>=2){
      //on ne fait iren
    }

    //le sommet a pas été supprimé avec la suppresion d l'arc (donc pas sommet isolé)
    // on compte le nombre d'arcs incidents à node1 qui existe à t
    incidentNumber = stg.getIncidentEdgessAt(node2, t).size();
    if(incidentNumber == 0){
      //y en a pas ? edge était le seul
      // on supprime l'existence de node1 à cette date et sa geom et attributs
      node2.existsAt(t,false);
      node2.setGeometryAt(t, null);
      node2.setWeightAt(t, -1);
      for(STProperty<Double> ind : node2.getTIndicators()){
        ind.setValueAt(t, null);
      }
    }
    else if(incidentNumber == 1){
      //est-ce un sommet ficif?
      if(node2.getGeometryAt(t) == null){
        //non
        //on fait rien
      }
      else{
        //on va enlever le caractère fictif du sommet
        node2.existsAt(t, true);
      }
    }
    else if(incidentNumber>=2){
      //on ne fait iren
    }

    if(stg.getIncidentEdges(node1).size() == 0){
      stg.removeVertex(node1);
    }
    if(stg.getIncidentEdges(node2).size() == 0){
      stg.removeVertex(node2);
    }

  }

  public static void deleteEdge(STGraph stg, String shp){
    IPopulation<IFeature> liens = ShapefileReader.read(shp);
    System.out.println(liens.size());
    for(IFeature f :liens){
      STEntity e= null;
      for(STEntity ee: stg.getEdges()){
        if(ee.getGeometry().toGeoxGeometry().distance(f.getGeom())<0.0001){
          e = ee;
          break;
        }
      }
      if(e == null){
        System.out.println("null edge : " + f.getGeom());
        continue;
      }
      DeleteEdge.deleteEdge(stg,e);
    }
    //stg.updateGeometries();
  }

  public static void deleteEdgeAt(STGraph stg, FuzzyTemporalInterval t, String shp){
    IPopulation<IFeature> liens = ShapefileReader.read(shp);
    System.out.println(liens.size());
    for(IFeature f :liens){
      STEntity e= null;
      for(STEntity ee: stg.getEdges()){
        if(ee.getGeometry().toGeoxGeometry().distance(f.getGeom())<0.00001){
          e = ee;
          break;
        }
      }
      if(e == null){
        System.out.println(f.getGeom().toString());
        continue;
      }
      DeleteEdge.deleteEdge(stg,e, t);
    }
    //stg.updateGeometries();
  }

  public static void main(String[] args) {
    String shpLiens = "/home/bcostes/Bureau/TAG/corrections_patterns/etape5/delete_edges.shp";

    String inputStg ="/home/bcostes/Bureau/TAG/corrections_patterns/test/tag_corrected.tag";
    String outputStg ="/home/bcostes/Bureau/TAG/corrections_patterns/test/tag_corrected.shp";
    String outputStg2 ="/home/bcostes/Bureau/TAG/corrections_patterns/test/tag_corrected.tag";

    STGraph stg= TAGIoManager.deserialize(inputStg);

    DeleteEdge.deleteEdge(stg, shpLiens);

//    FuzzyTemporalInterval t;
//    try {
//     // t = new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4);
//      t = new FuzzyTemporalInterval
//        (new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4);
//      //t = new FuzzyTemporalInterval(new double[]{1887,1888,1888,1889},new double[]{0,1,1,0}, 4);
//      //t = new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4);
//      
//       t =new FuzzyTemporalInterval
//         (new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4);
//      DeleteEdge.deleteEdgeAt(stg, t, shpLiens);
//    } catch (XValuesOutOfOrderException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (YValueOutOfRangeException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }



    TAGIoManager.serializeBinary(stg, outputStg2);

    TAGIoManager.exportTAG(stg, outputStg);
    TAGIoManager.exportSnapshots(stg, outputStg, TAGIoManager.NODE_AND_EDGE);
    
    

    IPopulation<IFeature> pop = new Population<IFeature>();



    ReincarnationSTPattern pattern = new ReincarnationSTPattern();       
    for(STEntity edge : stg.getEdges()){
      STProperty<Boolean> ts = edge.getTimeSerie();
      if(pattern.find(ts)){
        // System.out.println("Réincarnation ! ");
        List<FuzzyTemporalInterval> times = pattern.findEvent(edge.getTimeSerie());
        DefaultFeature f = new DefaultFeature(edge.getGeometry().toGeoxGeometry());
        AttributeManager.addAttribute(f, "death", times.get(0).toString(), "String");
        AttributeManager.addAttribute(f, "re-live", times.get(1).toString(), "String");
        pop.add(f);
      }
    }
    System.out.println(pop.size());
    ShapefileWriter.write(pop,"/home/bcostes/Bureau/TAG/corrections_patterns/test/reincarnations.shp");
    pop.clear();


    AppearanceSTPattern pattern2 = new AppearanceSTPattern();       
    for(STEntity edge : stg.getEdges()){
      STProperty<Boolean> ts = edge.getTimeSerie();

      if(pattern2.find(ts)){
        List<FuzzyTemporalInterval> times = pattern2.findEvent(ts);
        DefaultFeature f = new DefaultFeature(edge.getGeometry().toGeoxGeometry());
        AttributeManager.addAttribute(f, "live", times.get(0).toString(), "String");
        AttributeManager.addAttribute(f, "death", times.get(1).toString(), "String");
        pop.add(f);
      }        
    }      
    System.out.println(pop.size());
    ShapefileWriter.write(pop, "/home/bcostes/Bureau/TAG/corrections_patterns/test/appearances.shp");

  }

}
