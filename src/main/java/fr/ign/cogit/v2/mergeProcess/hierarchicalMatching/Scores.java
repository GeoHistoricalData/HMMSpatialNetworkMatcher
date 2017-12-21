package fr.ign.cogit.v2.mergeProcess.hierarchicalMatching;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.appariement.EnsembleDeLiens;
import fr.ign.cogit.geoxygene.contrib.appariement.Lien;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

/**
 * get matching scores
 * @author bcostes
 *
 */
public class Scores {

  IPopulation<IFeature> pop1;
  IPopulation<IFeature> pop2;
  EnsembleDeLiens matchingAuto;
  EnsembleDeLiens matchingManuel;

  IPopulation<IFeature> errorPlus = new Population<IFeature>();
  IPopulation<IFeature> errorLess = new Population<IFeature>();


  public Scores(IPopulation<IFeature> pop1, IPopulation<IFeature> pop2){
    this.pop1 = pop1;
    this.pop2 = pop2;
    this.matchingAuto = new EnsembleDeLiens();
    this.matchingManuel = new EnsembleDeLiens();
  }


  public double getAccuracy(){
    //précision = vp/(vp + fp) = appariement ok / appariement processus auto
    double vp = 0;
    double tot = matchingAuto.size();
    for(IFeature f1: pop1){
      for(IFeature f2: pop2){
        boolean lienAuto = false;
        Lien lien = null;
        for(Lien la: matchingAuto){
          if(la.getObjetsRef().contains(f1) && la.getObjetsComp().contains(f2) ||  
              la.getObjetsComp().contains(f1) && la.getObjetsRef().contains(f2) ){
            //on a un lien auto
            lienAuto = true;
            lien = la;
            break;
          }
        }
        //lien manuel? 
        boolean lienManual = false;
        for(Lien la: matchingManuel){
          if(la.getObjetsRef().contains(f1) && la.getObjetsComp().contains(f2) ||  
              la.getObjetsComp().contains(f1) && la.getObjetsRef().contains(f2) ){
            if(lienAuto){
              //on a un lien manuel et auto => vp
              vp ++;
              lienManual = true;
              break;
            }
            else{
              //lien manuel mais pas auto
              this.errorLess.add(new DefaultFeature(new GM_LineString(la.getGeom().coord())));
            }
          }
        }
        if(!lienManual && lienAuto){
          this.errorPlus.add(new DefaultFeature(new GM_LineString(lien.getGeom().coord())));
        }
      }
    }
    return vp / ((double)tot);
  }

  public double getRecall(){
    //rappel = vp / (vérité terrain)
    double vp = 0;
    double tot = matchingManuel.size();
    for(IFeature f1: pop1){
      for(IFeature f2: pop2){
        boolean lienAuto = false;
        for(Lien la: matchingAuto){
          if(la.getObjetsRef().contains(f1) && la.getObjetsComp().contains(f2) ||  
              la.getObjetsComp().contains(f1) && la.getObjetsRef().contains(f2) ){
            //on a un lien auto
            lienAuto = true;
            break;
          }
        }
        //lien manuel? 
        if(lienAuto){
          for(Lien la: matchingManuel){
            if(la.getObjetsRef().contains(f1) && la.getObjetsComp().contains(f2) ||  
                la.getObjetsComp().contains(f1) && la.getObjetsRef().contains(f2) ){
              //on a un lien manuel et auto => vp
              vp ++;
              break;
            }
          }
        }
      }
    }
    return vp / ((double)tot);
  }

  public void init(IPopulation<IFeature> matchingAuto, IPopulation<IFeature> matchingManuel, IPopulation<IFeature> popRef, IPopulation<IFeature> popComp/* , /IPopulation<IFeature>zoneRef*/){
   // IGeometry zoneR = zoneRef.get(0).getGeom();
    for(IFeature fref: popRef){
      /*if(!zoneR.contains(fref.getGeom()) && ! zoneR.intersects(fref.getGeom())){
        continue;
      }*/
      for(IFeature m: matchingAuto){
        if((new GM_Point(m.getGeom().coord().get(0))).distance(fref.getGeom()) < 0.01 ||
            (new GM_Point(m.getGeom().coord().get(1))).distance(fref.getGeom()) < 0.01){
          for(IFeature fcomp: popComp){
            if(m.getGeom().distance(fcomp.getGeom()) < 0.01){
              Lien l = this.matchingAuto.nouvelElement();
              l.addObjetRef(fref);
              l.addObjetComp(fcomp);
              break;
            }
          }
        }
      }

      for(IFeature m: matchingManuel){
        if((new GM_Point(m.getGeom().coord().get(0))).distance(fref.getGeom()) < 0.01 ||
            (new GM_Point(m.getGeom().coord().get(1))).distance(fref.getGeom()) < 0.01){
          for(IFeature fcomp: popComp){
            if(m.getGeom().distance(fcomp.getGeom()) < 0.01){
              Lien l = this.matchingManuel.nouvelElement();
              l.addObjetRef(fref);
              l.addObjetComp(fcomp);
              break;
            }
          }
        }
      }
    }
    
    this.matchingAuto.creeGeometrieDesLiens();
    this.matchingManuel.creeGeometrieDesLiens();
  }

  public static void main(String[] args) {
    // TODO Auto-generated method stub

   // IPopulation<IFeature> zoneRef = ShapefileReader.read("/home/bcostes/Bureau/score_matching/jacoubet_poubelle/zone1/zone_jacoubet.shp");
    IPopulation<IFeature> popRef = ShapefileReader.read("/home/bcostes/Bureau/stag_json/stag_json_edges.shp");
    IPopulation<IFeature> popComp = ShapefileReader.read("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/travaux_1789_1854/travaux_pol.shp");
    IPopulation<IFeature> matchingAuto = ShapefileReader.read("/home/bcostes/Bureau/matching_test4.shp");
    IPopulation<IFeature> matchingManuel = ShapefileReader.read("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/travaux_1789_1854/matching_manual_merged.shp");


    Scores s = new Scores(popRef, popComp);
    s.init(matchingAuto, matchingManuel, popRef, popComp/*, zoneRef*/);

    System.out.println("Précision : " + s.getAccuracy());
    System.out.println("Rappel : " + s.getRecall());
    System.out.println(s.errorPlus.size());
    System.out.println(s.errorLess.size());
//ShapefileWriter.write(s.errorPlus, "/home/bcostes/Bureau/errorPlus.shp");
//ShapefileWriter.write(s.errorLess, "/home/bcostes/Bureau/errorLess.shp");


    //    IPopulation<IFeature> out = new Population<IFeature>();
    //    for(Lien l : s.matchingAuto){
    //      IDirectPositionList list = new DirectPositionList();
    //      list.add(Operateurs.milieu(new GM_LineString(l.getObjetsRef().get(0).getGeom().coord())));
    //      list.add(Operateurs.milieu(new GM_LineString(l.getObjetsComp().get(0).getGeom().coord())));
    //      out.add(new DefaultFeature(new GM_LineString(list)));
    //    }
    //        
    //    ShapefileWriter.write(out, "/home/bcostes/Bureau/test.shp");
  }

}
