package fr.ign.cogit.v2.tag.enrichment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IEnvelope;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IPoint;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Chargeur;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.matching.hmmm.HMMMapMatcher;
import fr.ign.cogit.geoxygene.matching.hmmm.HMMMapMatcher.Node;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.morphogenesis.network.utils.ConnectedComponents;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;


/**
 * Apparie des points adresses  + date avec leur stedge correspondant.
 * 1: filtrage temporel des stedges
 * 2: regroupement des points adresse par nom + critère distancd (car parfois plusieurs rues
 * ont le même nom)
 * 3: chaque groupe de points est considéré comme une trace gps
 * TODO: faut-il remettre les points dans l'ordre ?
 * 4: on lance l'algo HMMM
 * 5 : on récupère les stentity appariés
 * @author bcostes
 *
 */
public class AdressMapMatching{

  /**
   * STGraph used for the matching process
   */
  private STGraph stag;
  /**
   * Polygons to match with STEdges of STGraph
   */
  private Set<AdressPoint> adressesDataset;
  /**
   * Matching result: each STEdge is matched with one or several adresses.
   */
  private Map<STEntity, Set<AdressPoint>> matching;
  /**
   * Unmatched adresses
   */
  private Set<AdressPoint> unmatched;

  private String streetNameAttribute;
  private String adresseNumAttribute;

  /**
   * Paramètres du mapmatching
   */
  private final double sigmaZ = 5.;
  private final  double selection = 30.;
  private final  double beta = 0.25;
  private final  double distanceLimit = 20.;




  public static String s ="";

  /**
   * Seuil au dela duquel on considère qu'on a plus une seule "trace gps"
   */
  private final double euclidean_thresold = 150;


  public AdressMapMatching(STGraph stag, String adressesSourceFile, FuzzyTemporalInterval date,
      String streetNameAttribute, String adresseNumAttribute){
    this.stag = stag;
    this.adressesDataset = new HashSet<AdressPoint>();
    this.matching = new HashMap<STEntity, Set<AdressPoint>>();
    this.unmatched = new HashSet<AdressPoint>();
    this.streetNameAttribute = streetNameAttribute;
    this.adresseNumAttribute = adresseNumAttribute;
    this.loadAdresses(adressesSourceFile, date);
  }


  /**
   * Regroupe les points adresses pour constituer des "traces gps"
   * Utilise le nom de la rue et un critère de distance
   * @return
   */
  private Set<Set<AdressPoint>> clustering(){
    // On commence par regrouper par nom
    Map<String, Set<AdressPoint>> firstClusters = new HashMap<String, Set<AdressPoint>>();
    for(AdressPoint ad: this.adressesDataset){
      String name = ad.getStreetName().toLowerCase();
      boolean found = false;
      for(String otherName: firstClusters.keySet()){
        //TODO: améliorer la sensibilité de l'algo à la distance toponymique
        // ici on utilise simplement la distance de levenshtein
        //if(LDist.compute(name, otherName) <= 0.05){
        if(name.equals(otherName)){
          //on considère que c'est le même nom
          firstClusters.get(otherName).add(ad);
          found = true;
          break;
        }
      }
      if(!found){
        Set<AdressPoint> set = new HashSet<AdressPoint>();
        set.add(ad);
        firstClusters.put(name, set);
      }
    }

    //on va maintenant filtrer par distance
    Set<Set<AdressPoint>>finalClusters = new HashSet<Set<AdressPoint>>();

    int cpt2=0;

    for(String name : firstClusters.keySet()){

      cpt2++;
      System.out.println("clustering : " + cpt2+ " / "+ firstClusters.keySet().size());

      if(firstClusters.get(name).size() == 1){
        this.unmatched.add(firstClusters.get(name).iterator().next());
        continue;
      }

      UndirectedSparseMultigraph<AdressPoint, Integer> proximityGraph = new UndirectedSparseMultigraph<AdressPoint, Integer>();
      int cpt = 0;
      for(AdressPoint ad: firstClusters.get(name)){
        for(AdressPoint ad2: firstClusters.get(name)){
          if(ad.equals(ad2)){
            continue;
          }
          double d = ad.getGeom().distance(ad2.getGeom());
          if(d<=this.euclidean_thresold){
            proximityGraph.addEdge(cpt++, ad, ad2);

          }
        }
      }
      //composantes connexes
      ConnectedComponents<AdressPoint, Integer> cc= new ConnectedComponents<AdressPoint, Integer>(proximityGraph);
      if(proximityGraph.getVertexCount() == 0){
        continue;
      }
      List<UndirectedSparseMultigraph<AdressPoint, Integer>> ccc = cc.buildConnectedComponents();
      for(UndirectedSparseMultigraph<AdressPoint, Integer> g: ccc){
        Set<AdressPoint> pts = new HashSet<AdressPoint>();
        pts.addAll(g.getVertices());
        finalClusters.add(pts);
      }
    }
    return finalClusters;
  }

  /**
   * Load adresses point from shapefile
   * @param shp
   */
  private void loadAdresses(String shp, FuzzyTemporalInterval date){
    IPopulation<IFeature> pts = ShapefileReader.read(shp);
    for(IFeature pt: pts){
      AdressPoint ad = new AdressPoint(pt.getGeom());
      ad.setDate(date);
      String name = pt.getAttribute(this.streetNameAttribute).toString();
      String text = pt.getAttribute(this.adresseNumAttribute).toString();
      text=text.replaceAll("\\D+","");
      if(text.equals("")){
        continue;
      }
      int num = Integer.parseInt(text);
      ad.setStreetName(name);
      ad.setNum(num);
      this.adressesDataset.add(ad);
    }
  }




  class MapMatching extends HMMMapMatcher{

    public MapMatching(IFeatureCollection<? extends IFeature> gpsPop,
        IFeatureCollection<? extends IFeature> network, double sigmaZ,
        double selection, double beta, double distanceLimit) {
      super(gpsPop, network, sigmaZ, selection, beta, distanceLimit);
    }

    @Override
    protected void importNetwork(IFeatureCollection<? extends IFeature> network) {
      // TODO Auto-generated method stub
      Chargeur.importAsEdges(network, this.getNetworkMap(), "",
          null, "", null, null, 0.0);
    }

  }


  /**
   * Recalade des points sur le stedge apparié
   * Simple translation: pas de déformation des points appariés à un meme stedge entre eux
   * Calcul local de la translation: fait pour chaque stedge
   * @param cluster
   * @param edge
   */
  public void realignment(Set<AdressPoint> cluster, STEntity edge){

    //boundary
    IFeatureCollection<IFeature> pop = new Population<IFeature>();
    pop.addAll(cluster);
    IEnvelope env= pop.getEnvelope();
    // calcul de la translation
    double xeven =0, yeven=0, xuneven =0, yuneven =0;
    int cpteven=0, cptuneven = 0;
    for(AdressPoint ad: cluster){
      if(ad.getNum() %2 == 0){
        //numéro pair
        cpteven++;
        //Projection
        IDirectPosition proj = Operateurs.projection(ad.getGeom().coord().get(0),
            (ILineString)edge.getGeometry().toGeoxGeometry());
        xeven += (proj.getX() - ad.getGeom().coord().get(0).getX());
        yeven += (proj.getY() - ad.getGeom().coord().get(0).getY());
      }
      else{
        //numéro impair
        cptuneven++;
        //Projection
        IDirectPosition proj = Operateurs.projection(ad.getGeom().coord().get(0),
            (ILineString)edge.getGeometry().toGeoxGeometry());
        xuneven += (proj.getX() - ad.getGeom().coord().get(0).getX());
        yuneven += (proj.getY() - ad.getGeom().coord().get(0).getY());
      }
    }
    xeven /=((double)cpteven);
    yeven /=((double)cpteven);
    xuneven /=((double)cptuneven);
    yuneven /=((double)cptuneven);

    if(cpteven == 0 || cptuneven == 0){
      return;
    }

    //paramètres translation
    double tx = 0.5*(xeven + xuneven);
    double ty = 0.5*(yeven + yuneven);

    //translation
    for(AdressPoint ad: cluster){
      IPoint newPt = new GM_Point(new DirectPosition(ad.getGeom().coord().get(0).getX() + tx,
          ad.getGeom().coord().get(0).getY() + ty));
      if(!env.contains(newPt)){
        continue;
      }


      s+=tx +";"+ +ty + "\n";
      ad.setGeom(newPt);
    }

  }

  /**
   * Main map matching process of adresses data with stag objects
   */
  public void match(){

    IPopulation<IFeature> out = new Population<IFeature>();



    //  domaine d'existence de l'étude
//    List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>();
//    times.addAll(this.stag.getTemporalDomain().asList());
//    // qu'on trie dans l'ordre chronologique croissant
//    Collections.sort(times);
//    //on récupère le snapshot le plus proche
//    final double trank = FuzzyTemporalInterval.ChengFuzzyRank(this.adressesDataset.iterator().next().getDate());
//    FuzzyTemporalInterval tclosest= null;
//    double dmin = Double.MAX_VALUE;
//    for(FuzzyTemporalInterval t2: times){
//      double d =Math.abs(trank - FuzzyTemporalInterval.ChengFuzzyRank(t2));
//      if(d<dmin){
//        dmin = d;
//        tclosest = t2;
//      }
//    }
    //on modifie la géométrie des stedge en fusionnant en privilégiant tclosest
    for(FuzzyTemporalInterval t: stag.getTemporalDomain().asList()){
      stag.getAccuracies().setValueAt(t, 1.0);
    }
    //stag.getAccuracies().setValueAt(tclosest, 100000.);
    stag.updateGeometries();

    //************************************************************************************
    //***************** 1 ere étape : filtrage temporel des candidats ********************
    //************************************************************************************
//    Set<FuzzyTemporalInterval> possibleDates = new HashSet<FuzzyTemporalInterval>();
//    FuzzyTemporalInterval dateAdresses = this.adressesDataset.iterator().next().getDate();
//    // on cherche où tombe la date du polygone dans la liste times
//    double tmin = dateAdresses.getX(0);
//    double tmax = dateAdresses.getX(dateAdresses.size()-1);
//    int indexMin = -1, indexMax = -1;
//    for(int i=0; i< times.size(); i++){
//      if(!dateAdresses.nonIntersectionTest(times.get(i))){
//        if(indexMin == -1){
//          indexMin = Math.max(0, i-1);
//        }
//        indexMax = Math.min(times.size()-1, i+1);
//      }
//    }
//    if(indexMin == -1){
//      //pas d'intersection !
//      for(int i=0; i< times.size(); i++){
//        if(i != times.size() -1){
//          if(times.get(i).getX(times.get(i).size()-1) < tmin && times.get(i+1).getX(0)> tmax){
//            indexMin = i;
//            indexMax = i+1;
//            break;
//          }
//        }
//        else{
//          indexMin = i;
//          indexMax = indexMin;
//        }
//      }
//    }
//    for(int i=indexMin; i<=indexMax; i++){
//      possibleDates.add(times.get(i));
//    }
//    Set<STEntity> edges = new HashSet<STEntity>();
//    for(FuzzyTemporalInterval t: possibleDates){
//      edges.addAll(this.stag.getEdgesAt(t));
//    }
    //transformation en feature
    Map<IFeature, STEntity> mapFeaturesEntities = new HashMap<IFeature, STEntity>();
    for(STEntity e: stag.getEdges()){
      IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
      mapFeaturesEntities.put(f, e);
    }



    //************************************************************************************
    //***************** 2 eme étape : clustering des points adresses *********************
    //************************************************************************************
    Set<Set<AdressPoint>> clusters = this.clustering();


    //************************************************************************************
    //***************** 3 eme étape : matching des points adresses ***********************
    //************************************************************************************




    IFeatureCollection<IFeature> network = new Population<IFeature>();
    network.addAll(mapFeaturesEntities.keySet());

    MapMatching mapM = new MapMatching(new Population<IFeature>(), network, sigmaZ,
        selection, beta, distanceLimit);

    int cpt=0;

    for(Set<AdressPoint> cluster: clusters){     
      cpt++;
      System.out.println(cpt+" / " + clusters.size());
      //il faut remettre la trace dans l'ordre
      IFeatureCollection<IFeature> trace = new Population<IFeature>();
      IFeatureCollection<IFeature> traceReverse = new Population<IFeature>();

      //on commence par les numéro pairs
      List<AdressPoint> list = new ArrayList<AdressPoint>();
      List<AdressPoint> listReverse = new ArrayList<AdressPoint>();

      for(AdressPoint pt: cluster){
        int num = pt.getNum();
        if(num %2 != 0){
          continue;
        }
        if(list.isEmpty()){
          list.add(pt);
          continue;
        }
        if(list.get(0).getNum()> pt.getNum()){
          list.add(0, pt);
          continue;
        }
        for(int i =0; i< list.size()-1; i++){
          if(list.get(i).getNum() < pt.getNum() &&
              list.get(i+1).getNum() >= pt.getNum()){
            list.add(i+1, pt);
            break;
          }
        }
        if(!list.contains(pt)){
          list.add(pt);
        }
      }
      
      trace.addAll(list);
      listReverse.addAll(list);
      
      
      //numéro pairs
      list = new ArrayList<AdressPoint>();
      for(AdressPoint pt: cluster){
        int num = pt.getNum();
        if(num %2 == 0){
          continue;
        }
        if(list.isEmpty()){
          list.add(pt);
          continue;
        }
        if(list.get(0).getNum()> pt.getNum()){
          list.add(0, pt);
          continue;
        }
        for(int i =0; i< list.size()-1; i++){
          if(list.get(i).getNum() < pt.getNum() &&
              list.get(i+1).getNum() >= pt.getNum()){
            list.add(i+1, pt);
            break;
          }
        }
        if(!list.contains(pt)){
          list.add(pt);
        }
      }
      Collections.reverse(list);
      trace.addAll(list);
      
      listReverse.addAll(list);
      Collections.reverse(listReverse);
      traceReverse.addAll(listReverse);
      

      mapM.getPoints().clear();
      IFeatureCollection<? extends IFeature> popPts = mapM.getPoints();
      @SuppressWarnings("unchecked")
      Population<IFeature> p = (Population<IFeature>)popPts;
      p.addAll(trace);
      
      
      Map<AdressPoint, Arc> mapDirect = new HashMap<AdressPoint, Arc>();


      Node result = mapM.computeTransitions();

      if(result == null || result.getStates() == null || result.getStates().isEmpty()){
        this.unmatched.addAll(cluster);
        continue;
      }

      //            for(ILineString l :result.getGeometry()){
      //            out.add(new DefaultFeature(l));
      //            }
      //            ShapefileWriter.write(out, "/home/bcostes/Bureau/test.shp");

      // récup de l'arc apparié
      for(int i=0; i< mapM.getPoints().size(); i++){
        AdressPoint pt = (AdressPoint)mapM.getPoints().get(i);
        Arc matchedArc = result.getStates().get(i);
        mapDirect.put(pt, matchedArc);        
      }
      
      @SuppressWarnings("unchecked")
      Population<IFeature> preverse = (Population<IFeature>)popPts;
      preverse.clear();
      preverse.addAll(traceReverse);
      result = mapM.computeTransitions();
      
      if(result == null || result.getStates() == null || result.getStates().isEmpty()){
        this.unmatched.addAll(cluster);
        continue;
      }
      
      
      Map<AdressPoint, Arc> mapReverse = new HashMap<AdressPoint, Arc>();
     
      // récup de l'arc apparié
      for(int i=0; i< mapM.getPoints().size(); i++){
        AdressPoint pt = (AdressPoint)mapM.getPoints().get(i);
        Arc matchedArc = result.getStates().get(i);
        mapReverse.put(pt, matchedArc);        
      }
      

      Set<AdressPoint> adsMatched = new HashSet<AdressMapMatching.AdressPoint>();
      for(AdressPoint ad: mapDirect.keySet()){
        if(!mapReverse.containsKey(ad)){
          continue;
        }
        if(!mapDirect.get(ad).equals(mapReverse.get(ad))){
          continue;
        }
        adsMatched.add(ad);
      }
      
      // récup de l'arc apparié
      for(AdressPoint pt: adsMatched){
        Arc matchedArc = mapDirect.get(pt);
        STEntity closestE =mapFeaturesEntities.get(matchedArc.getCorrespondant(0));
        if(this.matching.containsKey(closestE)){
          this.matching.get(closestE).add(pt);
        }
        else{
          Set<AdressPoint> set = new HashSet<AdressPoint>();
          set.add(pt);
          this.matching.put(closestE, set);
        }
      }
      
    }
  }

  public Map<STEntity, Set<AdressPoint>> getMatching() {
    return this.matching;
  }


  class AdressPoint extends DefaultFeature{
    // date associée au polygone

    public AdressPoint(IGeometry g){
      this.setGeom(g);
    }

    private FuzzyTemporalInterval date;

    //nom
    private String streetName;

    //numéro

    private int num;

    public FuzzyTemporalInterval getDate() {
      return date;
    }

    public void setDate(FuzzyTemporalInterval date) {
      this.date = date;
    }

    public String getStreetName() {
      return streetName;
    }

    public void setStreetName(String streetName) {
      this.streetName = streetName;
    }

    public int getNum() {
      return num;
    }

    public void setNum(int num) {
      this.num = num;
    }
  }


  public static void main(String[] args) {
    // TODO Auto-generated method stub

//
        String inputStg ="/home/bcostes/Bureau/stag_json2/stag_json.tag";
        STGraph stg = TAGIoManager.deserialize(inputStg);
        IPopulation<IFeature> matching = new Population<IFeature>();
        for(STEntity e : stg.getEdges()){
          if(e.getJsonAttributes() != null && !e.getJsonAttributes().isEmpty()){
            IDirectPosition p= Operateurs.milieu((ILineString)e.getGeometry().toGeoxGeometry());
            for(JSonAttribute att: e.getJsonAttributes()){
              IDirectPosition p2 = att.decodeGeometry().coord().get(0);
            IDirectPositionList l= new DirectPositionList();
            l.add(p);
            l.add(p2);
            matching.add(new DefaultFeature(new GM_LineString(l)));
            }
          }
        }
         ShapefileWriter.write(matching, "/home/bcostes/Bureau/matching.shp");
    //    
    //    
    //
//    String inputStg ="/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/final_ind/tag_corrected_ind.tag";
//    STGraph stg = TAGIoManager.deserialize(inputStg);
//    for(STEntity e : stg.getEdges()){
//      e.setJsonAttributes(new HashSet<JSonAttribute>());
//    }
//
//
//    FuzzyTemporalInterval date = null;
//    try {
//      date = new FuzzyTemporalInterval(new double[]{1808,1810,1836,1853},new double[]{0,1,1,0}, 4);
//    } catch (XValuesOutOfOrderException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (YValueOutOfRangeException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//
//
//
//    AdressMapMatching mapM = new AdressMapMatching(stg, "/media/bcostes/Data/Benoit/these/donnees/vecteur/adresses/vasserot_l93/adresses_vasserot_corrigees.shp", date, "nom_entier", "num_voies");
//   // AdressMapMatching mapM = new AdressMapMatching(stg, "/home/bcostes/Bureau/pts.shp", date, "nom_entier", "num_voies");
//
//    mapM.match();
//
//    IPopulation<IFeature> matching = new Population<IFeature>();
//    //    
//        //IPopulation<IFeature> realignment = new Population<IFeature>();
//
//
//    for(STEntity e : mapM.getMatching().keySet()){
//      if(mapM.getMatching().get(e).isEmpty()){
//        continue;
//      }
//     IDirectPosition p= Operateurs.milieu((ILineString)e.getGeometry().toGeoxGeometry());
//
//      for(AdressPoint ad: mapM.getMatching().get(e)){
//        IDirectPosition p2 = ad.getGeom().coord().get(0);
//        IDirectPositionList l= new DirectPositionList();
//        l.add(p);
//        l.add(p2);
//        matching.add(new DefaultFeature(new GM_LineString(l)));
//                JSonAttribute json =JSonAttribute.createJSonAttribute("Adresse", ad);
//                try {
//                  json.putO("num", Integer.toString(ad.getNum()));
//                  json.putO("name", ad.getStreetName());
//                  
//                  JSONObject jsontime = new JSONObject();
//                  jsontime.put("time_size", ad.getDate().size());
//                  double[]array = new double[ad.getDate().size()];
//                  for(int i=0; i< ad.getDate().size(); i++){
//                    array[i] = ad.getDate().getX(i);
//                  }
//                  jsontime.put("fuzzyset", array);
//        
//                  json.putO("date", jsontime);
//                  json.putO("description", "Vasserot");
//                  e.getJsonAttributes().add(json);
//                  
//                  
//                } catch (JSONException e1) {
//                  // TODO Auto-generated catch block
//                  e1.printStackTrace();
//                }
//      }
//    }
//          
//          //recalage
//    //        Set<AdressPoint> pts = mapM.getMatching().get(e);
//    //      mapM.realignment(pts, e);
//          
//          
//    //      for(AdressPoint pdata: pts){
//    //        IDirectPosition p2 = pdata.getGeom().coord().get(0);
//    //        IDirectPositionList l= new DirectPositionList();
//    //        l.add(p);
//    //        l.add(p2);
//    //        matching.add(new DefaultFeature(new GM_LineString(l)));
//    //        
//    //        realignment.add(new DefaultFeature(pdata.getGeom()));
//    //      }
//    //    }
//    //    
//    //    
//        try {
//          date = new FuzzyTemporalInterval(new double[]{1887,1888,1888,1889},new double[]{0,1,1,0}, 4);
//        } catch (XValuesOutOfOrderException e) {
//          // TODO Auto-generated catch block
//          e.printStackTrace();
//        } catch (YValueOutOfRangeException e) {
//          // TODO Auto-generated catch block
//          e.printStackTrace();
//        }
//        mapM = new AdressMapMatching(stg, "/media/bcostes/Data/Benoit/these/donnees/vecteur/adresses/poubelle/poubelle_emprise.shp", date, "nom_entier", "num_voies");
//        //AdressMapMatching mapM = new AdressMapMatching(stg, "/home/bcostes/Bureau/points.shp", date, "nom_entier", "num_voies");
//    
//        mapM.match();
//    
//    //    IPopulation<IFeature> matching = new Population<IFeature>();
//    //    
//    //    IPopulation<IFeature> realignment = new Population<IFeature>();
//    
//        
//        for(STEntity e : mapM.getMatching().keySet()){
//    
//          if(mapM.getMatching().get(e).isEmpty()){
//            continue;
//          }
//          IDirectPosition p= Operateurs.milieu((ILineString)e.getGeometry().toGeoxGeometry());
//          
//          for(AdressPoint ad: mapM.getMatching().get(e)){
//            JSonAttribute json =JSonAttribute.createJSonAttribute("Adresse", ad);
//            try {
//              json.putO("num", Integer.toString(ad.getNum()));
//              json.putO("name", ad.getStreetName());
//              
//              JSONObject jsontime = new JSONObject();
//              jsontime.put("time_size", ad.getDate().size());
//              double[]array = new double[ad.getDate().size()];
//              for(int i=0; i< ad.getDate().size(); i++){
//                array[i] = ad.getDate().getX(i);
//              }
//              jsontime.put("fuzzyset", array);
//    
//              json.putO("date", jsontime);
//              json.putO("description", "Poubelle");
//              e.getJsonAttributes().add(json);
//              
//              
//            } catch (JSONException e1) {
//              // TODO Auto-generated catch block
//              e1.printStackTrace();
//            }
//          }
//        }
//          
//          try {
//            date = new FuzzyTemporalInterval(new double[]{2009,2010,2010,2011},new double[]{0,1,1,0}, 4);
//          } catch (XValuesOutOfOrderException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//          } catch (YValueOutOfRangeException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//          }
//           mapM = new AdressMapMatching(stg, "/media/bcostes/Data/Benoit/these/donnees/vecteur/adresses/BDAdresse/adresses_rge_emprise.shp", date, "NOM_VOIE", "NUMERO");
//           // mapM = new AdressMapMatching(stg, "/home/bcostes/Bureau/pts.shp", date, "NOM_VOIE", "NUMERO");
//    
//          mapM.match();
//    
//         // IPopulation<IFeature> matching = new Population<IFeature>();
//          
//         // IPopulation<IFeature> realignment = new Population<IFeature>();
//    
//          
//          for(STEntity e : mapM.getMatching().keySet()){
//            if(mapM.getMatching().get(e).isEmpty()){
//              continue;
//            }
//           IDirectPosition p= Operateurs.milieu((ILineString)e.getGeometry().toGeoxGeometry());
//            
//           for(AdressPoint ad: mapM.getMatching().get(e)){
//           IDirectPosition p2 = ad.getGeom().coord().get(0);
//           IDirectPositionList l= new DirectPositionList();
//           l.add(p);
//           l.add(p2);
//           matching.add(new DefaultFeature(new GM_LineString(l)));
//              JSonAttribute json =JSonAttribute.createJSonAttribute("Adresse", ad);
//              try {
//                json.putO("num", Integer.toString(ad.getNum()));
//                json.putO("name", ad.getStreetName());
//                
//                JSONObject jsontime = new JSONObject();
//                jsontime.put("time_size", ad.getDate().size());
//                double[]array = new double[ad.getDate().size()];
//                for(int i=0; i< ad.getDate().size(); i++){
//                  array[i] = ad.getDate().getX(i);
//                }
//                jsontime.put("fuzzyset", array);
//    
//                json.putO("date", jsontime);
//                json.putO("description", "BDAdresse");
//                e.getJsonAttributes().add(json);
//                
//                
//              } catch (JSONException e1) {
//                // TODO Auto-generated catch block
//                e1.printStackTrace();
//              }
//            }
//          }
//          
//            
//            
//       TAGIoManager.serializeBinary(stg, "/home/bcostes/Bureau/stag_json2/stag_json.tag");


   /// ShapefileWriter.write(matching, "/home/bcostes/Bureau/matching.shp");
   // ShapefileWriter.write(realignment, "/home/bcostes/Bureau/realignment.shp");




    //    IPopulation<IFeature> out = new Population<IFeature>();
    //    for(ILineString l : n.getGeometry()){
    //      out.add(new DefaultFeature(l));
    //    }
    //    ShapefileWriter.write(out, "/home/bcostes/Bureau/test.shp");

  } 

}
