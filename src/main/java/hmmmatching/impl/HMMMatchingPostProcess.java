package hmmmatching.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.distance.Frechet;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.morphogenesis.network.utils.ConnectedComponents;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;

public class HMMMatchingPostProcess {


  public Map<Arc, Set<Arc>> lpsolving(Map<Arc, Set<Arc>> matching2) {

    Map<Arc, List<Arc>> matching = new HashMap<>();

    for(Arc a : matching2.keySet()){
      matching.put(a, new ArrayList<>(matching2.get(a)));
    }


    Map<Arc, Set<Arc>> finalMatching = new HashMap<Arc, Set<Arc>>();
    // prise de décision
    Map<Arc, List<Arc>> candidatesMatching = new HashMap<Arc, List<Arc>>();
    Map<Arc, List<Arc>> reverseCandidatesMatching = new HashMap<Arc, List<Arc>>();

    for (Arc aref : matching.keySet()) {
      for (Arc acomp : matching.get(aref)) {
        if (!candidatesMatching.containsKey(aref)) {
          candidatesMatching.put(aref, new ArrayList<Arc>());
        }
        if (!candidatesMatching.get(aref).contains(acomp)) {
          candidatesMatching.get(aref).add(acomp);
        }
        if (!reverseCandidatesMatching.containsKey(acomp)) {
          reverseCandidatesMatching.put(acomp, new ArrayList<Arc>());
        }
        if (!reverseCandidatesMatching.get(acomp).contains(aref)) {
          reverseCandidatesMatching.get(acomp).add(aref);
        }
      }
    }
    // arccomp
    UndirectedSparseMultigraph<Arc, Integer> graph = new UndirectedSparseMultigraph<Arc, Integer>();
    int cptEdges = 0;
    for (Arc arcRef : candidatesMatching.keySet()) {
      if (candidatesMatching.get(arcRef).isEmpty()) {
        continue;
      }
      if (candidatesMatching.get(arcRef).size() == 1) {
        graph.addVertex(candidatesMatching.get(arcRef).iterator().next());
      } else {
        Iterator<Arc> pit = candidatesMatching.get(arcRef).iterator();
        Arc a1 = pit.next();
        while (pit.hasNext()) {
          Arc a2 = pit.next();
          graph.addEdge(cptEdges++, a1, a2);
        }
      }
    }
    // on regroupe par composantes connexes
    ConnectedComponents<Arc, Integer> cc = new ConnectedComponents<Arc, Integer>(
        graph);
    List<UndirectedSparseMultigraph<Arc, Integer>> connectedComponents = cc
        .buildConnectedComponents();

    for (UndirectedSparseMultigraph<Arc, Integer> connectedComponent : connectedComponents) {
      // pour chaque graphe d'appariement
      if (connectedComponent.getVertexCount() == 0) {
        // WTF ?
        continue;
      }
      Set<Arc> candidates = new HashSet<Arc>();
      // les arcscomp concernés
      candidates.addAll(connectedComponent.getVertices());
      // les arcsRef
      Set<Arc> references = new HashSet<Arc>();
      for (Arc arcComp : candidates) {
        references.addAll(reverseCandidatesMatching.get(arcComp));
      }

      if (references.size() == 1 && candidates.size() == 1) {
        Arc aref = references.iterator().next();
        Arc arccomp = candidates.iterator().next();

        if (finalMatching.containsKey(aref)) {
          finalMatching.get(aref).add(arccomp);
        } else {
          Set<Arc> l = new HashSet<Arc>();
          l.add(arccomp);
          finalMatching.put(aref, l);
        }
        continue;
      }

      // on fait les ACluster
      AClusterCollection clusterColRef = new AClusterCollection(
          new ArrayList<Arc>(references));
      AClusterCollection clusterColComp = new AClusterCollection(
          new ArrayList<Arc>(candidates));


      Map<Integer, ACluster> indexesRef = new HashMap<Integer, ACluster>();
      Map<Integer, ACluster> indexesComp = new HashMap<Integer, ACluster>();
      int cptRef = 0;
      int cptComp = 0;

      LocalHypergraph hypergraph = new LocalHypergraph();

      for (ACluster clusterRef : clusterColRef) {
        indexesRef.put(cptRef++, clusterRef);
        hypergraph.getHypervertices().addAll(clusterRef.getArcs());
      }
      for (ACluster clusterComp : clusterColComp) {
        indexesComp.put(cptComp++, clusterComp);
        hypergraph.getHypervertices().addAll(clusterComp.getArcs());
      }
      Map<Integer, Set<Arc>> indexHyperArcs = new HashMap<Integer, Set<Arc>>();
      int cpt = 0;
      if (indexesComp.size() == 0 || indexesRef.size() == 0) {
        continue;
      }

      for (int i = 0; i < indexesRef.keySet().size(); i++) {
        ACluster clusterRef = indexesRef.get(i);

        ILineString l1 =/* Operateurs.resampling(*/new GM_LineString(
            clusterRef.getGeometrie().getControlPoint())/*,50)*/;

        for (int j = 0; j < indexesComp.size(); j++) {
          ACluster clusterComp = indexesComp.get(j);
          // création d'un hyperarc
          Set<Arc> newHypArc = new HashSet<Arc>();
          newHypArc.addAll(clusterRef.getArcs());
          newHypArc.addAll(clusterComp.getArcs());
          // hypergraph.getHyperedges().add(newHypArc);
          boolean ok = true;
          for (Arc aref : clusterRef.getArcs()) {
            if (!candidatesMatching.get(aref)
                .containsAll(clusterComp.getArcs())) {
              ok = false;
              break;
            }
          }

          if (!ok) {
            // hypergraph.getHyperedges().put(newHypArc, Double.MIN_VALUE);
            continue;
          }
          indexHyperArcs.put(cpt++, newHypArc);

          // double distance1 =
          // Math.min(Distances.premiereComposanteHausdorff(clusterRef.getGeometrie(),
          // clusterComp.getGeometrie()),
          // Distances.premiereComposanteHausdorff(clusterComp.getGeometrie(),
          // clusterRef.getGeometrie()));

          // if(distance1> parameters.selection){
          // hypergraph.getCosts().put(newHypArc, Double.MAX_VALUE);
          // continue;
          // }

          //
          // if(Math.max(clusterRef.longueur() , clusterComp.longueur()) >
          // 5 * Math.min(clusterRef.longueur() , clusterComp.longueur())){
          // hypergraph.getCosts().put(newHypArc, Double.MAX_VALUE);
          // continue;
          // }

          ILineString l2 = /*Operateurs.resampling(*/new GM_LineString(
              clusterComp.getGeometrie().getControlPoint())/*,50)*/;



          double distance2 = Math.min(
              Frechet.discreteFrechetWithProjection(l1, l2),
              Frechet.discreteFrechetWithProjection(l1.reverse(), l2));



          //    
          //          Angle ori1 = Operateurs.directionPrincipale(l1.getControlPoint());
          //          Angle ori2 = Operateurs.directionPrincipale(l2.getControlPoint());
          //          Angle ori3 = Operateurs.directionPrincipale(l2.reverse().getControlPoint());
          //          double value = Math.min(Angle.ecart(ori1, ori2).getValeur()*180./Math.PI,
          //              Angle.ecart(ori1, ori3).getValeur()*180./Math.PI); 
          //
          //
          //          double d1 = Distances.premiereComposanteHausdorff(l1,l2);
          //          double d2 = Distances.premiereComposanteHausdorff(l2,l1);
          //          double distance = Math.min(d1,d2);

          // double probaT = Math.exp(-(distance2 + dM + value));
          double probaT =1./(distance2/** value * distance*/) ;

          hypergraph.getHyperedges().put(newHypArc, probaT);
        }

      }

      // résolution du pb d'optimisation linéaire
      LPWizard lpw = new LPWizard();
      for (Integer i : indexHyperArcs.keySet()) {
        String var = "x" + i;
        lpw.plus(var, hypergraph.getHyperedges().get(indexHyperArcs.get(i)));
        lpw.setBoolean(var);
      }

      // maximisation
      lpw.setMinProblem(false);
      // les contraintes ....
      cpt = 0;
      for (Arc vertex : hypergraph.getHypervertices()) {
        LPWizardConstraint lpwc = lpw.addConstraint("c" + cpt, 1, ">=");
        for (Integer i : indexHyperArcs.keySet()) {
          Set<Arc> hyparc = indexHyperArcs.get(i);
          if (hyparc.contains(vertex)) {
            String var = "x" + i;
            lpwc.plus(var, 1.);
          }
        }
        cpt++;
      }


      LPSolution sol = lpw.solve();
      for (int i = 0; i < indexHyperArcs.size(); i++) {
        if (sol.getBoolean("x" + i)) {

          Set<Arc> hyparc = indexHyperArcs.get(i);
          List<Arc> arcref = new ArrayList<Arc>();
          Set<Arc> arccomp = new HashSet<Arc>();
          for (Arc vertex : hyparc) {
            if (references.contains(vertex)) {
              arcref.add(vertex);
            } else {
              arccomp.add(vertex);
            }
          }

          for (Arc aref : arcref) {
            if (finalMatching.containsKey(aref)) {
              finalMatching.get(aref).addAll(arccomp);
            } else {
              finalMatching.put(aref, arccomp);
            }

          }
        }
      }
    }

    return finalMatching;

  }







  static class LocalHypergraph {
    private Set<Arc> hypvertices;
    // private Set<Set<Arc>> hypedges;
    private Map<Set<Arc>, Double> hypedges;

    public LocalHypergraph() {
      // this.hypedges = new HashSet<Set<Arc>>();
      this.hypvertices = new HashSet<Arc>();
      this.hypedges = new HashMap<Set<Arc>, Double>();
    }

    public Map<Set<Arc>, Double> getHyperedges() {
      return hypedges;
    }

    public void setHyperedges(Map<Set<Arc>, Double> costs) {
      this.hypedges = costs;
    }

    public Set<Arc> getHypervertices() {
      return hypvertices;
    }

    public void setHypervertices(Set<Arc> hypvertices) {
      this.hypvertices = hypvertices;
    }
  }
}
