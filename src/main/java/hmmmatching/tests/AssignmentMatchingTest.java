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
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;

public class AssignmentMatchingTest {

  private double selection = 50;
  private double alpha = 1.;
  private double beta = 1.;
  private double gamma = 1;
  private double penaltyfactor = 1;

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
  private Map<Arc, List<Arc>> matching;

  /**
   * Unmatched edges
   */
  Set<Arc> unmatched;
  public AssignmentMatchingTest(){
    this.netRef = new CarteTopo("Reference network");
    this.netComp = new CarteTopo("Comparisson network");
    this.matching = new HashMap<Arc, List<Arc>>();
    this.unmatched = new HashSet<Arc>();
  }

  /**
   * Initialization
   */
  public void init(String netref, String netcomp){
    /*
     * Lectures
     */
    IPopulation<IFeature> inRef = ShapefileReader.read(netref);
    IPopulation<IFeature> inComp = ShapefileReader.read(netcomp);

    /*
     * Création des réseaux
     */
    IPopulation<Arc> popArcRef = this.netRef.getPopArcs();
    for(IFeature f : inRef){
      Arc a = popArcRef.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      //a.addCorrespondant(f);
    }
    IPopulation<Arc> popArcComp = this.netComp.getPopArcs();
    for(IFeature f : inComp){
      Arc a = popArcComp.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
     // a.addCorrespondant(f);
    }
    this.netRef.creeTopologieArcsNoeuds(0);
    this.netRef.creeNoeudsManquants(0);
    this.netRef.rendPlanaire(0.);
    this.netRef.filtreDoublons(0);
    this.netRef.filtreArcsNull(0);
    this.netRef.filtreArcsDoublons();
    for(Arc a : this.netRef.getPopArcs()){
      a.addCorrespondant(new DefaultFeature((ILineString)a.getGeometrie().clone()));
    }
    this.netRef.filtreNoeudsSimples();
    this.netRef.creeTopologieArcsNoeuds(0);
    this.netRef.rendPlanaire(0);
    this.netRef.filtreNoeudsSimples();
    this.netRef.getPopArcs().initSpatialIndex(Tiling.class,false);
    this.netComp.creeTopologieArcsNoeuds(0);
    this.netComp.creeNoeudsManquants(0);
    this.netComp.rendPlanaire(0);
    this.netComp.filtreDoublons(0);
    this.netComp.filtreArcsNull(0);
    this.netComp.filtreArcsDoublons();
    for(Arc a : this.netComp.getPopArcs()){
      a.addCorrespondant(new DefaultFeature((ILineString)a.getGeometrie().clone()));
    }
    this.netComp.filtreNoeudsSimples();
    this.netComp.creeTopologieArcsNoeuds(0);
    this.netComp.rendPlanaire(0);
    this.netComp.filtreNoeudsSimples();
    this.netComp.getPopArcs().initSpatialIndex(Tiling.class,false);
    //TODO: filtrage des noeuds  simples ?
  }

  /**
   * Run hmm networks matching process
   */
  public void process(){

    Map<Arc,Set<Arc>> candidatesMatching = new HashMap<Arc, Set<Arc>>();
    Map<Arc,Set<Arc>> reverseCandidatesMatching = new HashMap<Arc, Set<Arc>>();



    for(Arc arcRef : this.netRef.getPopArcs()){
      candidatesMatching.put(arcRef, new HashSet<Arc>());
      Collection<Arc> candidates = this.netComp.getPopArcs().select(arcRef.getGeom(), this.selection);
      for(Arc arcComp : candidates){
        double distance = Math.min(Distances.premiereComposanteHausdorff(arcRef.getGeometrie(), arcComp.getGeometrie()),
            Distances.premiereComposanteHausdorff(arcComp.getGeometrie(), arcRef.getGeometrie()));

        if(distance>this.selection){
          continue;
        }
        if(arcRef.getGeometrie().length() > 2* arcComp.getGeometrie().length() ||  
            arcComp.getGeometrie().length() > 2* arcRef.getGeometrie().length()  ){
          if(arcRef.getGeometrie().length()<arcComp.getGeometrie().length()){
            IDirectPosition pref1 = arcRef.getGeometrie().startPoint();
            IDirectPosition pref2 = arcRef.getGeometrie().endPoint();

            List<IDirectPosition> pts = new ArrayList<IDirectPosition>(arcComp.getGeometrie().getControlPoint());
            int  pos1= Operateurs.projectAndInsertWithPosition(pref1,pts);
            int  pos2= Operateurs.projectAndInsertWithPosition(pref2,pts);

            if(pos1 == pos2){
              Angle ori1 = Operateurs.directionPrincipale(Operateurs.resampling(
                  arcRef.getGeometrie(),5).getControlPoint());
              Angle ori2 = Operateurs.directionPrincipale(Operateurs.resampling(
                  arcComp.getGeometrie(),5).getControlPoint());
              double value = Angle.ecart(ori1, ori2).getValeur()*180./Math.PI;          
              //              double angleSkeletonV = (ori1.getValeur() > Math.PI/2) ? (Math.PI - ori1.getValeur()) :(ori1.getValeur());
              //              double angleEntityV = (ori2.getValeur() > Math.PI/2) ? (Math.PI - ori2.getValeur()) :(ori2.getValeur());
              //              double value = Math.abs(angleSkeletonV - angleEntityV)*180./Math.PI;
              if(value < 20){
                candidatesMatching.get(arcRef).add(arcComp);
                if(!reverseCandidatesMatching.containsKey(arcComp)){
                  reverseCandidatesMatching.put(arcComp, new HashSet<Arc>());
                }
                reverseCandidatesMatching.get(arcComp).add(arcRef);
              }
              continue;
            }

            List<IDirectPosition> ptsExtract = null;
            if(pos1>pos2){
              ptsExtract = pts.subList(pos2, pos1+1);
            }
            else{
              ptsExtract = pts.subList(pos1, pos2+1);
            }
            ILineString newline = new GM_LineString(new DirectPositionList(ptsExtract));
            if(newline.length()<0.25*arcRef.getGeometrie().length()){
              //perpendicular lines probably
              continue;
            }

            Angle ori1 = Operateurs.directionPrincipale(Operateurs.resampling(
                arcRef.getGeometrie(),5).getControlPoint());
            Angle ori2 = Operateurs.directionPrincipale(Operateurs.resampling(
                newline,5).getControlPoint());
            double value = Angle.ecart(ori1, ori2).getValeur()*180./Math.PI;          
            //          double angleSkeletonV = (ori1.getValeur() > Math.PI/2) ? (Math.PI - ori1.getValeur()) :(ori1.getValeur());
            //          double angleEntityV = (ori2.getValeur() > Math.PI/2) ? (Math.PI - ori2.getValeur()) :(ori2.getValeur());
            //          double value = Math.abs(angleSkeletonV - angleEntityV)*180./Math.PI;
            if(value > 20){
              continue;
            }
            candidatesMatching.get(arcRef).add(arcComp);
            if(!reverseCandidatesMatching.containsKey(arcComp)){
              reverseCandidatesMatching.put(arcComp, new HashSet<Arc>());
            }
            reverseCandidatesMatching.get(arcComp).add(arcRef);
            continue;
          }
          else{
            IDirectPosition pref1 = arcComp.getGeometrie().startPoint();
            IDirectPosition pref2 = arcComp.getGeometrie().endPoint();

            List<IDirectPosition> pts = new ArrayList<IDirectPosition>(arcRef.getGeometrie().getControlPoint());
            int  pos1= Operateurs.projectAndInsertWithPosition(pref1,pts);
            int  pos2= Operateurs.projectAndInsertWithPosition(pref2,pts);

            if(pos1 == pos2){
              Angle ori1 = Operateurs.directionPrincipale(Operateurs.resampling(
                  arcRef.getGeometrie(),5).getControlPoint());
              Angle ori2 = Operateurs.directionPrincipale(Operateurs.resampling(
                  arcComp.getGeometrie(),5).getControlPoint());
              double value = Angle.ecart(ori1, ori2).getValeur()*180./Math.PI;          
              //            double angleSkeletonV = (ori1.getValeur() > Math.PI/2) ? (Math.PI - ori1.getValeur()) :(ori1.getValeur());
              //            double angleEntityV = (ori2.getValeur() > Math.PI/2) ? (Math.PI - ori2.getValeur()) :(ori2.getValeur());
              //            double value = Math.abs(angleSkeletonV - angleEntityV)*180./Math.PI;
              if(value < 20){
                candidatesMatching.get(arcRef).add(arcComp);
                if(!reverseCandidatesMatching.containsKey(arcComp)){
                  reverseCandidatesMatching.put(arcComp, new HashSet<Arc>());
                }
                reverseCandidatesMatching.get(arcComp).add(arcRef);
              }
              continue;
            }

            List<IDirectPosition> ptsExtract = null;
            if(pos1>pos2){
              ptsExtract = pts.subList(pos2, pos1+1);
            }
            else{
              ptsExtract = pts.subList(pos1, pos2+1);
            }
            ILineString newline = new GM_LineString(new DirectPositionList(ptsExtract));
            if(newline.length()<0.25*arcComp.getGeometrie().length()){
              //perpendicular lines probably
              continue;
            }

            Angle ori1 = Operateurs.directionPrincipale(Operateurs.resampling(
                arcComp.getGeometrie(),5).getControlPoint());
            Angle ori2 = Operateurs.directionPrincipale(Operateurs.resampling(
                newline,5).getControlPoint());
            double value = Angle.ecart(ori1, ori2).getValeur()*180./Math.PI;          
            //          double angleSkeletonV = (ori1.getValeur() > Math.PI/2) ? (Math.PI - ori1.getValeur()) :(ori1.getValeur());
            //          double angleEntityV = (ori2.getValeur() > Math.PI/2) ? (Math.PI - ori2.getValeur()) :(ori2.getValeur());
            //          double value = Math.abs(angleSkeletonV - angleEntityV)*180./Math.PI;
            if(value > 20){
              continue;
            }
            candidatesMatching.get(arcRef).add(arcComp);
            if(!reverseCandidatesMatching.containsKey(arcComp)){
              reverseCandidatesMatching.put(arcComp, new HashSet<Arc>());
            }
            reverseCandidatesMatching.get(arcComp).add(arcRef);
            continue;
          }
        }
        else{

          Angle ori1 = Operateurs.directionPrincipale(Operateurs.resampling(
              arcRef.getGeometrie(),5).getControlPoint());
          Angle ori2 = Operateurs.directionPrincipale(Operateurs.resampling(
              arcComp.getGeometrie(),5).getControlPoint());
          double value = Angle.ecart(ori1, ori2).getValeur()*180./Math.PI;          
          //        double angleSkeletonV = (ori1.getValeur() > Math.PI/2) ? (Math.PI - ori1.getValeur()) :(ori1.getValeur());
          //        double angleEntityV = (ori2.getValeur() > Math.PI/2) ? (Math.PI - ori2.getValeur()) :(ori2.getValeur());
          //        double value = Math.abs(angleSkeletonV - angleEntityV)*180./Math.PI;
          if(value > 20){
            continue;
          }

          candidatesMatching.get(arcRef).add(arcComp);
          if(!reverseCandidatesMatching.containsKey(arcComp)){
            reverseCandidatesMatching.put(arcComp, new HashSet<Arc>());
          }
          reverseCandidatesMatching.get(arcComp).add(arcRef);
        }
      }
    }


    //arccomp
    UndirectedSparseMultigraph<Arc, Integer> graph = new UndirectedSparseMultigraph<Arc, Integer>();
    int cptEdges = 0;
    for(Arc arcRef: candidatesMatching.keySet()){
      if(candidatesMatching.get(arcRef).isEmpty()){
        continue;
      }
      if(candidatesMatching.get(arcRef).size() == 1){
        graph.addVertex(candidatesMatching.get(arcRef).iterator().next());
      }
      else{
        Iterator<Arc> pit = candidatesMatching.get(arcRef).iterator();
        Arc a1 = pit.next();
        while(pit.hasNext()){
          Arc a2 = pit.next();
          graph.addEdge(cptEdges++, a1, a2);
        }
      }
    }
    //on regroupe par composantes connexes
    System.out.println("Building connected components");
    ConnectedComponents<Arc, Integer> cc = new ConnectedComponents<Arc, Integer>(graph);
    List<UndirectedSparseMultigraph<Arc, Integer>> connectedComponents = cc.buildConnectedComponents();
    System.out.println("Connected components built");
    int cptC = 0;
    
    for(UndirectedSparseMultigraph<Arc, Integer> connectedComponent: connectedComponents){
      cptC ++;
      System.out.println(cptC+"/"+connectedComponents.size() + " cc processed ...");
      //pour chaque graphe d'appariement
      if(connectedComponent.getVertexCount() == 0){
        //WTF ?
        continue;
      }
      Set<Arc> candidates= new HashSet<Arc>();
      //les arcscomp concernés
      candidates.addAll(connectedComponent.getVertices());
      
      
      //les arcsRef
      Set<Arc> references= new HashSet<Arc>();
      for(Arc arcComp: candidates){
        references.addAll(reverseCandidatesMatching.get(arcComp));
      }
      

      
      //on fait les ACluster
      AClusterCollection clusterColRef = new AClusterCollection(new ArrayList<Arc>(references));
      AClusterCollection clusterColComp = new AClusterCollection(new ArrayList<Arc>(candidates));
      Map<Integer, ACluster> indexesRef= new HashMap<Integer, ACluster>();
      Map<Integer, ACluster> indexesComp = new HashMap<Integer, ACluster>();
      int cptRef=0;
      int cptComp = 0;
      
      LocalHypergraph hypergraph = new LocalHypergraph();

      for(ACluster clusterRef: clusterColRef){
        indexesRef.put(cptRef++, clusterRef);
        hypergraph.getHypervertices().addAll(clusterRef.getArcs());
      }
      for(ACluster clusterComp: clusterColComp){
        indexesComp.put(cptComp++, clusterComp);
        hypergraph.getHypervertices().addAll(clusterComp.getArcs());
      }



      Map<Integer,Set<Arc>> indexHyperArcs = new HashMap<Integer,Set<Arc>>();
      int cpt = 0;
      if(indexesComp.size()== 0 || indexesRef.size() ==0){
        continue;
      }
      

      
      for(int i=0; i< indexesRef.keySet().size(); i++){
        ACluster clusterRef = indexesRef.get(i);
        for(int j=0;j< indexesComp.size(); j++){
          ACluster clusterComp = indexesComp.get(j);
          //création d'un hyperarc
          Set<Arc> newHypArc = new HashSet<Arc>();
          newHypArc.addAll(clusterRef.getArcs());
          newHypArc.addAll(clusterComp.getArcs());
          indexHyperArcs.put( cpt++, newHypArc);
          hypergraph.getHyperedges().add(newHypArc);

          boolean ok = true;
          for(Arc aref: clusterRef.getArcs()){
            if(!candidatesMatching.get(aref).containsAll(clusterComp.getArcs())){
              ok = false;
              break;
            }
          }
          if(!ok){
            hypergraph.getCosts().put(newHypArc, Double.MIN_VALUE);
            continue;
          }

          if(clusterRef.getGeometrie().distance(clusterComp.getGeometrie())>this.selection){
            hypergraph.getCosts().put(newHypArc, Double.MIN_VALUE);
            continue;
          }

          double distance = Distances.hausdorff(clusterRef.getGeometrie(), clusterComp.getGeometrie());
          double proba = Math.exp(-distance / this.alpha);
          Angle ori1 = Operateurs.directionPrincipale(Operateurs.resampling(
              clusterRef.getGeometrie(),5).getControlPoint());
          Angle ori2 = Operateurs.directionPrincipale(Operateurs.resampling(
              clusterComp.getGeometrie(),5).getControlPoint());
          double value = Angle.ecart(ori1, ori2).getValeur()*180./Math.PI;          
          //        double angleSkeletonV = (ori1.getValeur() > Math.PI/2) ? (Math.PI - ori1.getValeur()) :(ori1.getValeur());
          //        double angleEntityV = (ori2.getValeur() > Math.PI/2) ? (Math.PI - ori2.getValeur()) :(ori2.getValeur());
          //        double value = Math.abs(angleSkeletonV - angleEntityV)*180./Math.PI;
          double proba2 = Math.exp(-value/ this.gamma);
          double d2 = Math.abs(clusterRef.longueur() - clusterComp.longueur());
          double proba3 = Math.exp(-d2/ this.beta);

          //on diminue le score des "longs" aggrégats
                    double penalty = Math.exp(
                        -Math.min(clusterRef.getArcs().size(),  clusterComp.getArcs().size())/ this.penaltyfactor);
          double probaT = proba * proba2 * proba3 * penalty;
          hypergraph.getCosts().put(newHypArc, probaT);
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
      for(Arc vertex: hypergraph.getHypervertices()){
        LPWizardConstraint lpwc = lpw.addConstraint("c"+cpt,1,">=");
        for(Integer i: indexHyperArcs.keySet()){
          Set<Arc> hyparc = indexHyperArcs.get(i);
          if(hyparc.contains(vertex)){
            String var = "x" + i; 
            lpwc.plus(var, 1.);
          }
        }
        cpt++;
      }

      LPSolution sol = lpw.solve();

      for(int i=0; i< indexHyperArcs.size(); i++){
        if(sol.getBoolean("x"+i)){
          Set<Arc> hyparc = indexHyperArcs.get(i);
          List<Arc> arcref = new ArrayList<Arc>();
          List<Arc> arccomp = new ArrayList<Arc>();
          for(Arc vertex: hyparc){
            if(references.contains(vertex)){
              arcref.add(vertex);
            }
            else{
              arccomp.add(vertex);
            }
          }

          for(Arc aref: arcref){
            if(this.matching.containsKey(aref)){
              this.matching.get(aref).addAll(arccomp);
            }
            else{
              this.matching.put(aref, arccomp);
            }

          }
        }
      }

      //      HungarianAlgorithm ha = new HungarianAlgorithm(array);
      //      System.out.println("Hungarian algorithme : " + array.length+" * " + array[0].length );
      //      int result[] = ha.execute();
      //      System.out.println("ha done");
      //      for(int i=0; i< result.length; i++){
      //        ACluster ref = indexesRef.get(i);
      //        if(result [i] == -1 ){
      //          continue;
      //        }
      //        ACluster bv = indexesComp.get(result[i]);
      //        for(Arc aref: ref.getArcs()){
      //          if(this.matching.containsKey(aref)){
      //            this.matching.get(aref).addAll(bv.getArcs());
      //          }
      //          else{
      //            this.matching.put(aref, bv.getArcs());
      //          }
      //        }
      //      }
    }


    for(Arc a: this.netRef.getPopArcs()){
      if(!this.matching.containsKey(a)){
        this.unmatched.add(a);
      }
    }

    System.out.println("Matched : "  +this.matching.keySet().size());
    System.out.println("Unmatched : "+ this.unmatched.size());


    IPopulation<IFeature> out = new Population<IFeature>();
    for(Arc a: matching.keySet()){
      IDirectPosition p1 =Operateurs.milieu(a.getGeometrie());
      for(Arc a2: matching.get(a)){
        IDirectPosition p2 =Operateurs.projection(p1,a2.getGeometrie());
        if(p2.equals(a2.getGeometrie().getControlPoint(0)) || p2.equals(
            a2.getGeometrie().getControlPoint(a2.getGeometrie().getControlPoint().size()-1))){
          p2 = Operateurs.milieu(a2.getGeometrie());
          IDirectPosition p3 =Operateurs.projection(p2, a.getGeometrie()); 
          if(!p3.equals(a.getGeometrie().getControlPoint(0)) && !p3.equals(
              a.getGeometrie().getControlPoint(a.getGeometrie().getControlPoint().size()-1))){
            p1  = p3;
          }
        }
        IFeature f = new DefaultFeature(new GM_LineString(new DirectPositionList(Arrays.asList(p1,p2))));
        out.add(f);
      }
    }
    ShapefileWriter.write(out, "/home/bcostes/Bureau/test_HA30.shp");
    out.clear();
    for(Arc a : unmatched){
      out.add(a);
    }
    ShapefileWriter.write(out, "/home/bcostes/Bureau/notmatched_HA.shp");
  }

  class LocalHypergraph{
    private Set<Arc> hypvertices;
    private Set<Set<Arc>> hypedges;
    private Map<Set<Arc>, Double> costs;

    public LocalHypergraph(){
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

  public static void main(String args[]){


    AssignmentMatchingTest test = new AssignmentMatchingTest();
    test.init("/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/verniquet_l93_utf8_corr.shp", 
        "/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/jacoubet_l93_utf8.shp");

    test.process();



  }

}
