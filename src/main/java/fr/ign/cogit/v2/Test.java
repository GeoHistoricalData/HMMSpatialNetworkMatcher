package fr.ign.cogit.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.manual.corrections.tag.criterions.WidthMassFunction;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.STProperty;

public class Test {

  public static void main(String[] args) throws XValuesOutOfOrderException, YValueOutOfRangeException {


    //  IPopulation<IFeature> inp = ShapefileReader.read("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/travaux_1789_1854/travaux_pol.shp");

    //    String inputStg ="/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/final/tag_corrected.tag";
    //    STGraph stg = TAGIoManager.deserialize(inputStg);   

    //   




    //    
    //    IPopulation<IFeature> out = new Population<IFeature>();
    //    for(STEntity e : stg.getEdges()){
    //      System.out.println("-------------");
    //      for(JSonAttribute a: e.getJsonAttributes()){
    //        if(a.getName().equals("adresse")){continue;}
    //        System.out.println(a.getName()+" "+ a.getJson().toString());
    //        IDirectPosition p;
    //        try {
    //          p = WktGeOxygene.makeGeOxygene(a.getJson().get("geometry").toString()).centroid();
    //          IFeature f = new DefaultFeature(new GM_LineString(Arrays.asList(Operateurs.milieu((ILineString)e.getGeometry().toGeoxGeometry()),p )));
    //          AttributeManager.addAttribute(f, "date", a.getJson().get("date").toString(), "String");
    //          out.add(f);
    //        } catch (ParseException e1) {
    //          // TODO Auto-generated catch block
    //          e1.printStackTrace();
    //        } catch (JSONException e1) {
    //          // TODO Auto-generated catch block
    //          e1.printStackTrace();
    //        }
    //      }
    //    }
    //    ShapefileWriter.write(out,"/home/bcostes/Bureau/t.shp");


    //    IPopulation<IFeature> inm = ShapefileReader.read("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/travaux_1789_1854/matching_manual_merged.shp");
    ////
    ////  
    ////    
    ////    
    ////   
    ////
    //    inp.initSpatialIndex(Tiling.class,false);
    //    inm.initSpatialIndex(Tiling.class,false);
    //    IPopulation<IFeature> out = new Population<IFeature>();
    //    
    //
    //    for(IFeature link: inm){
    //      Collection<IFeature> polygons = inp.select(link.getGeom(),50);
    //      IFeature pol = null;
    //      for(IFeature p:polygons){
    //        if(p.getGeom().distance(new GM_Point(link.getGeom().coord().get(0)))<0.005 ||
    //            p.getGeom().distance(new GM_Point(link.getGeom().coord().get(1)))<0.005){
    //          pol= p;
    //          break;
    //        }
    //      }
    //        if(pol==null){System.out.println(link.getGeom());continue;}
    //
    //        String date = pol.getAttribute("date").toString();
    //        
    //        
    //        for(STEntity e : stg.getEdges()){
    //          if(e.getGeometry().toGeoxGeometry().distance(new GM_Point(link.getGeom().coord().get(0)))<0.005 ||
    //              e.getGeometry().toGeoxGeometry().distance(new GM_Point(link.getGeom().coord().get(1)))<0.005){
    //
    //            JSonAttribute att = new JSonAttribute("works");
    //            att.putO("geometry", WktGeOxygene.makeWkt(pol.getGeom()));
    //            att.hasGeometry(true);
    //            att.putI("date", (int)Integer.parseInt(date));       
    //            e.getJsonAttributes().add(att);
    //          }
    //        }
    //      }
    //    TAGIoManager.serializeBinary(stg, "/home/bcostes/Bureau/tag_works.tag");

    //    System.out.println(out.size()+" / " + inm.size());
    //
    //    ShapefileWriter.write(out, "/home/bcostes/Bureau/matching.shp");
    //    

    //  String inputStg ="/home/bcostes/Bureau/stag_json/stag_json.tag";
    //  STGraph stg = TAGIoManager.deserialize(inputStg);
    //  
    //  stg.addFuzzyTemporalInterval(new FuzzyTemporalInterval(new double[]{1853,1854,1854,1855},new double[]{0,1,1,0}, 4),1.);


    //    //  
    //
    //
    //    //    
    //    //    
    //    //
    //    //    
    String inputStg ="/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/final/tag_corrected.tag";
    STGraph stg = TAGIoManager.deserialize(inputStg);



    STProperty<Double> accuracies = new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Attribute, "Accuracies");
    Map<FuzzyTemporalInterval, Double> accuraciesMap = new HashMap<FuzzyTemporalInterval, Double>();
    accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4),
        1.);

    accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4),
        1.);
    accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4),
        1.);
    accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1870,1871,1871,1872},new double[]{0,1,1,0}, 4),
        1.);
    accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1887,1888,1888,1889},new double[]{0,1,1,0}, 4),
        1.);
    accuracies.setValues(accuraciesMap);
    stg.setAccuracies(accuracies);
    stg.updateGeometries();
    System.out.println(stg.getEdgeCount()+" "+ stg.getVertexCount());
    
    
    TAGIoManager.exportTAG(stg, "/home/bcostes/Bureau/data_igast/stag_paris_5snaps_1789_1888.tag");
    TAGIoManager.exportSnapshots(stg, "/home/bcostes/Bureau/data_igast/new_snapshots_geom_ini/stag_snapshot.shp", TAGIoManager.EDGE_ONLY);
    TAGIoManager.exportTAGSnapshots(stg, "/home/bcostes/Bureau/data_igast/new_snapshots_geom_stag/stag_snapshot.shp");
    
    
    //    //    //stg.setPlanar();
    //    //    //System.out.println(stg.getEdgeCount()+" "+ stg.getVertexCount());
    //    //
    //    //    TAGIoManager.exportTAG(stg, "/home/bcostes/Bureau/test.shp");
    //
    //    //    System.out.println(stg.getEdgeCount()+" "+ stg.getVertexCount());
    //
    //    //    IPopulation<IFeature> out = new Population<IFeature>();
    //    //
    //    //    
    //    //    LifeSTPattern p = new LifeSTPattern();
    //    //    for(STEntity e: stg.getEdges()){
    //    //      if(p.find(e.getTimeSerie())){
    //    //        out.add(new DefaultFeature(e.getGeometry().toGeoxGeometry()));
    //    //      }
    //    //    }
    //    //
    //    //    ShapefileWriter.write(out, "/home/bcostes/Bureau/travaux/tag_works/birth_patterns.shp");
    //    //
    //    //    out = new Population<IFeature>();
    //    //    DeathSTPattern p2 = new DeathSTPattern();
    //    //    for(STEntity e: stg.getEdges()){
    //    //      if(p2.find(e.getTimeSerie())){
    //    //        out.add(new DefaultFeature(e.getGeometry().toGeoxGeometry()));
    //    //      }
    //    //    }
    //    //
    //    //    ShapefileWriter.write(out, "/home/bcostes/Bureau/travaux/tag_works/death_patterns.shp");
    //    //   
    //    IPopulation<IFeature> out2 = new Population<IFeature>();
    //
    //    //new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4)
    //    //new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4)
    //
    //    for(STEntity e :stg.getEdges())
    //    {    
    //      //arcs crées après t1
    //      if(e.existsAt(new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4)) 
    //          && e.existsAt(new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4))){
    //
    //          IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
    //          AttributeManager.addAttribute(f, "proba", 1, "Integer");
    //
    //          out2.add(f);
    //          continue;
    //        }
    //
    //        if(e.existsAt(new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4))){
    //          if(!e.getJsonAttributes().isEmpty()){
    //            if(JSonAttribute.getJsonAttributeByName("works", e.getJsonAttributes()) == null || 
    //                JSonAttribute.getJsonAttributeByName("works", e.getJsonAttributes()).isEmpty()){
    //              IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
    //              AttributeManager.addAttribute(f, "proba", 1, "Integer");
    //
    //              out2.add(f);
    //              continue;
    //            }
    //            try {
    //              int date = JSonAttribute.getJsonAttributeByName("works", e.getJsonAttributes()).iterator().next()
    //                  .getJson().getInt("date");
    //              if( date >= 1829){
    //                continue;
    //              }
    //              else{
    //                double[] cxval = new double[] { 1789,1808,1828};
    //                double[] cyval = new double[] { 1,1,0 };
    //                FuzzySet continuation_filter = new FuzzySet(cxval, cyval, 3);
    //                IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
    //                AttributeManager.addAttribute(f, "proba", continuation_filter.getMembership(date), "Double");
    //                AttributeManager.addAttribute(f, "date",date, "Integer");
    //
    //                out2.add(f);
    //              }
    //            }
    //            catch (JSONException e1) {
    //              // TODO Auto-generated catch block
    //              e1.printStackTrace();
    //            }
    //          }
    //          else{
    //            IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
    //            AttributeManager.addAttribute(f, "proba", 1, "Integer");
    //
    //            out2.add(f);
    //          }
    //        }   
    //      }
    //      ShapefileWriter.write(out2, "/home/bcostes/Bureau/1808_proba.shp");


    //
    //    stg.neighborhoodEdgeLocalIndicator(new ClosenessCentrality(), 3, NORMALIZERS.CONVENTIONAL, false);
    //    stg.neighborhoodEdgeLocalIndicator(new ClosenessCentrality(), 5, NORMALIZERS.CONVENTIONAL, false);
    //    stg.neighborhoodEdgeLocalIndicator(new ClosenessCentrality(), 7, NORMALIZERS.CONVENTIONAL, false);
    //    stg.neighborhoodEdgeLocalIndicator(new ClosenessCentrality(), 10, NORMALIZERS.CONVENTIONAL, false);
    //
    //    stg.neighborhoodEdgeLocalIndicator(new StraightnessCentrality(), 3, NORMALIZERS.CONVENTIONAL, false);
    //    stg.neighborhoodEdgeLocalIndicator(new StraightnessCentrality(), 5, NORMALIZERS.CONVENTIONAL, false);
    //    stg.neighborhoodEdgeLocalIndicator(new StraightnessCentrality(), 7, NORMALIZERS.CONVENTIONAL, false);
    //    stg.neighborhoodEdgeLocalIndicator(new StraightnessCentrality(), 10, NORMALIZERS.CONVENTIONAL, false);
    //    
    //
    //
    //    TAGIoManager.serializeBinary(stg, "/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/final_ind_local/tag_corrected_ind.tag");
    //    TAGIoManager.exportTAG(stg, "/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/final_ind_local/tag_corrected_ind.shp");
    //    TAGIoManager.serializeXml(stg, "/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/final_ind_local/tag_corrected_ind.xml");
    //

  }

  public static double getWidth(ILineString le, IPopulation<IFeature> polygons, int beg){

    // géométrie de la rue
    Collection<IFeature> closestPolygons = polygons.select(le, 0);
    if(!closestPolygons.isEmpty()){
      // on regarde si cette intersection contient au moins la moitié de la ligne
      Iterator<IFeature> it = closestPolygons.iterator();
      IGeometry union = it.next().getGeom();
      while(it.hasNext()){
        union = union.union(it.next().getGeom());
      }
      if(union != null && union.intersects(le) && union.intersection(le) != null && union.intersection(le).length() > 0.75* le.length()){
        return -1;
      }
    }
    List<Double> values = new ArrayList<Double>();
    for(int i=beg+1; i<100-beg; i+=5){
      IDirectPosition point = Operateurs.pointEnAbscisseCurviligne(le, le.length() * ((double)i)/100.);
      //milieu
      closestPolygons = polygons.select(point, 0);
      if(closestPolygons.isEmpty()){
        // milieu pas dans un ilot
        WidthMassFunction w= new WidthMassFunction();
        double v = w.getNormalWidth(point, le, i, polygons);
        if(v >0){
          values.add(v);
        }
      }             
    }

    if(values.size() != 0){
      //moyenne, écart type
      double moyenne = 0;
      double sigma = 0;
      for (Double t : values) {
        moyenne += t;
      }
      moyenne /= (double) values.size();
      //      for (Double t : values) {
      //        sigma += (moyenne - t) * (moyenne - t);
      //      }
      //      sigma /= (double) values.size();
      //      sigma = Math.sqrt(sigma);
      //on retire tout ce qui est supérieur à moyenne +- 3sigma
      //      double width =0;
      //      double cpt=0.;
      //      for (Double t : values) {
      //        if(t <= moyenne + 2*sigma && t >= moyenne - 2*sigma){
      //          width += t;
      //          cpt += 1.;
      //        }
      //      }
      //  return  (float)(width / cpt);
      return (double)moyenne;
    }
    return -1;
  }

}
