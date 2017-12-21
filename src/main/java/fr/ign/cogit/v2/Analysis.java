package fr.ign.cogit.v2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.lwjgl.Sys;

import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;

public class Analysis {

  public static void main(String[] args) {
    //String inputStg ="/home/bcostes/Bureau/tag_copie/tag_corrected_ind.tag";



    String inputStg ="/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/final_ind/tag_corrected_ind.tag";
    STGraph stg = TAGIoManager.deserialize(inputStg);

    //    for(FuzzyTemporalInterval t : stg.getTemporalDomain().asList()){
    //      String s = "";
    //      s+=t.getX(0)+"\n";
    //      for(GraphEntity e : stg.getSnapshotAt(t).getEdges()){
    //        s += e.getLocalIndicators().get("Betw")+"\n";
    //      }
    //      FileWriter fw;
    //      try {
    //        fw = new FileWriter("/media/bcostes/Data/Benoit/these/analyses/indicateurs/locaux/centralities/betw_profil_"+t.getX(0)+".txt");
    //        BufferedWriter bw = new BufferedWriter(fw);
    //        bw.write(s);
    //        bw.flush();
    //        bw.close();
    //
    //      } catch (IOException e) {
    //        // TODO Auto-generated catch block
    //        e.printStackTrace();
    //      }
    //    }
    //   
    //  }


    //        for(FuzzyTemporalInterval t :stg.getTemporalDomain().asList()){
    //          IPopulation<IFeature> out = new Population<IFeature>();
    //    
    //          for(STEntity e: stg.getEdges()){
    //            if(!e.existsAt(t)){
    //              continue;
    //            }
    //            ILineString l = Operateurs.resampling((ILineString)e.getGeometry().toGeoxGeometry(), 10);
    //            for(IDirectPosition p: l.getControlPoint()){
    //              IFeature f = new DefaultFeature(new GM_Point(p));
    //              for(STProperty<Double> att: e.getTIndicators()){
    //                String s = att.getName();
    //                double d = e.getIndicatorAt(s, t);
    //                AttributeManager.addAttribute(f, s, d, "Double");
    //              }
    //              out.add(f);
    //            }
    //          }s
    //          ShapefileWriter.write(out, "/home/bcostes/Bureau/centralities/points_"+t.getX(0)+".shp");
    //        }
    //      }


    
    List<FuzzyTemporalInterval> times = stg.getTemporalDomain().asList();
    Collections.sort(times);
    for(int i=0; i< times.size()-1; i++){
      String  s = times.get(i).getX(0)+"-"+times.get(i+1).getX(0)+"\n";

      Set<Double> betw = new HashSet<Double>();
      for(STEntity e : stg.getEdgesAt(times.get(i))){
        betw.add((double)Math.round(1000000000 * e.getIndicatorAt("Betw", times.get(i)))/1000000000);
      }
      List<Double> betwOrdered = new ArrayList<Double>(betw);
      Collections.sort(betwOrdered);
      Collections.reverse(betwOrdered);


      
      Map<STEntity, Integer> order = new HashMap<STEntity, Integer>();
      int cpt = 1;
      Set<STEntity> eee = new HashSet<STEntity>();
      eee.addAll(stg.getEdgesAt(times.get(i)));
      for(Double d : betwOrdered){
        for(STEntity e: eee){
          if(d == (double)Math.round(1000000000 * e.getIndicatorAt("Betw", times.get(i)))/1000000000){
            order.put(e, cpt);
          }
        }
        cpt++;
      }
      cpt--;
      
      int newedges = 0;
      for(STEntity e : order.keySet()){
        if(!e.existsAt(times.get(i+1))){
          //n'existait pas déja avant
          newedges++;
        }
      }

      
      for(int j=1; j<=100; j++){
        int orderMax = (int) (cpt* j / 100 );
        int count = 0;
        int countNew = 0;
        for(STEntity e : order.keySet()){
          if(order.get(e)<= orderMax){
            count ++;
            if(!e.existsAt(times.get(i+1))){
              //n'existait pas déja avant
              countNew++;
            }
          }
        }
        double percent = 100. * ((double)countNew / (double)newedges);
        s+= percent+"\n";
      }


      FileWriter fw;
      try {
        fw = new FileWriter("/media/bcostes/Data/Benoit/these/analyses/indicateurs/locaux/centralities/diff/betw/deleted_edges/"+
            "deleted_edges_total"+times.get(i).getX(0)+".txt");
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(s);
        bw.flush();
        bw.close();

      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }


  }

  //  String inputStg ="/media/bcostes/Data/Benoit/these/analyses/indicateurs/locaux/orientation/orientation_0.shp";
  //  IPopulation<IFeature> in = ShapefileReader.read(inputStg);
  //  
  //  IPopulation<IFeature> out = new Population<IFeature>();
  //  for(IFeature fin : in){
  //    ILineString l = Operateurs.resampling(new GM_LineString(fin.getGeom().coord()), 10);
  //    double d = Double.parseDouble(fin.getAttribute("ang").toString());
  //    for(IDirectPosition p: l.getControlPoint()){
  //      IFeature f = new DefaultFeature(new GM_Point(p));
  //      AttributeManager.addAttribute(f, "c", d, "Double");
  //      out.add(f);
  //    }
  //  }
  //  ShapefileWriter.write(out, "/home/bcostes/Bureau/points_angles.shp");
  //}


  //   
  //
  //
  //    System.out.println(stg.getVertexCount()+" "+ stg.getEdgeCount());
  //
  //    for(FuzzyTemporalInterval t : stg.getTemporalDomain().asList()){
  //      String s = t.getX(0)+"\n";
  //      JungSnapshot snap = stg.getSnapshotAt(t);
  //      List<Double> or = (new EdgesOrientation()).calculateGeometricalIndicator(snap);
  //      for(Double d : or){
  //        s+=d+"\n";
  //      }
  //      FileWriter fw;
  //      try {
  //        fw = new FileWriter("/media/bcostes/Data/Benoit/these/analyses/indicateurs/locaux/orientation/orientations_sansstag_"+t.getX(0)+".txt");
  //        BufferedWriter bw = new BufferedWriter(fw);
  //        bw.write(s);
  //        bw.flush();
  //        bw.close();
  //      
  //      } catch (IOException e) {
  //        // TODO Auto-generated catch block
  //        e.printStackTrace();
  //      }    }
  //    

  //    stg.graphGlobalIndicator(new Alpha(),false);
  //    stg.graphGlobalIndicator(new AveragePathLength(),false);
  //    stg.graphGlobalIndicator(new Beta(),false);
  //    stg.graphGlobalIndicator(new ClusteringCoefficient(),false);
  //    stg.graphGlobalIndicator(new Density(),false);
  //    stg.graphGlobalIndicator(new DetourIndex(),false);
  //    stg.graphGlobalIndicator(new Diameter(),false);
  //    stg.graphGlobalIndicator(new Gamma(),false);
  //    stg.graphGlobalIndicator(new MeanEdgeLength(),false);
  //    stg.graphGlobalIndicator(new MeanEdgesOrientation(),false);
  //    stg.graphGlobalIndicator(new MeanNodeDegree(),false);
  //    stg.graphGlobalIndicator(new Mu(),false);
  //    stg.graphGlobalIndicator(new Pi(),false);
  //    stg.graphGlobalIndicator(new TotalEdgeLength(),false);
  //    
  //    String s ="";
  //    for(STProperty<Double> ind : stg.getGlobalIndicators()){
  //      s+=ind.getName()+";";
  //      for(FuzzyTemporalInterval t : stg.getTemporalDomain().asList()){
  //        s+= ind.getValueAt(t)+";";
  //      }
  //      s = s.substring(0, s.length()-1);
  //      s+= "\n";
  //    }
  //




  // stg.edgeLocalIndicator(new MaxDistance(), NORMALIZERS.NONE, false);
  // TAGIoManager.serializeBinary(stg, "/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/final/tag_corrected_ind.tag");

  // TAGIoManager.exportSnapshots(stg, "/home/bcostes/Bureau/tmp/tag_corrected.shp", TAGIoManager.NODE_AND_EDGE);
  //}

}
