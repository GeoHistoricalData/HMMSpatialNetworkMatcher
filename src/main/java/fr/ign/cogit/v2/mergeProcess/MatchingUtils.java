package fr.ign.cogit.v2.mergeProcess;

import java.util.HashSet;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.appariement.EnsembleDeLiens;
import fr.ign.cogit.geoxygene.contrib.appariement.Lien;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.lineage.MatchingLink;
import fr.ign.cogit.v2.lineage.STGroupe;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;

public class MatchingUtils {

  /**
   * Calcul des relations ST maximale (i.e des composantes connexes dans le graphe orienté dont les
   * sommets sont les features appariés (arcs et sommet) et les arcs les liens d'appariement
   * @param links
   * @return
   */
  public static EnsembleDeLiens getConnectedMatchs(EnsembleDeLiens links){
    IFeatureCollection<IFeature> pop1 = new Population<IFeature>();
    IFeatureCollection<IFeature> pop2 = new Population<IFeature>();
    for(Lien link:links){
      for(IFeature fRef: link.getObjetsRef()){
        if(!pop1.contains(fRef)){
          pop1.add(fRef);
        }
      }
      for(IFeature fComp: link.getObjetsComp()){
        if(!pop2.contains(fComp)){
          pop2.add(fComp);
        }
      }
    }
    return links.regroupeLiens(pop1, pop2);
  }

  /**
   * Convertit un EnsembleDeLiens entre un STGraph stg et un snapshot g2 (qui est aussi un STGraph
   * à une seule date) en lien d'appariement générique MatchingLink
   * @param liens
   * @param stgraph
   * @param t2
   * @param g2
   * @return
   */
  public static Set<MatchingLink> createGenericMatchingLinks(EnsembleDeLiens liens, STGraph stgraph, FuzzyTemporalInterval t2,
      STGraph g2) {
    Set<MatchingLink> stlinks = new HashSet<MatchingLink>();
    for (Lien lien : liens) {
      MatchingLink stlink = new MatchingLink();
      STGroupe sources = new STGroupe();
      STGroupe targets = new STGroupe();
      for (IFeature source : lien.getObjetsRef()) {
        // noeud ou arc ?
        if (source.getGeom() instanceof GM_Point) {
          // sommet
          STEntity stnode = null;
          for (STEntity stnode2 : stgraph.getVertices()) {
            if (((GM_Point) stnode2.getGeometry().toGeoxGeometry())
                .equalsExact(source.getGeom(), 0.005)) {
              stnode = stnode2;
              break;
            }
          }
          if (stnode != null) {
            targets.getNodes().add(stnode);
          } else {
            // problème : noeud non trouvé
            System.out
            .println("Sommet 1 non trouvé : DefaultFiliationGraphBuilder.createSTLinks");
            System.out.println(source.getGeom().toString());
            System.exit(-16);
          }
        } else {
          STEntity stedge = null;
          for (STEntity stedge2 : stgraph.getEdges()) {
            if (stedge2.getGeometry().toGeoxGeometry().equals(source.getGeom())) {
              stedge = stedge2;
              break;
            }
          }
          if (stedge != null) {
            targets.getEdges().add(stedge);
          } else {
            // problème : arc non trouvé
            System.out
            .println("Arc 1 non trouvé : DefaultFiliationGraphBuilder.createSTLinks");
            System.out.println(source.getGeom().toString());
            System.exit(-17);
          }
        }
      }
      // ************************************************************
      // objets comp
      for (IFeature target : lien.getObjetsComp()) {
        // noeud ou arc ?
        if (target.getGeom() instanceof GM_Point) {
          // sommet
          STEntity stnode = null;
          for (STEntity stnode2 : g2.getVertices()) {
            if (((GM_Point) stnode2.getGeometry().toGeoxGeometry())
                .equals(target.getGeom())) {
              stnode = stnode2;
              break;
            }
          }
          if (stnode != null) {
            sources.getNodes().add(stnode);
          } else {
            // problème : noeud non trouvé
            System.out
            .println("Sommet 2 non trouvé : DefaultFiliationGraphBuilder.createSTLinks");
            System.exit(-18);
          }
        } else {
          // arc
          STEntity stedge = null;
          for (STEntity stedge2 : g2.getEdges()) {
            if (stedge2.getGeometry().toGeoxGeometry().equals(target.getGeom())) {
              stedge = stedge2;
              break;
            }
          }
          if (stedge != null) {
            sources.getEdges().add(stedge);
          } else {
            // problème : arc non trouvé
            System.out
            .println("Arc 2 non trouvé : DefaultFiliationGraphBuilder.createSTLinks");
            System.exit(-19);
          }
        }
      }
      stlink.setSources(sources);
      stlink.setTargets(targets);
      stlinks.add(stlink);
    }
    if (stlinks.size() != liens.size()) {
      System.out
      .println("Taille de la liste de STLink en sortie non conforme à la taille de l'EnsembleDeLiens en entrée");
      System.exit(-20);
    }
    return stlinks;
  }

  /**
   * Filtre un ensemble de liens d'appariement générique selon des patterns d'appariement qu'on juge
   * acceptables. Voir AcceptableMatchingPattern
   * @param links
   */
  public static void filterAcceptableMatchingPatterns(Set<MatchingLink> links){
    for(MatchingLink link: new HashSet<MatchingLink>(links)){
      boolean isAcceptable = false;
      for(AcceptableMatchingPattern pattern: AcceptableMatchingPattern.values()){
        if(pattern.isMatchedBy(link)){
          // c'est un pattern acceptable
          isAcceptable = true;
          break;
        }           
      }
      if (! isAcceptable){
        //on va regarder si c'est un cas particulier de l'appariement
        // de Mustiere & Devogele
        //i.e {e} -> {n*e, (n-1)*v} ou {m*e, (m-1)*v} -> {n*e, (n-1) * v}
        if(link.getSources().getEdges().size() == 1 && link.getSources().getNodes().isEmpty()){
          // 1 edge source
          if(link.getTargets().getEdges().size()>1){
            if(link.getTargets().getEdges().size() == link.getTargets().getNodes().size() +1){
              // cas {e} -> {n*e, (n-1)*v}
              //on le transforme en {e} -> {n*e}
              link.getTargets().setNodes(new HashSet<STEntity>());
              continue;
            }
          }
        }
        else if(link.getSources().getEdges().size()>1){
          if(link.getSources().getEdges().size() == link.getSources().getNodes().size() +1){
            // on a en source : {m*e, (m-1)*v}
            if(link.getTargets().getEdges().size() == 1 && link.getTargets().getNodes().isEmpty()){
              //cas {m*e, (m-1)*v} -> {e}
              //on le transforme en {m*e} -> {e}
              link.getSources().setNodes(new HashSet<STEntity>());
              continue;
            }
            else if(link.getTargets().getEdges().size()>1){
              if(link.getTargets().getEdges().size() == link.getTargets().getNodes().size() +1){
                // cas {m*e, (m-1)*v} -> {n*e, (n-1)*v}
                //on le transforme en {m*e} -> {n*e}
                link.getSources().setNodes(new HashSet<STEntity>());
                link.getTargets().setNodes(new HashSet<STEntity>());
                continue;
              }
            } 
          }
        }

        links.remove(link);
      }
    }
  }

  /**
   * Filtre un ensemble de liens d'appariement générique selon un pattern d'appariement acceptable
   * donné
   * @param pattern
   * @param links
   * @return
   */
  public static Set<MatchingLink> findAcceptableMatchingPattern(AcceptableMatchingPattern pattern,
      Set<MatchingLink> links){
    Set<MatchingLink> result = new HashSet<MatchingLink>();
    for(MatchingLink link: links){
      if(pattern.isMatchedBy(link)){
        // c'est bon
        result.add(link);
      }
    }
    return result;
  }

  /**
   * Cherche un lien d'appariement générique dont entity est une (parmis les) des sources / targets
   * respectant un certain pattern
   * @param entity
   * @param pattern
   * @param stlinks
   * @return
   */
  public static MatchingLink findMatchingLink(STEntity entity, AcceptableMatchingPattern pattern,
      Set<MatchingLink> stlinks){
    Set<MatchingLink> links = MatchingUtils.findAcceptableMatchingPattern(pattern,
        stlinks);
    if (links.isEmpty()) {
      return null;
    }
    // il y a des liens d'appariement qui respectent le pattern
    for (MatchingLink link : links) {
      // entity est-il bien dans les sources ?
      if (entity.getType() == STEntity.NODE
          && link.getSources().getNodes().contains(entity)) {
        return link;
      }
      if (entity.getType() == STEntity.EDGE
          && link.getSources().getEdges().contains(entity)) {
        return link;
      }
      // ou une cible ?
      if (entity.getType() == STEntity.NODE
          && link.getTargets().getNodes().contains(entity)) {
        return link;
      }
      if (entity.getType() == STEntity.EDGE
          && link.getTargets().getEdges().contains(entity)) {
        return link;
      }
    }
    return null;        
  }


}
