package fr.ign.cogit.v2.patterns.trust;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.lineage.MatchingLink;
import fr.ign.cogit.v2.patterns.AppearanceSTPattern;
import fr.ign.cogit.v2.patterns.FullStabilitySTPattern;
import fr.ign.cogit.v2.patterns.ReincarnationSTPattern;
import fr.ign.cogit.v2.patterns.STPattern;
import fr.ign.cogit.v2.patterns.STPattern.PATTERN_TYPE;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;

public class MatchingInconsistency extends InconsistencyCriteria{

  private final static double dtopo = 0;
  private final static double dtopo2 = 1;
  private final static double deucli = 100.;


  @Override
  public void evaluate(STGraph stgraph, STEntity edge, PATTERN_TYPE type) {
    List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>();
    for(FuzzyTemporalInterval t : edge.getTimeSerie().getValues().keySet()){
      if(edge.existsAt(t)){
        times.add(t);
      }
    }
    times.remove(null);
    List<STEntity> matchPattern = new ArrayList<STEntity>();
    //on va chercher des patterns compatibles
    FullStabilitySTPattern stability = new FullStabilitySTPattern();
    for(STEntity e : stgraph.getEdges()){
      if(stability.find(e.getTimeSerie())){
        continue;
      }
      // il reste des patterns de naissance, mort, réincarnation et apparition
      // compatibilité des domaines temporels ?
      boolean add= true;
      for(FuzzyTemporalInterval t : times){
        if(e.existsAt(t)){
          add = false;
          break;
        }
      }
      if(add){
        // les domaines temporels d'existence ne s'intersectent pas
        matchPattern.add(e);
      }
    }

    // on a les patterns candidats
    boolean closeMatchTopo = false;
    boolean closeMatchTopo2 = false;
    boolean closeMatchEucli = false;
    STEntity node1 = stgraph.getEndpoints(edge).getFirst();
    STEntity node2 = stgraph.getEndpoints(edge).getSecond();
    DijkstraShortestPath<STEntity, STEntity> distances = new DijkstraShortestPath<STEntity, STEntity>(stgraph);

    for(STEntity e2: matchPattern){
      if(edge.equals(e2)){
        continue;
      }
      STEntity node21 = stgraph.getEndpoints(e2).getFirst();
      STEntity node22 = stgraph.getEndpoints(e2).getSecond();

      if(distances.getDistance(node1, node21).intValue()<= dtopo ){
        if(distances.getDistance(node2, node22).intValue()<= dtopo){
          closeMatchTopo = true;
          break;
        }
      }
      else  if(distances.getDistance(node1, node22).intValue()<= dtopo){
        if(distances.getDistance(node2, node21).intValue()<= dtopo){
          //on vérifie que les séries temporelles sont disjointes
          closeMatchTopo = true;
          break;
        }
      }
      if(!closeMatchTopo2 && distances.getDistance(node1, node21).intValue()<= dtopo ){
        if(distances.getDistance(node2, node22).intValue()<= dtopo2){
          closeMatchTopo2 = true;
          continue;
        }
      }
      else  if(!closeMatchTopo2 &&  distances.getDistance(node1, node22).intValue()<= dtopo){
        if(distances.getDistance(node2, node21).intValue()<= dtopo2){
          //on vérifie que les séries temporelles sont disjointes
          closeMatchTopo2 = true;
          continue;
        }
      }
      else if(!closeMatchTopo2 &&  distances.getDistance(node1, node21).intValue()<= dtopo2 ){
        if(distances.getDistance(node2, node22).intValue()<= dtopo){
          closeMatchTopo2 = true;
          continue;
        }
      }
      else  if(!closeMatchTopo2 &&  distances.getDistance(node1, node22).intValue()<= dtopo2){
        if(distances.getDistance(node2, node21).intValue()<= dtopo){
          //on vérifie que les séries temporelles sont disjointes
          closeMatchTopo2 = true;
          continue;
        }
      }
      else if(!closeMatchTopo2 &&  !closeMatchEucli && distances.getDistance(node1, node21).intValue()<= dtopo){
        if(node2.getGeometry().toGeoxGeometry().coord().get(0).distance(node22.getGeometry().toGeoxGeometry().coord().get(0))<= deucli){
          //on vérifie que les séries temporelles sont disjointes
          closeMatchEucli = true;
          continue;
        }
      }
      else  if(!closeMatchTopo2 &&  !closeMatchEucli && distances.getDistance(node1, node22).intValue()<dtopo){
        if(node2.getGeometry().toGeoxGeometry().coord().get(0).distance(node21.getGeometry().toGeoxGeometry().coord().get(0))<= deucli){
          //on vérifie que les séries temporelles sont disjointes
          closeMatchEucli = true;
          continue;
        }
      }
      else if(!closeMatchTopo2 &&  !closeMatchEucli && distances.getDistance(node2, node21).intValue()<= dtopo){
        if(node1.getGeometry().toGeoxGeometry().coord().get(0).distance(node22.getGeometry().toGeoxGeometry().coord().get(0))<= deucli){
          //on vérifie que les séries temporelles sont disjointes
          closeMatchEucli = true;
          continue;
        }
      }
      else  if(!closeMatchTopo2 &&  !closeMatchEucli && distances.getDistance(node2, node22).intValue()<dtopo){
        if(node1.getGeometry().toGeoxGeometry().coord().get(0).distance(node21.getGeometry().toGeoxGeometry().coord().get(0))<= deucli ){
          //on vérifie que les séries temporelles sont disjointes
          closeMatchEucli = true;
          continue;
        }
      }
    }
    if(closeMatchTopo){
      this.trust = 1.;
    }
    else if(closeMatchTopo2){
      this.trust = 0.5;
    }
    else if(closeMatchEucli){
      this.trust = 0;
    }
    else{
      this.trust = -1;
    }

  }

  public static void generateMatchingIncosistencies(STGraph stgraph, STPattern pattern, Set<MatchingLink> result) {
    for(STEntity edge: stgraph.getEdges()){
      if(!pattern.find(edge.getTimeSerie())){
        continue;
      }
      // on regarde si ça a pas déja été sélectionné
      boolean ok = true;
      for(MatchingLink l: result){
        if(l.getTargets().getEdges().contains(edge)){
          ok = false;
          break;
        }      
        if(l.getSources().getEdges().contains(edge)){
          ok = false;
          break;
        }  
      }
      if(!ok){
        continue;
      }
      List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>();
      for(FuzzyTemporalInterval t : edge.getTimeSerie().getValues().keySet()){
        if(edge.existsAt(t)){
          times.add(t);
        }
      }
      times.remove(null);
      List<STEntity> matchPattern = new ArrayList<STEntity>();
      //on va chercher des patterns compatibles
      FullStabilitySTPattern stability = new FullStabilitySTPattern();
      for(STEntity e : stgraph.getEdges()){
        if(stability.find(e.getTimeSerie())){
          continue;
        }
        // il reste des patterns de naissance, mort, réincarnation et apparition
        // compatibilité des domaines temporels ?
        boolean add= true;
        for(FuzzyTemporalInterval t : times){
          if(e.existsAt(t)){
            add = false;
            break;
          }
        }
        if(add){
          // les domaines temporels d'existence ne s'intersectent pas
          matchPattern.add(e);
        }
      }

      // on a les patterns candidats
      STEntity closeMatchedTopoo = null;
      STEntity node1 = stgraph.getEndpoints(edge).getFirst();
      STEntity node2 = stgraph.getEndpoints(edge).getSecond();
      DijkstraShortestPath<STEntity, STEntity> distances = new DijkstraShortestPath<STEntity, STEntity>(stgraph);

      for(STEntity e2: matchPattern){
        if(edge.equals(e2)){
          continue;
        }
        // déja utilisé?
        ok = true;
        for(MatchingLink l: result){
          if(l.getTargets().getEdges().contains(e2)){
            ok = false;
            break;
          }      
          if(l.getSources().getEdges().contains(e2)){
            ok = false;
            break;
          }  
        }
        if(!ok){
          continue;
        }
        STEntity node21 = stgraph.getEndpoints(e2).getFirst();
        STEntity node22 = stgraph.getEndpoints(e2).getSecond();

        if(distances.getDistance(node1, node21).intValue()<= dtopo ){
          if(distances.getDistance(node2, node22).intValue()<= dtopo){
            closeMatchedTopoo = e2;
            break;
          }
        }
        else  if(distances.getDistance(node1, node22).intValue()<= dtopo){
          if(distances.getDistance(node2, node21).intValue()<= dtopo){
            //on vérifie que les séries temporelles sont disjointes
            closeMatchedTopoo = e2;
            break;
          }
        }
      }
      if(closeMatchedTopoo != null){
        //c'ets bon
        MatchingLink l = new MatchingLink();
        l.getSources().getEdges().add(edge);
        l.getTargets().getEdges().add(closeMatchedTopoo);
        result.add(l);
      }
    }
  }

  public static void main(String[] args) {
    STGraph stg = TAGIoManager.deserialize("/home/bcostes/Bureau/TAG/corrections_patterns/etape2/tag_corrected.tag");





    AppearanceSTPattern app = new AppearanceSTPattern();

    IPopulation<IFeature> out = new Population<IFeature>();


    //Set<MatchingLink> links = new HashSet<MatchingLink>();


    for(STEntity e : stg.getEdges()){
      if(app.find(e.getTimeSerie())){
        MatchingInconsistency criteria = new MatchingInconsistency();
        criteria.evaluate(stg, e, PATTERN_TYPE.APPEARANCE);
        if(criteria.getTrust() !=-1){
          IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
          AttributeManager.addAttribute(f,"cc", criteria.getTrust(), "String");
          out.add(f);
          continue;
        }

      }
    }


    //MatchingInconsistency.generateMatchingIncosistencies(stg, app, links);



    ReincarnationSTPattern ree = new ReincarnationSTPattern();

    for(STEntity e : stg.getEdges()){
      if(ree.find(e.getTimeSerie())){
        MatchingInconsistency criteria = new MatchingInconsistency();
        criteria.evaluate(stg, e, PATTERN_TYPE.REINCARNATION);
        if(criteria.getTrust() !=-1){

          //System.out.println(e.getTimeSerie().toSequence()+" : " + criteria.getTrust());
          IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
          AttributeManager.addAttribute(f,"cc", criteria.getTrust(), "String");
          out.add(f);
        }
      }
    }

    ShapefileWriter.write(out, "/home/bcostes/Bureau/TAG/corrections_patterns/etape2/matching_inconsistencies.shp");



    //    MatchingInconsistency.generateMatchingIncosistencies(stg, ree, links);
    //    System.out.println("links.size() : "+links.size());
    //    System.out.println(CorrectionMerge.cpt);
    //  //  
    //    STGraph stg2 = CorrectionMerge.correctMerge(stg, new ArrayList<MatchingLink>(links));
    //    String outputStg ="/home/bcostes/Bureau/TAG/corrections_patterns/etape1/tag_corrected.shp";
    //    String outputStg2 ="/home/bcostes/Bureau/TAG/corrections_patterns/etape1/tag_corrected.tag";
    //
    //       
    //    
    //   TAGIoManager.serializeBinary(stg2, outputStg2);
    //
    //   TAGIoManager.exportTAG(stg2, outputStg);
    //   TAGIoManager.exportSnapshots(stg2, outputStg, TAGIoManager.NODE_AND_EDGE);
    //    
    //    System.out.println(links.size());

    //    IPopulation<IFeature> pop = new Population<IFeature>();
    //    
    //    
    //    
    //    ReincarnationSTPattern pattern = new ReincarnationSTPattern();       
    //    for(STEntity edge : stg2.getEdges()){
    //      STProperty<Boolean> ts = edge.getTimeSerie();
    //      if(pattern.find(ts)){
    //        // System.out.println("Réincarnation ! ");
    //        List<FuzzyTemporalInterval> times = pattern.findEvent(edge.getTimeSerie());
    //        DefaultFeature f = new DefaultFeature(edge.getGeometry().toGeoxGeometry());
    //        AttributeManager.addAttribute(f, "death", times.get(0).toString(), "String");
    //        AttributeManager.addAttribute(f, "re-live", times.get(1).toString(), "String");
    //        pop.add(f);
    //      }
    //    }
    //    System.out.println(pop.size());
    //    ShapefileWriter.write(pop,"/home/bcostes/Bureau/TAG/corrections_patterns/etape1/reincarnations.shp");
    //    pop.clear();
    //
    //
    //    AppearanceSTPattern pattern2 = new AppearanceSTPattern();       
    //    for(STEntity edge : stg2.getEdges()){
    //      STProperty<Boolean> ts = edge.getTimeSerie();
    //
    //      if(pattern2.find(ts)){
    //        List<FuzzyTemporalInterval> times = pattern2.findEvent(ts);
    //        DefaultFeature f = new DefaultFeature(edge.getGeometry().toGeoxGeometry());
    //        AttributeManager.addAttribute(f, "live", times.get(0).toString(), "String");
    //        AttributeManager.addAttribute(f, "death", times.get(1).toString(), "String");
    //        pop.add(f);
    //      }        
    //    }      
    //    System.out.println(pop.size());
    //    ShapefileWriter.write(pop, "/home/bcostes/Bureau/TAG/corrections_patterns/etape1/appearances.shp");

  }

}
