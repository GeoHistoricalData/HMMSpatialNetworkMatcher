package fr.ign.cogit.v2.manual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IPoint;
import fr.ign.cogit.geoxygene.contrib.appariement.EnsembleDeLiens;
import fr.ign.cogit.geoxygene.contrib.appariement.Lien;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.lineage.MatchingLink;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;

public class MatchingLinkIOManager {

  public static void exportEnsembleDeLiens(EnsembleDeLiens liens, String shp) {
    ShapefileWriter.write(liens, shp);
  }

  /**
   * Crée les lines d'appriement à partir d'un shp
   * @param popArcs1
   * @param popNoeuds1
   * @param popArcs2
   * @param popNoeuds2
   * @param shpLiens
   * @return
   */
  public static EnsembleDeLiens importEnsembleDeLiens(  IPopulation<IFeature> popArcs1,
      IPopulation<IFeature> popNoeuds1,IPopulation<IFeature> popArcs2 , IPopulation<IFeature> popNoeuds2, String shpLiens) {
    EnsembleDeLiens liens = new EnsembleDeLiens();
    IPopulation<IFeature> p1 = new Population<IFeature>();
    IPopulation<IFeature> p2 = new Population<IFeature>();
    p1.addAll(popArcs1);
    p1.addAll(popNoeuds1);
    p2.addAll(popArcs2);
    p2.addAll(popNoeuds2);

    IPopulation<IFeature> popLiens = ShapefileReader.read(shpLiens);
    for (IFeature fLien : popLiens) {
      IDirectPosition pos1 = fLien.getGeom().coord().get(0);
      IDirectPosition pos2 = fLien.getGeom().coord()
          .get(fLien.getGeom().coord().size() - 1);
//      if(pos1.equals(pos2)){
//        continue;
//      }
      // on va cherche quel objet est concerné
      IFeature objetRef = null;
      for (IFeature f1 : p1) {
        if (f1.getGeom() instanceof IPoint) {
          if (f1.getGeom().coord().get(0).equals(pos1, 0.001)
              || f1.getGeom().coord().get(0).equals(pos2, 0.001)) {
            objetRef = f1;
            break;
          }
        }
      }
      if (objetRef == null) {
        for (IFeature f1 : p1) {
          if (!(f1.getGeom() instanceof IPoint)) {
            if (f1.getGeom().distance(new GM_Point(pos1)) < 0.001
                || f1.getGeom().distance(new GM_Point(pos2)) < 0.001) {
              objetRef = f1;
              break;
            }
          }
        }
      }

      IFeature objetComp = null;
      for (IFeature f2 : p2) {
        if (f2.getGeom() instanceof IPoint) {
          if (f2.getGeom().coord().get(0).equals(pos1, 0.001)
              || f2.getGeom().coord().get(0).equals(pos2, 0.001)) {
            objetComp = f2;
            break;
          }
        }
      }
      if (objetComp == null) {
        for (IFeature f2 : p2) {
          if (!(f2.getGeom() instanceof IPoint)) {
            if (f2.getGeom().distance(new GM_Point(pos1)) < 0.001
                || f2.getGeom().distance(new GM_Point(pos2)) < 0.001) {
              objetComp = f2;
              break;
            }
          }
        }
      }
      if(objetComp == null || objetRef == null){
        System.out.println(fLien.getGeom().toString());
        continue;

      }
      Lien l = liens.nouvelElement();
      l.addObjetRef(objetRef);
      l.addObjetComp(objetComp);
    }
    return liens.regroupeLiens(p1, p2);
  }

  /**
   * Crée les stlink à partir d'un shp
   * Attention : le sens des linestring est important pour différencier sources / targets
   * @return
   */
  public static List<MatchingLink> stLinkFromShp(STGraph stg, String shp){
    List<MatchingLink> result = new ArrayList<MatchingLink>();

    IPopulation<IFeature> popArcs1 = new Population<IFeature>();
    IPopulation<IFeature> popArcs2 = new Population<IFeature>();
    IPopulation<IFeature> popNoeuds1 = new Population<IFeature>();
    IPopulation<IFeature> popNoeuds2 = new Population<IFeature>();

    Map<IFeature, STEntity> mapArcs1 = new HashMap<IFeature, STEntity>();
    Map<IFeature, STEntity> mapArcs2 = new HashMap<IFeature, STEntity>();
    Map<IFeature, STEntity> mapNoeuds1 = new HashMap<IFeature, STEntity>();
    Map<IFeature, STEntity> mapNoeuds2 = new HashMap<IFeature, STEntity>();


    for(STEntity e: stg.getEdges()){
      popArcs1.add(new DefaultFeature(e.getGeometry().toGeoxGeometry()));
      mapArcs1.put(popArcs1.get(popArcs1.size()-1), e);
      popArcs2.add(new DefaultFeature(e.getGeometry().toGeoxGeometry()));
      mapArcs2.put(popArcs2.get(popArcs2.size()-1), e);
    }
    for(STEntity e: stg.getVertices()){
      popNoeuds1.add(new DefaultFeature(e.getGeometry().toGeoxGeometry()));
      mapNoeuds1.put(popNoeuds1.get(popNoeuds1.size()-1), e);
     popNoeuds2.add(new DefaultFeature(e.getGeometry().toGeoxGeometry()));
      mapNoeuds2.put(popNoeuds2.get(popNoeuds2.size()-1), e);
    }
    IPopulation<IFeature> p1 = new Population<IFeature>();
    IPopulation<IFeature> p2 = new Population<IFeature>();
    p1.addAll(popArcs1);
    p1.addAll(popNoeuds1);
    p2.addAll(popArcs2);
    p2.addAll(popNoeuds2);
    EnsembleDeLiens liens = new EnsembleDeLiens();


    IPopulation<IFeature> popLiens = ShapefileReader.read(shp);
    for (IFeature fLien : popLiens) {
      IDirectPosition pos1 = fLien.getGeom().coord().get(0);
      IDirectPosition pos2 = fLien.getGeom().coord()
          .get(fLien.getGeom().coord().size() - 1);
      // on va cherche quel objet est concerné
      IFeature objetRef = null;
      for (IFeature f1 : p1) {
        if (f1.getGeom() instanceof IPoint) {
          if (f1.getGeom().coord().get(0).equals(pos1, 0.001)) {
            objetRef = f1;
            break;
          }
        }
      }
      if (objetRef == null) {
        for (IFeature f1 : p1) {
          if (!(f1.getGeom() instanceof IPoint)) {
            if (f1.getGeom().distance(new GM_Point(pos1)) < 0.001) {
              objetRef = f1;
              break;
            }
          }
        }
      }

      IFeature objetComp = null;
      for (IFeature f2 : p2) {
        if(mapArcs2.containsKey(f2) && mapArcs2.get(f2).equals(mapArcs1.get(objetRef)) ||
            mapNoeuds2.containsKey(f2) && mapNoeuds2.get(f2).equals(mapNoeuds1.get(objetRef))){
          continue;
        }
        if (f2.getGeom() instanceof IPoint) {
          if (f2.getGeom().coord().get(0).equals(pos2, 0.001)) {
            objetComp = f2;
            break;
          }
        }
      }
      if (objetComp == null) {
        for (IFeature f2 : p2) {
          if(mapArcs2.containsKey(f2) && mapArcs2.get(f2).equals(mapArcs1.get(objetRef)) ||
              mapNoeuds2.containsKey(f2) && mapNoeuds2.get(f2).equals(mapNoeuds1.get(objetRef))){
            continue;
          }
          if (!(f2.getGeom() instanceof IPoint)) {
            if (f2.getGeom().distance(new GM_Point(pos2)) < 0.001) {
              objetComp = f2;
              break;
            }
          }
        }
      }
      if(objetComp == null || objetRef == null){
        System.out.println(fLien.getGeom().toString());
      }
      Lien l = liens.nouvelElement();
      l.addObjetRef(objetRef);
      l.addObjetComp(objetComp);
    }
    
    
    EnsembleDeLiens newLiens =  liens.regroupeLiens(p1, p2);
    
    
    for(Lien l : newLiens){
      MatchingLink link = new MatchingLink();
      //sources
      for(IFeature f : l.getObjetsRef()){
        if (f.getGeom() instanceof IPoint){
          link.getSources().getNodes().add(mapNoeuds1.get(f));
        }
        else{
          link.getSources().getEdges().add(mapArcs1.get(f));
        }
      }
      for(IFeature f : l.getObjetsComp()){
        if (f.getGeom() instanceof IPoint){
          link.getTargets().getNodes().add(mapNoeuds2.get(f));
        }
        else{
          link.getTargets().getEdges().add(mapArcs2.get(f));
        }
      }
      result.add(link);
    }

    return result;
  }

  public static void main(String args[]) {
    /*FuzzyTemporalInterval told = new FuzzyTemporalInterval(1785, 1790, 1793, 1795);
    FuzzyTemporalInterval tsnapshot = new FuzzyTemporalInterval(1808, 1810, 1836, 1839);
    String repOld = "/home/bcostes/Bureau/TAG/etape0";
    String repNew = "/home/bcostes/Bureau/TAG/etape1";
    EnsembleDeLiens liens = STLinkIOManager.importEnsembleDeLiens(repOld
        + "/snapshot_" + told.a() + "_" + told.d() + "_edges.shp", repOld
        + "/snapshot_" + told.a() + "_" + told.d() + ".shp", repNew
        + "/snapshot_" + tsnapshot.a() + "_" + tsnapshot.d() + "_edges.shp",
        repNew + "/snapshot_" + tsnapshot.a() + "_" + tsnapshot.d() + ".shp",
        repNew + "/matching.shp");
    System.out.println(liens.size());*/
  }
}
