package hmmmatching.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.contrib.geometrie.Distances;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;
import hmmmatching.impl.ACluster;
import hmmmatching.impl.AClusterCollection;

public class Test {


  private double alpha = 1.;
  private double beta = 1.;
  private double gamma = 1;

  /**
   * Symbole = comp
   * etats cachés = ref
   */

  /**
   * Reference network
   */
  CarteTopo netRef;

  /**
   * Comparisson network: network to be matched
   */
  CarteTopo netComp;

  /**
   * Direct matching : netRef -> netComp
   */
  Map<Arc, ACluster> directMatching;

  /**
   * Direct matching : netComp -> netRef
   */
  Map<Arc,ACluster> reverseMatching;

  /**
   * New edges to be matched
   */
  Set<Arc> unmatched;

  public Test(){
    this.netRef = new CarteTopo("Reference network");
    this.netComp = new CarteTopo("Comparisson network");
    this.directMatching = new HashMap<Arc,ACluster>();
    this.reverseMatching = new HashMap<Arc,ACluster>();
    this.unmatched = new HashSet<Arc>();
    //TODO: switcher netref <=> netcomp en fonction du LOD
  }

  /**
   * Initialization
   * TODO : supprimer cette fonction
   */
  public void init(String netref, String netcomp, String matching, String newedges){
    /*
     * Lectures
     */
    IPopulation<IFeature> inRef = ShapefileReader.read(netref);
    IPopulation<IFeature> inComp = ShapefileReader.read(netcomp);
    IPopulation<IFeature> inmatching = ShapefileReader.read(matching);
    IPopulation<IFeature> inunmatched = ShapefileReader.read(newedges);

    /*
     * Création des réseaux
     */
    IPopulation<Arc> popArcRef = this.netRef.getPopArcs();
    for(IFeature f : inRef){
      Arc a = popArcRef.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      a.addCorrespondant(f);
    }
    IPopulation<Arc> popArcComp = this.netComp.getPopArcs();
    for(IFeature f : inComp){
      Arc a = popArcComp.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      a.addCorrespondant(f);
    }
    this.netRef.creeTopologieArcsNoeuds(0);
    this.netRef.creeNoeudsManquants(0);
    this.netRef.rendPlanaire(0);



    this.netRef.getPopArcs().initSpatialIndex(Tiling.class,false);

    /*
     * Création des liens d'appariement
     */
    Map<Arc, List<Arc>> directMatchingLocal = new HashMap<Arc, List<Arc>>();
    Map<Arc, List<Arc>> reverseMatchingLocal = new HashMap<Arc, List<Arc>>();

    for(IFeature match : inmatching){
      IDirectPosition p1 = match.getGeom().coord().get(0);
      IDirectPosition p2 = match.getGeom().coord().get(1);

      Collection<Arc> candidatesRef = this.netRef.getPopArcs().select(p1,0.001);
      candidatesRef.addAll(this.netRef.getPopArcs().select(p2,0.001));
      Collection<Arc> candidatesComp= this.netComp.getPopArcs().select(p1,0.001);
      candidatesComp.addAll(this.netComp.getPopArcs().select(p2,0.001));
      Arc aref = null;
      Arc acomp = null;
      for(Arc ar: candidatesRef){
        if(ar.getGeom().distance(new GM_Point(p1))<0.001 ||
            ar.getGeom().distance(new GM_Point(p2))<0.001 ){
          aref = ar;
          break;
        }
      }
      for(Arc ar: candidatesComp){
        if(ar.getGeom().distance(new GM_Point(p1))<0.001 ||
            ar.getGeom().distance(new GM_Point(p2))<0.001 ){
          acomp = ar;
          break;
        }
      }
      if(directMatchingLocal.containsKey(aref)){
        directMatchingLocal.get(aref).add(acomp);
      }
      else{
        List<Arc> set = new ArrayList<Arc>();
        set.add(acomp);
        directMatchingLocal.put(aref, set);
      }
      if(reverseMatchingLocal.containsKey(acomp)){
        reverseMatchingLocal.get(acomp).add(aref);
      }
      else{
        List<Arc> set = new ArrayList<Arc>();
        set.add(aref);
        reverseMatchingLocal.put(acomp, set);
      }
    }
    for(Arc a: directMatchingLocal.keySet()){
      this.directMatching.put(a, new ACluster(directMatchingLocal.get(a)));
    }
    for(Arc a: reverseMatchingLocal.keySet()){
      this.reverseMatching.put(a, new ACluster(reverseMatchingLocal.get(a)));
    }

    /*
     * new edges
     */
    for(IFeature f : inunmatched){
      Arc a = this.netComp.getPopArcs().nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      a.addCorrespondant(f);
      this.unmatched.add(a);
    }
    this.netComp.creeTopologieArcsNoeuds(0.0001);
    this.netComp.creeNoeudsManquants(0.0001);
    this.netComp.rendPlanaire(0.001);
    this.netComp.filtreDoublons(0.001);
    this.netComp.getPopArcs().initSpatialIndex(Tiling.class,false);


  }

  public void process(){

    long t0= System.currentTimeMillis();

    /*
     * construction des strokes self fit
     */
    //TODO : pour le moment on considère qu'on a qu'un seul stroke d'ajouté
    List<Arc> stroke = new ArrayList<Arc>();
    Arc astart = null;
    for(Arc a : this.unmatched){
      Set<Arc> neighbors = new HashSet<Arc>();
      neighbors.addAll(a.getNoeudIni().getEntrants());
      neighbors.addAll(a.getNoeudIni().getSortants());
      neighbors.addAll(a.getNoeudFin().getEntrants());
      neighbors.addAll(a.getNoeudFin().getSortants());
      neighbors.removeAll(this.unmatched);
      if(!neighbors.isEmpty()){
        for(Arc nei : neighbors){
          if(!this.reverseMatching.containsKey(nei)){
            continue;
          }
          astart = nei;
          break;
        }
      }
      if(astart != null){
        break;
      }
    }
    
    
    Noeud nodeRef = astart.getNoeudIni();
    boolean nodeOk = false;
    for(Arc a : this.unmatched){
      if(a.getNoeudIni().equals(nodeRef) || a.getNoeudFin().equals(nodeRef)){
        nodeOk = true;
        break;
      }
    }
    if(!nodeOk){
      nodeRef = astart.getNoeudFin();
    }


    while(!this.unmatched.isEmpty()){
      Arc aref2 = null;
      for(Arc aa: this.unmatched){
        if(aa.getNoeudIni().equals(nodeRef) || aa.getNoeudFin().equals(nodeRef)){
          aref2 = aa;
          break;
        }
      }
      stroke.add(aref2);
      if(aref2.getNoeudIni().equals(nodeRef)){
        nodeRef = aref2.getNoeudFin();
      }
      else{
        nodeRef = aref2.getNoeudIni();
      }
      this.unmatched.remove(aref2);
    }

    Node n = this.match(astart, stroke);


    IPopulation<IFeature> out = new Population<IFeature>();
    for(int i=1; i< n.getStates().size(); i++){
      Arc a2 = stroke.get(i-1);
      ACluster a = n.getStates().get(i);
      for(Arc aa: a.getArcs()){
        out.add(new DefaultFeature(new GM_LineString(new DirectPositionList(Arrays.asList(Operateurs.milieu(aa.getGeometrie()),
            Operateurs.milieu(a2.getGeometrie()))))));
      }
    }
    long t= System.currentTimeMillis();

    System.out.println(t-t0 + "ms");
    ShapefileWriter.write(out, "/home/bcostes/Bureau/test0.shp");
  }

  private Node match(Arc start, List<Arc> stroke) {
    Hashtable<ACluster, Double> start_p = new Hashtable<ACluster, Double>();
    // ref, comp
    Hashtable<Arc, Hashtable<ACluster, Double>> emit_p = new Hashtable<Arc, Hashtable<ACluster, Double>>();

    Hashtable<Arc, Collection<Arc>> candidateEdges = new Hashtable<Arc, Collection<Arc>>(
        stroke.size());
    for (Arc a : stroke) {
      candidateEdges.put(a, new ArrayList<Arc>(0));
    }


    start_p.put(this.reverseMatching.get(start), new Double(1.));



    //LOGGER.info("emission probabilities");
    for (Arc arc : stroke) {
      Collection<Arc> candidateArc = this.netRef.getPopArcs().select(arc
          .getGeom(), this.selection);
      //création des agrégats éventuels et filtrage automatique si fusion impossible
      AClusterCollection clustercol = new AClusterCollection(new ArrayList<Arc>(candidateArc));
      for (ACluster cluster : clustercol) {
        double emissionP = emissionProbability(arc, cluster);
        if(emit_p.containsKey(arc)){
          emit_p.get(arc).put(cluster, emissionP);
        }
        else{
          Hashtable<ACluster, Double> emit = new Hashtable<ACluster, Double>();
          emit.put(cluster, emissionP);
          emit_p.put(arc, emit);
        }
      }
    }
    //LOGGER.info("viterbi");
    Node result = forward_viterbi(start,start_p, emit_p, stroke);
    return result;

  }

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
   * Route Localization distance. This parameter is used in order to limit the
   * number of roads we try to match a gps point with.
   */
  private double selection = 40.0;





  /**
   * Compute the emission probability between the point p and the given edge
   * arc.
   * @param p a gps point
   * @param arc an edge
   * @return the emission probability
   */
  private double emissionProbability(Arc a1, ACluster a2) {
    double distance = Math.min(Distances.premiereComposanteHausdorff(a1.getGeometrie(), a2.getGeometrie()),
        Distances.premiereComposanteHausdorff(a2.getGeometrie(), a1.getGeometrie()));

    double a = distance / this.alpha;
    double proba = a + Math.log(this.alpha);
    
    
 
    Angle ori1 = Operateurs.directionPrincipale(Operateurs.resampling(
        a1.getGeometrie(),5).getControlPoint());
    Angle ori2 = Operateurs.directionPrincipale(Operateurs.resampling(
        a2.getGeometrie(),5).getControlPoint());
    double value = Angle.ecart(ori1, ori2).getValeur()*180./Math.PI;          
//  double angleSkeletonV = (ori1.getValeur() > Math.PI/2) ? (Math.PI - ori1.getValeur()) :(ori1.getValeur());
//  double angleEntityV = (ori2.getValeur() > Math.PI/2) ? (Math.PI - ori2.getValeur()) :(ori2.getValeur());
//  double value = Math.abs(angleSkeletonV - angleEntityV)*180./Math.PI;
    
    
    double b = value / this.gamma;
    double proba2 = b + Math.log(this.gamma);
    
    
    double d2 = Math.abs(a1.longueur() - a2.longueur());
    double c = d2 / this.beta;
    double proba3 = c+ Math.log(this.beta);

    return -(proba + proba3 + proba2);
  }

  /**
   * Compute the transition probability corresponding to the probability that p2
   * is matched to a2 given that p1 is matched to a1.
   * @param p1 a gps point
   * @param p2 another gps point
   * @param a1 the edge p1 is matched to
   * @param a2 the edge p2 is matched to
   * @return the transition probability corresponding to the probability that p2
   *         is matched to a2 given that p1 is matched to a1.
   */
  Transition transitionProbability(Arc acomp1, Arc acomp2, ACluster aref1, ACluster aref2) {


    if(!acomp1.getGeometrie().coord().get(0).equals(acomp2.getGeometrie().coord().get(0)) &&
        !acomp1.getGeometrie().coord().get(acomp1.getGeometrie().coord().size()-1).
        equals(acomp2.getGeometrie().coord().get(0)) &&
        !acomp1.getGeometrie().coord().get(0).
        equals(acomp2.getGeometrie().coord().get(acomp2.getGeometrie().coord().size()-1)) &&
        !acomp1.getGeometrie().coord().get(acomp1.getGeometrie().coord().size()-1).equals(
            acomp2.getGeometrie().coord().get(acomp2.getGeometrie().coord().size()-1))){
      return new Transition(Double.NEGATIVE_INFINITY);
    } 

    if(!aref1.getGeometrie().coord().get(0).equals(aref2.getGeometrie().coord().get(0)) &&
        !aref1.getGeometrie().coord().get(aref1.getGeometrie().coord().size()-1).
        equals(aref2.getGeometrie().coord().get(0)) &&
        !aref1.getGeometrie().coord().get(0).
        equals(aref2.getGeometrie().coord().get(aref2.getGeometrie().coord().size()-1)) &&
        !aref1.getGeometrie().coord().get(aref1.getGeometrie().coord().size()-1).equals(
            aref2.getGeometrie().coord().get(aref2.getGeometrie().coord().size()-1))){
      return new Transition(Double.NEGATIVE_INFINITY);
    } 


    for(Arc a1: aref1.getArcs()){
      if(aref2.getArcs().contains(a1)){
        return new Transition(Double.NEGATIVE_INFINITY);
      }
    }
    for(Arc a1: aref2.getArcs()){
      if(aref1.getArcs().contains(a1)){
        return new Transition(Double.NEGATIVE_INFINITY);
      }
    }
    
    if(aref1.equals(aref2)){

      IDirectPosition pcompMiddle = null;
      Angle angle1 = null;
      if(acomp1.getGeometrie().getControlPoint(0).equals(acomp2.getGeometrie().getControlPoint(0))){
        pcompMiddle = acomp1.getGeometrie().getControlPoint(0);
        angle1 =  Angle.angleTroisPoints(
            acomp1.getGeometrie().getControlPoint(1), pcompMiddle, acomp2.getGeometrie().getControlPoint(1));
      }
      else if(acomp1.getGeometrie().getControlPoint(0).equals(acomp2.getGeometrie().getControlPoint(
          acomp2.getGeometrie().getControlPoint().size()-1))){
        pcompMiddle = acomp1.getGeometrie().getControlPoint(0);
        angle1 =  Angle.angleTroisPoints(
            acomp1.getGeometrie().getControlPoint(1), pcompMiddle, acomp2.getGeometrie().getControlPoint(
                acomp2.getGeometrie().getControlPoint().size()-2));
      }
      else if(acomp1.getGeometrie().getControlPoint(acomp1.getGeometrie().getControlPoint().size()-1).equals(acomp2.getGeometrie().getControlPoint(0))){
        pcompMiddle = acomp1.getGeometrie().getControlPoint(acomp1.getGeometrie().getControlPoint().size()-1);
        angle1 =  Angle.angleTroisPoints(
            acomp1.getGeometrie().getControlPoint(acomp1.getGeometrie().getControlPoint().size()-2),
            pcompMiddle, acomp2.getGeometrie().getControlPoint(1));
      }
      else{
        pcompMiddle = acomp1.getGeometrie().getControlPoint(acomp1.getGeometrie().getControlPoint().size()-1);
        angle1 =  Angle.angleTroisPoints(
            acomp1.getGeometrie().getControlPoint(acomp1.getGeometrie().getControlPoint().size()-2),
            pcompMiddle, acomp2.getGeometrie().getControlPoint(acomp2.getGeometrie().getControlPoint().size()-2));
      }

      IDirectPosition pproj = Operateurs.projection(pcompMiddle, aref1.getGeometrie());
      if(pproj.equals(aref1.getGeometrie().getControlPoint(0)) ||
          pproj.equals(aref1.getGeometrie().getControlPoint(aref1.getGeometrie().getControlPoint().size()-1))){

        Angle angle2 = new Angle(0);
        double d = Angle.ecart(angle1, angle2).getValeur();
        double a = (d*180/Math.PI) / this.gamma;
        double proba = a + Math.log(this.gamma);
        return new Transition(-proba);
      }
      else{
        ILineString newline = Operateurs.projectionEtInsertion(pcompMiddle, aref1.getGeometrie());
        IDirectPosition p1 = null, p2 = null;
        for(int i=0; i< newline.coord().size(); i++){
          if(newline.getControlPoint(i).equals(pproj)){
            p1 = newline.getControlPoint(i-1);
            p2 = newline.getControlPoint(i+1);
            break;
          }
        }
        Angle angle2 = Angle.angleTroisPoints(p1, pcompMiddle, p2);
        double d = Angle.ecart(angle1, angle2).getValeur();
        double a = (d*180/Math.PI) / this.gamma;
        double proba = a + Math.log(this.gamma);
        return new Transition(-proba);
      }

    }
    else if(acomp1.equals(acomp2)){
      IDirectPosition prefMiddle = null;
      Angle angle1 = null;
      if(aref1.getGeometrie().getControlPoint(0).equals(aref2.getGeometrie().getControlPoint(0))){
        prefMiddle = aref1.getGeometrie().getControlPoint(0);
        angle1 =  Angle.angleTroisPoints(
            aref1.getGeometrie().getControlPoint(1), prefMiddle, aref2.getGeometrie().getControlPoint(1));
      }
      else if(aref1.getGeometrie().getControlPoint(0).equals(aref2.getGeometrie().getControlPoint(
          aref2.getGeometrie().getControlPoint().size()-1))){
        prefMiddle = acomp1.getGeometrie().getControlPoint(0);
        angle1 =  Angle.angleTroisPoints(
            aref1.getGeometrie().getControlPoint(1), prefMiddle, aref2.getGeometrie().getControlPoint(
                aref2.getGeometrie().getControlPoint().size()-2));
      }
      else if(aref1.getGeometrie().getControlPoint(aref1.getGeometrie().getControlPoint().size()-1).equals(aref2.getGeometrie().getControlPoint(0))){
        prefMiddle = acomp1.getGeometrie().getControlPoint(aref1.getGeometrie().getControlPoint().size()-1);
        angle1 =  Angle.angleTroisPoints(
            aref1.getGeometrie().getControlPoint(aref1.getGeometrie().getControlPoint().size()-2),
            prefMiddle, aref2.getGeometrie().getControlPoint(1));
      }
      else{
        prefMiddle = aref1.getGeometrie().getControlPoint(aref1.getGeometrie().getControlPoint().size()-1);
        angle1 =  Angle.angleTroisPoints(
            aref1.getGeometrie().getControlPoint(aref1.getGeometrie().getControlPoint().size()-2),
            prefMiddle, aref2.getGeometrie().getControlPoint(aref2.getGeometrie().getControlPoint().size()-2));
      }

      IDirectPosition pproj = Operateurs.projection(prefMiddle, acomp1.getGeometrie());
      if(pproj.equals(acomp1.getGeometrie().getControlPoint(0)) ||
          pproj.equals(acomp1.getGeometrie().getControlPoint(acomp1.getGeometrie().getControlPoint().size()-1))){

        Angle angle2 = new Angle(0);
        double d = Angle.ecart(angle1, angle2).getValeur();
        double a = (d*180/Math.PI) / this.gamma;
        double proba = a + Math.log(this.gamma);
        return new Transition(-proba);
      }
      else{
        ILineString newline = Operateurs.projectionEtInsertion(prefMiddle, acomp1.getGeometrie());
        IDirectPosition p1 = null, p2 = null;
        for(int i=0; i< newline.coord().size(); i++){
          if(newline.getControlPoint(i).equals(pproj)){
            p1 = newline.getControlPoint(i-1);
            p2 = newline.getControlPoint(i+1);
            break;
          }
        }
        Angle angle2 = Angle.angleTroisPoints(p1, prefMiddle, p2);
        double d = Angle.ecart(angle1, angle2).getValeur();
        double a = (d*180/Math.PI) / this.gamma;
        double proba = a + Math.log(this.gamma);
        return new Transition(-proba);

      }
    }
    else{
      IDirectPosition prefini1 = aref1.getGeometrie().getControlPoint(0);
      IDirectPosition preffin1= aref1.getGeometrie().getControlPoint(
          aref1.getGeometrie().getControlPoint().size()-1);
      IDirectPosition prefini2 = aref2.getGeometrie().getControlPoint(0);
      IDirectPosition preffin2 = aref2.getGeometrie().getControlPoint(
          aref2.getGeometrie().getControlPoint().size()-1);
      
      IDirectPosition p1 = null, p2 = null, p3 = null;

      if(!prefini1.equals(prefini2) &&
          !prefini1.equals(preffin2) &&
          !preffin1.equals(prefini2)&&
          !preffin1.equals(preffin2)){
        return new Transition(Double.NEGATIVE_INFINITY);
      }

      if(prefini1.equals(prefini2)){
        p1 =  aref1.getGeometrie().getControlPoint(1);
        p2 = aref1.getGeometrie().getControlPoint(0);
        p3 = aref2.getGeometrie().getControlPoint(1);
      }
      else if(prefini1.equals(preffin2)){
        p1 =  aref1.getGeometrie().getControlPoint(1);
        p2 = aref1.getGeometrie().getControlPoint(0);
        p3 = aref2.getGeometrie().getControlPoint(aref2.getGeometrie().getControlPoint().size()-2);
      }
      else if(preffin1.equals(prefini2)){
        p1 = aref1.getGeometrie().getControlPoint( aref1.getGeometrie().getControlPoint().size()-2);
        p2 = aref2.getGeometrie().getControlPoint(0);
        p3 = aref2.getGeometrie().getControlPoint(1);
      }
      else{
        p1 =  aref1.getGeometrie().getControlPoint( aref1.getGeometrie().getControlPoint().size()-2);
        p2 = aref1.getGeometrie().getControlPoint( aref1.getGeometrie().getControlPoint().size()-1);
        p3 = aref2.getGeometrie().getControlPoint(aref2.getGeometrie().getControlPoint().size()-2);
      }
      Angle angle1 = Angle.angleTroisPoints(p1, p2, p3);

      IDirectPosition pcompini1 = acomp1.getGeometrie().getControlPoint(0);
      IDirectPosition pcompfin1 = acomp1.getGeometrie().getControlPoint(
          acomp1.getGeometrie().getControlPoint().size()-1);
      IDirectPosition pcompini2 = acomp2.getGeometrie().getControlPoint(0);
      IDirectPosition pcompfin2 = acomp2.getGeometrie().getControlPoint(
          acomp2.getGeometrie().getControlPoint().size()-1);

      p1 = null;
      p2 = null;
      p3 = null;
      if(pcompini1.equals(pcompini2)){
        p1 =  acomp1.getGeometrie().getControlPoint(1);
        p2 = acomp1.getGeometrie().getControlPoint(0);
        p3 = acomp2.getGeometrie().getControlPoint(1);
      }
      else if(pcompini1.equals(pcompfin2)){
        p1 =  acomp1.getGeometrie().getControlPoint(1);
        p2 = acomp1.getGeometrie().getControlPoint(0);
        p3 = acomp2.getGeometrie().getControlPoint(acomp2.getGeometrie().getControlPoint().size()-2);
      }
      else if(pcompfin1.equals(pcompini2)){
        p1 = acomp1.getGeometrie().getControlPoint( acomp1.getGeometrie().getControlPoint().size()-2);
        p2 = acomp2.getGeometrie().getControlPoint(0);
        p3 = acomp2.getGeometrie().getControlPoint(1);
      }
      else{
        p1 =  acomp1.getGeometrie().getControlPoint( acomp1.getGeometrie().getControlPoint().size()-2);
        p2 = acomp1.getGeometrie().getControlPoint( acomp1.getGeometrie().getControlPoint().size()-1);
        p3 = acomp2.getGeometrie().getControlPoint(acomp2.getGeometrie().getControlPoint().size()-2);
      }
      Angle angle2 = Angle.angleTroisPoints(p1, p2 , p3);
      double d = Angle.ecart(angle1, angle2).getValeur();
      double a = (d*180/Math.PI) / this.gamma;
      double proba = a + Math.log(this.gamma);
      return new Transition(-proba);
    }

  }

  /**
   * Run the viterbi algorithm.
   * @param start_p start probabilities
   * @param emit_p emission probabilities
   * @return the best match
   */
  private Node forward_viterbi(Arc start, Hashtable<ACluster, Double> start_p,
      Hashtable<Arc, Hashtable<ACluster, Double>> emit_p, List<Arc> stroke) {
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
    Arc previous = start;
    Collection<ACluster> previousCandidates = start_p.keySet();
    // run viterbi for all observations
    for (int i = 0; i < stroke.size(); i++) {
      Arc current = stroke.get(i);
      //LOGGER.info("Point " + i + " = " + current);
      Set<ACluster> candidates = emit_p.get(current).keySet();
      Hashtable<ACluster, Node> U = new Hashtable<ACluster, Node>(candidates.size());
      // LOGGER.info("Candidates " + candidates.size());
      for (ACluster nextState : candidates) {
        double total = 0;
        double valmax = Double.NEGATIVE_INFINITY;
        List<ACluster> argmax = null;
        List<ACluster> states = null;
        double emit = Double.NEGATIVE_INFINITY;
        if (emit_p.get(current) != null
            && emit_p.get(current).get(nextState) != null) {
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
          Transition transProb = transitionProbability(previous, current,
              sourceState, nextState);
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
            /*for (Arc a : transProb.listeArcs) {
              // if (a != sourceState) {
              argmax.add(a);
              // }
            }*/
            states.add(nextState);
            //geometries.add(transProb.geometry);
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
      //  LOGGER.debug("total " + total);
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

  public static void main(String args[]){

    
    
    int length = 40;
    int[] bitVector = new int[length + 2];
    for (int i = 0; i <= length + 1; i++) {
      bitVector[i] = 0;
    }
    long t0 = System.currentTimeMillis();
    int cpt =0;
    while(bitVector[length + 1] != 1){
      for (int index = 1; index <= length; index++) {
        if (bitVector[index] == 1) {

        }
      }
      int i = 1;
      while (bitVector[i] == 1) {
        bitVector[i] = 0;
        i++;
      }
      bitVector[i] = 1;
      
      cpt++;
     if(cpt %1000000 == 0){
       System.out.println((System.currentTimeMillis()-t0));
       t0 = System.currentTimeMillis();
       cpt = 0;
     }
    }
    
    System.out.println("done");
    
    
//
//    Test test = new Test();
//    test.init("/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/verniquet_l93_utf8_corr.shp", 
//        "/home/bcostes/Bureau/netcomp.shp", "/home/bcostes/Bureau/matching.shp", "/home/bcostes/Bureau/unmatched.shp");
//
//    test.process();



  }

}
