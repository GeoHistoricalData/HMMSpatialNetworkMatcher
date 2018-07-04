package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.utils;


import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.contrib.appariement.EnsembleDeLiens;
import fr.ign.cogit.geoxygene.contrib.appariement.Lien;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.index.Tiling;

/**
 * get matching scores
 * @author bcostes
 *
 */
public class Scores {

  private IPopulation<IFeature> pop1;
  private IPopulation<IFeature> pop2;
  private EnsembleDeLiens matchingAuto;
  private EnsembleDeLiens matchingManuel;

//  IPopulation<IFeature> errorPlus = new Population<>();
//  IPopulation<IFeature> errorLess = new Population<>();

  @SuppressWarnings("unused")
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

    for(Lien la:matchingAuto){
      for(IFeature f1 : la.getObjetsRef()){
        for(IFeature f2 : la.getObjetsComp()){
          for(Lien lm:matchingManuel){
            if(lm.getObjetsRef().contains(f1) && lm.getObjetsComp().contains(f2) ||  
                lm.getObjetsComp().contains(f1) && lm.getObjetsRef().contains(f2) ){
              vp ++;
              break;
            }
          }
        }
      }
    }
    return vp / tot;
  }

  public double getRecall(){
    //rappel = vp / (vérité terrain)
    double vp = 0;
    double tot = matchingManuel.size();
    for(Lien la:matchingAuto){
      for(IFeature f1 : la.getObjetsRef()){
        for(IFeature f2 : la.getObjetsComp()){
          for(Lien lm:matchingManuel){
            if(lm.getObjetsRef().contains(f1) && lm.getObjetsComp().contains(f2) ||  
                lm.getObjetsComp().contains(f1) && lm.getObjetsRef().contains(f2) ){
              vp ++;
              break;
            }
          }
        }
      }
    }

    return vp / tot;
  }

  public void init(IPopulation<IFeature> matchingAuto, IPopulation<IFeature> matchingManuel, IPopulation<IFeature> popRef, IPopulation<IFeature> popComp){

    popRef.initSpatialIndex(Tiling.class, false);
    popComp.initSpatialIndex(Tiling.class, false);
    matchingAuto.initSpatialIndex(Tiling.class, false);
    matchingManuel.initSpatialIndex(Tiling.class, false);

    for(IFeature fref: popRef){
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
  }

  public static void main(String[] args) {
    // TODO Auto-generated method stub

    // IPopulation<IFeature> zoneRef = ShapefileReader.read("/home/bcostes/Bureau/score_matching/jacoubet_poubelle/zone1/zone_jacoubet.shp");
    IPopulation<IFeature> popRef = ShapefileReader.read("/home/bcostes/Documents/IGN/articles/article_appariement2/matchings/manual_matching/snapshot_1784.0_1791.0_edges.shp");
    IPopulation<IFeature> popComp = ShapefileReader.read("/home/bcostes/Documents/IGN/articles/article_appariement2/matchings/manual_matching/snapshot_1825.0_1836.0_edges.shp");
    IPopulation<IFeature> matchingManuel= ShapefileReader.read("/home/bcostes/Documents/IGN/articles/article_appariement2/matchings/manual_matching/matching.shp");
    IPopulation<IFeature> matchingAuto = ShapefileReader.read("/home/bcostes/Bureau/test2_simplified.shp");


    System.out.println("go with " + matchingManuel.size());

    Scores s = new Scores(popRef, popComp);
    s.init(matchingAuto, matchingManuel, popRef, popComp/*, zoneRef*/);
    System.out.println("orientation_frechet_simplified");
    System.out.println("Précision : " + s.getAccuracy());
    System.out.println("Rappel : " + s.getRecall());

  }

}
