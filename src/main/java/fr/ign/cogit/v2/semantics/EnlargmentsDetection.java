package fr.ign.cogit.v2.semantics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.index.Tiling;
import fr.ign.cogit.morphogenesis.network.analysis.streetWidth.StreetWidth;
import fr.ign.cogit.v2.manual.corrections.tag.criterions.WidthMassFunction;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.utils.streetsWidth.StreetsWidth;
import nrc.fuzzy.FuzzySet;
import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;

public class EnlargmentsDetection {

  Map<FuzzyTemporalInterval, IPopulation<IFeature>> polygons;
  FuzzySet trust_filter;
  STGraph stg;

  public EnlargmentsDetection(STGraph stg, Map<FuzzyTemporalInterval, String> shps){
    this.stg = stg;
    this.polygons = new HashMap<FuzzyTemporalInterval, IPopulation<IFeature>>();
    for(FuzzyTemporalInterval t: shps.keySet()){
      IPopulation<IFeature> poly = ShapefileReader.read(shps.get(t));
      poly.initSpatialIndex(Tiling.class, false);
      this.polygons.put(t, poly);
    }

    try {
      // width < 10
      double[] cxval = new double[] { 1, 2, 2.5 };
      double[] cyval = new double[] { 0, 1, 1 };
      this.trust_filter = new FuzzySet(cxval, cyval, 3);


    } catch (XValuesOutOfOrderException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (YValueOutOfRangeException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /*
   * Detection d'élargissements, entre t et t+1 pour tout t
   */
  public void detectEnlargments(){

  }

  /*
   * Détection d'élargissements entre t1 et t2
   * On suppose t2 > t1
   */
  public void detectEnlargments(FuzzyTemporalInterval t1, FuzzyTemporalInterval t2){
    for(STEntity edge : this.stg.getEdges()){
      int indext1 = -1;
      int indext2 = -1;

      //on choisit l'ilotier le plus proche temporellement
      IPopulation<IFeature> polygont1 = null;
      IPopulation<IFeature> polygont2 = null;
      List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>();
      times.addAll(this.polygons.keySet());
      Collections.sort(times, new Comparator<FuzzyTemporalInterval>(){
        @Override
        public int compare(FuzzyTemporalInterval o1, FuzzyTemporalInterval o2) {
          return WidthMassFunction.compare(o1, o2);
        }
      });
      if(times.contains(t1)){
        polygont1 = this.polygons.get(t1);
      }
      else{
        for(int i=0; i< times.size() -1; i++){
          if(WidthMassFunction.compare(times.get(i), t1) == -1 &&
              WidthMassFunction.compare(times.get(i+1), t1) == 1 ){
            polygont1 = this.polygons.get(times.get(i));
            indext1=i;
          }
        }
      }
      if(times.contains(t2)){
        polygont2 = this.polygons.get(t2);
      }
      else{
        for(int i=0; i< times.size() -1; i++){
          if(WidthMassFunction.compare(times.get(i), t2) == -1 &&
              WidthMassFunction.compare(times.get(i+1), t2) == 1 ){
            polygont2 = this.polygons.get(times.get(i+1));
            indext2 = i+1;

          }
        }
      }


      if(polygont1 != null && polygont2 != null){
        Map<GraphEntity, Double> widthst1= StreetsWidth.getStreetsWidth(this.stg.getSnapshotAt(t1), polygont1);
        float width2 = this.getStreetsWidth( (ILineString) ho.getGeometry(), polygonAfter);

        while(width1 <=0 && indexBefore >0){
          indexBefore--;
          polygonBefore = this.polygons.get(times.get(indexBefore));
          width1 = this.getStreetsWidth((ILineString) to.getGeometry(), polygonBefore);
        }
        while(width2 <=0 && indexAfter < times.size()-1){
          indexAfter++;
          polygonAfter= this.polygons.get(times.get(indexAfter));
          width2 = this.getStreetsWidth((ILineString) to.getGeometry(), polygonAfter);
        }




        if( width1>0 && width2>0){
          if(width2 < width1){
            //pas de rétreceissement a priori
            if (Arrays.equals(hypothesis, new byte[] { 0,1})) {
              return 1f;
            }
            return 0f;
          }
          //              if (Arrays.equals(hypothesis, new byte[] { 1, 1, 1 ,1})) {
          //                  return 0f;
          //              }
          double d1 = width1;
          double d2 =width2;
          double ratio = d2 / d1;
          if(d1 <= 10){
            if (Arrays.equals(hypothesis, new byte[] { 1,0})) {
              return (float) this.continuation_filiation_error_filter.getMembership(ratio);
            } else if (Arrays.equals(hypothesis, new byte[] { 0,1 })) {
              return (float)(1f- this.continuation_filiation_error_filter.getMembership(ratio));
            }
            return 0;
          }

          else{
            if (Arrays.equals(hypothesis, new byte[] { 1,0})) {
              return (float) this.continuation_filiation_error_filter2.getMembership(ratio);
            } else if (Arrays.equals(hypothesis, new byte[] { 0,1 })) {
              return (float) (1f- this.continuation_filiation_error_filter2.getMembership(ratio));
            }
            return 0;
          }
        }
        else{
          if (Arrays.equals(hypothesis, new byte[] { 0,1})) {
            return 1f;
          }
          return 0f;
        }
      }
      else{
        if (Arrays.equals(hypothesis, new byte[] { 0,1})) {
          return 1f;
        }
        return 0f;
      }
    }
  }

  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
