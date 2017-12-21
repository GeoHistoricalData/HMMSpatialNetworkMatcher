package hmmmatching.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Groupe;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
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
import hmmmatching.impl.ACluster;
import hmmmatching.impl.AClusterCollection;
import hmmmatching.impl.StrokeTopo;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;

public class Test2 {

  private double alpha = 1.;
  //private double beta = 1.;
  private double gamma = 1;
  private int stroke_length = 3;
  //private double penaltyfactor = 1;

  private double distance_threshold = 250;
  private double selection = 25;

  /**
   * Symbole = comp etats cachés = ref
   */

  /**
   * Reference network
   */
  private CarteTopo netRef;

  /**
   * Comparisson network: network to be matched
   */
  private CarteTopo netComp;

  /**
   * matching temporary result
   */
  private Map<Arc, List<ACluster>> matching;

  /**
   * Unmatched edges
   */
  Set<Arc> unmatched;

  Stack<Arc> unprocessed;

  public Test2() {
    this.netRef = new CarteTopo("Reference network");
    this.netComp = new CarteTopo("Comparisson network");
    this.matching = new HashMap<Arc, List<ACluster>>();
    this.unmatched = new HashSet<Arc>();
    this.unprocessed = new Stack<Arc>();
  }

  /**
   * Initialization
   */
  public void init(String netref, String netcomp) {
    /*
     * Lectures
     */
    IPopulation<IFeature> inRef = ShapefileReader.read(netref);
    IPopulation<IFeature> inComp = ShapefileReader.read(netcomp);

    /*
     * Création des réseaux
     */
    IPopulation<Arc> popArcRef = this.netRef.getPopArcs();
    for (IFeature f : inRef) {
      Arc a = popArcRef.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      a.addCorrespondant(f);
    }
    IPopulation<Arc> popArcComp = this.netComp.getPopArcs();
    for (IFeature f : inComp) {
      Arc a = popArcComp.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      a.addCorrespondant(f);
    }
    this.netRef.creeTopologieArcsNoeuds(1);
    this.netRef.creeNoeudsManquants(1);
    this.netRef.rendPlanaire(1);
    this.netRef.filtreDoublons(1);
    this.netRef.filtreArcsNull(1);
    this.netRef.filtreArcsDoublons();
    this.netRef.filtreNoeudsSimples();
    this.netRef.getPopArcs().initSpatialIndex(Tiling.class, false);
    for (Arc a : this.netRef.getPopArcs()) {
      a.setPoids(a.longueur());
    }
    this.netComp.creeTopologieArcsNoeuds(1);
    this.netComp.creeNoeudsManquants(1);
    this.netComp.rendPlanaire(1);
    this.netComp.filtreDoublons(1);
    this.netComp.filtreArcsNull(1);
    this.netComp.filtreArcsDoublons();
    this.netComp.filtreNoeudsSimples();
    this.netComp.getPopArcs().initSpatialIndex(Tiling.class, false);
    for (Arc a : this.netComp.getPopArcs()) {
      a.setPoids(a.longueur());
    }
    // TODO: filtrage des noeuds simples ?
  }

  /**
   * Run hmm networks matching process
   */
  public void process() {

    IPopulation<IFeature> out = new Population<IFeature>();

    // arc non appariés et non déja utilisé en début de stroke
    this.unprocessed.addAll(this.netComp.getPopArcs());

    long seed = 42L;
    Random rng = new Random(seed);
    List<List<Arc>> strokes = StrokeTopo.buildStroke(this.netComp.getPopArcs(), Math.PI / 6., 1, rng);

    int cpt = 0;
    for (List<Arc> stroke : strokes) {
      cpt++;
      System.out.println(cpt + "/" + strokes.size());
      if (stroke.size() < this.stroke_length || stroke.size() > 5) {
        continue;
      }

      // ---------> for output debug only ------------------
      List<ILineString> ll = new ArrayList<ILineString>();
      for (Arc a : stroke) {
        ll.add(new GM_LineString(a.getGeometrie().coord()));
      }
      ILineString lm = Operateurs.union(ll);
      out.add(new DefaultFeature(lm));
      // <----------------------------------------------------

      this.unprocessed.remove(stroke.get(0));
      Node hmm = this.match(stroke);
      if (hmm != null && hmm.getStates() != null && !hmm.getStates().isEmpty()) {
        for (int i = 0; i < hmm.getStates().size(); i++) {
          Arc a2 = stroke.get(i);
          this.unprocessed.remove(a2);
          ACluster a = hmm.getStates().get(i);
          if (this.matching.containsKey(a2)) {
            this.matching.get(a2).add(a);
          } else {
            List<ACluster> l = new ArrayList<ACluster>();
            l.add(a);
            this.matching.put(a2, l);
          }
        }
        if (hmm.getStates().size() != stroke.size()) {
          Collections.reverse(stroke);
          hmm = this.match(stroke);
          if (hmm != null && hmm.getStates() != null && !hmm.getStates().isEmpty()) {
            for (int i = 0; i < hmm.getStates().size(); i++) {
              Arc a2 = stroke.get(i);
              this.unprocessed.remove(a2);
              ACluster a = hmm.getStates().get(i);
              if (this.matching.containsKey(a2)) {
                this.matching.get(a2).add(a);
              } else {
                List<ACluster> l = new ArrayList<ACluster>();
                l.add(a);
                this.matching.put(a2, l);
              }
            }
          }
        }
      } else {
        // REVERSE MATCHING
        Collections.reverse(stroke);
        hmm = this.match(stroke);
        if (hmm != null && hmm.getStates() != null && !hmm.getStates().isEmpty()) {
          for (int i = 0; i < hmm.getStates().size(); i++) {
            Arc a2 = stroke.get(i);
            this.unprocessed.remove(a2);
            ACluster a = hmm.getStates().get(i);
            if (this.matching.containsKey(a2)) {
              this.matching.get(a2).add(a);
            } else {
              List<ACluster> l = new ArrayList<ACluster>();
              l.add(a);
              this.matching.put(a2, l);
            }
          }
        }
      }
    }

    //
    ShapefileWriter.write(out, "/home/bcostes/Bureau/strokes.shp");
    out.clear();
    //
    //

    while (!this.unprocessed.isEmpty()) {
      System.out.println(this.unprocessed.size());
      Arc current = this.unprocessed.pop();
      Noeud ini = current.getNoeudIni();
      Noeud fin = current.getNoeudFin();
      Noeud random = null;
      List<Arc> stroke = new ArrayList<Arc>();
      // sélection en forme de Tore autour de arc.getNoeudIni()
      // IGeometry geom1 = current.getNoeudIni().getGeom().buffer(500);
      // IGeometry geom2 = current.getNoeudIni().getGeom().buffer(1000);
      IGeometry geom1 = current.getNoeudIni().getGeom().buffer(100);
      IGeometry geom2 = current.getNoeudIni().getGeom().buffer(50000);

      IGeometry geom = geom2.difference(geom1.intersection(geom2));
      List<Noeud> selection = new ArrayList<Noeud>(this.netComp.getPopNoeuds().select(geom));

      while (true) {
        random = selection.get((int) (Math.random() * (selection.size() - 1)));
        if (random.equals(ini) || random.equals(fin)) {
          continue;
        }

        Groupe ppc = ini.plusCourtChemin(random, 0);

        if (ppc == null) {
          continue;
        }
        stroke.clear();
        stroke.addAll(ppc.getListeArcs());
        if (!stroke.contains(current)) {
          stroke.add(0, current);
        }
        if (stroke.size() >= this.stroke_length && stroke.size() < 5) {
          break;
        } else {
          ppc.vide();
          ppc = null;
        }
      }

      // ---------> for output debug only ------------------
      List<ILineString> ll = new ArrayList<ILineString>();
      for (Arc a : stroke) {
        ll.add(new GM_LineString(a.getGeometrie().coord()));
      }
      ILineString lm = Operateurs.union(ll);
      out.add(new DefaultFeature(lm));
      // <----------------------------------------------------

      this.unprocessed.remove(stroke.get(0));
      Node hmm = this.match(stroke);
      if (hmm != null && hmm.getStates() != null && !hmm.getStates().isEmpty()) {
        for (int i = 0; i < hmm.getStates().size(); i++) {
          Arc a2 = stroke.get(i);
          this.unprocessed.remove(a2);
          ACluster a = hmm.getStates().get(i);
          if (this.matching.containsKey(a2)) {
            this.matching.get(a2).add(a);
          } else {
            List<ACluster> l = new ArrayList<ACluster>();
            l.add(a);
            this.matching.put(a2, l);
          }
        }
        if (hmm.getStates().size() != stroke.size()) {
          Collections.reverse(stroke);
          hmm = this.match(stroke);
          if (hmm != null && hmm.getStates() != null && !hmm.getStates().isEmpty()) {
            for (int i = 0; i < hmm.getStates().size(); i++) {
              Arc a2 = stroke.get(i);
              this.unprocessed.remove(a2);
              ACluster a = hmm.getStates().get(i);
              if (this.matching.containsKey(a2)) {
                this.matching.get(a2).add(a);
              } else {
                List<ACluster> l = new ArrayList<ACluster>();
                l.add(a);
                this.matching.put(a2, l);
              }
            }
          }
        }
      } else {
        // REVERSE MATCHING
        Collections.reverse(stroke);
        hmm = this.match(stroke);
        if (hmm != null && hmm.getStates() != null && !hmm.getStates().isEmpty()) {
          for (int i = 0; i < hmm.getStates().size(); i++) {
            Arc a2 = stroke.get(i);
            this.unprocessed.remove(a2);
            ACluster a = hmm.getStates().get(i);
            if (this.matching.containsKey(a2)) {
              this.matching.get(a2).add(a);
            } else {
              List<ACluster> l = new ArrayList<ACluster>();
              l.add(a);
              this.matching.put(a2, l);
            }
          }
        }
      }
    }

    ShapefileWriter.write(out, "/home/bcostes/Bureau/strokes_ppc.shp");
    out.clear();

    this.unmatched.addAll(this.netComp.getPopArcs());
    this.unmatched.removeAll(this.matching.keySet());

    // **************************************************************************************
    // ******************** comment here if LP SOLVINg -----> ******************************
    // **************************************************************************************

    // //calcul des scores
    // Map<Arc, List<Arc>> finalMatching = new HashMap<Arc, List<Arc>>();
    // for(Arc a: this.matching.keySet()){
    // List<Arc> map = new ArrayList<Arc>();
    // for(ACluster cluster: this.matching.get(a)){
    // for(Arc a2: cluster.getArcs()){
    // if(!map.contains(a2)){
    // map.add(a2);
    // }
    // }
    // }
    // finalMatching.put(a, map);
    // }

    // **************************************************************************************
    // ******************** end of comment here if LP SOLVINg -----> ***********************
    // **************************************************************************************

    // **************************************************************************************
    // ******************************************* LP SOLVINg HERE -----> *******************
    // **************************************************************************************

    // calcul des scores
    Map<Arc, List<Arc>> finalMatching = new HashMap<Arc, List<Arc>>();
    // prise de décision
    Map<Arc, List<Arc>> candidatesMatching = new HashMap<Arc, List<Arc>>();
    Map<Arc, List<Arc>> reverseCandidatesMatching = new HashMap<Arc, List<Arc>>();

    for (Arc aref : this.matching.keySet()) {
      for (ACluster ac : this.matching.get(aref)) {
        if (!candidatesMatching.containsKey(aref)) {
          candidatesMatching.put(aref, new ArrayList<Arc>());
        }
        for (Arc acomp : ac.getArcs()) {
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
          List<Arc> l = new ArrayList<Arc>();
          l.add(arccomp);
          finalMatching.put(aref, l);
        }
        continue;
      }

      // on fait les ACluster
      AClusterCollection clusterColRef = new AClusterCollection(new ArrayList<Arc>(references));
      AClusterCollection clusterColComp = new AClusterCollection(new ArrayList<Arc>(candidates));
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
      cpt = 0;
      if (indexesComp.size() == 0 || indexesRef.size() == 0) {
        continue;
      }

      for (int i = 0; i < indexesRef.keySet().size(); i++) {
        ACluster clusterRef = indexesRef.get(i);

        ILineString l1 = new GM_LineString(Operateurs.resampling(clusterRef.getGeometrie(), 25).getControlPoint());
        for (int j = 0; j < indexesComp.size(); j++) {
          ACluster clusterComp = indexesComp.get(j);

          // création d'un hyperarc
          Set<Arc> newHypArc = new HashSet<Arc>();
          newHypArc.addAll(clusterRef.getArcs());
          newHypArc.addAll(clusterComp.getArcs());
          indexHyperArcs.put(cpt++, newHypArc);
          hypergraph.getHyperedges().add(newHypArc);
          boolean ok = true;
          for (Arc aref : clusterRef.getArcs()) {
            if (!candidatesMatching.get(aref).containsAll(clusterComp.getArcs())) {
              ok = false;
              break;
            }
          }
          if (!ok) {
            hypergraph.getCosts().put(newHypArc, Double.MIN_VALUE);
            continue;
          }

          double distance1 = Distances.lineMedianDistance2(clusterRef.getGeometrie(), clusterComp.getGeometrie());

          if (distance1 > this.distance_threshold) {
            hypergraph.getCosts().put(newHypArc, Double.MIN_VALUE);
            continue;
          }

          ILineString l2 = new GM_LineString(Operateurs.resampling(clusterComp.getGeometrie(), 25).getControlPoint());
          double distance2 = Math.min(Frechet.discreteFrechet(l1, l2), Frechet.discreteFrechet(l1.reverse(), l2));

          // double distance = Distances.hausdorff(clusterRef.getGeometrie(), clusterComp.getGeometrie());
          // double proba = Math.exp(-distance / this.alpha);
          // Angle ori1 = Operateurs.directionPrincipale(Operateurs.resampling(
          // clusterRef.getGeometrie(),5).getControlPoint());
          // Angle ori2 = Operateurs.directionPrincipale(Operateurs.resampling(
          // clusterComp.getGeometrie(),5).getControlPoint());
          // double value = Angle.ecart(ori1, ori2).getValeur()*180./Math.PI;
          // // double angleSkeletonV = (ori1.getValeur() > Math.PI/2) ? (Math.PI - ori1.getValeur()) :(ori1.getValeur());
          // // double angleEntityV = (ori2.getValeur() > Math.PI/2) ? (Math.PI - ori2.getValeur()) :(ori2.getValeur());
          // // double value = Math.abs(angleSkeletonV - angleEntityV)*180./Math.PI;
          // double proba2 = math.exp(-value/ this.gamma);
          // double d2 = Math.abs(clusterRef.longueur() - clusterComp.longueur());
          // double proba3 = math.exp(-d2/ this.beta);

          double proba = /* Math.exp(- **/1. / (distance1 + distance2)/* / this.alpha) */;

          // on diminue le score des "longs" aggrégats
          // double penalty = Math.exp(
          // -Math.min(clusterRef.getArcs().size(), clusterComp.getArcs().size())/ this.penaltyfactor);
          double probaT = proba/* * proba2 * proba3 *//* penalty */;
          hypergraph.getCosts().put(newHypArc, probaT);
        }

      }

      // résolution du pb d'optimisation linéaire
      LPWizard lpw = new LPWizard();
      for (Integer i : indexHyperArcs.keySet()) {
        String var = "x" + i;
        lpw.plus(var, hypergraph.getCosts().get(indexHyperArcs.get(i)));
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

      System.out.print("solving .... ");
      LPSolution sol = lpw.solve();
      System.out.println("done");
      for (int i = 0; i < indexHyperArcs.size(); i++) {
        if (sol.getBoolean("x" + i)) {

          Set<Arc> hyparc = indexHyperArcs.get(i);
          List<Arc> arcref = new ArrayList<Arc>();
          List<Arc> arccomp = new ArrayList<Arc>();
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
    // **************************************************************************************
    // ***************************************** <------ LP SOLVING HERE *******************
    // **************************************************************************************

    for (Arc a : finalMatching.keySet()) {
      IDirectPosition p1 = Operateurs.milieu(a.getGeometrie());
      for (Arc a2 : finalMatching.get(a)) {
        IDirectPosition p2 = Operateurs.projection(p1, a2.getGeometrie());
        IDirectPosition p3 = Operateurs.milieu(a2.getGeometrie());
        IDirectPosition p4 = Operateurs.projection(p3, a.getGeometrie());

        ILineString l1 = new GM_LineString(new DirectPositionList(Arrays.asList(p1, p2)));
        ILineString l2 = new GM_LineString(new DirectPositionList(Arrays.asList(p3, p4)));

        IFeature f = null;
        if (l1.length() < l2.length()) {
          if (p2.equals(a2.getGeometrie().startPoint()) || p2.equals(a2.getGeometrie().endPoint())) {
            if (p4.equals(a.getGeometrie().startPoint()) || p4.equals(a.getGeometrie().endPoint())) {
              f = new DefaultFeature(new GM_LineString(new DirectPositionList(Arrays.asList(p1, p3))));
            } else {
              f = new DefaultFeature(l2);
            }
          } else {
            f = new DefaultFeature(l1);
          }
        } else {
          if (p4.equals(a.getGeometrie().startPoint()) || p4.equals(a.getGeometrie().endPoint())) {
            if (p2.equals(a2.getGeometrie().startPoint()) || p2.equals(a2.getGeometrie().endPoint())) {
              f = new DefaultFeature(new GM_LineString(new DirectPositionList(Arrays.asList(p1, p3))));
            } else {
              f = new DefaultFeature(l1);
            }
          } else {
            f = new DefaultFeature(l2);
          }
        }
        out.add(f);
      }
    }
    // ShapefileWriter.write(out, "/home/bcostes/Bureau/test_hydro2.shp");

    ShapefileWriter.write(out, "/home/bcostes/Bureau/test_hydro2.shp");
    out.clear();
    for (Arc a : unmatched) {
      out.add(a);
    }
    // ShapefileWriter.write(out, "/home/bcostes/Bureau/notmatched_hydro.shp");
    // ShapefileWriter.write(out, "/home/bcostes/Bureau/notmatched_railways.shp");

  }

  class LocalHypergraph {
    private Set<Arc> hypvertices;
    private Set<Set<Arc>> hypedges;
    private Map<Set<Arc>, Double> costs;

    public LocalHypergraph() {
      this.hypedges = new HashSet<Set<Arc>>();
      this.hypvertices = new HashSet<Arc>();
      this.costs = new HashMap<Set<Arc>, Double>();
    }

    public Map<Set<Arc>, Double> getCosts() {
      return costs;
    }

    public void setCosts(Map<Set<Arc>, Double> costs) {
      this.costs = costs;
    }

    public Set<Arc> getHypervertices() {
      return hypvertices;
    }

    public void setHypervertices(Set<Arc> hypvertices) {
      this.hypvertices = hypvertices;
    }

    public Set<Set<Arc>> getHyperedges() {
      return hypedges;
    }

    public void setHyperedges(Set<Set<Arc>> hypedges) {
      this.hypedges = hypedges;
    }

  }

  private Node match(List<Arc> stroke) {

    Hashtable<ACluster, Double> start_p = new Hashtable<ACluster, Double>();
    // ref, comp
    Hashtable<Arc, Hashtable<ACluster, Double>> emit_p = new Hashtable<Arc, Hashtable<ACluster, Double>>();

    Hashtable<Arc, Collection<Arc>> candidateEdges = new Hashtable<Arc, Collection<Arc>>(stroke.size());
    for (Arc a : stroke) {
      candidateEdges.put(a, new ArrayList<Arc>(0));
    }

    Arc start = stroke.get(0);

    Collection<Arc> candidates = this.netRef.getPopArcs().select(start.getGeom(), this.selection);

    AClusterCollection clustercol = new AClusterCollection(new ArrayList<Arc>(candidates));

    // LOGGER.info("start emission probabilities");
    for (ACluster cluster : clustercol) {
      double proba = emissionProbability(start, cluster);
      if (!Double.isInfinite(proba)) {
        start_p.put(cluster, new Double(proba));
        candidateEdges.get(start).add(cluster);
      }
    }

    // LOGGER.info("emission probabilities");
    for (int i = 1; i < stroke.size(); i++) {
      Arc arc = stroke.get(i);
      Collection<Arc> candidateArc = this.netRef.getPopArcs().select(arc.getGeom(), this.selection);
      // création des agrégats éventuels et filtrage automatique si fusion impossible

      clustercol = new AClusterCollection(new ArrayList<Arc>(candidateArc));
      if (clustercol.isEmpty()) {
        emit_p.put(arc, new Hashtable<ACluster, Double>());
      }
      boolean allNegativeInfinity = true;
      for (ACluster cluster : clustercol) {
        double emissionP = emissionProbability(arc, cluster);
        if (!Double.isInfinite(emissionP)) {
          allNegativeInfinity = false;
        }
        if (emit_p.containsKey(arc)) {
          emit_p.get(arc).put(cluster, emissionP);
        } else {
          Hashtable<ACluster, Double> emit = new Hashtable<ACluster, Double>();
          emit.put(cluster, emissionP);
          emit_p.put(arc, emit);
        }
      }
      if (allNegativeInfinity) {
        this.unprocessed.remove(arc);
      }
    }

    // LOGGER.info("viterbi");
    Node result = forward_viterbi(start_p, emit_p, stroke);
    return result;

  }

  // private Node match(Arc start, ACluster startMatching, List<Arc> stroke) {
  // Hashtable<ACluster, Double> start_p = new Hashtable<ACluster, Double>();
  // // ref, comp
  // Hashtable<Arc, Hashtable<ACluster, Double>> emit_p = new Hashtable<Arc, Hashtable<ACluster, Double>>();
  //
  //
  // start_p.put(startMatching, new Double(1.));
  //
  // //LOGGER.info("emission probabilities");
  // for (Arc arc : stroke) {
  // Collection<Arc> candidateArc = this.netRef.getPopArcs().select(arc
  // .getGeom(), this.selection);
  // //création des agrégats éventuels et filtrage automatique si fusion impossible
  // AClusterCollection clustercol = new AClusterCollection(new ArrayList<Arc>(candidateArc));
  // if(clustercol.isEmpty()){
  // emit_p.put(arc, new Hashtable<ACluster, Double>());
  // }
  // for (ACluster cluster : clustercol) {
  // double emissionP = emissionProbability(arc, cluster);
  // if(emit_p.containsKey(arc)){
  // emit_p.get(arc).put(cluster, emissionP);
  // }
  // else{
  // Hashtable<ACluster, Double> emit = new Hashtable<ACluster, Double>();
  // emit.put(cluster, emissionP);
  // emit_p.put(arc, emit);
  // }
  // }
  // }
  // //LOGGER.info("viterbi");
  // Node result = forward_viterbi(start,start_p, emit_p, stroke);
  // return result;
  //
  // }

  /**
   * The list of gps points to match.
   */
  private IFeatureCollection<? extends Arc> points = null;

  /**
   * @return the list of gps points to match.
   */
  public IFeatureCollection<? extends Arc> getPoints() {
    return points;
  }

  /**
   * Compute the emission probability between the point p and the given edge arc.
   * 
   * @param p
   *          a gps point
   * @param arc
   *          an edge
   * @return the emission probability
   */
  private double emissionProbability(Arc a1, ACluster a2) {
    // double distance = Math.min(Distances.premiereComposanteHausdorff(a1.getGeometrie(), a2.getGeometrie()),
    // Distances.premiereComposanteHausdorff(a2.getGeometrie(), a1.getGeometrie()));

    double distance = this.lineMedianDistance(a1.getGeometrie(), a2.getGeometrie());

    if (distance > this.distance_threshold) {
      return Double.NEGATIVE_INFINITY;

    }
    double a = distance / this.alpha;
    double proba = a + Math.log(this.alpha);
    //

    // Angle ori1 = Operateurs.directionPrincipale(Operateurs.resampling(
    // a1.getGeometrie(),5).getControlPoint());
    // Angle ori2 = Operateurs.directionPrincipale(Operateurs.resampling(
    // a2.getGeometrie(),5).getControlPoint());
    // double value = Angle.ecart(ori1, ori2).getValeur()*180./Math.PI;
    // // double angleSkeletonV = (ori1.getValeur() > Math.PI/2) ? (Math.PI - ori1.getValeur()) :(ori1.getValeur());
    // // double angleEntityV = (ori2.getValeur() > Math.PI/2) ? (Math.PI - ori2.getValeur()) :(ori2.getValeur());
    // double value = Math.abs(angleSkeletonV - angleEntityV)*180./Math.PI;
    //
    // if(value > 45.){
    // return Double.NEGATIVE_INFINITY;
    // }

    //
    // double b = value / this.gamma;
    // double proba2 = b + Math.log(this.gamma);
    //
    //
    // double d2 = Math.abs(a1.longueur() - a2.longueur());
    //
    //
    // if(d2 > Math.min(a1.longueur(), a2.longueur())*0.9){
    // return Double.NEGATIVE_INFINITY;
    // }
    //
    //
    //
    // double c = d2 / this.beta;
    // double proba3 = c+ Math.log(this.beta);

    //
    ILineString l1 = new GM_LineString(Operateurs.resampling(a1.getGeometrie(), 25).getControlPoint());
    ILineString l2 = new GM_LineString(Operateurs.resampling(a2.getGeometrie(), 25).getControlPoint());

    // ILineString l1 = new GM_LineString(a1.getGeometrie().getControlPoint());
    // ILineString l2 = new GM_LineString(a2.getGeometrie().getControlPoint());

    double distance2 = Math.min(Frechet.discreteFrechet(l1, l2), Frechet.discreteFrechet(l1.reverse(), l2));
    // if(distance2>2*this.selection){
    // return Double.NEGATIVE_INFINITY;
    // }
    double a3 = distance2 / this.alpha;
    double proba2 = a3 + Math.log(this.alpha);
    return -(proba + proba2);

    // return -(proba /*9+ proba3 + proba2*/);

    // ILineString l1 = new GM_LineString(Operateurs.resampling(a1.getGeometrie(),5).getControlPoint());
    // ILineString l2 = new GM_LineString(Operateurs.resampling(a2.getGeometrie(),5).getControlPoint());
    //
    // double distance = Math.min(Frechet.discreteFrechet(l1, l2), Frechet.discreteFrechet(l1.reverse(), l2));
    // if(distance>this.selection){
    // return Double.NEGATIVE_INFINITY;
    // }
    // double a = distance / this.alpha;
    // double proba = a + Math.log(this.alpha);
    // return -proba;
  }

  /**
   * Compute the transition probability corresponding to the probability that p2 is matched to a2 given that p1 is matched to a1.
   * 
   * @param p1
   *          a gps point
   * @param p2
   *          another gps point
   * @param a1
   *          the edge p1 is matched to
   * @param a2
   *          the edge p2 is matched to
   * @return the transition probability corresponding to the probability that p2 is matched to a2 given that p1 is matched to a1.
   */
  Transition transitionProbability(Arc acomp1, Arc acomp2, ACluster aref1, ACluster aref2) {

    if (!acomp1.getGeometrie().coord().get(0).equals(acomp2.getGeometrie().coord().get(0))
        && !acomp1.getGeometrie().coord().get(acomp1.getGeometrie().coord().size() - 1).equals(acomp2.getGeometrie().coord().get(0))
        && !acomp1.getGeometrie().coord().get(0).equals(acomp2.getGeometrie().coord().get(acomp2.getGeometrie().coord().size() - 1))
        && !acomp1.getGeometrie().coord().get(acomp1.getGeometrie().coord().size() - 1).equals(acomp2.getGeometrie().coord().get(acomp2.getGeometrie().coord().size() - 1))) {
      return new Transition(Double.NEGATIVE_INFINITY);
    }

    if (!aref1.getGeometrie().coord().get(0).equals(aref2.getGeometrie().coord().get(0))
        && !aref1.getGeometrie().coord().get(aref1.getGeometrie().coord().size() - 1).equals(aref2.getGeometrie().coord().get(0))
        && !aref1.getGeometrie().coord().get(0).equals(aref2.getGeometrie().coord().get(aref2.getGeometrie().coord().size() - 1))
        && !aref1.getGeometrie().coord().get(aref1.getGeometrie().coord().size() - 1).equals(aref2.getGeometrie().coord().get(aref2.getGeometrie().coord().size() - 1))) {
      return new Transition(Double.NEGATIVE_INFINITY);
    }

    if (aref1.equals(aref2)) {

      IDirectPosition pcompMiddle = null;
      Angle angle1 = null;
      if (acomp1.getGeometrie().getControlPoint(0).equals(acomp2.getGeometrie().getControlPoint(0))) {
        pcompMiddle = acomp1.getGeometrie().getControlPoint(0);
        angle1 = Angle.angleTroisPoints(acomp1.getGeometrie().getControlPoint(1), pcompMiddle, acomp2.getGeometrie().getControlPoint(1));
      } else if (acomp1.getGeometrie().getControlPoint(0).equals(acomp2.getGeometrie().getControlPoint(acomp2.getGeometrie().getControlPoint().size() - 1))) {
        pcompMiddle = acomp1.getGeometrie().getControlPoint(0);
        angle1 = Angle.angleTroisPoints(acomp1.getGeometrie().getControlPoint(1), pcompMiddle,
            acomp2.getGeometrie().getControlPoint(acomp2.getGeometrie().getControlPoint().size() - 2));
      } else if (acomp1.getGeometrie().getControlPoint(acomp1.getGeometrie().getControlPoint().size() - 1).equals(acomp2.getGeometrie().getControlPoint(0))) {
        pcompMiddle = acomp1.getGeometrie().getControlPoint(acomp1.getGeometrie().getControlPoint().size() - 1);
        angle1 = Angle.angleTroisPoints(acomp1.getGeometrie().getControlPoint(acomp1.getGeometrie().getControlPoint().size() - 2), pcompMiddle,
            acomp2.getGeometrie().getControlPoint(1));
      } else {
        pcompMiddle = acomp1.getGeometrie().getControlPoint(acomp1.getGeometrie().getControlPoint().size() - 1);
        angle1 = Angle.angleTroisPoints(acomp1.getGeometrie().getControlPoint(acomp1.getGeometrie().getControlPoint().size() - 2), pcompMiddle,
            acomp2.getGeometrie().getControlPoint(acomp2.getGeometrie().getControlPoint().size() - 2));
      }

      IDirectPosition pproj = Operateurs.projection(pcompMiddle, aref1.getGeometrie());
      if (pproj.equals(aref1.getGeometrie().getControlPoint(0)) || pproj.equals(aref1.getGeometrie().getControlPoint(aref1.getGeometrie().getControlPoint().size() - 1))) {

        Angle angle2 = new Angle(0);
        double d = Angle.ecart(angle1, angle2).getValeur();
        if (d * 180 / Math.PI > 45.) {
          return new Transition(Double.NEGATIVE_INFINITY);
        }
        double a = (d * 180 / Math.PI) / this.gamma;
        double proba = a + Math.log(this.gamma);
        return new Transition(-proba);
      } else {
        ILineString newline = Operateurs.projectionEtInsertion(pcompMiddle, aref1.getGeometrie());
        IDirectPosition p1 = null, p2 = null;
        for (int i = 0; i < newline.coord().size(); i++) {
          if (newline.getControlPoint(i).equals(pproj)) {
            p1 = newline.getControlPoint(i - 1);
            p2 = newline.getControlPoint(i + 1);
            break;
          }
        }
        Angle angle2 = Angle.angleTroisPoints(p1, pcompMiddle, p2);
        double d = Angle.ecart(angle1, angle2).getValeur();
        if (d * 180 / Math.PI > 45.) {
          return new Transition(Double.NEGATIVE_INFINITY);
        }
        double a = (d * 180 / Math.PI) / this.gamma;
        double proba = a + Math.log(this.gamma);
        return new Transition(-proba);
      }

    } else if (acomp1.equals(acomp2)) {

      if (!aref1.equals(aref2)) {
        for (Arc a1 : aref1.getArcs()) {
          if (aref2.getArcs().contains(a1)) {
            return new Transition(Double.NEGATIVE_INFINITY);
          }
        }
        for (Arc a1 : aref2.getArcs()) {
          if (aref1.getArcs().contains(a1)) {
            return new Transition(Double.NEGATIVE_INFINITY);
          }
        }

      }

      IDirectPosition prefMiddle = null;
      Angle angle1 = null;
      if (aref1.getGeometrie().getControlPoint(0).equals(aref2.getGeometrie().getControlPoint(0))) {
        prefMiddle = aref1.getGeometrie().getControlPoint(0);
        angle1 = Angle.angleTroisPoints(aref1.getGeometrie().getControlPoint(1), prefMiddle, aref2.getGeometrie().getControlPoint(1));
      } else if (aref1.getGeometrie().getControlPoint(0).equals(aref2.getGeometrie().getControlPoint(aref2.getGeometrie().getControlPoint().size() - 1))) {
        prefMiddle = acomp1.getGeometrie().getControlPoint(0);
        angle1 = Angle.angleTroisPoints(aref1.getGeometrie().getControlPoint(1), prefMiddle,
            aref2.getGeometrie().getControlPoint(aref2.getGeometrie().getControlPoint().size() - 2));
      } else if (aref1.getGeometrie().getControlPoint(aref1.getGeometrie().getControlPoint().size() - 1).equals(aref2.getGeometrie().getControlPoint(0))) {
        prefMiddle = aref1.getGeometrie().getControlPoint(aref1.getGeometrie().getControlPoint().size() - 1);
        angle1 = Angle.angleTroisPoints(aref1.getGeometrie().getControlPoint(aref1.getGeometrie().getControlPoint().size() - 2), prefMiddle,
            aref2.getGeometrie().getControlPoint(1));
      } else {
        prefMiddle = aref1.getGeometrie().getControlPoint(aref1.getGeometrie().getControlPoint().size() - 1);
        angle1 = Angle.angleTroisPoints(aref1.getGeometrie().getControlPoint(aref1.getGeometrie().getControlPoint().size() - 2), prefMiddle,
            aref2.getGeometrie().getControlPoint(aref2.getGeometrie().getControlPoint().size() - 2));
      }

      IDirectPosition pproj = Operateurs.projection(prefMiddle, acomp1.getGeometrie());
      if (pproj.equals(acomp1.getGeometrie().getControlPoint(0)) || pproj.equals(acomp1.getGeometrie().getControlPoint(acomp1.getGeometrie().getControlPoint().size() - 1))) {

        Angle angle2 = new Angle(0);
        double d = Angle.ecart(angle1, angle2).getValeur();
        if (d * 180 / Math.PI > 45.) {
          return new Transition(Double.NEGATIVE_INFINITY);
        }
        double a = (d * 180 / Math.PI) / this.gamma;
        double proba = a + Math.log(this.gamma);
        return new Transition(-proba);
      } else {
        ILineString newline = Operateurs.projectionEtInsertion(prefMiddle, acomp1.getGeometrie());
        IDirectPosition p1 = null, p2 = null;
        for (int i = 0; i < newline.coord().size(); i++) {
          if (newline.getControlPoint(i).equals(pproj)) {
            p1 = newline.getControlPoint(i - 1);
            p2 = newline.getControlPoint(i + 1);
            break;
          }
        }
        Angle angle2 = Angle.angleTroisPoints(p1, prefMiddle, p2);
        double d = Angle.ecart(angle1, angle2).getValeur();
        if (d * 180 / Math.PI > 45.) {
          return new Transition(Double.NEGATIVE_INFINITY);
        }
        double a = (d * 180 / Math.PI) / this.gamma;
        double proba = a + Math.log(this.gamma);
        return new Transition(-proba);

      }
    } else {
      for (Arc a1 : aref1.getArcs()) {
        if (aref2.getArcs().contains(a1)) {
          return new Transition(Double.NEGATIVE_INFINITY);
        }
      }
      for (Arc a1 : aref2.getArcs()) {
        if (aref1.getArcs().contains(a1)) {
          return new Transition(Double.NEGATIVE_INFINITY);
        }
      }

      IDirectPosition prefini1 = aref1.getGeometrie().getControlPoint(0);
      IDirectPosition preffin1 = aref1.getGeometrie().getControlPoint(aref1.getGeometrie().getControlPoint().size() - 1);
      IDirectPosition prefini2 = aref2.getGeometrie().getControlPoint(0);
      IDirectPosition preffin2 = aref2.getGeometrie().getControlPoint(aref2.getGeometrie().getControlPoint().size() - 1);

      IDirectPosition p1 = null, p2 = null, p3 = null;

      if (!prefini1.equals(prefini2) && !prefini1.equals(preffin2) && !preffin1.equals(prefini2) && !preffin1.equals(preffin2)) {
        return new Transition(Double.NEGATIVE_INFINITY);
      }

      if (prefini1.equals(prefini2)) {
        p1 = aref1.getGeometrie().getControlPoint(1);
        p2 = aref1.getGeometrie().getControlPoint(0);
        p3 = aref2.getGeometrie().getControlPoint(1);
      } else if (prefini1.equals(preffin2)) {
        p1 = aref1.getGeometrie().getControlPoint(1);
        p2 = aref1.getGeometrie().getControlPoint(0);
        p3 = aref2.getGeometrie().getControlPoint(aref2.getGeometrie().getControlPoint().size() - 2);
      } else if (preffin1.equals(prefini2)) {
        p1 = aref1.getGeometrie().getControlPoint(aref1.getGeometrie().getControlPoint().size() - 2);
        p2 = aref2.getGeometrie().getControlPoint(0);
        p3 = aref2.getGeometrie().getControlPoint(1);
      } else {
        p1 = aref1.getGeometrie().getControlPoint(aref1.getGeometrie().getControlPoint().size() - 2);
        p2 = aref1.getGeometrie().getControlPoint(aref1.getGeometrie().getControlPoint().size() - 1);
        p3 = aref2.getGeometrie().getControlPoint(aref2.getGeometrie().getControlPoint().size() - 2);
      }
      Angle angle1 = Angle.angleTroisPoints(p1, p2, p3);

      IDirectPosition pcompini1 = acomp1.getGeometrie().getControlPoint(0);
      IDirectPosition pcompfin1 = acomp1.getGeometrie().getControlPoint(acomp1.getGeometrie().getControlPoint().size() - 1);
      IDirectPosition pcompini2 = acomp2.getGeometrie().getControlPoint(0);
      IDirectPosition pcompfin2 = acomp2.getGeometrie().getControlPoint(acomp2.getGeometrie().getControlPoint().size() - 1);

      p1 = null;
      p2 = null;
      p3 = null;
      if (pcompini1.equals(pcompini2)) {
        p1 = acomp1.getGeometrie().getControlPoint(1);
        p2 = acomp1.getGeometrie().getControlPoint(0);
        p3 = acomp2.getGeometrie().getControlPoint(1);
      } else if (pcompini1.equals(pcompfin2)) {
        p1 = acomp1.getGeometrie().getControlPoint(1);
        p2 = acomp1.getGeometrie().getControlPoint(0);
        p3 = acomp2.getGeometrie().getControlPoint(acomp2.getGeometrie().getControlPoint().size() - 2);
      } else if (pcompfin1.equals(pcompini2)) {
        p1 = acomp1.getGeometrie().getControlPoint(acomp1.getGeometrie().getControlPoint().size() - 2);
        p2 = acomp2.getGeometrie().getControlPoint(0);
        p3 = acomp2.getGeometrie().getControlPoint(1);
      } else {
        p1 = acomp1.getGeometrie().getControlPoint(acomp1.getGeometrie().getControlPoint().size() - 2);
        p2 = acomp1.getGeometrie().getControlPoint(acomp1.getGeometrie().getControlPoint().size() - 1);
        p3 = acomp2.getGeometrie().getControlPoint(acomp2.getGeometrie().getControlPoint().size() - 2);
      }
      Angle angle2 = Angle.angleTroisPoints(p1, p2, p3);
      double d = Angle.ecart(angle1, angle2).getValeur();
      if (d * 180 / Math.PI > 45.) {
        return new Transition(Double.NEGATIVE_INFINITY);
      }
      double a = (d * 180 / Math.PI) / this.gamma;
      double proba = a + Math.log(this.gamma);
      return new Transition(-proba);
    }

  }

  /**
   * Run the viterbi algorithm.
   * 
   * @param start_p
   *          start probabilities
   * @param emit_p
   *          emission probabilities
   * @return the best match
   */
  private Node forward_viterbi(Hashtable<ACluster, Double> start_p, Hashtable<Arc, Hashtable<ACluster, Double>> emit_p, List<Arc> stroke) {
    Hashtable<ACluster, Node> T = new Hashtable<ACluster, Node>(start_p.keySet().size());

    // initialize base cases
    for (ACluster state : start_p.keySet()) {
      List<ACluster> argmax = new ArrayList<ACluster>(1);
      List<ACluster> states = new ArrayList<ACluster>(1);
      argmax.add(state);
      states.add(state);
      Double prob = start_p.get(state);
      if (prob == null) {
        prob = Double.NEGATIVE_INFINITY;
      } else {
        // LOGGER.info("initial probability = " + prob + " for " + state.getGeom());
        T.put(state, new Node(prob, argmax, states, prob));
      }
    }
    List<Integer> dropped = new ArrayList<Integer>(0);
    Arc previous = stroke.get(0);
    Collection<ACluster> previousCandidates = start_p.keySet();
    // run viterbi for all observations
    for (int i = 1; i < stroke.size(); i++) {
      Arc current = stroke.get(i);
      // LOGGER.info("Point " + i + " = " + current);
      Set<ACluster> candidates = emit_p.get(current).keySet();
      Hashtable<ACluster, Node> U = new Hashtable<ACluster, Node>(candidates.size());
      // LOGGER.info("Candidates " + candidates.size());
      for (ACluster nextState : candidates) {
        double total = 0;
        double valmax = Double.NEGATIVE_INFINITY;
        List<ACluster> argmax = null;
        List<ACluster> states = null;
        double emit = Double.NEGATIVE_INFINITY;
        if (emit_p.get(current) != null && emit_p.get(current).get(nextState) != null) {
          emit = emit_p.get(current).get(nextState).doubleValue();
        }
        if (Double.isInfinite(emit)) {
          // LOGGER.debug("emit null for " + nextState);
          continue;
        }
        // LOGGER.info("Source Candidates " + previousCandidates.size());
        for (ACluster sourceState : previousCandidates) {
          Node node = T.get(sourceState);
          if (node == null) {
            continue;
          }
          double prob = node.getProb();
          List<ACluster> v_path = node.getPath();
          List<ACluster> v_states = node.getStates();
          double v_prob = node.getVProb();
          // LOGGER.debug("emit = " + emit);
          // LOGGER.info(" " + prob + " " + v_path + " " + v_prob);
          Transition transProb = transitionProbability(previous, current, sourceState, nextState);
          double trans = transProb.proba;
          if (Double.isInfinite(trans)) {
            // LOGGER.debug("trans null for " + previous + " to " + output);
            // LOGGER.debug("\t with" + source_state + " and " + next_state);
            continue;
          }
          // LOGGER.debug("trans = " + trans);
          double p = emit + trans;
          // LOGGER.debug("p = " + p);
          prob += p;
          v_prob += p;
          total += prob;
          // LOGGER.debug("v_prob = " + v_prob + " valmax = " + valmax);
          if (v_prob > valmax) {
            argmax = new ArrayList<ACluster>(v_path);
            states = new ArrayList<ACluster>(v_states);
            /*
             * for (Arc a : transProb.listeArcs) { // if (a != sourceState) { argmax.add(a); // } }
             */
            states.add(nextState);
            // geometries.add(transProb.geometry);
            valmax = v_prob;
          }
        }
        if (!Double.isInfinite(valmax) && argmax != null) {
          U.put(nextState, new Node(total, argmax, states, valmax));
        }
      }
      if (!U.isEmpty()) {
        // clean up
        for (Node p : T.values()) {
          if (p.getPath() != null) {
            p.getPath().clear();
          }
        }
        T.clear();
        T = U;
        previous = current;
        previousCandidates = candidates;
      } else {
        // LOGGER.info("dropping junk point " + current);
        // this.unprocessed.remove(stroke.get(i));
        dropped.add(i);
      }
    }
    double total = 0;
    List<ACluster> argmax = null;
    List<ACluster> states = null;
    double valmax = Double.NEGATIVE_INFINITY;
    // find the best match
    // LOGGER.debug("find the best match");
    for (ACluster state : T.keySet()) {
      Node objs = T.get(state);
      double prob = objs.getProb();
      List<ACluster> v_path = objs.getPath();
      List<ACluster> v_states = objs.getStates();
      double v_prob = objs.getVProb();
      total += prob;
      // LOGGER.debug("total " + total);
      if (v_prob > valmax) {
        argmax = v_path;
        valmax = v_prob;
        states = v_states;
        // LOGGER.debug("best match " + v_path);
      }
    }

    return new Node(total, argmax, states, valmax);
  }

  /**
   * Run the viterbi algorithm.
   * 
   * @param start_p
   *          start probabilities
   * @param emit_p
   *          emission probabilities
   * @return the best match
   */
  // private Node forward_viterbi(Arc start, Hashtable<ACluster, Double> start_p,
  // Hashtable<Arc, Hashtable<ACluster, Double>> emit_p, List<Arc> stroke) {
  // Hashtable<ACluster, Node> T = new Hashtable<ACluster, Node>(start_p.keySet().size());
  //
  // // initialize base cases
  // for (ACluster state : start_p.keySet()) {
  // List<ACluster> argmax = new ArrayList<ACluster>(1);
  // List<ACluster> states = new ArrayList<ACluster>(1);
  // argmax.add(state);
  // states.add(state);
  // Double prob = start_p.get(state);
  // if (prob == null) {
  // prob = Double.NEGATIVE_INFINITY;
  // } else {
  // // LOGGER.info("initial probability = " + prob + " for " + state.getGeom());
  // T.put(state, new Node(prob, argmax, states, prob));
  // }
  // }
  // List<Integer> dropped = new ArrayList<Integer>(0);
  // Arc previous = start;
  // Collection<ACluster> previousCandidates = start_p.keySet();
  // // run viterbi for all observations
  // for (int i = 0; i < stroke.size(); i++) {
  // Arc current = stroke.get(i);
  // //LOGGER.info("Point " + i + " = " + current);
  //
  // Set<ACluster> candidates = emit_p.get(current).keySet();
  // Hashtable<ACluster, Node> U = new Hashtable<ACluster, Node>(candidates.size());
  // // LOGGER.info("Candidates " + candidates.size());
  // for (ACluster nextState : candidates) {
  // double total = 0;
  // double valmax = Double.NEGATIVE_INFINITY;
  // List<ACluster> argmax = null;
  // List<ACluster> states = null;
  // double emit = Double.NEGATIVE_INFINITY;
  // if (emit_p.get(current) != null
  // && emit_p.get(current).get(nextState) != null) {
  // emit = emit_p.get(current).get(nextState).doubleValue();
  // }
  // if (Double.isInfinite(emit)) {
  // // LOGGER.debug("emit null for " + nextState);
  // continue;
  // }
  // // LOGGER.info("Source Candidates " + previousCandidates.size());
  // for (ACluster sourceState : previousCandidates) {
  // Node node = T.get(sourceState);
  // if (node == null) {
  // continue;
  // }
  // double prob = node.getProb();
  // List<ACluster> v_path = node.getPath();
  // List<ACluster> v_states = node.getStates();
  // double v_prob = node.getVProb();
  // // LOGGER.debug("emit = " + emit);
  // // LOGGER.info(" " + prob + " " + v_path + " " + v_prob);
  // Transition transProb = transitionProbability(previous, current,
  // sourceState, nextState);
  // double trans = transProb.proba;
  // if (Double.isInfinite(trans)) {
  // // LOGGER.debug("trans null for " + previous + " to " + output);
  // // LOGGER.debug("\t with" + source_state + " and " + next_state);
  // continue;
  // }
  // // LOGGER.debug("trans = " + trans);
  // double p = emit + trans;
  // // LOGGER.debug("p = " + p);
  // prob += p;
  // v_prob += p;
  // total += prob;
  // // LOGGER.debug("v_prob = " + v_prob + " valmax = " + valmax);
  // if (v_prob > valmax) {
  // argmax = new ArrayList<ACluster>(v_path);
  // states = new ArrayList<ACluster>(v_states);
  // /*for (Arc a : transProb.listeArcs) {
  // // if (a != sourceState) {
  // argmax.add(a);
  // // }
  // }*/
  // states.add(nextState);
  // //geometries.add(transProb.geometry);
  // valmax = v_prob;
  // }
  // }
  // if (!Double.isInfinite(valmax) && argmax != null) {
  // U.put(nextState, new Node(total, argmax, states, valmax));
  // }
  // }
  // if (!U.isEmpty()) {
  // // clean up
  // for (Node p : T.values()) {
  // if (p.getPath() != null) {
  // p.getPath().clear();
  // }
  // }
  // T.clear();
  // T = U;
  // previous = current;
  // previousCandidates = candidates;
  // } else {
  // // LOGGER.info("dropping junk point " + current);
  // dropped.add(i);
  // }
  // }
  // double total = 0;
  // List<ACluster> argmax = null;
  // List<ACluster> states = null;
  // double valmax = Double.NEGATIVE_INFINITY;
  // // find the best match
  // // LOGGER.debug("find the best match");
  // for (ACluster state : T.keySet()) {
  // Node objs = T.get(state);
  // double prob = objs.getProb();
  // List<ACluster> v_path = objs.getPath();
  // List<ACluster> v_states = objs.getStates();
  // double v_prob = objs.getVProb();
  // total += prob;
  // // LOGGER.debug("total " + total);
  // if (v_prob > valmax) {
  // argmax = v_path;
  // valmax = v_prob;
  // states = v_states;
  // // LOGGER.debug("best match " + v_path);
  // }
  // }
  //
  // return new Node(total, argmax, states, valmax);
  // }

  private double lineMedianDistance(ILineString l1, ILineString l2) {
    List<Double> distances = new ArrayList<Double>();
    ILineString longest = l1, other = l2;
    if (l2.length() > l1.length()) {
      longest = l2;
      other = l1;
    }

    for (IDirectPosition pt : other.coord()) {
      distances.add(Distances.distance(pt, longest));
    }

    Collections.sort(distances);
    int nb = distances.size();
    if ((nb % 2) == 0) {
      return distances.get(nb / 2);
    } else {
      int round = new Double(Math.ceil(nb / 2)).intValue();
      return (distances.get(round) + distances.get(round - 1)) / 2;
    }
  }

  /**
   * Nodes.
   */
  public class Node {

    private double prob;
    private double vProb;
    private List<ACluster> path;
    private List<ACluster> states;
    private List<GM_LineString> geometry;

    /**
     * @return
     */
    public double getProb() {
      return this.prob;
    }

    /**
     * @return
     */
    public List<ACluster> getPath() {
      return this.path;
    }

    /**
     * @return
     */
    public List<ACluster> getStates() {
      return this.states;
    }

    /**
     * @return
     */
    public double getVProb() {
      return this.vProb;
    }

    /**
     * @return
     */
    public List<GM_LineString> getGeometry() {
      return this.geometry;
    }

    /**
     * @param prob
     * @param path
     * @param states
     * @param vprob
     * @param geometries
     */
    public Node(double prob, List<ACluster> path, List<ACluster> states, double vprob) {
      this.prob = prob;
      this.path = path;
      this.states = states;
      this.vProb = vprob;
    }
  }

  /**
   * Transitions.
   */
  class Transition {
    private double proba;

    /**
     * @return
     */
    public double getProba() {
      return this.proba;
    }

    /**
     * @param distance
     * @param length
     * @param listeArcs
     * @param proba
     * @param geom
     */
    public Transition(double proba) {
      this.proba = proba;
    }
  }

  public static void main(String args[]) {

    // Test2 test = new Test2();
    // test.init("/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/jacoubet_l93_utf8.shp",
    // "/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/verniquet_l93_utf8_corr.shp"
    // /* "/home/bcostes/Bureau/test2.shp"*/);
    //// test.init("/home/bcostes/Bureau/ref.shp",
    //// "/home/bcostes/Bureau/comp.shp");
    //

    Test2 test = new Test2();
    test.init("/home/bcostes/Bureau/hydro_bdtopo.shp", "/home/bcostes/Bureau/hydro_bdcarto.shp");

    //
    // test.init("/home/bcostes/Bureau/bdcarto_railways.shp",
    // "/home/bcostes/Bureau/osm_railways.shp");

    test.process();
    //

    //
    // IPopulation<IFeature>in = ShapefileReader.read("/home/bcostes/Bureau/test/t.shp");
    // ILineString l1 = new GM_LineString(
    // Operateurs.resampling(in.get(0).getGeom().coord(), 5));
    // ILineString l2 = new GM_LineString(
    // Operateurs.resampling(in.get(1).getGeom().coord(), 5));
    //
    // System.out.println(Math.min(Frechet.discreteFrechet(l1, l2), Frechet.discreteFrechet(l1.reverse(), l2)));
    //
    // System.out.println(test.lineMedianDistance(l2, l1));
    //
    //
    // System.out.println(Distances.ecartSurface(l1, l2));
  }

}
