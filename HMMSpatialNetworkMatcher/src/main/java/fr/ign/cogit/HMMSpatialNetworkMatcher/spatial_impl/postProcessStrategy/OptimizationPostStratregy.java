package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.postProcessStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.PostProcessStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.core.HMMMatchingProcess;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.CompositeHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.CompositeObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.FeatHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.FeatObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.utils.Combinations;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.utils.ConnectedComponents;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;

public class OptimizationPostStratregy implements PostProcessStrategy{


  @Override
  public Map<IObservation, Set<IHiddenState>> simplify(HMMMatchingProcess hmmProcess) { 

        Map<FeatObservation, List<FeatHiddenState>> matching = new HashMap<>();

        for(IObservation a : hmmProcess.getMatching().keySet()){
          List<FeatHiddenState> states = new ArrayList<>();
          for(IHiddenState state : hmmProcess.getMatching().get(a)) {
            if(state instanceof CompositeHiddenState) {
              states.addAll(((CompositeHiddenState)state).getStates());
            }
            else {
              states.add((FeatHiddenState)state);
            }
          }
          matching.put((FeatObservation)a, states);
        }


        Map<IObservation, Set<IHiddenState>> finalMatching = new HashMap<>();
        // prise de décision
        Map<FeatObservation, List<FeatHiddenState>> candidatesMatching = new HashMap<>();
        Map<FeatHiddenState, List<FeatObservation>> reverseCandidatesMatching = new HashMap<>();

        for (FeatObservation aref : matching.keySet()) {
          for (FeatHiddenState acomp : matching.get(aref)) {
            if (!candidatesMatching.containsKey(aref)) {
              candidatesMatching.put(aref, new ArrayList<FeatHiddenState>());
            }
            if (!candidatesMatching.get(aref).contains(acomp)) {
              candidatesMatching.get(aref).add(acomp);
            }
            if (!reverseCandidatesMatching.containsKey(acomp)) {
              reverseCandidatesMatching.put(acomp, new ArrayList<FeatObservation>());
            }
            if (!reverseCandidatesMatching.get(acomp).contains(aref)) {
              reverseCandidatesMatching.get(acomp).add(aref);
            }
          }
        }
        // arccomp
        UndirectedSparseMultigraph<FeatHiddenState, Integer> graph = new UndirectedSparseMultigraph<FeatHiddenState, Integer>();
        int cptEdges = 0;
        for (FeatObservation arcRef : candidatesMatching.keySet()) {
          if (candidatesMatching.get(arcRef).isEmpty()) {
            continue;
          }
          if (candidatesMatching.get(arcRef).size() == 1) {
            graph.addVertex(candidatesMatching.get(arcRef).iterator().next());
          } else {
            Iterator<FeatHiddenState> pit = candidatesMatching.get(arcRef).iterator();
            FeatHiddenState a1 = pit.next();
            while (pit.hasNext()) {
              FeatHiddenState a2 = pit.next();
              graph.addEdge(cptEdges++, a1, a2);
            }
          }
        }
        // on regroupe par composantes connexes
        ConnectedComponents<FeatHiddenState, Integer> cc = new ConnectedComponents<FeatHiddenState, Integer>(
            graph);
        List<UndirectedSparseMultigraph<FeatHiddenState, Integer>> connectedComponents = cc
            .buildConnectedComponents();

        for (UndirectedSparseMultigraph<FeatHiddenState, Integer> connectedComponent : connectedComponents) {
          // pour chaque graphe d'appariement
          if (connectedComponent.getVertexCount() == 0) {
            // WTF ?
            continue;
          }
          
          double probaMax = Double.MIN_VALUE;
          double probaMin = Double.MAX_VALUE;
          
          Set<FeatHiddenState> candidates = new HashSet<>();
          // les arcscomp concernés
          candidates.addAll(connectedComponent.getVertices());
          // les arcsRef
          Set<FeatObservation> references = new HashSet<>();
          for (FeatHiddenState arcComp : candidates) {
            references.addAll(reverseCandidatesMatching.get(arcComp));
          }

          if (references.size() == 1 && candidates.size() == 1) {
            FeatObservation aref = references.iterator().next();
            FeatHiddenState arccomp = candidates.iterator().next();

            if (finalMatching.containsKey(aref)) {
              finalMatching.get(aref).add(arccomp);
            }
            else {
              Set<IHiddenState> l = new HashSet<>();
              l.add(arccomp);
              finalMatching.put(aref, l);
            }
            continue;
          }

          // on fait les ACluster
          Combinations<FeatHiddenState> combinationsStates = new Combinations<>();
          List<List<FeatHiddenState>> combinationsS =  combinationsStates.getAllCombinations(candidates);
          List<FeatHiddenState> clusterColComp = new ArrayList<>();
          for(List<FeatHiddenState> hdl : combinationsS) {
            if(hdl.size() == 1) {
              clusterColComp.add(hdl.get(0));
            }
            else {
              clusterColComp.add(new CompositeHiddenState(hdl));
            }
          }

          Combinations<FeatObservation> combinationsObs= new Combinations<>();
          List<List<FeatObservation>> combinationsO =  combinationsObs.getAllCombinations(references);
          List<FeatObservation> clusterColRef = new ArrayList<>();
          for(List<FeatObservation> hdl : combinationsO) {
            if(hdl.size() == 1) {
              clusterColRef.add(hdl.get(0));
            }
            else {
              clusterColRef.add(new CompositeObservation(hdl));
            }
          }



          Map<Integer, FeatObservation> indexesRef = new HashMap<>();
          Map<Integer, FeatHiddenState> indexesComp = new HashMap<>();
          int cptRef = 0;
          int cptComp = 0;

          LocalHypergraph hypergraph = new LocalHypergraph();

          for (FeatObservation clusterRef : clusterColRef) {
            indexesRef.put(cptRef++, clusterRef);
            if(clusterColRef instanceof CompositeObservation) {
              hypergraph.getHypervertices().addAll(((CompositeObservation)clusterRef).getObservations());
            }
            else {
              hypergraph.getHypervertices().add(clusterRef);
            }
          }
          for (FeatHiddenState clusterComp : clusterColComp) {
            indexesComp.put(cptComp++, clusterComp);
            if(clusterComp instanceof CompositeHiddenState) {
              hypergraph.getHypervertices().addAll(((CompositeHiddenState)clusterComp).getStates());
            }
            else {
              hypergraph.getHypervertices().add(clusterComp);
            }
          }
          Map<Integer, Set<IFeature>> indexHyperArcs = new HashMap<>();
          int cpt = 0;
          if (indexesComp.size() == 0 || indexesRef.size() == 0) {
            continue;
          }

          for (int i = 0; i < indexesRef.keySet().size(); i++) {
            List<FeatObservation> clusterRef = new ArrayList<>();
            if(indexesRef.get(i) instanceof CompositeObservation) {
              clusterRef.addAll(((CompositeObservation)indexesRef.get(i)).getObservations());
            }
            else {
              clusterRef.add(indexesRef.get(i));
            }



            for (int j = 0; j < indexesComp.size(); j++) {
              List<FeatHiddenState> clusterComp = new ArrayList<>();
              if(indexesComp.get(j) instanceof CompositeHiddenState) {
                clusterComp.addAll(((CompositeHiddenState)indexesComp.get(j)).getStates());
              }
              else {
                clusterComp.add(indexesComp.get(j));
              }          // création d'un hyperarc
              Set<IFeature> newHypArc = new HashSet<>();
              newHypArc.addAll(clusterRef);
              newHypArc.addAll(clusterComp);

              boolean ok = true;
              for (FeatObservation aref : clusterRef) {
                if (!candidatesMatching.get(aref)
                    .containsAll(clusterComp)) {
                  ok = false;
                  break;
                }
              }
  
              if (!ok) {
                // hypergraph.getHyperedges().put(newHypArc, Double.MIN_VALUE);
                continue;
              }
              indexHyperArcs.put(cpt++, newHypArc);


              double d = indexesRef.get(i).computeEmissionProbability(indexesComp.get(j));
              
              if(d > probaMax) {
                probaMax = d;
              }
              if(d < probaMin) {
                probaMin = d;
              }
              hypergraph.getHyperedges().put(newHypArc, d);
            }

          }
          
         for(Set<IFeature> lf : hypergraph.getHyperedges().keySet()) {
           double d = hypergraph.getHyperedges().get(lf);
           d = (d-probaMin)/(probaMax - probaMin);
           hypergraph.getHyperedges().put(lf,d);
         }

          // résolution du pb d'optimisation linéaire
          LPWizard lpw = new LPWizard();
       //  LpSolve solver = LpSolve.makeLp(0, hypergraph.getHypervertices().size());

          for (Integer i : indexHyperArcs.keySet()) {
            String var = "x" + i;
            lpw.plus(var, hypergraph.getHyperedges().get(indexHyperArcs.get(i)));
            lpw.setBoolean(var);
          }

          // maximisation
          lpw.setMinProblem(false);
          // les contraintes ....
          cpt = 0;
          for (IFeature vertex : hypergraph.getHypervertices()) {
            LPWizardConstraint lpwc = lpw.addConstraint("c" + cpt, 1, ">=");
            for (Integer i : indexHyperArcs.keySet()) {
              Set<IFeature> hyparc = indexHyperArcs.get(i);
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

              Set<IFeature> hyparc = indexHyperArcs.get(i);
              List<IObservation> arcref = new ArrayList<>();
              Set<IHiddenState> arccomp = new HashSet<>();
              for (IFeature vertex : hyparc) {
                if (vertex instanceof FeatObservation) {
                  arcref.add((FeatObservation)vertex);
                } else {
                  arccomp.add((FeatHiddenState)vertex);
                }
              }

              for (IObservation aref : arcref) {
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
        private Set<IFeature> hypvertices;
        // private Set<Set<Arc>> hypedges;
        private Map<Set<IFeature>, Double> hypedges;

        public LocalHypergraph() {
          // this.hypedges = new HashSet<Set<Arc>>();
          this.hypvertices = new HashSet<IFeature>();
          this.hypedges = new HashMap<Set<IFeature>, Double>();
        }

        public Map<Set<IFeature>, Double> getHyperedges() {
          return hypedges;
        }

        public void setHyperedges(Map<Set<IFeature>, Double> costs) {
          this.hypedges = costs;
        }

        public Set<IFeature> getHypervertices() {
          return hypvertices;
        }

        public void setHypervertices(Set<IFeature> hypvertices) {
          this.hypvertices = hypvertices;
        }
      }


  }
