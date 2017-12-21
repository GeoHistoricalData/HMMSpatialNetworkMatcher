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

import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;
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
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.contrib.geometrie.Vecteur;
import fr.ign.cogit.geoxygene.distance.Frechet;
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
 *  
 *   Utilise l'algorithme Hongrois pour cherche un couplage parfait de pooids minimal dans un graphe biparti valué
 * 
 * 
 * @author bcostes
 *
 */
public class PolygonMatcherHungarianAlgorithm {

  /**
   * Thresold used to filter candidates with euclidean distance criterion
   */
  private final double euclidean_thresold = 25;
  /**
   * Thresold used to filter candidates with overlaping criterion
   */
  private final double min_overlap = 0.95; // en %
  /**
   * Thresold used to filter candidates with orientation criterion
   */
  private final double angular_thresold = 25; // en degrés
  /**
   * Thresold used to calculate matching score
   */
  private final double frechet_thresold = 15;


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
  public PolygonMatcherHungarianAlgorithm(STGraph stag, String polygonSourceFile){
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
    // if(pdata.getGeom().intersects(stedge.getGeometry().toGeoxGeometry())){
    //      if(pdata.getGeom().intersection(stedge.getGeometry().toGeoxGeometry()).length() >= min_overlap*
    //          stedge.getGeometry().toGeoxGeometry().length()){
    //        return true;
    //      }
    //    }
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
      angleSkeleton  =/* Angle.ecart(Angle.anglePlat,*/ Operateurs.directionPrincipaleOrientee(list).angleAPiPres()/*)*/;
    }
    else{
      lineSkeleton = this.completeSkeleton(lines);
      angleSkeleton= /*Angle.ecart(Angle.anglePlat, */Operateurs.directionPrincipaleOrientee(lineSkeleton.coord()).angleAPiPres()/*)*/;
    }
    //    double angleSkeletonV = (angleSkeleton.getValeur() > Math.PI/2) ? (Math.PI - angleSkeleton.getValeur()) :(angleSkeleton.getValeur());
    //    double angleEntityV = (angleEntity.getValeur() > Math.PI/2) ? (Math.PI - angleEntity.getValeur()) :(angleEntity.getValeur());
    //    double value = Math.abs(angleSkeletonV - angleEntityV)*180./Math.PI;
    //    if (value< this.angular_thresold){
    //      return true;
    //    }


    double value = Math.min(Angle.ecart(angleEntity, angleSkeleton).getValeur() * 180./Math.PI,
        Angle.ecart(new Angle(Math.PI - angleEntity.getValeur()), angleSkeleton).getValeur() * 180./Math.PI);
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
    angleSkeleton  =  Operateurs.directionPrincipaleOrientee(lSeg.coord()).angleAPiPres();
    //    angleSkeletonV = (angleSkeleton.getValeur() > Math.PI/2) ? (Math.PI - angleSkeleton.getValeur()) :(angleSkeleton.getValeur());
    //    value = Math.abs(angleSkeletonV - angleEntityV)*180./Math.PI;
    //    return (value< this.angular_thresold);

     value = Math.min(Angle.ecart(angleEntity, angleSkeleton).getValeur() * 180./Math.PI,
        Angle.ecart(new Angle(Math.PI - angleEntity.getValeur()), angleSkeleton).getValeur() * 180./Math.PI);
    if (value< this.angular_thresold){
      return true;
    }
    return false;
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
      //   on cherche où tombe la date du polygone dans la liste times
      //            int indexT = -1;
      //            boolean inFuzzySet = false;
      //            for(FuzzyTemporalInterval t: times){
      //              if((int)t.getX(0) <= dateP &&(int)t.getX(t.size()-1) >= dateP){
      //                indexT = times.indexOf(t);
      //                inFuzzySet= true;
      //                break;
      //              }
      //            }
      //            if(!inFuzzySet){
      //              // la date tombe entre deux intervalles temporels du domaine d'existence du STAG
      //              for(int i=0; i< times.size()-1; i++){
      //                if((int)times.get(i).getX(times.get(i).size()-1) < dateP && 
      //                    (int)times.get(i+1).getX(0)>dateP){
      //                  indexT = i;
      //                  break;
      //                }         
      //              }
      //              //on choisit comme candidats les entités qui existent à la date immédiatement postérieure
      //              //à la date du polygone
      //              stcandidates.addAll(this.stag.getEdgesAt(times.get(indexT+1)));
      //            }
      //            else{
      //              // la date tombe pile dans un intervalle temporel du domaine d'existence
      //              if(indexT != times.size()-1){
      //                //si on tombe pas sur la dernière date,
      //                // on considère comme candidats les entités qui existent à la date de l'intervalle
      //                //et a celle imédiatement postérieure
      //                stcandidates.addAll(this.stag.getEdgesAt(times.get(indexT)));
      //                stcandidates.addAll(this.stag.getEdgesAt(times.get(indexT+1)));
      //              }
      //              else{
      //                stcandidates.addAll(this.stag.getEdgesAt(times.get(indexT)));
      //              }
      //            }
      //
      //      stcandidates.addAll(this.stag.getEdges());
      for(STEntity e : this.stag.getEdges()){
        // if(e.existsAt(times.get(1)) || e.existsAt(times.get(2)) || e.existsAt(times.get(3))){
        stcandidates.add(e);
        // }
      }
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

        IPopulation<IFeature> out = new Population<IFeature>();
        for(STEntity e: candidates.keySet()){
          IDirectPosition p1 = Operateurs.milieu((ILineString)e.getGeometry().toGeoxGeometry());
          for(PolygonData p: candidates.get(e)){
            IDirectPosition p2 = p.getGeom().centroid();
            
    
            IDirectPositionList list = new DirectPositionList();
            list.add(p1);
            list.add(p2);
            
            IFeature f = new DefaultFeature(new GM_LineString(list));
            out.add(f);
          }
        }
    
        ShapefileWriter.write(out, "/home/bcostes/Bureau/cadnidates.shp");

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
    //    Map<BiGraphVertex, STEntity> mapMatchingGraphRoot = new HashMap<BiGraphVertex, STEntity>();
    //    // racines de l'arbre: les stedge candidats
    //    Set<BiGraphVertex> initSet = new HashSet<BiGraphVertex>();
    //    for(STEntity e: candidates.keySet()){
    //      if(candidates.get(e).isEmpty()){
    //        continue;
    //      }
    //      BiGraphVertex v =new BiGraphVertex();
    //      v.getEntities().add(e);
    //      initSet.add(v);
    //      mapMatchingGraphRoot.put(v, e);
    //    }
    //    //création du graphe de candidats
    //    DirectedGraph<BiGraphVertex, Integer> candidatesGraph = this.createCandidatesGraph(initSet, candidates);
    //    System.out.println("Candidates graph : " + candidatesGraph.getVertexCount()+" "+ candidatesGraph.getEdgeCount());


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
    System.out.println("Building connected components");
    ConnectedComponents<PolygonData, Integer> cc = new ConnectedComponents<PolygonData, Integer>(graphPolygons);
    List<UndirectedSparseMultigraph<PolygonData, Integer>> connectedComponents = cc.buildConnectedComponents();
    System.out.println("Connected components built");
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

      // Structure qui associe chaque racine de l'arbre des candidats au stedge correspondant
      Map<BiGraphVertex, STEntity> mapMatchingGraphRoot = new HashMap<BiGraphVertex, STEntity>();
      // racines de l'arbre: les stedge candidats
      Set<BiGraphVertex> initSet = new HashSet<BiGraphVertex>();
      for(STEntity e: candidates.keySet()){
        if(candidates.get(e).isEmpty()){
          continue;
        }
        boolean selected = false;
        for(PolygonData p: pdata){
          if(candidates.get(e).contains(p)){
            selected = true;
            break;
          }
        }
        if(!selected){
          continue;
        }
        BiGraphVertex v =new BiGraphVertex();
        v.getEntities().add(e);
        initSet.add(v);
        mapMatchingGraphRoot.put(v, e);
      }
      //création du graphe de candidats
      System.out.println(initSet.size());
      DirectedGraph<BiGraphVertex, Integer> candidatesGraph = this.createCandidatesGraph(initSet, candidates);
      //Map<PolygonData, Map<BiGraphVertex, Double>> scores = this.createCandidatesGraph_test(initSet, candidates);
      //System.out.println("Candidates graph : " + scores.size());


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
          
          
          lineMerged = Operateurs.resampling(lineMerged, 5);
          lineSkeleton = Operateurs.resampling(lineSkeleton, 5);
          
          score =Math.min(Frechet.discreteFrechet(lineMerged, lineSkeleton),
              Frechet.discreteFrechet(lineMerged, lineSkeleton.reverse()));
          
          //
          //          score = Math.min(Distances.premiereComposanteHausdorff(lineMerged, lineSkeleton),
          //              Distances.premiereComposanteHausdorff(lineMerged, lineMerged));
          score =  this.evaluate(score);

          double longueurDif = lineSkeleton.length() - lineMerged.length();
          double ratio = Math.max(lineMerged.length(), lineSkeleton.length());
          if(longueurDif>0){
            score *= Math.exp((-longueurDif)/(ratio * 0.1));
          }

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

            
            lineMerged = Operateurs.resampling(lineMerged, 5);
            lineSkeleton = Operateurs.resampling(lineSkeleton, 5);

            score =Math.min(Frechet.discreteFrechet(lineMerged, lineSkeleton),
                Frechet.discreteFrechet(lineMerged, lineSkeleton.reverse()));
            //
            //               score = Math.min(Distances.premiereComposanteHausdorff(lineMerged, lineSkeleton),
            //                  Distances.premiereComposanteHausdorff(lineMerged, lineMerged));
            score =  this.evaluate(score);

            double longueurDif = lineSkeleton.length() - lineMerged.length();
            double ratio = Math.max(lineMerged.length(), lineSkeleton.length());
            if(longueurDif>0){
              score *= Math.exp((-longueurDif)/(ratio * 0.1));
            }
            
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



      //on fait les ACluster
            Map<Integer, PolygonData> indexesRef= new HashMap<Integer, PolygonData>();
            Map<Integer, BiGraphVertex> indexesComp = new HashMap<Integer, BiGraphVertex>();
            Map<Integer, STEntity> indexesEntities= new HashMap<Integer, STEntity>();
      
            int cptRef=0;
      
            LocalHypergraph hypergraph = new LocalHypergraph();
      
      
            for(PolygonData clusterRef: scores.keySet()){
              indexesRef.put(clusterRef.hashCode(), clusterRef);
              hypergraph.getHypervertices().add( clusterRef.hashCode());
              for(BiGraphVertex v: scores.get(clusterRef).keySet()){
                if(indexesComp.containsKey(v.hashCode())){
                  continue;
                }
                for(STEntity e : v.getEntities()){
                  indexesComp.put(v.hashCode(), v);
                  hypergraph.getHypervertices().add(e.hashCode());
                  indexesEntities.put(e.hashCode(), e);
                }
      
              }
            }
      
      
      
            Map<Integer,Set<Integer>> indexHyperArcs = new HashMap<Integer,Set<Integer>>();
            int cpt = 0;
            if(indexesComp.size()== 0 || indexesRef.size() ==0){
              continue;
            }
      
      
      
            for(Integer  i : indexesRef.keySet()){
              PolygonData clusterRef = indexesRef.get(i);
              for(Integer  j : indexesComp.keySet()){
                BiGraphVertex clusterComp = indexesComp.get(j);
                //création d'un hyperarc
                Set<Integer> newHypArc = new HashSet<Integer>();
                newHypArc.add(clusterRef.hashCode());
                for(STEntity e : clusterComp.getEntities()){
                  newHypArc.add(e.hashCode());
                }
                indexHyperArcs.put( cpt++, newHypArc);
                hypergraph.getHyperedges().add(newHypArc);
                if(scores.get(clusterRef).containsKey(clusterComp)){
                  hypergraph.getCosts().put(newHypArc, scores.get(clusterRef).get(clusterComp));
                }
                else{
                  hypergraph.getCosts().put(newHypArc, Double.MIN_VALUE);
                }
              }
            }
      
            //résolution du pb d'optimisation linéaire
            LPWizard lpw = new LPWizard();
            for(Integer i: indexHyperArcs.keySet()){
              String var = "x" + i; 
              lpw.plus(var, hypergraph.getCosts().get(indexHyperArcs.get(i)));
              lpw.setBoolean(var);
            }
            //maximisation
            lpw.setMinProblem(false);
            //les contraintes ....
            cpt=0;
            for(Integer vertex: hypergraph.getHypervertices()){
              LPWizardConstraint lpwc = lpw.addConstraint("c"+cpt,1,">=");
              for(Integer i: indexHyperArcs.keySet()){
                Set<Integer> hyparc = indexHyperArcs.get(i);
                if(hyparc.contains(vertex)){
                  String var = "x" + i; 
                  lpwc.plus(var, 1.);
                }
              }
              cpt++;
            }
      
            LPSolution sol = lpw.solve();



//
//      Map<Integer, PolygonData> mapIPol = new HashMap<Integer, PolygonData>();
//      Map<Integer, BiGraphVertex> mapVPol = new HashMap<Integer, BiGraphVertex>();
//      Set<BiGraphVertex> allbv = new HashSet<BiGraphVertex>();
//      int cpt=0;
//      int cptv = 0;
//      for(PolygonData p: scores.keySet()){
//        for(BiGraphVertex v: scores.get(p).keySet()){
//          if(allbv.contains(v)){continue;}
//          allbv.add(v);
//          mapVPol.put(cptv, v );
//          cptv++;
//        }
//        mapIPol.put(cpt, p);
//        cpt++;
//      }
//
//      double[][] array = new double[scores.keySet().size()][allbv.size()];
//      for(int i=0; i< scores.keySet().size(); i++){
//        for(int j=0;j< allbv.size(); j++){
//          if(!scores.get(mapIPol.get(i)).containsKey(mapVPol.get(j))){
//            array[i][j] = Double.MAX_VALUE;
//          }
//          else{
//            array[i][j] = 1.-scores.get(mapIPol.get(i)).get(mapVPol.get(j));
//          }
//        }
//      }
//
//      HungarianAlgorithm ha = new HungarianAlgorithm(array);
//      System.out.println("Hungarian algorithme : " + array.length+" * " + array[0].length );
//      int result[] = ha.execute();
//      System.out.println("ha done");
//

      // Finalisation: remplissage de la structure d'appariements
      //chaque polygones est associés aux stedges représentés par son candidat
      // dans le graphe des candidats

            for(int i=0; i< indexHyperArcs.size(); i++){
              if(sol.getBoolean("x"+i)){
                Set<Integer> hyparc = indexHyperArcs.get(i);
                List<Integer> arcref = new ArrayList<Integer>();
                List<Integer> arccomp = new ArrayList<Integer>();
                List<STEntity> entities = new ArrayList<STEntity>();
      
                for(Integer vertex: hyparc){
                  if(indexesRef.keySet().contains(vertex)){
                    arcref.add(vertex);
                  }
                  else{
                    arccomp.add(vertex);
                    entities.add(indexesEntities.get(vertex));
                  }
                }
      
                for(Integer aref: arcref){
                  PolygonData pol = indexesRef.get(aref);
                  for(STEntity e:entities){
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
//      for(int i=0; i< result.length; i++){
//        PolygonData pol = mapIPol.get(i);
//        if(result [i] == -1){
//          //not matched
//          continue;
//        }
//        BiGraphVertex bv = mapVPol.get(result[i]);
//
//        //recalage ? 
//        // this.realignment(pol, solution.get(pol).getEntities());
//
//
//        for(STEntity e: bv.getEntities()){
//          if(this.matching.containsKey(e)){
//            this.matching.get(e).add(pol);
//          }
//          else{
//            Set<PolygonData> set = new HashSet<PolygonData>();
//            set.add(pol);
//            this.matching.put(e, set);
//          }
//        }
//      }
    }
  }


  private double evaluate(double score) {
    return Math.exp(-score / this.frechet_thresold);
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







  public Map<PolygonData, Map<BiGraphVertex, Double>> createCandidatesGraph_test(Set<BiGraphVertex> candidates, 
      Map<STEntity, Set<PolygonData>> allCandidates){

    Map<PolygonData, Map<BiGraphVertex, Double>> result = new HashMap<PolygonData, Map<BiGraphVertex,Double>>();

    /*
     * Liste de candidats pour chaque polygone
     */
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

    Map<ILineString, STEntity> mapL = new HashMap<ILineString, STEntity>();
    for(BiGraphVertex v :candidates){
      mapL.put((ILineString)v.getEntities().iterator().next().getGeometry().toGeoxGeometry(), 
          v.getEntities().iterator().next());
    }
    UndirectedSparseMultigraph<STEntity, Integer> gc = new UndirectedSparseMultigraph<STEntity, Integer>();
    int cpt=0;
    for(BiGraphVertex v: candidates){
      gc.addVertex(v.getEntities().iterator().next());
    }
    List<ILineString> listL = new ArrayList<ILineString>(mapL.keySet());
    for(int i=0; i< listL.size()-1; i++){
      for(int j=i+1; j< listL.size(); j++){
        if(listL.get(i).startPoint().equals(listL.get(j).startPoint()) || 
            listL.get(i).startPoint().equals(listL.get(j).endPoint())  || 
            listL.get(i).endPoint().equals(listL.get(j).startPoint())  ||
            listL.get(i).endPoint().equals(listL.get(j).endPoint())){
          gc.addEdge(cpt++, mapL.get(listL.get(i)), mapL.get(listL.get(j)));
        }
      }
    }
    ConnectedComponents<STEntity, Integer> ccbuilder = new ConnectedComponents<STEntity, Integer>(gc);
    List<UndirectedSparseMultigraph<STEntity, Integer>> cc = ccbuilder.buildConnectedComponents();
    for(UndirectedSparseMultigraph<STEntity, Integer> gcc: cc){
      Set<Set<STEntity>> wrongCombinations = new HashSet<Set<STEntity>>();
      Set<Set<STEntity>> rightCombinations = new HashSet<Set<STEntity>>();
      Stack<STEntity> stack = new Stack<STEntity>();
      stack.addAll(gcc.getVertices());
      while(!stack.isEmpty()){
        STEntity v= stack.pop();
        Set<STEntity> currentCombinations = new HashSet<STEntity>();
        currentCombinations.add(v);
        rightCombinations.add(currentCombinations);
        recTree(v, currentCombinations, gcc, rightCombinations, wrongCombinations, allCandidatesReverse);
      }

      for(Set<STEntity> ee: rightCombinations){
        BiGraphVertex v = new BiGraphVertex();
        v.setEntities(ee);
        for( PolygonData pol: allCandidatesReverse.keySet()){
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
          if(allCandidatesReverse.get(pol).containsAll(ee)){
            double score = 0;
            List<ILineString> list = new ArrayList<ILineString>();
            for(STEntity e :v.getEntities()){
              list.add(new GM_LineString((e.getGeometry().toGeoxGeometry().coord())));
            }
            // aggrégation des géométries des stedge
            ILineString lineMerged = Operateurs.union(new ArrayList<ILineString>(list));
            //  lineSkeleton = Operateurs.resampling(lineSkeleton, 2);
            //  lineMerged = Operateurs.resampling(lineMerged, 2);
            score =Math.min(Frechet.discreteFrechet(lineMerged, lineSkeleton),
                Frechet.discreteFrechet(lineMerged, lineSkeleton.reverse()));

            // score =Distances.hausdorff(lineMerged, lineSkeleton); 
            score =  this.evaluate(score);

            double longueurDif = lineSkeleton.length() - lineMerged.length();
            if(longueurDif>0){
              score *= Math.exp((-longueurDif)/50);
            }

            if(score == 0){
              continue;
            }
            if(result.containsKey(pol)){
              result.get(pol).put(v, score);
            }
            else{
              Map<BiGraphVertex, Double> s = new HashMap<BiGraphVertex, Double>();
              s.put(v, score);
              result.put(pol, s);
            }
          }
        }
      }
    }

    return result;
  }



  private void recTree(STEntity v, Set<STEntity> currentCombinations,
      UndirectedSparseMultigraph<STEntity, Integer> gcc,
      Set<Set<STEntity>> rightCombinations, Set<Set<STEntity>> wrongCombinations, Map<PolygonData, Set<STEntity>> allCandidatesReverse) {

    Set<STEntity> neighbors = new HashSet<STEntity>();
    neighbors.addAll(gcc.getNeighbors(v));
    neighbors.removeAll(currentCombinations);
    if(neighbors.isEmpty()){
      return;
    }
    for(STEntity vertex: neighbors){
      Set<STEntity> combination = new HashSet<STEntity>();
      combination.add(vertex);
      combination.addAll(currentCombinations);
      if(wrongCombinations.contains(combination)){
        continue;
      }
      boolean okCandidate =false;
      for(PolygonData p : allCandidatesReverse.keySet()){
        if(allCandidatesReverse.get(p).containsAll(combination)){
          okCandidate = true;
          break;
        }
      }
      if(!okCandidate){
        wrongCombinations.add(combination);
        continue;
      }
      rightCombinations.add(combination);
      recTree(vertex, combination, gcc, rightCombinations, wrongCombinations, allCandidatesReverse);
    }


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
    @Override
    public int hashCode(){
      return this.geom.hashCode();
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

  public Set<PolygonData> getPolygonDataset() {
    return polygonDataset;
  }

  public void setPolygonDataset(Set<PolygonData> polygonDataset) {
    this.polygonDataset = polygonDataset;
  }

  class LocalHypergraph{
    private Set<Integer> hypvertices;
    private Set<Set<Integer>> hypedges;
    private Map<Set<Integer>, Double> costs;

    public LocalHypergraph(){
      this.hypedges = new HashSet<Set<Integer>>();
      this.hypvertices = new HashSet<Integer>();
      this.costs = new HashMap<Set<Integer>, Double>();
    }

    public Map<Set<Integer>, Double> getCosts() {
      return costs;
    }

    public void setCosts(Map<Set<Integer>, Double> costs) {
      this.costs = costs;
    }

    public Set<Integer> getHypervertices() {
      return hypvertices;
    }

    public void setHypervertices(Set<Integer> hypvertices) {
      this.hypvertices = hypvertices;
    }

    public Set<Set<Integer>> getHyperedges() {
      return hypedges;
    }

    public void setHyperedges(Set<Set<Integer>> hypedges) {
      this.hypedges = hypedges;
    }

  }


  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    // TODO Auto-generated method stub



    String inputStg ="/home/bcostes/Bureau/stag_json/stag_json.tag";
    STGraph stg = TAGIoManager.deserialize(inputStg);

    PolygonMatcherHungarianAlgorithm mapM = new PolygonMatcherHungarianAlgorithm(stg, "/home/bcostes/Bureau/pol.shp");
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

    //TAGIoManager.serializeBinary(stg, "/media/bcostes/Data/Benoit/Bureau/travaux/tag_works/tag_works.tag");

    ShapefileWriter.write(matching, "/home/bcostes/Bureau/matching_test4.shp");
    //ShapefileWriter.write(polRecal, "/home/bcostes/Bureau/travaux/polRecal.shp");

  }


}
