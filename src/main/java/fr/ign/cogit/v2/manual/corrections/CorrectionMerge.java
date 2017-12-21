package fr.ign.cogit.v2.manual.corrections;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.geometry.LightLineString;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.lineage.MatchingLink;
import fr.ign.cogit.v2.manual.MatchingLinkIOManager;
import fr.ign.cogit.v2.mergeProcess.MergingProcess;
import fr.ign.cogit.v2.mergeProcess.mergeUtility.MergeUtility;
import fr.ign.cogit.v2.mergeProcess.mergeUtility.MergeUtils;
import fr.ign.cogit.v2.mergeProcess.mergeUtility.SingleEdgeMultipleEdgeMU;
import fr.ign.cogit.v2.mergeProcess.mergeUtility.SingleEdgeSingleEdgeMU;
import fr.ign.cogit.v2.mergeProcess.mergeUtility.SingleNodeSingleNodeMU;
import fr.ign.cogit.v2.merging.Split11Links;
import fr.ign.cogit.v2.merging.Split1NLinks;
import fr.ign.cogit.v2.merging.SplitNNLinks;
import fr.ign.cogit.v2.patterns.AppearanceSTPattern;
import fr.ign.cogit.v2.patterns.ReincarnationSTPattern;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.STProperty;

/**
 * TODO : si on veut fusionner uniquement des sommets ? Pour l'instant, marche pour les arcs
 * @author bcostes
 *
 */
public class CorrectionMerge {
  
  
  public static int cpt = 0;


  /**
   * Vérifie si on peut fusionner en regard de la temporalité des arcs et des sommets
   * @return
   */
  public static boolean checkTemporalConstraint(MatchingLink stlink){
    Set<STEntity> edgesSource= new HashSet<STEntity>();
    edgesSource.addAll(stlink.getSources().getEdges());
    Set<STEntity> edgesTarget= new HashSet<STEntity>();
    edgesTarget.addAll(stlink.getTargets().getEdges());

    Set<FuzzyTemporalInterval> timesSource = new HashSet<FuzzyTemporalInterval>();
    for(STEntity source :edgesSource){
      for(FuzzyTemporalInterval t :source.getTimeSerie().getValues().keySet()){
        if(source.existsAt(t)){
          timesSource.add(t);
        }
      }
    }

    //si un arc de target a une date d'existence de timesSource, on ne peut pas fusionner
    for(STEntity target :edgesTarget){
      for(FuzzyTemporalInterval t : timesSource){
        if(target.existsAt(t)){
          return false;
        }
      }
    }
    return true;
  }

  public static STGraph correctMerge(STGraph stg, String shp){
    //on reconstitue les liens maximaux
    List<MatchingLink> stlinks = MatchingLinkIOManager.stLinkFromShp(stg, shp);


    return CorrectionMerge.correctMerge(stg, stlinks);
  }

  public static STGraph correctMerge(STGraph stg, List<MatchingLink> stlinks){
    //on reconstitue les liens maximaux           

    if(stlinks.isEmpty()){
      return stg;
    }
//    for(MatchingLink l : stlinks){
//      if(!CorrectionMerge.checkTemporalConstraint(l)){
//        continue;
//        // return stg;
//      }
//    }


    
    
    //on peut fusionner
    MergingProcess mergeP = new MergingProcess(stg.getTemporalDomain(), stg);
    mergeP.setMatchingLinks(new HashSet<MatchingLink>(stlinks));
    
    
    System.out.println(stlinks.size());


   

    for(MatchingLink stlink : new ArrayList<MatchingLink>(stlinks)){


      //pour chaque lien maximal
      Set<STEntity> edgesSource= new HashSet<STEntity>();
      edgesSource.addAll(stlink.getSources().getEdges());
      Set<STEntity> edgesTarget= new HashSet<STEntity>();
      edgesTarget.addAll(stlink.getTargets().getEdges());
      

      //on va récupérer les sommets extrèmes
      STEntity node11 = null, node12 = null, node21=null, node22 =null;
      if(edgesSource.size() == 1){
        node11 = stg.getEndpoints(edgesSource.iterator().next()).getFirst();
        node12 = stg.getEndpoints(edgesSource.iterator().next()).getSecond();
      }
      else{
        Set<STEntity> nodes = new HashSet<STEntity>();
        for(STEntity e : edgesSource){
          nodes.add(stg.getEndpoints(e).getFirst());
          nodes.add(stg.getEndpoints(e).getSecond());

        }
        for(STEntity n: nodes){
          Set<STEntity> incident = new HashSet<STEntity>(stg.getIncidentEdges(n));
          for(STEntity e : stg.getIncidentEdges(n)){
            if(!edgesSource.contains(e)){
              incident.remove(e);
            }
          }
          if(incident.size() == 1){
            node11 = n;
          }
        }
        for(STEntity n: nodes){
          if(n.equals(node11)){
            continue;
          }
          Set<STEntity> incident = new HashSet<STEntity>(stg.getIncidentEdges(n));
          for(STEntity e : stg.getIncidentEdges(n)){
            if(!edgesSource.contains(e)){
              incident.remove(e);
            }
          }
          if(incident.size() == 1){
            node12 = n;
          }
        }
      }
      if(edgesTarget.size() == 1){
        node21 = stg.getEndpoints(edgesTarget.iterator().next()).getFirst();
        node22 = stg.getEndpoints(edgesTarget.iterator().next()).getSecond();
      }
      else{
        Set<STEntity> nodes = new HashSet<STEntity>();
        for(STEntity e : edgesTarget){
          nodes.add(stg.getEndpoints(e).getFirst());
          nodes.add(stg.getEndpoints(e).getSecond());

        }
        for(STEntity n: nodes){
          Set<STEntity> incident = new HashSet<STEntity>(stg.getIncidentEdges(n));
          for(STEntity e : stg.getIncidentEdges(n)){
            if(!edgesTarget.contains(e)){
              incident.remove(e);
            }
          }
          if(incident.size() == 1){
            node21 = n;
          }
        }
        for(STEntity n: nodes){
          if(n.equals(node11)){
            continue;
          }
          Set<STEntity> incident = new HashSet<STEntity>(stg.getIncidentEdges(n));
          for(STEntity e : stg.getIncidentEdges(n)){
            if(!edgesTarget.contains(e)){
              incident.remove(e);
            }
          }
          if(incident.size() == 1){
            node22 = n;
          }
        }
      }
      //si deux des sommets sont diférents c'ets mort
      if(!node11.equals(node21) && !node11.equals(node22) && !node12.equals(node21) && !node12.equals(node22)){
        continue; 
      }
      else if(node11.equals(node21) && !node12.equals(node22) ){
        //sont'ils séparé par un arc?
        Set<STEntity>incident =new HashSet<STEntity>(stg.getIncidentEdges(node12));
        for(STEntity e :incident){
          if(stg.getEndpoints(e).getFirst().equals(node12) &&
              stg.getEndpoints(e).getSecond().equals(node22) ||  stg.getEndpoints(e).getSecond().equals(node12) &&
              stg.getEndpoints(e).getFirst().equals(node22)){
            //c'est bon
            //un seommet corredpons, on les fusionne si ils sont séparé par un seul arc
            MergeUtils.mergeNodes(mergeP.getStGraph(), node12, node22);

            //suppression de l'arc
            mergeP.getStGraph().removeEdge(e);
            break;
          }   
        }
      }
      else if(node11.equals(node22) && !node12.equals(node21) ){
        Set<STEntity>incident =new HashSet<STEntity>(stg.getIncidentEdges(node12));
        for(STEntity e :incident){
          if(stg.getEndpoints(e).getFirst().equals(node12) &&
              stg.getEndpoints(e).getSecond().equals(node21) ||  stg.getEndpoints(e).getSecond().equals(node12) &&
              stg.getEndpoints(e).getFirst().equals(node21)){
            //c'est bon
            //un seommet corredpons, on les fusionne si ils sont séparé par un seul arc
            MergeUtils.mergeNodes(mergeP.getStGraph(), node12, node21);
            //suppression de l'arc
            mergeP.getStGraph().removeEdge(e);
            break;
          }   
        }
      }
      else if(node12.equals(node21) && !node11.equals(node22) ){
        //un seommet corredpons, on les fusionne si ils sont séparé par un seul arc
        Set<STEntity>incident =new HashSet<STEntity>(stg.getIncidentEdges(node11));
        for(STEntity e :incident){
          if(stg.getEndpoints(e).getFirst().equals(node11) &&
              stg.getEndpoints(e).getSecond().equals(node22) ||  stg.getEndpoints(e).getSecond().equals(node11) &&
              stg.getEndpoints(e).getFirst().equals(node22)){
            //c'est bon
            //un seommet corredpons, on les fusionne si ils sont séparé par un seul arc
            MergeUtils.mergeNodes(mergeP.getStGraph(), node11, node22);
            //suppression de l'arc
            mergeP.getStGraph().removeEdge(e);
            break;
          }   
        }
      }
      else if(node12.equals(node22) && !node11.equals(node21) ){
        Set<STEntity>incident =new HashSet<STEntity>(stg.getIncidentEdges(node11));
        for(STEntity e :incident){
          if(stg.getEndpoints(e).getFirst().equals(node11) &&
              stg.getEndpoints(e).getSecond().equals(node21) ||  stg.getEndpoints(e).getSecond().equals(node11) &&
              stg.getEndpoints(e).getFirst().equals(node21)){
            //c'est bon
            //un seommet corredpons, on les fusionne si ils sont séparé par un seul arc
            MergeUtils.mergeNodes(mergeP.getStGraph(), node11, node21);
            //suppression de l'arc
            mergeP.getStGraph().removeEdge(e);
            break;
          }   
        }
      }
      else{
        //égalité des deux extrémité => on fait rien
      }
      if(edgesSource.size() == 1){
        //un arc source
        if(edgesTarget.size() == 1){
          // lien 1 :1 stlinks
          MergeUtility mU = new SingleEdgeSingleEdgeMU(stlink);
          mU.merge(mergeP);
        }
        else if(edgesTarget.size()>1){
          //lien 1:n
          MergeUtility mU = new SingleEdgeMultipleEdgeMU(stlink);
          mU.merge(mergeP);
        }
      }
      else if(edgesSource.size() > 1){
        //un arc source
        if(edgesTarget.size() == 1){
          // lien N :1 stlinks
          MergeUtility mU = new SingleEdgeSingleEdgeMU(stlink);
          mU.merge(mergeP);
        }
        else if(edgesTarget.size()>1){
          //lien n:m
          MergeUtility mU = new SingleEdgeMultipleEdgeMU(stlink);
          mU.merge(mergeP);
        }
      }
      CorrectionMerge.cpt++;
    }
    for (STEntity e : mergeP.getStGraph().getEdges()) {
      for (FuzzyTemporalInterval t : e.getTimeSerie().getValues().keySet()) {
          if (e.getGeometryAt(t) != null) {
              ILineString ll = ((LightLineString) e.getGeometryAt(t))
                      .toGeoxGeometry();
              if (mergeP.getStGraph().getEndpoints(e).getFirst().getGeometryAt(t) != null
                      && mergeP.getStGraph().getEndpoints(e).getSecond().getGeometryAt(t) != null) {
                  // if (ll.coord().size() == 2) {
                  // ll = Operateurs.echantillone(ll, ll.length() / 3.);
                  // }
                  IDirectPosition pp1 = mergeP.getStGraph().getEndpoints(e).getFirst()
                          .getGeometryAt(t).toGeoxGeometry().coord().get(0);
                  IDirectPosition pp2 = mergeP.getStGraph().getEndpoints(e).getSecond()
                          .getGeometryAt(t).toGeoxGeometry().coord().get(0);
                  if (pp1.equals(ll.getControlPoint(0))
                          && !pp2.equals(ll
                                  .getControlPoint(ll.getControlPoint().size() - 1))) {
                      ll.setControlPoint(ll.getControlPoint().size() - 1, pp2);
                      e.setGeometryAt(t, new LightLineString(ll.coord()));
                  } else if (pp1.equals(ll.getControlPoint(ll.getControlPoint()
                          .size() - 1)) && !pp2.equals(ll.getControlPoint(0))) {
                      ll.setControlPoint(0, pp2);
                      e.setGeometryAt(t, new LightLineString(ll.coord()));
                  } else if (pp2.equals(ll.getControlPoint(0))
                          && !pp1.equals(ll
                                  .getControlPoint(ll.getControlPoint().size() - 1))) {
                      ll.setControlPoint(ll.getControlPoint().size() - 1, pp1);
                      e.setGeometryAt(t, new LightLineString(ll.coord()));
                  } else if (pp2.equals(ll.getControlPoint(ll.getControlPoint()
                          .size() - 1)) && !pp1.equals(ll.getControlPoint(0))) {
                      ll.setControlPoint(0, pp1);
                      e.setGeometryAt(t, new LightLineString(ll.coord()));
                  }
              }
          }
      }
  }
  
    mergeP.getStGraph().updateGeometries();
    return mergeP.getStGraph();
  }


  public static void main(String[] args) {
    // TODO Auto-generated method stub

    String shpLiens = "/home/bcostes/Bureau/TAG/corrections_patterns/etape4/matching.shp";
    String inputStg ="/home/bcostes/Bureau/TAG/corrections_patterns/etape4/tag_corrected.tag";
    String outputStg ="/home/bcostes/Bureau/TAG/corrections_patterns/etape4/tag_corrected.shp";
    String outputStg2 ="/home/bcostes/Bureau/TAG/corrections_patterns/etape4/tag_corrected.tag";

    STGraph stg= TAGIoManager.deserialize(inputStg);

    STGraph stg2 = CorrectionMerge.correctMerge(stg, shpLiens);


    TAGIoManager.serializeBinary(stg2, outputStg2);

    TAGIoManager.exportTAG(stg2, outputStg);
    TAGIoManager.exportSnapshots(stg2, outputStg, TAGIoManager.NODE_AND_EDGE);
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
    ShapefileWriter.write(pop,"/home/bcostes/Bureau/TAG/corrections_patterns/etape4/reincarnations.shp");
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
    ShapefileWriter.write(pop, "/home/bcostes/Bureau/TAG/corrections_patterns/etape4/appearances.shp");


  }

}
