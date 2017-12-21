package fr.ign.cogit.v2.tag.enrichment;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;

public class MatchingEvaluation {

  public static void main(String args[]){
    IPopulation<IFeature> polygons = ShapefileReader.read("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/travaux_1789_1854/travaux_pol.shp");
    IPopulation<IFeature> stag = ShapefileReader.read("/home/bcostes/Bureau/stag_json/stag_json_edges.shp");
    IPopulation<IFeature> matchingAuto = ShapefileReader.read("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/travaux_1789_1854/matching_test4.shp");
    IPopulation<IFeature> matchingManuel = ShapefileReader.read("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/travaux_1789_1854/matching_manual_merged.shp");

    int correcMatch  =0;
    int falseMatch = 0;
    int expected = matchingManuel.size();
    
    
    
    
    for(IFeature link: matchingAuto){
      IDirectPosition p1 = link.getGeom().coord().get(0);
      IDirectPosition p2 =  link.getGeom().coord().get(link.getGeom().coord().size()-1);
      IFeature pol = null;
      IFeature starc = null;
      for(IFeature poll: polygons){
        if(p1.toGM_Point().distance(poll.getGeom())<0.005 || p2.toGM_Point().distance(poll.getGeom())<0.005){
          pol = poll;
          break;
        }
      }
      for(IFeature st: stag){
        if(p1.toGM_Point().distance(st.getGeom())<0.005  || p2.toGM_Point().distance(st.getGeom())<0.005 ){
          starc = st;
          break;
        }
      }
      boolean found = false;
      for(IFeature linkM : matchingManuel){
        IDirectPosition p11 = linkM.getGeom().coord().get(0);
        IDirectPosition p22 =  link.getGeom().coord().get(linkM.getGeom().coord().size()-1);
        if(p11.toGM_Point().distance(pol.getGeom())<0.005 && p22.toGM_Point().distance(starc.getGeom())<0.005
            || p22.toGM_Point().distance(pol.getGeom()) <0.005&& p11.toGM_Point().distance(starc.getGeom())<0.005){
          found = true;
          break;
        }
      }
      if(found){
        correcMatch++;
      }

      else{
        falseMatch++;
      }
    }
    
    System.out.println(correcMatch);
    System.out.println(falseMatch);
    System.out.println(expected);

    System.out.println("précision : " + (double)correcMatch/((double)correcMatch + (double)falseMatch));
    System.out.println("rappel : " + (double)correcMatch/(double)expected);

  }
}
