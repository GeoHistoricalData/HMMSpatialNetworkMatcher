package hmmmatching.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;

public class Utils {


  /**
   * Construit un chemin pseudo aléatoire dans un graphe entre les sommets
   * ini et fin (le chemin est globalement guidé dans la bonne direction ...(en termes
   * géographiques)
   * @param ini
   * @param fin
   * @return
   */
  public static List<Arc> randomPath(Noeud ini, Noeud fin){
    // TODO : vérifier que ini et fin sont dans la même composante connexe ...

    Noeud backupIni = ini;
    List<Arc> path = new ArrayList<>();
    // on récupère les voisins de ini
    List<Arc> incidents = new ArrayList<>(Utils.getIncidentEdges(ini));
    // on essaye de guide le processus en pondérant la sélection aléatoire par la distance entre
    // le noeud de l'arc et le noeud final (pour guider le chemin vers le noeud final)
    List<org.apache.commons.math3.util.Pair<Arc,Double>> itemWeights = new ArrayList<>();
    Map<Arc, Double> distances = new HashMap<Arc,Double>();
    for(Arc possibleNext : incidents){
      Noeud other = possibleNext.getOtherSide(ini);
      double d= other.distance(fin);
      distances.put(possibleNext, d);
    }
    double dmin = Double.MAX_VALUE;
    for(Double d: distances.values()){
      if(d < dmin){
        dmin = d;
      }
    }
    for(Arc possibleNext : distances.keySet()){
      if(distances.get(possibleNext) == 0){
        itemWeights.add(new Pair<>(possibleNext, Double.MAX_VALUE));
      }
      else{
        itemWeights.add(new Pair<>(possibleNext, Math.exp(-distances.get(possibleNext) /dmin)));
      }
    }
    Arc nextEdge = new EnumeratedDistribution<Arc>(itemWeights).sample();//TODO replace by multinomial ???
    Noeud next = nextEdge.getOtherSide(ini);
    path.add(nextEdge);
    while(!next.equals(fin)){
      ini = next;
      incidents = new ArrayList<>(Utils.getIncidentEdges(ini));
      if(incidents.size() != 1){
        incidents.remove(nextEdge);
      }
      // on essaye de guide le processus en pondérant la sélection aléatoire par la distance entre
      // le noeud de l'arc et le noeud final (pour guider le chemin vers le noeud final)
      itemWeights = new ArrayList<>();
      distances = new HashMap<Arc,Double>();
      for(Arc possibleNext : incidents){
        Noeud other = possibleNext.getOtherSide(ini);
        double d= other.distance(fin);
        distances.put(possibleNext, d);
      }
      dmin = Double.MAX_VALUE;
      for(Double d: distances.values()){
        if(d < dmin){
          dmin = d;
        }
      }
      for(Arc possibleNext : distances.keySet()){
        if(distances.get(possibleNext) == 0){
          itemWeights.add(new Pair<>(possibleNext, Double.MAX_VALUE));
        }
        else{
          itemWeights.add(new Pair<>(possibleNext, Math.exp(-distances.get(possibleNext) /dmin)));
        }
      }
      nextEdge = new EnumeratedDistribution<Arc>(itemWeights).sample();

      //      random = new Random();
      //      r = random.nextInt(incidents.size());
      //      nextEdge = incidents.get(
      //          r );
      next = nextEdge.getOtherSide(ini);
      path.add(nextEdge);
    }

    //on veut un chemin "simple"
    if((new HashSet<>(path)).size() != path.size()){
      // si des arcs sont utilisés plusieurs fois on va faire une opération assez complexe
      //on considère le sous graphe partiel engendré par les arcs de path et leurs sommets
      // puis ne retenir que le plus cours chemin entre ini et fin dans ce sous-graphe partiel
      // Cela ne revient PAS à considérer le ppc dans le grapahe initial ! (la génération
      // du chemin aléatoire a en qq sorte filtré le graphe)

      //création du sous-graphe partiel
      UndirectedSparseMultigraph<Noeud, Arc> ssg = new UndirectedSparseMultigraph<>();
      for(Arc a : (new HashSet<>(path))){
        ssg.addEdge(a, new edu.uci.ics.jung.graph.util.Pair<Noeud>(a.getNoeudIni(), a .getNoeudFin()));
      }
      Transformer<Arc, Double> wtTransformer = new Transformer<Arc, Double>() {
        @Override
        public Double transform(Arc a) {
          return a.longueur();                
        }            
      };
      DijkstraShortestPath<Noeud, Arc> sp = new DijkstraShortestPath<>(ssg, wtTransformer);
      // calcul du ppc sur ce sous-graphe
      path.clear();
      path.addAll(sp.getPath(backupIni, fin));

    }

    return path;

  }

  /**
   * Donne les voisins d'un sommet n
   * Suppose que la carte topo derrière est instanciée
   * @param n
   * @return
   */
  public static Set<Noeud> getNeighbors(Noeud n){
    Set<Noeud> neighbors = new HashSet<>();
    for(Arc in: n.getEntrants()){
      neighbors.add(in.getNoeudIni());
      neighbors.add(in.getNoeudFin());
    }
    for(Arc out: n.getSortants()){
      neighbors.add(out.getNoeudIni());
      neighbors.add(out.getNoeudFin());
    }
    neighbors.remove(n);;
    return neighbors;
  }

  /**
   * Donne les arcs incidents à un sommet n
   * Suppose que la carte topo derrière est instanciée
   * @param n
   * @return
   */
  public static Set<Arc> getIncidentEdges(Noeud n){
    Set<Arc> incidents = new HashSet<>();
    incidents.addAll(n.getEntrants());
    incidents.addAll(n.getSortants());
    return incidents;
  }

  /**
   * Découpe une LineString en morceaux si sa longueur est trop importante, avec resampling préalable
   * @param l
   * @param lengthMax
   * @param threshold
   * @return
   */
  public static List<ILineString> splitLineString(ILineString l, double  lengthMax, double threshold){
    if(threshold <0 ){
      threshold = l.length() / 10;
    }
    l = Operateurs.resampling(l, threshold);
    List<ILineString> newL = new ArrayList<>();   
    while(l.length() > lengthMax){
      ILineString l1 = new GM_LineString(Operateurs.premiersPoints(l, lengthMax));
      for(int i=0; i< l1.getControlPoint().size()-1; i++){
        l.removeControlPoint(l1.getControlPoint(i));
      }
      newL.add(l1);
    }
    newL.add(l);
    return newL;
  }

  public static IPopulation<IFeature> exportMatchinLinks(Map<Arc,List<ACluster>> matching){

    Map<Arc, Set<Arc>> matchingF = new HashMap<>();

    for(Arc a : matching.keySet()){
      matchingF.put(a, new HashSet<Arc>());
      for(ACluster cl : matching.get(a)){
        matchingF.get(a).addAll(cl.getArcs());
      }
    }

    return Utils.exportMatchinLinks2(matchingF);
  }

  public static IPopulation<IFeature> exportMatchinLinks2(Map<Arc,Set<Arc>> matching){
    Map<IFeature, Set<IFeature>> controlFinalLinks = new HashMap<>();
    IPopulation<IFeature> out = new Population<>();

    for(Arc a: matching.keySet()){
      for(IFeature f1: a.getCorrespondants()){
        ILineString g1  = new GM_LineString( f1.getGeom().coord());
        g1  = Operateurs.resampling(g1, g1.length()/4);
        IDirectPosition p11 = g1.getControlPoint(1);
        IDirectPosition p12 =Operateurs.milieu(g1);
        IDirectPosition p13 = g1.getControlPoint(g1.getControlPoint().size()-2);

        for(Arc a2: matching.get(a)){
          for(IFeature f2: a2.getCorrespondants()){
            if(controlFinalLinks.containsKey(f1)){
              if(controlFinalLinks.get(f1).contains(f2)){
                continue;
              }
              controlFinalLinks.get(f1).add(f2);
            }
            else{
              controlFinalLinks.put(f1, new HashSet<>(Arrays.asList(f2)));
            }
            ILineString g2  = new GM_LineString( f2.getGeom().coord());
            g2  = Operateurs.resampling(g2, g2.length()/4);
            IDirectPosition p21 = g2.getControlPoint(1);
            IDirectPosition p22 =Operateurs.milieu(g2);
            IDirectPosition p23 = g2.getControlPoint(g2.getControlPoint().size()-2);

            IDirectPosition pproj11 = Operateurs.projection(p11, g2);
            if(pproj11.equals(g2.startPoint(),1) || pproj11.equals(g2.endPoint(),1)){
              pproj11 = p11;
            }
            IDirectPosition pproj12 = Operateurs.projection(p12, g2);
            if(pproj12.equals(g2.startPoint(),0.005) || pproj12.equals(g2.endPoint(),0.005)){
              pproj12 = p12;
            }
            IDirectPosition pproj13 = Operateurs.projection(p13, g2);
            if(pproj13.equals(g2.startPoint(),1) || pproj13.equals(g2.endPoint(),1)){
              pproj13 = p13;
            }

            IDirectPosition pproj21 = Operateurs.projection(p21, g1);
            if(pproj21.equals(g1.startPoint(),1) || pproj21.equals(g1.endPoint(),1)){
              pproj21 = p21;
            }
            IDirectPosition pproj22 = Operateurs.projection(p22, g1);
            if(pproj22.equals(g1.startPoint(),0.005) || pproj22.equals(g1.endPoint(),0.005)){
              pproj22 = p22;
            }
            IDirectPosition pproj23 = Operateurs.projection(p23, g1);
            if(pproj23.equals(g1.startPoint(),1) || pproj23.equals(g1.endPoint(),1)){
              pproj23 = p23;
            }

            ILineString line11 = new GM_LineString(Arrays.asList(p11, pproj11));
            ILineString line12 = new GM_LineString(Arrays.asList(p12, pproj12));
            ILineString line13 = new GM_LineString(Arrays.asList(p13, pproj13));
            ILineString line14 = new GM_LineString(Arrays.asList(p12, p22));

            ILineString line21 = new GM_LineString(Arrays.asList(p21, pproj21));
            ILineString line22 = new GM_LineString(Arrays.asList(p22, pproj22));
            ILineString line23 = new GM_LineString(Arrays.asList(p23, pproj23));


            ILineString line31 = new GM_LineString(Arrays.asList(p12, p21));
            ILineString line32 = new GM_LineString(Arrays.asList(p12, p22));
            ILineString line33 = new GM_LineString(Arrays.asList(p12, p23));
            ILineString line34 = new GM_LineString(Arrays.asList(p11, p22));
            ILineString line35 = new GM_LineString(Arrays.asList(p12, p22));
            ILineString line36 = new GM_LineString(Arrays.asList(p13, p22));

            List<ILineString> lines = new ArrayList<>(Arrays.asList(line11, line12,line13,
                line14, line21, line22, line23, line31, line32, line33,
                line34, line35,line36));

            ILineString lmin = Collections.min(lines, new Comparator<ILineString>() {
              @Override
              public int compare(ILineString l1,ILineString l2) {
                if(l1.startPoint().equals(l1.endPoint(), 0.005)){
                  return 1;
                }
                else if(l2.startPoint().equals(l2.endPoint(), 0.005)){
                  return -1;
                }
                else{
                  double length1 = l1.length();  
                  double length2 = l2.length();
                  if(length1 > length2){
                    return 1;
                  }
                  else if(length1 < length2){
                    return -1;
                  }
                  return 0;
                }
              }
            });

            IFeature f = new DefaultFeature(lmin);
            //  IFeature f = new DefaultFeature(new GM_LineString(new DirectPositionList(Arrays.asList(p12,p22))));
            out.add(f);
          }
        }

      }
    }
    return out;
  }

  /**
   * Renvoie tous les sous-ensembles de l'ensemble constitué par les arcs en entrée
   * @param arcs
   * @return
   */
  public List<List<Arc>> enumerateSubsets(List<Arc> arcs){
    List<List<Arc>> result = new ArrayList<>();
    int length = arcs.size();
    int[] bitVector = new int[length + 2];
    for (int i = 0; i <= length + 1; i++) {
      bitVector[i] = 0;
    }
    while(bitVector[length + 1] != 1){
      List<Arc> currentSubSet = new ArrayList<>();
      for (int index = 1; index <= length; index++) {
        if (bitVector[index] == 1) {
          Arc value = arcs.get(index - 1);
          currentSubSet.add(value);
        }
      }
      int i = 1;
      while (bitVector[i] == 1) {
        bitVector[i] = 0;
        i++;
      }
      bitVector[i] = 1;
      result.add(currentSubSet);
    }
    return result;    
  }


}
