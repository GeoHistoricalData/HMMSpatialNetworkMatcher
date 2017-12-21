package hmmmatching.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.contrib.geometrie.Distances;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.distance.Frechet;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;
import fr.ign.cogit.morphogenesis.network.utils.ConnectedComponents;
import hmmmatching.rewriting2.impl.ACluster;
import hmmmatching.rewriting2.impl.AClusterCollection;
import hmmmatching.rewriting2.impl.HMMParameters;
import hmmmatching.rewriting2.impl.Utils;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;

public class OptMatching {

  public static Collection<Arc> incrementalSearch(Arc arc, IPopulation<Arc> pop, double selection, double angleT) {
    double startT = 25;
    double inc = 5.;
    Angle ori1 = Operateurs.directionPrincipale(Operateurs.resampling(arc.getGeometrie(), 5).getControlPoint());
    boolean goOn = true;
    Collection<Arc> candidates = null;
    while (goOn && startT <= selection) {
      candidates = pop.select(arc.getGeometrie(), startT);
      if (!candidates.isEmpty()) {
        for (Arc candidate : candidates) {
          Angle ori2 = Operateurs.directionPrincipale(Operateurs.resampling(candidate.getGeometrie(), 5).getControlPoint());
          Angle ori3 = Operateurs.directionPrincipale(Operateurs.resampling(candidate.getGeometrie().reverse(), 5).getControlPoint());
          double value = Math.min(Angle.ecart(ori1, ori2).getValeur() * 180. / Math.PI, Angle.ecart(ori1, ori3).getValeur() * 180. / Math.PI);
          if (value < angleT) {
            goOn = false;
            break;
          }
        }
      }
      startT += inc;
    }
    return candidates;
  }

  public static void main(String[] args) {

    double angleThreshold_TP = 45.;
    double angleThreshold_EP = angleThreshold_TP;
    double distanceThresold_EP = 15;
    double selection = 200;
    int stroke_length = 5;
    boolean resampling = false;
    boolean lpsolving = false;

    HMMParameters parameters = new HMMParameters(angleThreshold_TP, angleThreshold_EP, distanceThresold_EP, selection, stroke_length, resampling, lpsolving);

    /*
     * Lectures des shapsfiles
     */
    IPopulation<IFeature> inRef = ShapefileReader.read("/home/bcostes/Bureau/matching_hmm/bdcarto_railways.shp");
    IPopulation<IFeature> inComp = ShapefileReader.read("/home/bcostes/Bureau/matching_hmm/osm_railways.shp");

    /*
     * Création des réseaux
     */
    CarteTopo netRefTmp = new CarteTopo("ref");
    CarteTopo netCompTmp = new CarteTopo("comp");
    IPopulation<Arc> popArcRef = netRefTmp.getPopArcs();
    for (IFeature f : inRef) {
      Arc a = popArcRef.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
    }
    IPopulation<Arc> popArcComp = netCompTmp.getPopArcs();
    for (IFeature f : inComp) {
      Arc a = popArcComp.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
    }
    netRefTmp.creeTopologieArcsNoeuds(1);
    netRefTmp.creeNoeudsManquants(1);
    netRefTmp.rendPlanaire(1);
    netRefTmp.filtreDoublons(1);
    netRefTmp.filtreArcsNull(1);
    netRefTmp.filtreArcsDoublons();
    netRefTmp.filtreNoeudsSimples();

    netCompTmp.creeTopologieArcsNoeuds(1);
    netCompTmp.creeNoeudsManquants(1);
    netCompTmp.rendPlanaire(1);
    netCompTmp.filtreDoublons(1);
    netCompTmp.filtreArcsNull(1);
    netCompTmp.filtreArcsDoublons();
    netCompTmp.filtreNoeudsSimples();
    CarteTopo netRef = new CarteTopo("ref");
    CarteTopo netComp = new CarteTopo("comp");
    popArcRef = netRef.getPopArcs();
    for (Arc f : netRefTmp.getPopArcs()) {
      Arc a = popArcRef.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      a.addCorrespondant(f);
    }
    popArcComp = netComp.getPopArcs();
    for (Arc f : netCompTmp.getPopArcs()) {
      Arc a = popArcComp.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      a.addCorrespondant(f);
    }
    netRef.creeNoeudsManquants(0);
    netRef.creeTopologieArcsNoeuds(1);
    netComp.creeNoeudsManquants(0);
    netComp.creeTopologieArcsNoeuds(1);
    netRefTmp = null;
    netCompTmp = null;

    if (parameters.resampling) {
      if (netRef.getPopArcs().size() > netComp.getPopArcs().size()) {
        netComp.projete(netRef, parameters.selection, parameters.selection, false);
      } else {
        netRef.projete(netComp, parameters.selection, parameters.selection, false);
      }
    }
    netRef.getPopArcs().initSpatialIndex(Tiling.class, false);
    for (Arc a : netRef.getPopArcs()) {
      a.setPoids(a.longueur());
      a.setOrientation(2);
    }
    netComp.getPopArcs().initSpatialIndex(Tiling.class, false);
    for (Arc a : netComp.getPopArcs()) {
      a.setPoids(a.longueur());
      a.setOrientation(2);
    }

    Map<Arc, Set<Arc>> finalMatching = new HashMap<Arc, Set<Arc>>();
    // prise de décision
    Map<Arc, Set<Arc>> candidatesMatching = new HashMap<Arc, Set<Arc>>();
    Map<Arc, Set<Arc>> reverseCandidatesMatching = new HashMap<Arc, Set<Arc>>();

    IPopulation<IFeature> outT = new Population<>();

    for (Arc arcRef : netRef.getPopArcs()) {
      candidatesMatching.put(arcRef, new HashSet<Arc>());

      Collection<Arc> candidates = OptMatching.incrementalSearch(arcRef, netComp.getPopArcs(), parameters.selection, parameters.angleThreshold_EP);

      if (!candidates.isEmpty()) {
        for (Arc c : candidates) {
          outT.add(new DefaultFeature(new GM_LineString(Arrays.asList(Operateurs.milieu(arcRef.getGeometrie()), Operateurs.milieu(c.getGeometrie())))));

        }
      }

      for (Arc arcComp : candidates) {
        double distance = Math.min(Distances.premiereComposanteHausdorff(arcRef.getGeometrie(), arcComp.getGeometrie()),
            Distances.premiereComposanteHausdorff(arcComp.getGeometrie(), arcRef.getGeometrie()));

        if (distance > parameters.selection) {
          continue;
        }

        // if (!candidatesMatching.get(arcRef).contains(arcComp)) {
        // candidatesMatching.get(arcRef).add(arcComp);
        // }
        // if (!reverseCandidatesMatching.containsKey(arcComp)) {
        // reverseCandidatesMatching.put(arcComp, new HashSet<Arc>());
        // }
        // if (!reverseCandidatesMatching.get(arcComp).contains(arcRef)) {
        // reverseCandidatesMatching.get(arcComp).add(arcRef);
        // }
        if (arcRef.getGeometrie().length() > 2 * arcComp.getGeometrie().length() || arcComp.getGeometrie().length() > 2 * arcRef.getGeometrie().length()) {
          if (arcRef.getGeometrie().length() < arcComp.getGeometrie().length()) {
            IDirectPosition pref1 = arcRef.getGeometrie().startPoint();
            IDirectPosition pref2 = arcRef.getGeometrie().endPoint();

            List<IDirectPosition> pts = new ArrayList<IDirectPosition>(arcComp.getGeometrie().getControlPoint());
            int pos1 = Operateurs.projectAndInsertWithPosition(pref1, pts);
            int pos2 = Operateurs.projectAndInsertWithPosition(pref2, pts);

            if (pos1 == pos2) {
              Angle ori1 = Operateurs.directionPrincipale(Operateurs.resampling(arcRef.getGeometrie(), 5).getControlPoint());
              Angle ori2 = Operateurs.directionPrincipale(Operateurs.resampling(arcComp.getGeometrie(), 5).getControlPoint());
              Angle ori3 = Operateurs.directionPrincipale(Operateurs.resampling(arcComp.getGeometrie().reverse(), 5).getControlPoint());
              double value = Math.min(Angle.ecart(ori1, ori2).getValeur() * 180. / Math.PI, Angle.ecart(ori1, ori3).getValeur() * 180. / Math.PI);
              if (value < parameters.angleThreshold_EP) {
                candidatesMatching.get(arcRef).add(arcComp);
                if (!reverseCandidatesMatching.containsKey(arcComp)) {
                  reverseCandidatesMatching.put(arcComp, new HashSet<Arc>());
                }
                reverseCandidatesMatching.get(arcComp).add(arcRef);
              }
              continue;
            }

            List<IDirectPosition> ptsExtract = null;
            if (pos1 > pos2) {
              ptsExtract = pts.subList(pos2, pos1 + 1);
            } else {
              ptsExtract = pts.subList(pos1, pos2 + 1);
            }
            ILineString newline = new GM_LineString(new DirectPositionList(ptsExtract));
            if (newline.length() < 0.25 * arcRef.getGeometrie().length()) {
              // perpendicular lines probably
              continue;
            }

            Angle ori1 = Operateurs.directionPrincipale(Operateurs.resampling(arcRef.getGeometrie(), 5).getControlPoint());
            Angle ori2 = Operateurs.directionPrincipale(Operateurs.resampling(newline, 5).getControlPoint());
            Angle ori3 = Operateurs.directionPrincipale(Operateurs.resampling(newline.reverse(), 5).getControlPoint());
            double value = Math.min(Angle.ecart(ori1, ori2).getValeur() * 180. / Math.PI, Angle.ecart(ori1, ori3).getValeur() * 180. / Math.PI);
            if (value > parameters.angleThreshold_EP) {
              continue;
            }
            candidatesMatching.get(arcRef).add(arcComp);
            if (!reverseCandidatesMatching.containsKey(arcComp)) {
              reverseCandidatesMatching.put(arcComp, new HashSet<Arc>());
            }
            reverseCandidatesMatching.get(arcComp).add(arcRef);
            continue;
          } else {
            IDirectPosition pref1 = arcComp.getGeometrie().startPoint();
            IDirectPosition pref2 = arcComp.getGeometrie().endPoint();

            List<IDirectPosition> pts = new ArrayList<IDirectPosition>(arcRef.getGeometrie().getControlPoint());
            int pos1 = Operateurs.projectAndInsertWithPosition(pref1, pts);
            int pos2 = Operateurs.projectAndInsertWithPosition(pref2, pts);

            if (pos1 == pos2) {
              Angle ori1 = Operateurs.directionPrincipale(Operateurs.resampling(arcRef.getGeometrie(), 5).getControlPoint());
              Angle ori2 = Operateurs.directionPrincipale(Operateurs.resampling(arcComp.getGeometrie(), 5).getControlPoint());
              Angle ori3 = Operateurs.directionPrincipale(Operateurs.resampling(arcComp.getGeometrie().reverse(), 5).getControlPoint());
              double value = Math.min(Angle.ecart(ori1, ori2).getValeur() * 180. / Math.PI, Angle.ecart(ori1, ori3).getValeur() * 180. / Math.PI);
              // double angleSkeletonV = (ori1.getValeur() > Math.PI/2) ? (Math.PI - ori1.getValeur()) :(ori1.getValeur());
              // double angleEntityV = (ori2.getValeur() > Math.PI/2) ? (Math.PI - ori2.getValeur()) :(ori2.getValeur());
              // double value = Math.abs(angleSkeletonV - angleEntityV)*180./Math.PI;
              if (value < parameters.angleThreshold_EP) {
                candidatesMatching.get(arcRef).add(arcComp);
                if (!reverseCandidatesMatching.containsKey(arcComp)) {
                  reverseCandidatesMatching.put(arcComp, new HashSet<Arc>());
                }
                reverseCandidatesMatching.get(arcComp).add(arcRef);
              }
              continue;
            }

            List<IDirectPosition> ptsExtract = null;
            if (pos1 > pos2) {
              ptsExtract = pts.subList(pos2, pos1 + 1);
            } else {
              ptsExtract = pts.subList(pos1, pos2 + 1);
            }
            ILineString newline = new GM_LineString(new DirectPositionList(ptsExtract));
            if (newline.length() < 0.25 * arcComp.getGeometrie().length()) {
              // perpendicular lines probably
              continue;
            }

            Angle ori1 = Operateurs.directionPrincipale(Operateurs.resampling(arcComp.getGeometrie(), 5).getControlPoint());
            Angle ori2 = Operateurs.directionPrincipale(Operateurs.resampling(newline, 5).getControlPoint());
            Angle ori3 = Operateurs.directionPrincipale(Operateurs.resampling(newline.reverse(), 5).getControlPoint());
            double value = Math.min(Angle.ecart(ori1, ori2).getValeur() * 180. / Math.PI, Angle.ecart(ori1, ori3).getValeur() * 180. / Math.PI);
            if (value > parameters.angleThreshold_EP) {
              continue;
            }
            candidatesMatching.get(arcRef).add(arcComp);
            if (!reverseCandidatesMatching.containsKey(arcComp)) {
              reverseCandidatesMatching.put(arcComp, new HashSet<Arc>());
            }
            reverseCandidatesMatching.get(arcComp).add(arcRef);
            continue;
          }
        } else {

          Angle ori1 = Operateurs.directionPrincipale(Operateurs.resampling(arcRef.getGeometrie(), 5).getControlPoint());
          Angle ori2 = Operateurs.directionPrincipale(Operateurs.resampling(arcComp.getGeometrie(), 5).getControlPoint());
          Angle ori3 = Operateurs.directionPrincipale(Operateurs.resampling(arcComp.getGeometrie().reverse(), 5).getControlPoint());
          double value = Math.min(Angle.ecart(ori1, ori2).getValeur() * 180. / Math.PI, Angle.ecart(ori1, ori3).getValeur() * 180. / Math.PI);
          if (value > parameters.angleThreshold_EP) {
            continue;
          }

          candidatesMatching.get(arcRef).add(arcComp);
          if (!reverseCandidatesMatching.containsKey(arcComp)) {
            reverseCandidatesMatching.put(arcComp, new HashSet<Arc>());
          }
          reverseCandidatesMatching.get(arcComp).add(arcRef);
        }
      }
    }

    ShapefileWriter.write(outT, "/home/bcostes/Bureau/candidates.shp");

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
    System.out.println("Building connected components");
    ConnectedComponents<Arc, Integer> cc = new ConnectedComponents<Arc, Integer>(graph);
    List<UndirectedSparseMultigraph<Arc, Integer>> connectedComponents = cc.buildConnectedComponents();
    System.out.println("Connected components built");
    int cptC = 0;

    for (UndirectedSparseMultigraph<Arc, Integer> connectedComponent : connectedComponents) {
      cptC++;
      System.out.println(cptC + "/" + connectedComponents.size() + " cc processed ...");
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
      AClusterCollection clusterColRef = new AClusterCollection(new ArrayList<Arc>(references));
      AClusterCollection clusterColComp = new AClusterCollection(new ArrayList<Arc>(candidates));

      System.out.println(clusterColRef.size() + " " + clusterColComp.size());

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

        ILineString l1 = new GM_LineString(clusterRef.getGeometrie().getControlPoint());

        for (int j = 0; j < indexesComp.size(); j++) {
          ACluster clusterComp = indexesComp.get(j);
          // création d'un hyperarc
          Set<Arc> newHypArc = new HashSet<Arc>();
          newHypArc.addAll(clusterRef.getArcs());
          newHypArc.addAll(clusterComp.getArcs());
          // hypergraph.getHyperedges().add(newHypArc);
          boolean ok = true;
          for (Arc aref : clusterRef.getArcs()) {
            if (!candidatesMatching.get(aref).containsAll(clusterComp.getArcs())) {
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

          ILineString l2 = new GM_LineString(clusterComp.getGeometrie().getControlPoint());

          // Angle ori1 = Operateurs.directionPrincipale(Operateurs.resampling(
          // l1,5).getControlPoint());
          // Angle ori2 = Operateurs.directionPrincipale(Operateurs.resampling(
          // l2,5).getControlPoint());
          // Angle ori3 = Operateurs.directionPrincipale(Operateurs.resampling(
          // l2.reverse(),5).getControlPoint());
          // double value = Math.min(Angle.ecart(ori1,
          // ori2).getValeur()*180./Math.PI,
          // Angle.ecart(ori1, ori3).getValeur()*180./Math.PI);

          double distance2 = Math.min(Frechet.discreteFrechetWithProjection(l1, l2), Frechet.discreteFrechetWithProjection(l1.reverse(), l2));

          double diff = Math.abs(l1.length() - l2.length());

          double probaT = 1. / (distance2 + diff/* +value */);
          hypergraph.getHyperedges().put(newHypArc, probaT);
        }

      }

      System.out.println("ok1");

      System.out.println(indexHyperArcs.size());
      // résolution du pb d'optimisation linéaire
      LPWizard lpw = new LPWizard();
      for (Integer i : indexHyperArcs.keySet()) {
        String var = "x" + i;
        lpw.plus(var, hypergraph.getHyperedges().get(indexHyperArcs.get(i)));
        lpw.setBoolean(var);
      }
      System.out.println("o2");

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

      System.out.println("ok3");

      System.out.print("solving .... ");
      LPSolution sol = lpw.solve();
      System.out.println("done");
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

    IPopulation<IFeature> out = Utils.exportMatchinLinks2(finalMatching);
    ShapefileWriter.write(out, "/home/bcostes/Bureau/tmp/optmatching.shp");

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
    //
    // public Set<Set<Arc>> getHyperedges() {
    // return hypedges;
    // }
    //
    // public void setHyperedges(Set<Set<Arc>> hypedges) {
    // this.hypedges = hypedges;
    // }

  }

}
