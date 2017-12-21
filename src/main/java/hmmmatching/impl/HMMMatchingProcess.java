package hmmmatching.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ForkJoinPool;

import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;

/**
 * Classe correspondant à une itération du processus d'appariement. Une itération correspond à l'appariement d'un ACluster, i.e d'un ensemble d'arcs connexes, et donc d'une
 * séquence d'observations. Attention : le code est parralélisé (RecursiveTask).
 * 
 * @author bcostes
 */
public class HMMMatchingProcess {

  public Map<Arc, Set<Arc>> matchNetworks(CarteTopo netRef, CarteTopo netComp, HMMParameters parameters, Random rng) {
    long t0Ini = System.currentTimeMillis();
    /*
     * Construction des clusters d'observations
     */
    List<List<Arc>> strokes = new ArrayList<List<Arc>>();
    Stack<Arc> unprocessed = new Stack<>();
    unprocessed.addAll(netComp.getPopArcs());

    List<List<Arc>> strokesTemp = StrokeTopo.buildStroke(netComp.getPopArcs(), Math.PI / 6., 1, rng);
    Collections.sort(strokesTemp, new Comparator<List<Arc>>() {
      @Override
      public int compare(List<Arc> o1, List<Arc> o2) {
        if (o1.size() > o2.size()) {
          return -1;
        } else if (o1.size() < o2.size()) {
          return 1;
        } else {
          return 0;
        }
      }
    });

    Iterator<List<Arc>> itstroke = strokesTemp.iterator();
    List<Arc> stroke = itstroke.next();
    while (itstroke.hasNext() && stroke.size() >= parameters.stroke_length) {
      // on ne conserve que ceux "assez" longs ...
      unprocessed.removeAll(stroke);
      strokes.add(stroke);
      stroke = itstroke.next();
    }

    // Code nécessaire pour la suite de la construction des strokes ... pas pu
    // faire mieux --->
    Stack<Arc> unprocessedTmp = new Stack<>();
    CarteTopo t = new CarteTopo("");
    Map<Arc, Arc> mappingArcs = new HashMap<>();
    IPopulation<Arc> arcsT = t.getPopArcs();
    for (Arc a : netComp.getPopArcs()) {
      Arc newa = arcsT.nouvelElement();
      newa.setGeometrie(new GM_LineString(a.getGeometrie().getControlPoint()));
      mappingArcs.put(newa, a);
      newa.setPoids(newa.longueur());
      if (unprocessed.contains(a)) {
        unprocessedTmp.add(newa);
      }
    }
    t.creeTopologieArcsNoeuds(0);
    t.rendPlanaire(0);
    unprocessed.clear();
    unprocessed.addAll(unprocessedTmp);
    unprocessedTmp.clear();
    // <---

    // Si des arcs n'ont pas été inclus dans des séquences d'observations, on
    // les traite
    strokesTemp = new ArrayList<>();
    while (!unprocessed.isEmpty()) {
      Arc current = unprocessed.pop();
      Noeud ini = current.getNoeudIni();
      Noeud random = null;
      stroke = new ArrayList<Arc>();
      while (stroke.size() < parameters.stroke_length || !stroke.contains(current)) {
        random = t.getPopNoeuds().get(rng.nextInt(t.getPopNoeuds().size()));
        if (random.equals(ini)) {
          continue;
        }
        // On construit des chemins aléatoires (plus ou moins)
        stroke = Utils.randomPath(ini, random);
        if (!stroke.contains(current)) {
          stroke.add(0, current);
        }
      }
      strokesTemp.add(stroke);
      unprocessed.removeAll(stroke);
    }
    for (List<Arc> road : strokesTemp) {
      List<Arc> newR = new ArrayList<Arc>();
      for (Arc a : road) {
        newR.add(mappingArcs.get(a));
      }
      strokes.add(newR);
    }

    Collections.sort(strokes, new Comparator<List<Arc>>() {
      @Override
      public int compare(List<Arc> o1, List<Arc> o2) {
        if (o1.size() > o2.size()) {
          return -1;
        } else if (o1.size() < o2.size()) {
          return 1;
        } else {
          return 0;
        }
      }
    });

    /*
     * Process principale d'appariement. On traite les séquences d'observations les uns après les autres
     */

    HMMMatchingParallelProcess hmmIteration = new HMMMatchingParallelProcess(strokes, netRef, parameters);
    // le processus va être parralélisé
    int processeurs = Runtime.getRuntime().availableProcessors();
    ForkJoinPool pool = new ForkJoinPool(processeurs);
    // Résultats d'appariement
    Map<Arc, List<ACluster>> matching = pool.invoke(hmmIteration);

    if (!parameters.lpsolving) {
      // méthode de résolution en utilisant un matvhing inverse

      /*
       * reverse matching TODO : factoriser le code ... c'ets le même qu'au dessus en échangeant netRef et netComp !
       */

      strokes.clear();
      unprocessed.clear();
      unprocessed.addAll(netRef.getPopArcs());

      /*
       * Construction des strokes => séquences d'observations
       */

      strokesTemp.clear();
      strokesTemp = StrokeTopo.buildStroke(netRef.getPopArcs(), Math.PI / 6., 1, rng);

      Collections.sort(strokesTemp, new Comparator<List<Arc>>() {
        @Override
        public int compare(List<Arc> o1, List<Arc> o2) {
          if (o1.size() > o2.size()) {
            return -1;
          } else if (o1.size() < o2.size()) {
            return 1;
          } else {
            return 0;
          }
        }
      });

      itstroke = strokesTemp.iterator();
      stroke = itstroke.next();
      while (itstroke.hasNext() && stroke.size() >= parameters.stroke_length) {
        unprocessed.removeAll(stroke);
        strokes.add(stroke);
        stroke = itstroke.next();
      }

      // Code nécessaire pour la suite de la construction des strokes ... pas pu
      // faire mieux --->
      unprocessedTmp.clear();
      t = new CarteTopo("");
      mappingArcs.clear();
      arcsT.clear();
      arcsT = t.getPopArcs();
      for (Arc a : netRef.getPopArcs()) {
        Arc newa = arcsT.nouvelElement();
        newa.setGeometrie(new GM_LineString(a.getGeometrie().getControlPoint()));
        mappingArcs.put(newa, a);
        newa.setPoids(newa.longueur());
        if (unprocessed.contains(a)) {
          unprocessedTmp.add(newa);
        }
      }
      t.creeTopologieArcsNoeuds(0);
      t.rendPlanaire(0);
      unprocessed.clear();
      unprocessed.addAll(unprocessedTmp);
      unprocessedTmp.clear();
      // <---

      // Si des arcs n'ont pas été inclus dans des séquences d'observations, on
      // les traite
      strokesTemp = new ArrayList<>();
      while (!unprocessed.isEmpty()) {
        Arc current = unprocessed.pop();
        Noeud ini = current.getNoeudIni();
        Noeud random = null;
        stroke = new ArrayList<Arc>();
        while (stroke.size() < parameters.stroke_length || !stroke.contains(current)) {
          random = t.getPopNoeuds().get(rng.nextInt(t.getPopNoeuds().size()));
          if (random.equals(ini)) {
            continue;
          }
          // On construit des chemins aléatoires (plus ou moins)
          stroke = Utils.randomPath(ini, random);
          if (!stroke.contains(current)) {
            stroke.add(0, current);
          }
        }
        strokesTemp.add(stroke);
        unprocessed.removeAll(stroke);
      }
      for (List<Arc> road : strokesTemp) {
        List<Arc> newR = new ArrayList<Arc>();
        for (Arc a : road) {
          newR.add(mappingArcs.get(a));
        }
        strokes.add(newR);
      }

      Collections.sort(strokes, new Comparator<List<Arc>>() {
        @Override
        public int compare(List<Arc> o1, List<Arc> o2) {
          if (o1.size() > o2.size()) {
            return -1;
          } else if (o1.size() < o2.size()) {
            return 1;
          } else {
            return 0;
          }
        }
      });

      /*
       * Process principale d'appariement. On traite les séquences d'observations les uns après les autres
       */

      hmmIteration = new HMMMatchingParallelProcess(strokes, netComp, parameters);
      pool = new ForkJoinPool(processeurs);
      Map<Arc, List<ACluster>> matchingR = pool.invoke(hmmIteration);

      /*
       * compiling direct and reverse matching
       */

      Map<Arc, Set<Arc>> matchingF = new HashMap<>();
      //
      for (Arc a : netComp.getPopArcs()) {
        if (!matching.containsKey(a)) {
          continue;
        }
        for (ACluster ac1 : matching.get(a)) {
          for (Arc a2 : ac1.getArcs()) {
            if (!matchingR.containsKey(a2)) {
              continue;
            }
            for (ACluster ac2 : matchingR.get(a2)) {
              if (ac2.getArcs().contains(a)) {
                if (matchingF.containsKey(a)) {
                  matchingF.get(a).add(a2);
                } else {
                  Set<Arc> set = new HashSet<Arc>();
                  set.add(a2);
                  matchingF.put(a, set);
                }
              }
            }
          }
        }
      }
      long t1 = System.currentTimeMillis() - t0Ini;
      int seconde = (int) t1 / 1000;
      int minutes = seconde / 60;
      seconde -= minutes * 60 + 1;
      System.out.println("Traitement effectué en " + minutes + "m" + seconde + "s");

      return matchingF;
    } else {
      // méthode de résolution en utilisant l'optimisation linéaire sous contraintes
      Map<Arc, Set<Arc>> matchingF = new HashMap<>();
      for (Arc a : matching.keySet()) {
        matchingF.put(a, new HashSet<Arc>());
        for (ACluster cl : matching.get(a)) {
          matchingF.get(a).addAll(cl.getArcs());
        }
      }
      HMMMatchingPostProcess postProcess = new HMMMatchingPostProcess();
      matchingF = postProcess.lpsolving(matchingF);
      long t1 = System.currentTimeMillis() - t0Ini;
      int seconde = (int) t1 / 1000;
      int minutes = seconde / 60;
      seconde -= minutes * 60 + 1;
      System.out.println("Traitement effectué en " + minutes + "m" + seconde + "s");

      return matchingF;
    }

  }

}
