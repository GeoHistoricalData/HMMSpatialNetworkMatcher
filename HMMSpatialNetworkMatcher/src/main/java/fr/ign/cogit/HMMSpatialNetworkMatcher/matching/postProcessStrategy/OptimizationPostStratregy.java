package fr.ign.cogit.HMMSpatialNetworkMatcher.matching.postProcessStrategy;

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
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.CompositeHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.CompositeObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.HiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.impl.Observation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.core.HMMMatchingProcess;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.utils.Combinations;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.utils.ConnectedComponents;
import fr.ign.cogit.geoxygene.feature.FT_Feature;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;

public class OptimizationPostStratregy implements PostProcessStrategy{


  @Override
  public Map<IObservation, Set<IHiddenState>> simplify(HMMMatchingProcess hmmProcess) { 

        Map<Observation, List<HiddenState>> matching = new HashMap<>();

        for(IObservation a : hmmProcess.getMatching().keySet()){
          List<HiddenState> states = new ArrayList<>();
          for(IHiddenState state : hmmProcess.getMatching().get(a)) {
            if(state instanceof CompositeHiddenState) {
              states.addAll(((CompositeHiddenState)state).getStates());
            }
            else {
              states.add((HiddenState)state);
            }
          }
          matching.put((Observation)a, states);
        }


        Map<IObservation, Set<IHiddenState>> finalMatching = new HashMap<>();
        // prise de décision
        Map<Observation, List<HiddenState>> candidatesMatching = new HashMap<>();
        Map<HiddenState, List<Observation>> reverseCandidatesMatching = new HashMap<>();

        for (Observation aref : matching.keySet()) {
          for (HiddenState acomp : matching.get(aref)) {
            if (!candidatesMatching.containsKey(aref)) {
              candidatesMatching.put(aref, new ArrayList<HiddenState>());
            }
            if (!candidatesMatching.get(aref).contains(acomp)) {
              candidatesMatching.get(aref).add(acomp);
            }
            if (!reverseCandidatesMatching.containsKey(acomp)) {
              reverseCandidatesMatching.put(acomp, new ArrayList<Observation>());
            }
            if (!reverseCandidatesMatching.get(acomp).contains(aref)) {
              reverseCandidatesMatching.get(acomp).add(aref);
            }
          }
        }
        // arccomp
        UndirectedSparseMultigraph<HiddenState, Integer> graph = new UndirectedSparseMultigraph<HiddenState, Integer>();
        int cptEdges = 0;
        for (Observation arcRef : candidatesMatching.keySet()) {
          if (candidatesMatching.get(arcRef).isEmpty()) {
            continue;
          }
          if (candidatesMatching.get(arcRef).size() == 1) {
            graph.addVertex(candidatesMatching.get(arcRef).iterator().next());
          } else {
            Iterator<HiddenState> pit = candidatesMatching.get(arcRef).iterator();
            HiddenState a1 = pit.next();
            while (pit.hasNext()) {
              HiddenState a2 = pit.next();
              graph.addEdge(cptEdges++, a1, a2);
            }
          }
        }
        // on regroupe par composantes connexes
        ConnectedComponents<HiddenState, Integer> cc = new ConnectedComponents<HiddenState, Integer>(
            graph);
        List<UndirectedSparseMultigraph<HiddenState, Integer>> connectedComponents = cc
            .buildConnectedComponents();

        for (UndirectedSparseMultigraph<HiddenState, Integer> connectedComponent : connectedComponents) {
          // pour chaque graphe d'appariement
          if (connectedComponent.getVertexCount() == 0) {
            // WTF ?
            continue;
          }
          
          double probaMax = Double.MIN_VALUE;
          double probaMin = Double.MAX_VALUE;
          
          Set<HiddenState> candidates = new HashSet<>();
          // les arcscomp concernés
          candidates.addAll(connectedComponent.getVertices());
          // les arcsRef
          Set<Observation> references = new HashSet<>();
          for (HiddenState arcComp : candidates) {
            references.addAll(reverseCandidatesMatching.get(arcComp));
          }

          if (references.size() == 1 && candidates.size() == 1) {
            Observation aref = references.iterator().next();
            HiddenState arccomp = candidates.iterator().next();

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
          Combinations<HiddenState> combinationsStates = new Combinations<>();
          List<List<HiddenState>> combinationsS =  combinationsStates.getAllCombinations(candidates);
          List<HiddenState> clusterColComp = new ArrayList<>();
          for(List<HiddenState> hdl : combinationsS) {
            if(hdl.size() == 1) {
              clusterColComp.add(hdl.get(0));
            }
            else {
              clusterColComp.add(new CompositeHiddenState(hdl));
            }
          }

          Combinations<Observation> combinationsObs= new Combinations<>();
          List<List<Observation>> combinationsO =  combinationsObs.getAllCombinations(references);
          List<Observation> clusterColRef = new ArrayList<>();
          for(List<Observation> hdl : combinationsO) {
            if(hdl.size() == 1) {
              clusterColRef.add(hdl.get(0));
            }
            else {
              clusterColRef.add(new CompositeObservation(hdl));
            }
          }



          Map<Integer, Observation> indexesRef = new HashMap<>();
          Map<Integer, HiddenState> indexesComp = new HashMap<>();
          int cptRef = 0;
          int cptComp = 0;

          LocalHypergraph hypergraph = new LocalHypergraph();

          for (Observation clusterRef : clusterColRef) {
            indexesRef.put(cptRef++, clusterRef);
            if(clusterColRef instanceof CompositeObservation) {
              hypergraph.getHypervertices().addAll(((CompositeObservation)clusterRef).getObservations());
            }
            else {
              hypergraph.getHypervertices().add(clusterRef);
            }
          }
          for (HiddenState clusterComp : clusterColComp) {
            indexesComp.put(cptComp++, clusterComp);
            if(clusterComp instanceof CompositeHiddenState) {
              hypergraph.getHypervertices().addAll(((CompositeHiddenState)clusterComp).getStates());
            }
            else {
              hypergraph.getHypervertices().add(clusterComp);
            }
          }
          Map<Integer, Set<FT_Feature>> indexHyperArcs = new HashMap<>();
          int cpt = 0;
          if (indexesComp.size() == 0 || indexesRef.size() == 0) {
            continue;
          }

          for (int i = 0; i < indexesRef.keySet().size(); i++) {
            List<Observation> clusterRef = new ArrayList<>();
            if(indexesRef.get(i) instanceof CompositeObservation) {
              clusterRef.addAll(((CompositeObservation)indexesRef.get(i)).getObservations());
            }
            else {
              clusterRef.add(indexesRef.get(i));
            }



            for (int j = 0; j < indexesComp.size(); j++) {
              List<HiddenState> clusterComp = new ArrayList<>();
              if(indexesComp.get(j) instanceof CompositeHiddenState) {
                clusterComp.addAll(((CompositeHiddenState)indexesComp.get(j)).getStates());
              }
              else {
                clusterComp.add(indexesComp.get(j));
              }          // création d'un hyperarc
              Set<FT_Feature> newHypArc = new HashSet<>();
              newHypArc.addAll(clusterRef);
              newHypArc.addAll(clusterComp);

              boolean ok = true;
              for (Observation aref : clusterRef) {
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
          
         for(Set<FT_Feature> lf : hypergraph.getHyperedges().keySet()) {
           double d = hypergraph.getHyperedges().get(lf);
           d = (d-probaMin)/(probaMax - probaMin);
           System.out.println(d+" "+ probaMax+ " " + probaMin);
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
          for (FT_Feature vertex : hypergraph.getHypervertices()) {
            LPWizardConstraint lpwc = lpw.addConstraint("c" + cpt, 1, ">=");
            for (Integer i : indexHyperArcs.keySet()) {
              Set<FT_Feature> hyparc = indexHyperArcs.get(i);
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

              Set<FT_Feature> hyparc = indexHyperArcs.get(i);
              List<IObservation> arcref = new ArrayList<>();
              Set<IHiddenState> arccomp = new HashSet<>();
              for (FT_Feature vertex : hyparc) {
                if (vertex instanceof Observation) {
                  arcref.add((Observation)vertex);
                } else {
                  arccomp.add((HiddenState)vertex);
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
        private Set<FT_Feature> hypvertices;
        // private Set<Set<Arc>> hypedges;
        private Map<Set<FT_Feature>, Double> hypedges;

        public LocalHypergraph() {
          // this.hypedges = new HashSet<Set<Arc>>();
          this.hypvertices = new HashSet<FT_Feature>();
          this.hypedges = new HashMap<Set<FT_Feature>, Double>();
        }

        public Map<Set<FT_Feature>, Double> getHyperedges() {
          return hypedges;
        }

        public void setHyperedges(Map<Set<FT_Feature>, Double> costs) {
          this.hypedges = costs;
        }

        public Set<FT_Feature> getHypervertices() {
          return hypvertices;
        }

        public void setHypervertices(Set<FT_Feature> hypvertices) {
          this.hypvertices = hypvertices;
        }
      }


  }
