package fr.ign.cogit.v2.tag.enrichment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import fr.ign.cogit.cartagen.genealgorithms.polygon.Skeletonize;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineSegment;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.contrib.geometrie.Distances;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.contrib.geometrie.Vecteur;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_Polygon;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.conversion.WktGeOxygene;
import fr.ign.cogit.morphogenesis.network.utils.ConnectedComponents;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;

/**
 * Associate polygon data with dates with stedges of stag
 * N: M matching
 * L'algo fonctionne comme suit : 
 * 1: filtrage temporel des candidats en fonctions des dates associées aux polygones   
 * 2 :fitrage des candidats par distance et orientation aux squelettes des polygones
 * 3: construction du graphe des candidats (chaque candidats et leur différentes concaténations)
 * 4: Construction des graphes des contraintes (polygones et candidats communs)
 * 5: calcul du score de l'appariement de chaque polygone avec ses candidats du graphe de candidats. 
 * Utilisation d'heuristiques pour réduire l'espace de recherche
 * 6: Construction du graphe d'appariement  pour chaque graphe des contraintes 
 *  et choix de la solution qui maximise le score moyen (moyenne harmonique
 * pour favoriser les scores proches plutot que les gros scores (on va favorise 0.5 et 0.5 plutôt que 0.8 et 0.2 par ex/)
 * Si pas de solution satisfaisante, on associe chaque polygone à son candidat avec le plus gros score (appariement multiple)
 * 
 * Idée pour améliorer l'algo: problème de la taille de l'espace de recherche si beaucoups de candidats
 * communs à de nombreux polygones. Utiliser un MCMC ou autre algo d'optimisation stochastique ...
 * 
 * Utilise une méthode de prise de décision un peu à l'arrache ....
 * 
 * @author bcostes
 *
 */
public class PolygonFeatureMapMatching {

  /**
   * Thresold used to filter candidates with euclidean distance criterion
   */
  private final double euclidean_thresold = 20;
  /**
   * Thresold used to filter candidates with overlaping criterion
   */
  private final double min_overlap = 0.95; // en %
  /**
   * Thresold used to filter candidates with orientation criterion
   */
  private final double angular_thresold = 30; // en degrés
  /**
   * Thresold used to calculate matching score
   */
  private final double frechet_thresold = 35;


  /**
   * STGraph used for the matching process
   */
  private STGraph stag;
  /**
   * Polygons to match with STEdges of STGraph
   */
  private Set<PolygonData> polygonDataset;
  /**
   * Matching result: each STEdge is matched with one or several polygons.
   */
  private Map<STEntity, Set<PolygonData>> matching;
  /**
   * Unmatched polygons
   */
  private Set<PolygonData> unmatched;

  /**
   * Local variable used to find the best candidate for each polygon
   */
  private double solutionScore; // score of the matching link
  private Map<PolygonData, BiGraphVertex> solution; // a matching solution for each connected composant
  //in the graph built from polygons connected by common candidates.

  /**
   * Stdout variable
   */
  public static int CPT = 0;
  public static long SIZE = 0;


  /**
   * Constructeur
   * @param stag
   * @param polygonSourceFile
   */
  public PolygonFeatureMapMatching(STGraph stag, String polygonSourceFile){
    this.stag = stag;
    this.polygonDataset = new HashSet<PolygonData>();
    this.matching = new HashMap<STEntity, Set<PolygonData>>();
    this.unmatched = new HashSet<PolygonData>();
    this.shp2polygonData(polygonSourceFile);
  }

  /**
   * Load polygon data from shapefile source
   * @param shp
   */
  private void shp2polygonData(String shp){
    IPopulation<IFeature> data = ShapefileReader.read(shp);
    for(IFeature f : data){
      int date = Integer.parseInt(f.getAttribute(f.getFeatureType().getFeatureAttributeByName("date")).toString());
      //String type= f.getAttribute(f.getFeatureType().getFeatureAttributeByName("type")).toString();
      PolygonData pdata = new PolygonData(date);
      pdata.setGeom(new GM_Polygon(new GM_LineString(f.getGeom().coord())));
      pdata.setType(POLYGON_DATA_TYPE.PIERCING);
      this.polygonDataset.add(pdata);
    }
  }

  /**
   * Test if a given candidate is acceptable of matching with a given polygon
   * @param pdata
   * @param stedge
   * @return
   */
  private boolean isGoodCandidate(PolygonData pdata, STEntity stedge){
    //un candidat est acceptable si il est contenu à au moins x% dans le polygone
    if(pdata.getGeom().intersects(stedge.getGeometry().toGeoxGeometry())){
      if(pdata.getGeom().intersection(stedge.getGeometry().toGeoxGeometry()).length() >= min_overlap*
          stedge.getGeometry().toGeoxGeometry().length()){
        return true;
      }
    }
    //un candidat est acceptable si son orientation n'est pas incompatible avec
    //celle du squelette du polygone
    Angle angleEntity = Operateurs.directionPrincipale(stedge.getGeometry().toGeoxGeometry().coord());
    //squeletisation du polygone
    Set<ILineSegment> skeleton = Skeletonize.skeletonizeStraightSkeleton((IPolygon)pdata.getGeom());

    List<ILineString> lines = new ArrayList<ILineString>();
    for(ILineSegment segment: skeleton){
      lines.add(new GM_LineString(segment.coord()));
    }



    Angle angleSkeleton = null;
    ILineString lineSkeleton = null;
    if(!this.lineMergePossible(lines)){
      //merge impossible
      //récupération d'un côté de la polyligne du polygone
      IDirectPositionList list = new DirectPositionList();
      for(IDirectPosition p :pdata.getGeom().coord()){
        if(!list.contains(p)){
          list.add(p);
        }
        else{
          break;
        }
      }
      angleSkeleton  =/* Angle.ecart(Angle.anglePlat,*/ Operateurs.directionPrincipale(list)/*)*/;
    }
    else{
      lineSkeleton = this.completeSkeleton(lines);
      angleSkeleton= /*Angle.ecart(Angle.anglePlat, */Operateurs.directionPrincipale(lineSkeleton.coord())/*)*/;
    }
    double angleSkeletonV = (angleSkeleton.getValeur() > Math.PI/2) ? (Math.PI - angleSkeleton.getValeur()) :(angleSkeleton.getValeur());
    double angleEntityV = (angleEntity.getValeur() > Math.PI/2) ? (Math.PI - angleEntity.getValeur()) :(angleEntity.getValeur());
    double value = Math.abs(angleSkeletonV - angleEntityV)*180./Math.PI;
    if (value< this.angular_thresold){
      return true;
    }

    //sinon on essaye avec le lineSegment le plus proche (découpage des polygones non rectangulaires)
    if(lines.size() == 1){
      return false;
    }
    ILineString lSeg = null;
    double min = Double.MAX_VALUE;
    for(ILineString l: lines){
      double d =l.distance(stedge.getGeometry().toGeoxGeometry());
      if(d < min){
        lSeg = l;
        min = d;
      }
    }
    angleSkeleton  =  Operateurs.directionPrincipale(lSeg.coord());
    angleSkeletonV = (angleSkeleton.getValeur() > Math.PI/2) ? (Math.PI - angleSkeleton.getValeur()) :(angleSkeleton.getValeur());
    value = Math.abs(angleSkeletonV - angleEntityV)*180./Math.PI;
    return (value< this.angular_thresold);
  }

  /**
   * Main  matching process of polygon data with stag objects
   */
  public void match(){
    // chaque stedges va être associé potentiellement à plusieurs polygones
    Map<STEntity, Set<PolygonData>> candidates = new HashMap<STEntity, Set<PolygonData>>();
    for(STEntity edge: this.stag.getEdges()){
      //initialisation
      candidates.put(edge, new HashSet<PolygonData>());
    }

    //  domaine d'existence de l'étude
    List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>();
    times.addAll(this.stag.getTemporalDomain().asList());
    // qu'on trie dans l'ordre chronologique croissant
    Collections.sort(times);

   

    //start matching process      
    for(PolygonData pdata: this.polygonDataset){
      //pour chaque polygone
      //sa date
      int dateP = (int)pdata.getDate().getX(1);
      //candidats à l'appariement avec ce polygone
      Set<STEntity> stcandidates = new HashSet<STEntity>();
      //************************************************************************************
      //***************** 1 ere étape : filtrage temporel des candidats ********************
      //************************************************************************************
      // on cherche où tombe la date du polygone dans la liste times
//      int indexT = -1;
//      boolean inFuzzySet = false;
//      for(FuzzyTemporalInterval t: times){
//        if((int)t.getX(0) <= dateP &&(int)t.getX(t.size()-1) >= dateP){
//          indexT = times.indexOf(t);
//          inFuzzySet= true;
//          break;
//        }
//      }
//      if(!inFuzzySet){
//        // la date tombe entre deux intervalles temporels du domaine d'existence du STAG
//        for(int i=0; i< times.size()-1; i++){
//          if((int)times.get(i).getX(times.get(i).size()-1) < dateP && 
//              (int)times.get(i+1).getX(0)>dateP){
//            indexT = i;
//            break;
//          }         
//        }
//        //on choisit comme candidats les entités qui existent à la date immédiatement postérieure
//        //à la date du polygone
//        stcandidates.addAll(this.stag.getEdgesAt(times.get(indexT+1)));
//      }
//      else{
//        // la date tombe pile dans un intervalle temporel du domaine d'existence
//        if(indexT != times.size()-1){
//          //si on tombe pas sur la dernière date,
//          // on considère comme candidats les entités qui existent à la date de l'intervalle
//          //et a celle imédiatement postérieure
//          stcandidates.addAll(this.stag.getEdgesAt(times.get(indexT)));
//          stcandidates.addAll(this.stag.getEdgesAt(times.get(indexT+1)));
//        }
//        else{
//          stcandidates.addAll(this.stag.getEdgesAt(times.get(indexT)));
//        }
//      }
      stcandidates.addAll(this.stag.getEdges());

      //************************************************************************************
      //********** 2eme étape : filtrage des candidats par voisinage ***********************
      //************************************************************************************
      for(STEntity candidate: new HashSet<STEntity>(stcandidates)){
        if(!pdata.getGeom().buffer(this.euclidean_thresold).contains(
            candidate.getGeometry().toGeoxGeometry())){
          stcandidates.remove(candidate);
          continue;
        }
      }
      if(stcandidates.isEmpty()){
        //aucun candidat restant
        this.unmatched.add(pdata);
        continue;
      }

      //************************************************************************************
      //********** 3eme étape : filtrage des candidats par orientation *********************
      //************************************************************************************
      for(STEntity candidate: new HashSet<STEntity>(stcandidates)){
        if(!this.isGoodCandidate(pdata,candidate)){
          stcandidates.remove(candidate);
          continue;
        }
      }
      if(stcandidates.isEmpty()){
        //aucun candidat restant
        this.unmatched.add(pdata);
        continue;
      }
      for(STEntity candidate: stcandidates){
        candidates.get(candidate).add(pdata);
      }

    }


    //************************************************************************************
    //******************** 4eme étape : graphe des candidats *****************************
    //************************************************************************************
    //Le graphe des candidats est un arbre. Les racines représentant chaque stedge candidat à 
    // l'appariement. Chaque paire de racine est associée au candidat représentant l'aggrégation des deux 
    //stedge (des racines) si il forme une solution acceptable. Et ainsi de suite jusqu'aux feuilles
    // de l'abre des candidats, représantant l'aggrégation de l'ensemble des candidats (de toutes les racines)
    // si cette solution est acceptable.
    // si on a N stedges candidats, il y a au plus somme (k=1,N) (k parmi n) (ex pour 3: 7 candidats, pour 4: 15 candidats)

    // Structure qui associe chaque racine de l'arbre des candidats au stedge correspondant
    Map<BiGraphVertex, STEntity> mapMatchingGraphRoot = new HashMap<BiGraphVertex, STEntity>();
    // racines de l'arbre: les stedge candidats
    Set<BiGraphVertex> initSet = new HashSet<BiGraphVertex>();
    for(STEntity e: candidates.keySet()){
      if(candidates.get(e).isEmpty()){
        continue;
      }
      BiGraphVertex v =new BiGraphVertex();
      v.getEntities().add(e);
      initSet.add(v);
      mapMatchingGraphRoot.put(v, e);
    }
    //création du graphe de candidats
    DirectedGraph<BiGraphVertex, Integer> candidatesGraph = this.createCandidatesGraph(initSet, candidates);
    System.out.println("Candidates graph : " + candidatesGraph.getVertexCount()+" "+ candidatesGraph.getEdgeCount());


    //************************************************************************************
    //******************** 5eme étape : graphe des contraintes****************************
    //************************************************************************************
    //on va regrouper les polygones si ils ont au moins un candidats commun
    // un graphe des contraintes corrrespond à une composante connexe dans un graphe ou les sommets sont les polygones
    // et les arcs relient les polygones entre eux si ils ont au moins un candidat commun (graphe non orienté)
    UndirectedSparseMultigraph<PolygonData, Integer> graphPolygons = new UndirectedSparseMultigraph<PolygonData, Integer>();
    int cptPol = 0;
    for(STEntity candidate: candidates.keySet()){
      if(candidates.get(candidate).isEmpty()){
        continue;
      }
      if(candidates.get(candidate).size() == 1){
        graphPolygons.addVertex(candidates.get(candidate).iterator().next());
      }
      else{
        Iterator<PolygonData> pit = candidates.get(candidate).iterator();
        PolygonData p1 = pit.next();
        while(pit.hasNext()){
          PolygonData p2 = pit.next();
          graphPolygons.addEdge(cptPol++, p1, p2);
        }
      }
    }

    //on regroupe par composantes connexes
    ConnectedComponents<PolygonData, Integer> cc = new ConnectedComponents<PolygonData, Integer>(graphPolygons);
    List<UndirectedSparseMultigraph<PolygonData, Integer>> connectedComponents = cc.buildConnectedComponents();
    for(UndirectedSparseMultigraph<PolygonData, Integer> connectedComponent: connectedComponents){
      //pour chaque graphe d'appariement
      if(connectedComponent.getVertexCount() == 0){
        //WTF ?
        continue;
      }

      //on est en présence de PolygponData partageant au moins un candidat
      Set<PolygonData> pdata= new HashSet<PolygonData>();
      //les polygones concernés
      pdata.addAll(connectedComponent.getVertices());
      //structure qui stocke pour chaque polygone, ses candidats dans les graphe de candidat et le score associé
      // à leur appariement avec le polygone
      Map<PolygonData, Map<BiGraphVertex, Double>> scores = new HashMap<PolygonData, Map<BiGraphVertex,Double>>();
      for(PolygonData pol: pdata){
        //pour chaque polygone du graphe des contraintes
        //son squelette
        Set<ILineSegment> skeleton = Skeletonize.skeletonizeStraightSkeleton((IPolygon)pol.getGeom());
        List<ILineString> lines = new ArrayList<ILineString>();
        for(ILineSegment segment: skeleton){
          lines.add(new GM_LineString(segment.coord()));
        }
        ILineString lineSkeleton = null;
        if(this.lineMergePossible(lines)){
          lineSkeleton = this.completeSkeleton(lines);
        }
        else{
          //merge impossible
          //récupération d'un côté de la polyligne du polygone
          IDirectPositionList list = new DirectPositionList();
          for(IDirectPosition p :pol.getGeom().coord()){
            if(!list.contains(p)){
              list.add(p);
            }
            else{
              break;
            }
          }
          lineSkeleton  = new GM_LineString(list);
        }

        //pour le polygone concerné, stocke le score des contraintes
        // à chacun de ses candidats
        Map<BiGraphVertex, Double> scoresP = new HashMap<BiGraphVertex, Double>();
        // initialement, le polygone est appariés à des candidats représentant des stedge simples (les
        // racines du graphe de candidats)
        Set<BiGraphVertex> initVerticesSet = new HashSet<BiGraphVertex>();
        for(BiGraphVertex v: mapMatchingGraphRoot.keySet()){
          STEntity candidate = mapMatchingGraphRoot.get(v);
          if(candidates.get(candidate).contains(pol)){
            initVerticesSet.add(v);
            continue;
          }
        }
        //on va récupérer tous les candidats du polygones (les aggrégations) à partir de des racines de ses$
        // candidats dans le graphe de candidats de manière itérative
        // et pour chacun on calcul un score
        for(BiGraphVertex v: initVerticesSet){
          double score = 0;
          List<ILineString> list = new ArrayList<ILineString>();
          for(STEntity e :v.getEntities()){
            list.add(new GM_LineString((e.getGeometry().toGeoxGeometry().coord())));
          }
          // aggrégation des géométries des stedge
          ILineString lineMerged = Operateurs.union(new ArrayList<ILineString>(list));
          score =Distances.hausdorff(lineMerged, lineSkeleton);
          score =  this.evaluate(score);
          if(score == 0){
            // si le score est nul, on ne retient pas ce candidats
            continue;
          }
          //sinon on prend ce candidats et on lui associe son score d'appariement
          scoresP.put(v, score);
        }
        //on va ajouter récursivement tous les enfants des candidats retenus$
        // dans le graphe de candidats
        //structure pour ne pas traiter deux fois le meme candidat
        Set<BiGraphVertex> done = new HashSet<BiGraphVertex>();
        done.addAll(initVerticesSet);
        while(true){
          Set<BiGraphVertex> set = new HashSet<BiGraphVertex>();
          //récupération des enfants des candidats courrants
          for(BiGraphVertex v: initVerticesSet){
            for(BiGraphVertex successor: candidatesGraph.getSuccessors(v)){
              if(done.contains(successor)){
                //déja traité?
                continue;
              }
              boolean ok = true;
              //le candidat n'est retenu que si l'ensemble des stedge qu'il représente par aggrégation
              //sont aussi séparemment candidats avec le polygone
              // en gros si le polygone est initialement candidat aux stedge (e1, e2, e3), alors
              //un éventuel candidat (e1+e2+e3+e4) ou (e1+e4+e5) (les + pour l'aggrégation) 
              // ne serrai pas retenu
              for(STEntity s: successor.getEntities()){
                if(!candidates.get(s).contains(pol)){
                  ok = false;
                  break;
                }
              }
              if(ok){
                //c'est bon, on retient le candidat et on étudiera ses enfants dans le prochaine passe
                // de la boucle
                set.add(successor);
              }
            }
          }
          if(set.isEmpty()){
            //on a plus aucun nouveau candidat à étudier, on arrête la boucle
            break;
          }
          initVerticesSet.clear();
          //on a de nouveaux candidats à étudier
          initVerticesSet.addAll(set);
          done.addAll(initVerticesSet);
          for(BiGraphVertex v: initVerticesSet){
            //pour chacun de ces nouveaux candidats on regarde leur score d'appariement
            double score = 0;
            List<ILineString> list = new ArrayList<ILineString>();
            for(STEntity e :v.getEntities()){
              list.add(new GM_LineString((e.getGeometry().toGeoxGeometry().coord())));
            }
            ILineString lineMerged = Operateurs.union(new ArrayList<ILineString>(list));
            score =Distances.hausdorff(lineMerged, lineSkeleton);
            score =  this.evaluate(score);
            if(score == 0){
              //score =0, on ne retient pas le candidats
              continue;
            }
            //c'est bon, on associe son score au candidat
            scoresP.put(v, score);
          }
          //et on repart pour un tour de boucle, on va étudier les enfants des derniers candidats retenus
        }
        //une fois la boucle terminée, on associe au polygone tous les candidats 
        // du graphe de candidats retenus et leur score
        scores.put(pol, scoresP);
      }

      //************************************************************************************
      //******************** 6eme étape :choix de la meilleur solution**********************
      //************************************************************************************
      // Grpahe d'appariement: graphe dont les sommets sont les polygones du graphe des contraintes
      //courant et leurs candidats, et les arcs orientés relient les polygones à leur candidat.
      // Une solution du graphe d'appariement correspond à un ensemble de sous grpahe du graphe d'appariement
      // ou chaque polygone n'a qu'un seul et unique voisin (un candidat), et chaque candidat a au plus un voisin (un polygone)
      // les arcs sont pondérés par le score de l'appariement qu'il représente
      //on décide de retenir la solution qui maximise le score moyen de chaque polygone du graphe d'appariement
      // courrant, au sens de la moyenne harmonique.
      // problème: si on a N polygones dans le grpahe d'appariement, et que chaque polygone a mi candidats,
      // alors on a m1*m2*...*mN solutions possible ....
      //il faut réduire l'espace de recherche
      // TODO : développer ici un MCMC ou autre méthode d'optimisation
      //Pour le moment, on va regarder presque toutes les solutions ... mais on propose tout de meme
      // une heuristique pour éduire l'espace de recherche : 
      // on supprime tous les successeurs de X si score (X) > score tous les successeurs
      for(PolygonData pol: scores.keySet()){
        //pour cahque polygone du graphe d'appariement courrant
        //choix des candidats qu'on va supprimer
        Set<BiGraphVertex> remove = new HashSet<BiGraphVertex>();
        for(BiGraphVertex v: scores.get(pol).keySet()){
          Set<BiGraphVertex> successors = new HashSet<BiGraphVertex>();
          for(BiGraphVertex suc: candidatesGraph.getSuccessors(v)){
            if(scores.get(pol).containsKey(suc)){
              successors.add(suc);
            }
          }
          //un candidat à t(il un score meilleur que tous les scores de ses successeurs?
          boolean betterThanSuccesors = true;
          for(BiGraphVertex succ: successors){
            if(scores.get(pol).get(succ)> scores.get(pol).get(v)){
              betterThanSuccesors = false;
              break;
            }
          }
          if(betterThanSuccesors){
            // c'est le cas! 
            //on supprime récurssivement tous les successeurs (et les successeurs des succeseurs, etc.)
            while(!successors.isEmpty()){
              remove.addAll(successors);
              Set<BiGraphVertex> suc2 = new HashSet<BiGraphVertex>();
              for(BiGraphVertex suc: successors){
                if(!remove.contains(suc)){
                  for(BiGraphVertex sucsuc: candidatesGraph.getSuccessors(suc)){
                    if(scores.get(pol).containsKey(sucsuc)){
                      successors.add(sucsuc);
                    }
                  }        
                }
              }
              successors.clear();
              successors.addAll(suc2);
            }
          }
        }
        //on supprime tous les candidats qui ont été tagués
        for(BiGraphVertex v : remove) {
          scores.get(pol).remove(v);
        }
      }
      //heuristique terminée !

      //maintenant on cherche la meilleure solution dans le graphe d'appariement qui reste
      //méthode récursive
      //la meilleure solution trouvée
      this.solution = new HashMap<PolygonData, BiGraphVertex>();
      this.solutionScore = Double.MIN_VALUE;
      Map<PolygonData, BiGraphVertex> currentSolutionTest = new HashMap<PolygonData, BiGraphVertex>();
      Stack<PolygonData> itPdata = new Stack<PolygonData>();
      itPdata.addAll(scores.keySet());
      PolygonData pIni = itPdata.pop();

      //taille de l'espace de recherche
      SIZE= 1;
      for(PolygonData p : scores.keySet()){
        SIZE *= scores.get(p).size();
      }
      CPT=0;

      //on commence par étudier le premier plygone de la pile
      for(BiGraphVertex v: scores.get(pIni).keySet()){
        //chaque candidat de ce polygon est associé à un début de solution
        currentSolutionTest.put(pIni, v);
        Stack<PolygonData> itPData2 = new Stack<PolygonData>();
        itPData2.addAll(itPdata);
        // algo récurssif qui crée et étudie l'ensemble des solutions possibles
        // dans le grpahe d'appariement
        recOptimization(currentSolutionTest, itPData2, scores, candidatesGraph);
      }
      //c'est fini!
      if(solution.isEmpty()){
        //on a pas trouvé de compromi
        //on fait du matching multiple
        for(PolygonData pol: scores.keySet()){
          double max = Double.MIN_VALUE;
          for(BiGraphVertex v: scores.get(pol).keySet()){
            if(scores.get(pol).get(v) > max){
              max = scores.get(pol).get(v);
              solution.put(pol, v);
            }
          }
        }

      }

      // Finalisation: remplissage de la structure d'appariements
      //chaque polygones est associés aux stedges représentés par son candidat
      // dans le graphe des candidats


      for(PolygonData pol: solution.keySet()){


        //recalage ? 
       // this.realignment(pol, solution.get(pol).getEntities());


        for(STEntity e: solution.get(pol).getEntities()){
          if(scores.get(pol).get(solution.get(pol)) == 0){
            continue;
          }
          if(this.matching.containsKey(e)){
            this.matching.get(e).add(pol);
          }
          else{
            Set<PolygonData> set = new HashSet<PolygonData>();
            set.add(pol);
            this.matching.put(e, set);
          }
        }
      }
    }
  }

  /**
   * Algo récurssif qui crée l'ensemble des solutions possibles du graphe d'appariement
   *et choisi la meilleure
   * @param currentSolutionTest
   * @param itPdata
   * @param scores
   * @param matchingGraph
   */
  private void recOptimization(
      Map<PolygonData, BiGraphVertex> currentSolutionTest,
      Stack<PolygonData> itPdata, Map<PolygonData, Map<BiGraphVertex, Double>> scores,  DirectedGraph<BiGraphVertex, Integer> matchingGraph) {
    if(!itPdata.isEmpty()){
      // il reste des polygones non traités dans la pile
      PolygonData currentPdata = itPdata.pop(); 
      for(BiGraphVertex v: scores.get(currentPdata).keySet()){
        //on continue de constituer les solutions
        currentSolutionTest.put(currentPdata, v);
        Stack<PolygonData> itPdata2 = new Stack<PolygonData>();
        itPdata2.addAll(itPdata);
        recOptimization(currentSolutionTest, itPdata2, scores, matchingGraph);
      }
    }
    else{
      //on va tester la solution courrante !
      CPT++;
      if(CPT % 10 == 0){
        System.out.println(CPT + " / "+ SIZE);
      }
      //la solution est-elle possible ?
      //une solution est réalisable si les candidats retenus pour chaque polygone
      // sont indépendants: par de recoupement dans les stedges qu'ils représentent
      // en gros, un candidat retenu dans la solution testée ne doit pas être un successeurans le graphe des candidats
      // (meme éventuellement éloigné) d'un autre candidat retenu dans la solution testée d
      boolean solutionOk = true;
      Stack<BiGraphVertex> stack= new Stack<BiGraphVertex>();
      stack.addAll(currentSolutionTest.values());
      while(!stack.isEmpty()){
        BiGraphVertex v1 = stack.pop();
        for(BiGraphVertex v2: stack){
          if(v1.equals(v2)){
            //deux fois le meme candidats dans la solution ... pas possible !
            solutionOk = false;
            break;
          }
          for(STEntity e: v1.getEntities()){
            if(v2.getEntities().contains(e)){
              // recoupement des stedges .. pas possible !
              solutionOk = false;
              break;
            }
          }
          if(!solutionOk){
            break;
          }
        }
        if(!solutionOk){
          break;
        }
      }
      if(solutionOk){
        // solution réalisable ! 
        //on regarde le score de la solution
        double score = 0;
        for(PolygonData p: scores.keySet()){
          if(scores.get(p).get(currentSolutionTest.get(p))== 0){
            score = 0;
            break;
          }
          score += 1./scores.get(p).get(currentSolutionTest.get(p));
        }
        if(score !=0){
          score = (double)scores.size()  * 1./score;
        }
        if(score > solutionScore){
          //score meilleur que le meilleur score courrant, on prend cette nouvelle solution
          solutionScore = score;
          for(PolygonData p: currentSolutionTest.keySet()){
            solution.put(p, currentSolutionTest.get(p));
          }
        }
      }
    }
  }

  /**
   * Méthode qui crée l'arbre des candidats de manière récursive
   * @param candidates
   * @param allCandidates
   * @return
   */
  public DirectedGraph<BiGraphVertex, Integer> createCandidatesGraph(Set<BiGraphVertex> candidates, 
      Map<STEntity, Set<PolygonData>> allCandidates){
    int edgesCpt = 1;

    Map<PolygonData, Set<STEntity>> allCandidatesReverse = new HashMap<PolygonData, Set<STEntity>>();
    for(STEntity e: allCandidates.keySet()){
      for(PolygonData p: allCandidates.get(e)){
        if(allCandidatesReverse.containsKey(p)){
          allCandidatesReverse.get(p).add(e);
        }
        else{
          Set<STEntity> set = new HashSet<STEntity>();
          set.add(e);
          allCandidatesReverse.put(p, set);
        }
      }
    }
    DirectedGraph<BiGraphVertex, Integer> graph = new DirectedSparseGraph<BiGraphVertex, Integer>();
    for(BiGraphVertex e : candidates){
      graph.addVertex(e);
    }
    // on va tenter de concatener des candidats
    List<BiGraphVertex> vertex = new ArrayList<BiGraphVertex>();
    vertex.addAll(candidates);
    while(true){
      List<BiGraphVertex> newVertices = new ArrayList<BiGraphVertex>();
      for(int i =0; i< vertex.size()-1; i++){
        for(int j =i+1; j< vertex.size(); j++){
          if(vertex.get(i).getEntities().containsAll(vertex.get(j).getEntities())
              || vertex.get(j).getEntities().containsAll(vertex.get(i).getEntities())){
            continue;
          }
          Set<STEntity> entities = new HashSet<STEntity>();
          List<ILineString> lines = new ArrayList<ILineString>();
          entities.addAll(vertex.get(i).getEntities());
          entities.addAll(vertex.get(j).getEntities());
          //y a t(il un polygone avec tout ces candidats?
          boolean okCandidate =false;
          for(PolygonData p : allCandidatesReverse.keySet()){
            if(allCandidatesReverse.get(p).containsAll(entities)){
              okCandidate = true;
              break;
            }
          }
          if(!okCandidate){
            continue;
          }
          for(STEntity v: entities){
            lines.add(new GM_LineString(v.getGeometry().toGeoxGeometry().coord()));
          }

          if(!this.lineMergePossible(lines)){
            //on ne peut pas concaténer les géométries des stedge
            // représenté par ce futur candidat, on ne le retient donc pas
            continue;
          }    
          //on a réussi! on crée un nouveau GraphEntity
          BiGraphVertex newV = new BiGraphVertex();
          newV.getEntities().addAll(entities);
          if(graph.containsVertex(newV)){
            continue;
          }
          graph.addEdge(edgesCpt++, vertex.get(i), newV);
          graph.addEdge(edgesCpt++, vertex.get(j), newV);
          newVertices.add(newV);
          continue;
        }
      }
      if(newVertices.isEmpty()){
        break;
      }
      vertex.clear();
      vertex.addAll(newVertices);
    }
    return graph;
  }


  /**
   * Méthode qui teste si un ensemble de linestring peut être aggrégé
   * @param lines
   * @return
   */
  private boolean lineMergePossible(List<ILineString> lines){
    Map<IDirectPosition, Integer> pts = new HashMap<IDirectPosition, Integer>();
    for(ILineString l: lines){
      if(pts.containsKey(l.startPoint())){
        pts.put(l.startPoint(), pts.get(l.startPoint())+1);
      }
      else{
        pts.put(l.startPoint(),1);
      }
      if(pts.containsKey(l.endPoint())){
        pts.put(l.endPoint(), pts.get(l.endPoint())+1);
      }
      else{
        pts.put(l.endPoint(),1);
      }
    }
    int alone = 0;
    for(IDirectPosition p : pts.keySet()){
      if(pts.get(p) == 1){
        alone ++;
      }
    }
    pts.clear();
    pts = null;
    return (alone == 2);
  }

  public Map<STEntity, Set<PolygonData>> getMatching() {
    return matching;
  }

  public void setMatching(Map<STEntity, Set<PolygonData>> matching) {
    this.matching = matching;
  }


  public void realignment(PolygonData pdata, Set<STEntity> edges){
    //translation
    IDirectPosition centroid = pdata.getGeom().centroid();
    List<ILineString> lines = new ArrayList<ILineString>();
    for(STEntity e : edges){
      lines.add((ILineString) e.getGeometry().toGeoxGeometry());
    }
    ILineString lineMerged = Operateurs.union(lines);
    IDirectPosition centroidLine = lineMerged.centroid();


    double tx = centroidLine.getX() - centroid.getX();
    double ty = centroidLine.getY() - centroid.getY();

    IGeometry newGeom = (IGeometry)pdata.getGeom().clone();
    System.out.println(newGeom);
    for(int i=0; i< newGeom.coord().size(); i++){
      IDirectPosition p = newGeom.coord().get(i);
      p.setX(p.getX() + tx);
      p.setY(p.getY() + ty);
    }
    if(!newGeom.intersects(lineMerged)){
      return;
    }
    if(!pdata.getGeom().intersects(lineMerged) || 
        pdata.getGeom().intersects(lineMerged) &&newGeom.intersection(lineMerged).length() > pdata.getGeom().intersection(lineMerged).length()){
      pdata.setGeom(newGeom);
    }
  }


  /**
   * Local class of polygon data
   * @author bcostes
   *
   */
  class PolygonData{
    // date associée au polygone
    private FuzzyTemporalInterval date;
    // sa géométrie
    private IGeometry geom;
    //le type (un attribut supplémentaire)
    private POLYGON_DATA_TYPE type;
    public PolygonData(int date){
      try {
        this.date = new FuzzyTemporalInterval(new double[]{date-1,date,date,date+1},new double[]{0,1,1,0}, 4);
      } catch (XValuesOutOfOrderException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (YValueOutOfRangeException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    public FuzzyTemporalInterval getDate() {
      return date;
    }
    public void setDate(FuzzyTemporalInterval date) {
      this.date = date;
    }
    public IGeometry getGeom() {
      return geom;
    }
    public void setGeom(IGeometry geom) {
      this.geom = geom;
    }
    public POLYGON_DATA_TYPE getType() {
      return type;
    }
    public void setType(POLYGON_DATA_TYPE type) {
      this.type = type;
    }
  }

  /**
   * Exemple de typage possible dans le cas de polygones représentant des travaux de voirie
   * @author bcostes
   *
   */
  public enum POLYGON_DATA_TYPE {
    PIERCING,
    ENLARGMENT,
    RECTIFICATION,
    OTHER
  }

  /**
   * Classe utilisée pour l'appariement. Représente un candidat. La notion de bigraph
   * est liée ici à la notio de niveau hiérarchique (ou niveau de détail) car un candidat
   * peut etre l'aggrégation de plusieurs autres candidats
   * @author bcostes
   *
   */
  class BiGraphVertex{

    // les stedges concernées par ce candidat
    private Set<STEntity> entities;
    public BiGraphVertex(){
      this.entities = new HashSet<STEntity>();
    }
    public Set<STEntity> getEntities() {
      return entities;
    }
    public void setEntities(Set<STEntity> entities) {
      this.entities = entities;
    }
    @Override
    public boolean equals(Object o){
      BiGraphVertex e = (BiGraphVertex)o;
      return (this.hashCode() == e.hashCode());
    }
    @Override
    public int hashCode(){
      List<STEntity> l = new ArrayList<STEntity>(this.entities);
      Collections.sort(l,new Comparator<STEntity>(){
        public int compare(STEntity o1, STEntity o2) {
          if(o1.getId() < o2.getId()){
            return -1;
          }
          else if(o1.getId() ==  o2.getId()){
            return 0;
          }
          else{
            return 1;
          }
        }

      });
      return l.hashCode();
    }
  }
  
  private double evaluate(double score) {
    return Math.exp(-score / this.frechet_thresold);
  }

  /**
   * Complète le squelette d'un polygone en rajoutant un peu de longueur aux extrémités
   * @return
   */
  private ILineString completeSkeleton(List<ILineString> lines){
    ILineString lineSkeleton = Operateurs.union(new ArrayList<ILineString>(lines));

    Vecteur vstart = new Vecteur();
    Vecteur vend = new Vecteur();
    vstart.setX(lineSkeleton.getControlPoint(0).getX()-lineSkeleton.getControlPoint(1).getX());
    vstart.setY(lineSkeleton.getControlPoint(0).getY()-lineSkeleton.getControlPoint(1).getY());
    vstart.normalise();
    vend.setX(lineSkeleton.getControlPoint(lineSkeleton.getControlPoint().size()-1).getX()
        -lineSkeleton.getControlPoint(lineSkeleton.getControlPoint().size()-2).getX());
    vend.setY(lineSkeleton.getControlPoint(lineSkeleton.getControlPoint().size()-1).getY()
        -lineSkeleton.getControlPoint(lineSkeleton.getControlPoint().size()-2).getY());
    vend.normalise();

    lineSkeleton.setControlPoint(0, new DirectPosition(lineSkeleton.getControlPoint(0).getX() + 5 * vstart.getX(),
        lineSkeleton.getControlPoint(0).getY() + 5 * vstart.getY()));
    lineSkeleton.setControlPoint(lineSkeleton.getControlPoint().size()-1, new DirectPosition(lineSkeleton.getControlPoint(lineSkeleton.getControlPoint().size()-1).getX() + 5 * vend.getX(),
        lineSkeleton.getControlPoint(lineSkeleton.getControlPoint().size()-1).getY() + 5 * vend.getY()));

    return lineSkeleton;
  }
  
  
  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    // TODO Auto-generated method stub



    String inputStg ="/home/bcostes/Bureau/stag_json/stag_json.tag";
    STGraph stg = TAGIoManager.deserialize(inputStg);

    PolygonFeatureMapMatching mapM = new PolygonFeatureMapMatching(stg,"/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/travaux_1789_1854/travaux_pol.shp");
    mapM.match();

    IPopulation<IFeature> matching = new Population<IFeature>();
    IPopulation<IFeature> polRecal = new Population<IFeature>();
    
    for(STEntity e : stg.getEdges()){
      e.setJsonAttributes(new HashSet<JSonAttribute>());
    }

    for(STEntity e : mapM.getMatching().keySet()){
      if(mapM.getMatching().get(e).isEmpty()){
        continue;
      }
      IDirectPosition p= Operateurs.milieu((ILineString)e.getGeometry().toGeoxGeometry());
      for(PolygonData pdata: mapM.getMatching().get(e)){

        JSonAttribute att = new JSonAttribute("works");
        att.putO("geometry", WktGeOxygene.makeWkt(pdata.getGeom()));
        att.hasGeometry(true);
        att.putI("date", (int)pdata.date.getX(1));       
        att.putO("work_type", pdata.type.toString());
        e.getJsonAttributes().add(att);
        
        IDirectPosition p2 = Operateurs.projection(p, new GM_LineString(pdata.getGeom().coord()));
        IDirectPositionList l= new DirectPositionList();
        l.add(p);
        l.add(p2);
        matching.add(new DefaultFeature(new GM_LineString(l)));
      }
    }

//    for(PolygonData pdata: mapM.getPolygonDataset()){
//      polRecal.add(new DefaultFeature(pdata.getGeom()));
//    }
    
   // TAGIoManager.serializeBinary(stg, "/home/bcostes/Bureau/travaux/tag_works/tag_works.tag");
    
    ShapefileWriter.write(matching, "/home/bcostes/Bureau/travaux/matching_test_oldmethode.shp");
    //ShapefileWriter.write(polRecal, "/home/bcostes/Bureau/travaux/polRecal.shp");

  }

  public Set<PolygonData> getPolygonDataset() {
    return polygonDataset;
  }

  public void setPolygonDataset(Set<PolygonData> polygonDataset) {
    this.polygonDataset = polygonDataset;
  }


}
