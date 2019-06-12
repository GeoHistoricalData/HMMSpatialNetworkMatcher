package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.utils;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IPoint;
import fr.ign.cogit.geoxygene.contrib.appariement.EnsembleDeLiens;
import fr.ign.cogit.geoxygene.contrib.appariement.Lien;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.index.Tiling;

/**
 * get matching scores
 * 
 * @author bcostes
 *
 */
public class Scores {

  // private IPopulation<IFeature> pop1;
  // private IPopulation<IFeature> pop2;
  private EnsembleDeLiens matchingAuto;
  private EnsembleDeLiens matchingManuel;

  // IPopulation<IFeature> errorPlus = new Population<>();
  // IPopulation<IFeature> errorLess = new Population<>();

  // @SuppressWarnings("unused")
  public Scores(IPopulation<IFeature> pop1, IPopulation<IFeature> pop2) {
    // this.pop1 = pop1;
    // this.pop2 = pop2;
    this.matchingAuto = new EnsembleDeLiens();
    this.matchingManuel = new EnsembleDeLiens();
  }

  public double getAccuracy() {
    // précision = vp/(vp + fp) = appariement ok / appariement processus auto
    double vp = 0;
    double tot = matchingAuto.size();

    for (Lien la : matchingAuto) {
      for (IFeature f1 : la.getObjetsRef()) {
        for (IFeature f2 : la.getObjetsComp()) {
          for (Lien lm : matchingManuel) {
            if (lm.getObjetsRef().contains(f1) && lm.getObjetsComp().contains(f2) || lm.getObjetsComp().contains(f1) && lm.getObjetsRef().contains(f2)) {
              vp++;
              break;
            }
          }
        }
      }
    }
    System.out.println("Accuracy: true positives: " + vp + " out of " + tot);
    return vp / tot;
  }

  public double getRecall() {
    // rappel = vp / (vérité terrain)
    double vp = 0;
    double tot = matchingManuel.size();
    for (Lien la : matchingAuto) {
      for (IFeature f1 : la.getObjetsRef()) {
        for (IFeature f2 : la.getObjetsComp()) {
          for (Lien lm : matchingManuel) {
            if (lm.getObjetsRef().contains(f1) && lm.getObjetsComp().contains(f2) || lm.getObjetsComp().contains(f1) && lm.getObjetsRef().contains(f2)) {
              vp++;
              break;
            }
          }
        }
      }
    }

    System.out.println("Recall: true positives: " + vp + " out of " + tot);
    return vp / tot;
  }

  public void init(IPopulation<IFeature> matchingAuto, IPopulation<IFeature> matchingManuel, IPopulation<IFeature> popRef, IPopulation<IFeature> popComp) {

    popRef.initSpatialIndex(Tiling.class, false);
    popComp.initSpatialIndex(Tiling.class, false);
    matchingAuto.initSpatialIndex(Tiling.class, false);
    matchingManuel.initSpatialIndex(Tiling.class, false);

    for (IFeature fref : popRef) {
      for (IFeature m : matchingAuto) {
        IPoint p1 = new GM_Point(m.getGeom().coord().get(0));
        IPoint p2 = new GM_Point(m.getGeom().coord().get(1));
        if (p1.distance(fref.getGeom()) < 0.01 || p2.distance(fref.getGeom()) < 0.01) {
          for (IFeature fcomp : popComp) {
            if (p1.distance(fcomp.getGeom()) < 0.01 || p2.distance(fcomp.getGeom()) < 0.01) {
              Lien l = this.matchingAuto.nouvelElement();
              l.addObjetRef(fref);
              l.addObjetComp(fcomp);
              break;
            }
          }
        }
      }

      for (IFeature m : matchingManuel) {
        IPoint p1 = new GM_Point(m.getGeom().coord().get(0));
        IPoint p2 = new GM_Point(m.getGeom().coord().get(1));
        if (p1.distance(fref.getGeom()) < 0.01 || p2.distance(fref.getGeom()) < 0.01) {
          for (IFeature fcomp : popComp) {
            if (p1.distance(fcomp.getGeom()) < 0.01 || p2.distance(fcomp.getGeom()) < 0.01) {
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

  public static void main(String[] args) throws Exception {
    // IPopulation<IFeature> zoneRef = ShapefileReader.read("/home/bcostes/Bureau/score_matching/jacoubet_poubelle/zone1/zone_jacoubet.shp");
    IPopulation<IFeature> popRef = ShapefileReader.read("./manual_matching/snapshot_1784.0_1791.0_edges.shp");
    IPopulation<IFeature> popComp = ShapefileReader.read("./manual_matching/snapshot_1825.0_1836.0_edges.shp");
    IPopulation<IFeature> matchingManuel = ShapefileReader.read("./manual_matching/matching.shp");
    // IPopulation<IFeature> matchingAuto = ShapefileReader.read("./test_simplified.shp");
    IPopulation<IFeature> matchingAuto = ShapefileReader.read("/home/julien/devel/nm/matching_hmm_optim.shp");

    matchingAuto.removeIf((f) -> f.getAttribute("type").equals("FN"));
    
    System.out.println("go with " + matchingManuel.size() + " (manual) and " + matchingAuto.size() + " (auto)");

    Scores s = new Scores(popRef, popComp);
    s.init(matchingAuto, matchingManuel, popRef, popComp/* , zoneRef */);
    // writing the matching files as csv files
    /*
     * FileWriter writer = new FileWriter("manuel.csv"); for (Lien l : s.matchingManuel) { writer.write(l.getObjetsRef().get(0).getAttribute("ID") + "," +
     * l.getObjetsComp().get(0).getAttribute("ID") + "\n"); } writer.close(); writer = new FileWriter("auto.csv"); for (Lien l : s.matchingAuto) {
     * writer.write(l.getObjetsRef().get(0).getAttribute("ID") + "," + l.getObjetsComp().get(0).getAttribute("ID") + "\n"); } writer.close();
     */
    System.out.println("matching_out_hmm_optim");
    System.out.println("Précision : " + s.getAccuracy());
    System.out.println("Rappel : " + s.getRecall());

  }

}
