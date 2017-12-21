package fr.ign.cogit.v2.manual.corrections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.geometry.LightDirectPosition;
import fr.ign.cogit.v2.geometry.LightLineString;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.patterns.AppearanceSTPattern;
import fr.ign.cogit.v2.patterns.ReincarnationSTPattern;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.STProperty;
import fr.ign.cogit.v2.utils.GeometryUtils;

public class CorrectionInterpolation {

  public static int cpt = 0;


  /**
   * On va mettre le flag de la timeserie de edge à true pour t
   * @param stg
   * @param edge
   * @param t
   */
  public static boolean correct(STGraph stg, STEntity edge, FuzzyTemporalInterval t){
    if(edge.existsAt(t)){
      //existe déja à t
      CorrectionInterpolation.cpt++;
      return true;
    }      
    //récupération des extrémités
    STEntity node1 = stg.getEndpoints(edge).getFirst();
    STEntity node2 = stg.getEndpoints(edge).getSecond();
    Set<STEntity> incidents = new HashSet<STEntity>();
    incidents.addAll(stg.getIncidentEdges(node1));
    incidents.addAll(stg.getIncidentEdges(node2));
    incidents.remove(edge);
    boolean incidentExistsAtT = false;
    for(STEntity incident :incidents){
      if(incident.existsAt(t)){
        incidentExistsAtT = true;
        break;
      }
    }
    //il faut au moins un arc connecté qui existe à t
    if(!incidentExistsAtT){
      CorrectionInterpolation.cpt++;
      System.out.println(edge.getGeometry().toString());
      return false;
    }

    if(!node1.existsAt(t)){
      node1.existsAt(t, true);
    }
    if(!node2.existsAt(t)){
      node2.existsAt(t, true);
    }
    edge.existsAt(t, true);

    List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>(edge.getTimeSerie().getValues().keySet());
    times.remove(null);
    for(FuzzyTemporalInterval tt: new ArrayList<FuzzyTemporalInterval>(times)){
      if(!edge.existsAt(tt)){
        times.remove(tt);
      }
    }
    Collections.sort(times);

    times.remove(t);



    //Calcul de la nouvelle géométrie de edge pour la date t
    //calcul des coordonnées des extrémitées
    IDirectPosition pp1 = new DirectPosition(0.,0.);
    IDirectPosition pp2 = new DirectPosition(0.,0.);
    int cpt1 =0, cpt2=0;
    if(node1.getGeometryAt(t) == null){
      IDirectPosition pt = node1.getGeometry().toGeoxGeometry().coord().get(0);
      pp1.setX(pt.getX());
      pp1.setY(pt.getY());
    }
    else{
      pp1.setX(node1.getGeometryAt(t).toGeoxGeometry().coord().get(0).getX());
      pp1.setY(node1.getGeometryAt(t).toGeoxGeometry().coord().get(0).getY());
    }
    // }
    if(node2.getGeometryAt(t) == null){
      IDirectPosition pt = node2.getGeometry().toGeoxGeometry().coord().get(0);
      pp2.setX(pt.getX());
      pp2.setY(pt.getY());
    }
    else{
      pp2.setX(node2.getGeometryAt(t).toGeoxGeometry().coord().get(0).getX());
      pp2.setY(node2.getGeometryAt(t).toGeoxGeometry().coord().get(0).getY());
    }


    //Récupération des géométries de edge aux autres dates

    ILineString line =(ILineString)edge.getGeometry().toGeoxGeometry();

    if(line.getControlPoint(0).distance(pp1) >line
        .getControlPoint(line.coord().size() - 1).distance(pp1)) {
      line = line.reverse();
    }

    double lemax = line.length();
    int nbPts = (int) ((double) lemax / 1);
    // resampling of linestrings
    double pasVariable = line.length() / (double) (nbPts);
    line = Operateurs.resampling(line, pasVariable);
    Map<ILineString, Double> newLine = new HashMap<ILineString, Double>();
    newLine.put(line, 1.);
    newLine = GeometryUtils.helmert(newLine, pp1, pp2);

    IDirectPositionList lprovE = new DirectPositionList();
    lprovE.addAll(newLine.keySet().iterator().next().getControlPoint());

    GeometryUtils.filterLowAngles(lprovE, 5);
    GeometryUtils.filterLargeAngles(lprovE, 150);

    node1.setGeometryAt(t, new LightDirectPosition(pp1));
    node2.setGeometryAt(t, new LightDirectPosition(pp2));

    for(STEntity e : stg.getIncidentEdges(node1)){
      if(e.equals(edge)){
        continue;
      }
      if(!e.existsAt(t)){
        continue;
      }
      ILineString l = (ILineString)e.getGeometryAt(t).toGeoxGeometry();
      if(l.startPoint().distance(lprovE.get(0)) < l.endPoint().distance(lprovE.get(0))){
        l.setControlPoint(0, lprovE.get(0));
      }
      else{
        l.setControlPoint(l.getControlPoint().size()-1, lprovE.get(0));
      }
      IDirectPositionList list = l.coord();
      GeometryUtils.filterLowAngles(list, 5);
      GeometryUtils.filterLargeAngles(list, 150);
      e.setGeometryAt(t, new LightLineString(list));
    }
    for(STEntity e : stg.getIncidentEdges(node2)){
      if(e.equals(edge)){
        continue;
      }
      if(!e.existsAt(t)){
        continue;
      }
      ILineString l = (ILineString)e.getGeometryAt(t).toGeoxGeometry();
      if(l.startPoint().distance(lprovE.get(lprovE.size()-1)) < l.endPoint().distance(lprovE.get(lprovE.size()-1))){
        l.setControlPoint(0, lprovE.get(lprovE.size()-1));
      }
      else{
        l.setControlPoint(l.getControlPoint().size()-1, lprovE.get(lprovE.size()-1));
      }
      IDirectPositionList list = l.coord();
      GeometryUtils.filterLowAngles(list, 5);
      GeometryUtils.filterLargeAngles(list, 150);
      e.setGeometryAt(t, new LightLineString(list));
    }

    //Attribution de la geom
    edge.setGeometryAt(t, new LightLineString(lprovE));

    //stg.updateGeometries();

    return true;

  }

  public static void addDateToEdge(STGraph stg, FuzzyTemporalInterval t, String shp){
    IPopulation<IFeature> liens = ShapefileReader.read(shp);
    int cpt=0;
    System.out.println(liens.size());

    for(IFeature f :liens){
      cpt++;
      System.out.println(cpt + " / " + liens.size());
      STEntity edge= null;
      for(STEntity ee: stg.getEdges()){
        if(ee.getGeometry().toGeoxGeometry().distance(f.getGeom())<0.0001){
          edge = ee;
          break;
        }
      }
      if(edge == null) {
        System.out.println(f.getGeom().toString());
        continue;
      }
      //on a trouvé l'edge a modifier
      if(edge.existsAt(t)){
        //il existe déja pour t
        continue;
      }
      boolean corrected = CorrectionInterpolation.correct(stg, edge,t);
      if(!corrected){
        System.out.println("Date not added for edge : " +f.getGeom().toString());
      }
    }

  }

  public static void correctAllReincarnations(STGraph stg){
    ReincarnationSTPattern app = new ReincarnationSTPattern();
    Set<STEntity> reincarnations = new HashSet<STEntity>();
    for(STEntity e: stg.getEdges()){
      if(app.find(e.getTimeSerie())){
        reincarnations.add(e);
      }
    }
    Set<STEntity> newReincarnations = new HashSet<STEntity>();
    while(!reincarnations.isEmpty() && newReincarnations.size() != reincarnations.size()){
      newReincarnations.clear();
      newReincarnations.addAll(reincarnations);
      for(STEntity e: newReincarnations){
        CorrectionInterpolation.correct(stg, e, app.findEvent(e.getTimeSerie()).get(0));
      }
      reincarnations.clear();
      for(STEntity e: stg.getEdges()){
        if(app.find(e.getTimeSerie())){
          reincarnations.add(e);
        }
      }
    }

  }

  public static void correctSomeReincarnationPatterns(STGraph stg, List<String> patterns){
    ReincarnationSTPattern rec = new ReincarnationSTPattern();
    Set<STEntity> reincarnations = new HashSet<STEntity>();
    for(STEntity e: stg.getEdges()){
      if(rec.find(e.getTimeSerie()) && patterns.contains(rec.getSequence(e.getTimeSerie()))){
        reincarnations.add(e);
      }
    }
    Set<STEntity> newReincarnations = new HashSet<STEntity>();
    newReincarnations.clear();
    newReincarnations.addAll(reincarnations);
    int cpt=0;
    for(STEntity e: newReincarnations){
      cpt++;
      System.out.println(cpt + " / " + newReincarnations.size()+ " done.");
      while(rec.find(e.getTimeSerie())){
        CorrectionInterpolation.correct(stg, e, rec.findEvent(e.getTimeSerie()).get(0));
      }
    }
    System.out.println(CorrectionInterpolation.cpt);

  }

  public static void correctSomeReincarnationPatterns(STGraph stg, String shp){
    IPopulation<IFeature> liens = ShapefileReader.read(shp);
    ReincarnationSTPattern rec = new ReincarnationSTPattern();
    int cpt=0;
    for(IFeature f :liens){
      cpt++;
      System.out.println(cpt + " / " + liens.size());
      STEntity edge= null;
      for(STEntity ee: stg.getEdges()){
        if(ee.getGeometry().toGeoxGeometry().distance(f.getGeom())<0.000001){
          edge = ee;
          break;
        }
      }
      if(edge == null) {
        System.out.println(f.getGeom().toString());
        continue;
      }
      //on a trouvé l'edge a modifier
      //on vérifie qu'il s'agit bien d'une réincarnation
      if(!rec.find(edge.getTimeSerie())){
        continue;
      }
      while(rec.find(edge.getTimeSerie())){
        //tant qu'on trouve que cet arc corrspond a une reinc,on corrige pour la date trouvée
        boolean corrected = CorrectionInterpolation.correct(stg, edge, rec.findEvent(edge.getTimeSerie()).get(0));
        if(!corrected){
          System.out.println("Reincarnation pattern not corrected : " +f.getGeom().toString());
          break;
        }
      }
    }
  }


  public static void main(String args[]){


    //String inputLiens ="/home/bcostes/Bureau/TAG/corrections_patterns/etape5/correct_reincarnations.shp";
    
    String inputLiens ="/home/bcostes/Bureau/TAG/corrections_patterns/etape5/add_date_1889.shp";

    String inputStg ="/home/bcostes/Bureau/TAG/corrections_patterns/test/tag_corrected.tag";
    String outputStg ="/home/bcostes/Bureau/TAG/corrections_patterns/test/tag_corrected.shp";
    String outputStg2 ="/home/bcostes/Bureau/TAG/corrections_patterns/test/tag_corrected.tag";

    STGraph stg= TAGIoManager.deserialize(inputStg);

    // CorrectionInterpolation.correctSomeReincarnationPatterns(stg, Arrays.asList("00101", "11101", "01101"));
    //CorrectionInterpolation.correctSomeReincarnationPatterns(stg, inputLiens);
    
    FuzzyTemporalInterval t;
    try {
     // t = new FuzzyTemporalInterval
       //   (new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4);
      //t = new FuzzyTemporalInterval(new double[]{1887,1888,1888,1889},new double[]{0,1,1,0}, 4);
     // t = new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4);
      //t = new FuzzyTemporalInterval(new double[]{1870,1871,1871,1872},new double[]{0,1,1,0}, 4);
      t = new FuzzyTemporalInterval(new double[]{1887,1888,1888,1889},new double[]{0,1,1,0}, 4);
      //t = new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4);
      CorrectionInterpolation.addDateToEdge(stg, t, inputLiens);
    } catch (XValuesOutOfOrderException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (YValueOutOfRangeException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }


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
