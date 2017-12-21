package fr.ign.cogit.v2.tag.enrichment.stgeocoding.hmm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Groupe;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.algo.JtsAlgorithms;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.enrichment.stgeocoding.GeocodeType;
import fr.ign.cogit.v2.tag.enrichment.stgeocoding.GeocodedCandidate;
import fr.ign.cogit.v2.tag.enrichment.stgeocoding.STGeocoder;
import fr.ign.cogit.v2.tag.enrichment.stgeocoding.STGeocoderIO;
import fr.ign.cogit.v2.tag.enrichment.stgeocoding.TextualAdress;

public class TestHMM {
  /**
   * Static logger.
   */
  private static final Logger LOGGER = Logger.getLogger(TestHMM.class.getName());

  /**
   * The list of adresses points to match.
   */
  private List<TextualAdress> points = null;

  /**
   * List of geocoded candidates
   */
  private Map<TextualAdress, Set<GeocodedCandidate>> candidatesMapping = null;

  private List<GeocodedCandidate> candidates;

  /**
   * @return the list of gps points to match.
   */
  public List<TextualAdress> getAdresses() {
    return this.points;
  }

  /**
   * Beta describes the difference between route distances and great circle
   * distances.
   */
  private double beta = 0.0;


  private double distanceLimit = 50000.;


  /**
   * The topological map used to match the gps point to.
   */
  protected CarteTopo networkMap = null;

  /**
   * @return The topological map used to match the gps point to.
   */
  public CarteTopo getNetworkMap() {
    return networkMap;
  }

  private Map<GeocodedCandidate, Arc> mappingCandidateEdge;



  public TestHMM(List<TextualAdress> gpsPop,
      STGraph network, Map<TextualAdress, Set<GeocodedCandidate>> candidates) {

    this.points = gpsPop;
    this.candidatesMapping = candidates;
    this.networkMap = new CarteTopo("");
    IPopulation<Arc> arcs = this.networkMap.getPopArcs();
    this.mappingCandidateEdge = new HashMap<GeocodedCandidate, Arc>();
    this.candidates = new ArrayList<GeocodedCandidate>();

    for(STEntity e : network.getEdges()){
      Arc a = arcs.nouvelElement();
      a.setGeom(e.getGeometry().toGeoxGeometry());
    }

    
    this.networkMap.creeTopologieArcsNoeuds(0);
    this.networkMap.rendPlanaire(0.);
    this.calculateBeta();
    
    
    for(TextualAdress t: this.candidatesMapping.keySet()){
      for(GeocodedCandidate c: this.candidatesMapping.get(t)){
        Collection<Arc> aa = this.networkMap.getPopArcs().select(c.pos, 50);
        double dmin = Double.MAX_VALUE;
        Arc amin = null;
        for(Arc a : aa){
          double d = a.getGeom().distance(new GM_Point(c.pos));
          if(d<dmin){
            dmin = d;
            amin = a;
          }
        }
        this.mappingCandidateEdge.put(c, amin);
        if(!this.candidates.contains(c)){
          this.candidates.add(c);
        }
      }
    }


    

  }

  /**
   * Calcul de l'écart moyen entre deux adresses consécutives
   */
  private void calculateBeta(){
    this.beta = 7.;
  }




  /**
   * Compute the transitions and find the best match.
   * @return the {@link Node} containing the best match.
   */
  public Node computeTransitions() {
    Hashtable<GeocodedCandidate, Double> start_p = new Hashtable<GeocodedCandidate, Double>();
    Hashtable<TextualAdress, Hashtable<GeocodedCandidate, Double>> emit_p = new Hashtable<TextualAdress, Hashtable<GeocodedCandidate, Double>>();
    Hashtable<TextualAdress, Collection<GeocodedCandidate>> candidateEdges = new Hashtable<TextualAdress, Collection<GeocodedCandidate>>(this.points.size());
    for (TextualAdress f : this.points) {
      candidateEdges.put(f, new ArrayList<GeocodedCandidate>(0));
    }
    TextualAdress p = this.points.iterator().next();
    Set<GeocodedCandidate> candidates = this.candidatesMapping.get(p);
    //LOGGER.info("start emission probabilities");
    for (GeocodedCandidate a : candidates) {
      double proba = emissionProbability(p, a);
      if (!Double.isInfinite(proba)) {
        start_p.put(a, new Double(proba));
        candidateEdges.get(this.points.iterator().next()).add(a);
      }
    }
    //LOGGER.info("emission probabilities");


    for (TextualAdress ad : this.points) {
      Hashtable<GeocodedCandidate, Double> emit = new Hashtable<GeocodedCandidate, Double>();
      for (GeocodedCandidate f : this.candidatesMapping.get(ad)) {
        emit.put(f, new Double(emissionProbability(ad, f)));
      }
      emit_p.put(ad, emit);     
    }







    //System.out.println("viterbi");
    Node result = forward_viterbi(start_p, emit_p);
    double total = result.getProb();
    List<GeocodedCandidate> path = result.getPath();
    double proba = result.getVProb();
    // System.out.println("total length " + total);
    // System.out.println("path=" + path);
    //System.out.println("proba " + proba);
    return result;
  }

  /**
   * Compute the emission probability between the point p and the given edge
   * arc.
   * @param p a gps point
   * @param arc an edge
   * @return the emission probability
   */
  private double emissionProbability(TextualAdress ad, GeocodedCandidate candidate) {

    //type de géocodage
    GeocodeType type = candidate.type;
    double distanceG = 0f;
    if (type == GeocodeType.EXACT) {
      distanceG = 1f;
    }
    else if (type == GeocodeType.INTERPOLATION) {
      distanceG = 0.9f;
    }
    else if (type == GeocodeType.POOR_INTERPOLATION) {
      distanceG = 0.6f;
    }
    else if (type == GeocodeType.STREET) {
      distanceG = 0.3f;
    }
    else if (type == GeocodeType.STREET_INF) {
      distanceG = 0.2f;
    }
    else if (type == GeocodeType.STREET_SUP) {
      distanceG = 0.2f;
    }
    else if (type == GeocodeType.UNCERTAIN) {
      distanceG = 0.1f;
    }
    //critère temporel

    float maxY = 0.7f;
    float minY = 0.1f;
    float maxX = 50f;
    float minX = 5f;
    float distanceT = (float)Math.abs(FuzzyTemporalInterval.ChengFuzzyRank(candidate.date) -
        ad.getDate());
    if(distanceT <= minX){
      distanceT = maxY;
    }
    else if(distanceT > maxX) {
      distanceT = minY;
    }
    else{
      float a= (maxY - minY)/(minX - maxX);
      float b = maxY - a * minX;
      distanceT = a*distanceT + b;
    }


    double distance = (distanceT) * (distanceG);
    double x = (distance / 1.);
    // double a = 0.5 * x * x;
    //double b = Math.sqrt(2 * Math.PI);
    double proba = distance /*+ Math.log(b)*/;
    //System.out.println("proba e"+" "+ proba);

    return proba;
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
  Transition transitionProbability(TextualAdress p1, TextualAdress p2, GeocodedCandidate g1, GeocodedCandidate g2) {

    if(p1.getNum().equals(p2.getNum()) && g1.equals(g2)){
      return new Transition(0, 0, Double.POSITIVE_INFINITY, null);
    }
    //écart auquel on s'attend entre p1 et p2
    double ecartA = Math.abs(Integer.parseInt(p1.getNum()) - Integer.parseInt(p2.getNum()))/2. * this.beta;
    //calcul de l'écart approximatif sur le réseau entre a1 et a2
    Arc a1 = this.mappingCandidateEdge.get(g1);
    Arc a2 = this.mappingCandidateEdge.get(g2);

    
    IDirectPosition x1 = JtsAlgorithms.getClosestPoint(g1.pos, a1
        .getGeometrie());
    IDirectPosition x2 = JtsAlgorithms.getClosestPoint(g2.pos, a2
        .getGeometrie());
    double distanceRoute = 0;
    List<Arc> arcs = null;
    GM_LineString geom = null;
    if (a1 == a2) {
      distanceRoute = x1.distance(x2);
      arcs = new ArrayList<Arc>(1);
      arcs.add(a1);
      int index1 = Operateurs.insertionIndex(x1, a1.getGeometrie()
          .getControlPoint().getList());
      int index2 = Operateurs.insertionIndex(x2, a1.getGeometrie()
          .getControlPoint().getList());
      boolean reverse = false;
      if (index1 > index2) {
        reverse = true;
        int temp = index1;
        index1 = index2;
        index2 = temp;
      }
      IDirectPositionList l = new DirectPositionList(
          new ArrayList<IDirectPosition>(a1.getGeometrie().getControlPoint()
              .getList().subList(index1, index2)));
      if (reverse) {
        Collections.reverse(l.getList());
      }
      l.add(0, x1);
      l.add(x2);

      geom = new GM_LineString(l);
    } else {
      Groupe pcc = this.networkMap.copie("").shortestPath(x1, x2, a1, a2, g1.pos.distance(g2.pos)
          + this.distanceLimit);
      if (pcc != null) {
        distanceRoute = pcc.getLength();
        geom = (GM_LineString) pcc.getGeom();
        pcc.videEtDetache();
      }
    }
    if (distanceRoute >= g1.pos.distance(g2.pos) + this.distanceLimit) {
      return new Transition(g1.pos.distance(g2.pos), distanceRoute,
          Double.NEGATIVE_INFINITY, geom);
    }
    double ecartT = Math.abs(ecartA - distanceRoute);
    double proba= 1./(ecartT+1);

    //todo: sens des adresses?

    return new Transition(ecartA, distanceRoute, proba, geom);
  }

  /**
   * Run the viterbi algorithm.
   * @param start_p start probabilities
   * @param emit_p emission probabilities
   * @return the best match
   */
  private Node forward_viterbi(Hashtable<GeocodedCandidate, Double> start_p,
      Hashtable<TextualAdress, Hashtable<GeocodedCandidate, Double>> emit_p) {
    Hashtable<GeocodedCandidate, Node> T = new Hashtable<GeocodedCandidate, Node>(start_p.keySet().size());
    // initialize base cases
    for (GeocodedCandidate state : start_p.keySet()) {
      List<GeocodedCandidate> argmax = new ArrayList<GeocodedCandidate>(1);
      List<GeocodedCandidate> states = new ArrayList<GeocodedCandidate>(1);
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
    TextualAdress previous = this.points.get(0);
    Collection<GeocodedCandidate> previousCandidates = start_p.keySet();
    // run viterbi for all observations
    for(int i=1; i< this.points.size(); i++) {
      TextualAdress current = this.points.get(i);
      //LOGGER.info("Point " + i + " = " + current);
      Collection<GeocodedCandidate> candidates =  this.candidatesMapping.get(current);
      Hashtable<GeocodedCandidate, Node> U = new Hashtable<GeocodedCandidate, Node>(candidates.size());
      // LOGGER.info("Candidates " + candidates.size());
      for (GeocodedCandidate nextState : candidates) {
        double total = 0;
        double valmax = Double.NEGATIVE_INFINITY;
        List<GeocodedCandidate> argmax = null;
        List<GeocodedCandidate> states = null;
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
        for (GeocodedCandidate sourceState : previousCandidates) {
          Node node = T.get(sourceState);
          if (node == null) {
            continue;
          }
          double prob = node.getProb();
          List<GeocodedCandidate> v_path = node.getPath();
          List<GeocodedCandidate> v_states = node.getStates();
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
            argmax = new ArrayList<GeocodedCandidate>(v_path);
            states = new ArrayList<GeocodedCandidate>(v_states);
            //            for (GeocodedCandidate a : transProb.listeArcs) {
            //              // if (a != sourceState) {
            //              argmax.add(a);
            //              // }
            //            }
            states.add(nextState);
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
    List<GeocodedCandidate> argmax = null;
    List<GeocodedCandidate> states = null;
    List<GM_LineString> geometries = null;
    double valmax = Double.NEGATIVE_INFINITY;
    // find the best match
    // LOGGER.debug("find the best match");
    for (GeocodedCandidate state : T.keySet()) {
      LOGGER.debug("state " + state);
      Node objs = T.get(state);
      double prob = objs.getProb();
      List<GeocodedCandidate> v_path = objs.getPath();
      List<GeocodedCandidate> v_states = objs.getStates();
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
    // removing junk points
    Collections.reverse(dropped);
    for (Integer index : dropped) {
      this.points.remove(index.intValue());
    }
    return new Node(total, argmax, states, valmax);
  }

  /**
   * Nodes.
   */
  public class Node {

    private double prob;
    private double vProb;
    private List<GeocodedCandidate> path;
    private List<GeocodedCandidate> states;
    /**
     * @return
     */
    public double getProb() {
      return this.prob;
    }
    /**
     * @return
     */
    public List<GeocodedCandidate> getPath() {
      return this.path;
    }
    /**
     * @return
     */
    public List<GeocodedCandidate> getStates() {
      return this.states;
    }
    /**
     * @return
     */
    public double getVProb() {
      return this.vProb;
    }

    /**
     * @param prob
     * @param path
     * @param states
     * @param vprob
     * @param geometries
     */
    public Node(double prob, List<GeocodedCandidate> path, List<GeocodedCandidate> states, double vprob) {
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
    private GM_LineString geometry;
    private double distance;
    private double length;
    private double proba;
    /**
     * @return
     */
    public GM_LineString getGeometry() {
      return this.geometry;
    }
    /**
     * @return
     */
    public double getDistance() {
      return this.distance;
    }
    /**
     * @return
     */
    public double getLength() {
      return this.length;
    }

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
    public Transition(double distance, double length,
        double proba, GM_LineString geom) {
      this.distance = distance;
      this.length = length;
      this.proba = proba;
      this.geometry = geom;
    }
  }



  public static void main(String args[]){

    // TODO Auto-generated method stub
    String host = "127.0.0.1";
    String port = "5432";
    String db = "these_source";
    String login = "postgres";
    String password = "postgres";
    String tableName = "test";

    Set<TextualAdress> add = STGeocoderIO.fromPgsql(host, port, db, login, password, tableName,
        "num_adr", "nom_adr", "date");

    String inputStg ="/home/bcostes/Bureau/stag_json/stag_json.tag";
    STGraph stg = TAGIoManager.deserialize(inputStg);

    STGeocoder stgeocoder = new STGeocoder(stg, add);
    //création d'un dictionnaire des noms de rues (pour les changements de noms)
    Map<String, Set<String>> streetNamesDictionary = STGeocoderIO.getStreetNamesDictionary(stgeocoder);

    Map<TextualAdress, Set<GeocodedCandidate>> candidates = new HashMap<TextualAdress, Set<GeocodedCandidate>>();

    IPopulation<IFeature> out = new Population<IFeature>();
    for(TextualAdress ad : stgeocoder.getTextualAdresses()){
      Set<GeocodedCandidate>  geocodedCandidates = stgeocoder.stgeocode(ad, streetNamesDictionary);
      if(geocodedCandidates == null){
        stgeocoder.ungeocoded.add(ad);
        continue;
      }

      if(geocodedCandidates.isEmpty()){
        //on va essayer avec des synonymes
        geocodedCandidates = stgeocoder.stgeocodeSyn(ad, streetNamesDictionary);
        //si on a des candidats, on les marque comme incertains
        //        for(GeocodedCandidate c: geocodedCandidates){
        //          c.type = GeocodeType.UNCERTAIN;
        //        }
      }
      if(geocodedCandidates.isEmpty()){
        stgeocoder.ungeocoded.add(ad);
        continue;
      }
      if(geocodedCandidates.size()> 500){
        //pas raisonnable
        stgeocoder.ungeocoded.add(ad);
        continue;
      }
      candidates.put(ad, geocodedCandidates);
    }

    for(TextualAdress a: candidates.keySet()){
      for(GeocodedCandidate f: candidates.get(a)){
        IFeature ff = new DefaultFeature();
        ff.setGeom(new GM_Point(f.pos));
        AttributeManager.addAttribute(ff, "num",a.getNum(), "String");
        AttributeManager.addAttribute(ff, "name",a.getName(), "String");
        AttributeManager.addAttribute(ff, "method",f.type, "String");
        AttributeManager.addAttribute(ff, "date",f.date, "String");
        out.add(ff);
      }
    }


    ShapefileWriter.write(out, "/home/bcostes/Bureau/test.shp");

    out.clear();
    System.out.println("OK");


    List<TextualAdress> gpsPop = new ArrayList<TextualAdress>();
    Map<TextualAdress, Set<GeocodedCandidate>> cc = new HashMap<TextualAdress, Set<GeocodedCandidate>>();
    for(TextualAdress ad: candidates.keySet()){
      if(Integer.parseInt(ad.getNum()) %2 == 0){
        gpsPop.add(ad);
        cc.put(ad, candidates.get(ad));
      }
    }
    Collections.sort(gpsPop, new Comparator<TextualAdress>() {
      @Override
      public int compare(TextualAdress o1, TextualAdress o2) {
        if(Integer.parseInt(o1.getNum()) < Integer.parseInt(o2.getNum())){
          return -1;
        }
        else if(Integer.parseInt(o1.getNum()) > Integer.parseInt(o2.getNum())){
          return 1;
        }
        return 0;
      }
    });
    TestHMM hmm = new TestHMM(gpsPop, stg, candidates);
    System.out.println(hmm.getAdresses().size());
    Node n = hmm.computeTransitions();

    System.out.println("gpsPop.size() : " + hmm.getAdresses().size());

    for(int i=0; i<hmm.getAdresses().size(); i++) {
      System.out.println("--------");
      System.out.println(hmm.getAdresses().get(i).getNum()+" "+ hmm.getAdresses().get(i).getName());
      System.out.println(n.getStates().get(i).type +" "+ n.getStates().get(i).name+" "+ n.getStates().get(i).date.toString());
      IFeature ff = new DefaultFeature();
      ff.setGeom(new GM_Point(n.getStates().get(i).pos));
      AttributeManager.addAttribute(ff, "num",hmm.getAdresses().get(i).getNum(), "String");
      AttributeManager.addAttribute(ff, "name",hmm.getAdresses().get(i).getName(), "String");
      AttributeManager.addAttribute(ff, "method",n.getStates().get(i).type, "String");
      AttributeManager.addAttribute(ff, "date",n.getStates().get(i).date, "String");
      out.add(ff);
    }


    gpsPop = new ArrayList<TextualAdress>();
    cc = new HashMap<TextualAdress, Set<GeocodedCandidate>>();
    for(TextualAdress ad: candidates.keySet()){
      if(Integer.parseInt(ad.getNum()) %2 != 0){
        gpsPop.add(ad);
        cc.put(ad, candidates.get(ad));
      }
    }
    Collections.sort(gpsPop, new Comparator<TextualAdress>() {
      @Override
      public int compare(TextualAdress o1, TextualAdress o2) {
        if(Integer.parseInt(o1.getNum()) < Integer.parseInt(o2.getNum())){
          return -1;
        }
        else if(Integer.parseInt(o1.getNum()) > Integer.parseInt(o2.getNum())){
          return 1;
        }
        return 0;
      }
    });
    hmm = new TestHMM(gpsPop, stg, candidates);
    System.out.println(hmm.getAdresses().size());
    n = hmm.computeTransitions();

    System.out.println("gpsPop.size() : " + hmm.getAdresses().size());

    for(int i=0; i<hmm.getAdresses().size(); i++) {
      System.out.println("--------");
      System.out.println(hmm.getAdresses().get(i).getNum()+" "+ hmm.getAdresses().get(i).getName());
      System.out.println(n.getStates().get(i).type +" "+ n.getStates().get(i).name+" "+ n.getStates().get(i).date.toString());
      IFeature ff = new DefaultFeature();
      ff.setGeom(new GM_Point(n.getStates().get(i).pos));
      AttributeManager.addAttribute(ff, "num",hmm.getAdresses().get(i).getNum(), "String");
      AttributeManager.addAttribute(ff, "name",hmm.getAdresses().get(i).getName(), "String");
      AttributeManager.addAttribute(ff, "method",n.getStates().get(i).type, "String");
      AttributeManager.addAttribute(ff, "date",n.getStates().get(i).date, "String");
      out.add(ff);
    }

    ShapefileWriter.write(out, "/home/bcostes/Bureau/test2.shp");

    System.out.println("NO GEOCODED :  " + stgeocoder.getUngeocoded().size());

  }
}
