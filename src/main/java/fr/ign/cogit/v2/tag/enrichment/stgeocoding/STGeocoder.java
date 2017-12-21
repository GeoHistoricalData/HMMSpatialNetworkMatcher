package fr.ign.cogit.v2.tag.enrichment.stgeocoding;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.matching.dst.evidence.ChoiceType;
import fr.ign.cogit.geoxygene.matching.dst.evidence.EvidenceResult;
import fr.ign.cogit.geoxygene.matching.dst.evidence.MatchingProcess;
import fr.ign.cogit.geoxygene.matching.dst.evidence.codec.DefaultCodec;
import fr.ign.cogit.geoxygene.matching.dst.evidence.codec.EvidenceCodec;
import fr.ign.cogit.geoxygene.matching.dst.operators.CombinationAlgos;
import fr.ign.cogit.geoxygene.matching.dst.operators.DecisionOp;
import fr.ign.cogit.geoxygene.matching.dst.sources.Source;
import fr.ign.cogit.geoxygene.matching.dst.util.Pair;
import fr.ign.cogit.geoxygene.matching.dst.util.Utils;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.enrichment.stgeocoding.evidence.GeocodeHypothesis;
import fr.ign.cogit.v2.tag.enrichment.stgeocoding.evidence.GeocodeTypeSource;
import fr.ign.cogit.v2.tag.enrichment.stgeocoding.evidence.TemporalDistanceSource;
import fr.ign.cogit.v2.tag.enrichment.stgeocoding.evidence.ToponymicSource;
import geocodage.LexicalTools;
import geocodage.namedStreet.NamedStreet;

public class STGeocoder {

  //stag
  private STGraph stag;
  // bases d'adresses, une par date
  Map<FuzzyTemporalInterval, Set<AdressPoint>> adressesDataset;
  // adresses to geocode
  Set<TextualAdress> textualAdresses;
  // nom du JSonAttribute correspondant à un point adresse
  private final String ptAdName = "adresse";
  //pour stocker les adresses qu'on a pas réussi à géocoder
  public Set<TextualAdress> ungeocoded;
  // les namedstreet pour chaque date
  private Map<FuzzyTemporalInterval, Set<NamedStreet>> namedStreet;
  // mapping entre point adresse et leur namedstreet 
  private Map<AdressPoint, NamedStreet> namedStreetMapping;


  //les clusters d'adresses, par date et nom
  private Map<FuzzyTemporalInterval, Map<String, Set<AdressPoint>>> clusters;

  public Map<AdressPoint, NamedStreet> getNamedStreetMapping() {
    return namedStreetMapping;
  }

  public void setNamedStreetMapping(
      Map<AdressPoint, NamedStreet> namedStreetMapping) {
    this.namedStreetMapping = namedStreetMapping;
  }

  public Map<FuzzyTemporalInterval, Set<NamedStreet>>getNamedStreet() {
    return namedStreet;
  }

  public void setNamedStreet(Map<FuzzyTemporalInterval, Set<NamedStreet>> namedStreet) {
    this.namedStreet = namedStreet;
  }

  public STGeocoder(STGraph stag,  Set<TextualAdress> textualAdresses){
    this.stag = stag;
    this.textualAdresses= textualAdresses;
    STGeocoderIO.clustering(this);
    STGeocoderIO.loadNamedStreets(this);   
    STGeocoderIO.mapNamedStreet(this);

    this.ungeocoded = new HashSet<TextualAdress>();
  }





  public void stgeocode(){
    int cpt =0;
    //création d'un dictionnaire des noms de rues (pour les changements de noms)
    Map<String, Set<String>> streetNamesDictionary = STGeocoderIO.getStreetNamesDictionary(this);

    
    IPopulation<IFeature> out = new Population<IFeature>();
    for(TextualAdress ad : this.textualAdresses){
      Set<GeocodedCandidate>  geocodedCandidates = this.stgeocode(ad, streetNamesDictionary);
      if(geocodedCandidates == null){
        this.ungeocoded.add(ad);
        continue;
      }

      if(geocodedCandidates.isEmpty()){
        //on va essayer avec des synonymes
        geocodedCandidates = this.stgeocodeSyn(ad, streetNamesDictionary);
        //si on a des candidats, on les marque comme incertains
        if(!geocodedCandidates.isEmpty()){
        cpt++;
      }

        for(GeocodedCandidate c: geocodedCandidates){
          c.type = GeocodeType.UNCERTAIN;
        }
      }
      if(geocodedCandidates.isEmpty()){
        this.ungeocoded.add(ad);
        continue;
      }
      if(geocodedCandidates.size()> 500){
        //pas raisonnable
        this.ungeocoded.add(ad);
        continue;
      }
      //prise de décision
      try {
        Collection<Source<TextualAdress, GeocodeHypothesis>> criteria = new ArrayList<Source<TextualAdress, GeocodeHypothesis>>();
        criteria.add(new GeocodeTypeSource());
        //criteria.add(new ToponymicSource());
        criteria.add(new TemporalDistanceSource());

        boolean closed = true;

        // LinkedList<List<IFeature>> combinations = Combinations.enumerate(candidates);
        List<GeocodeHypothesis> hypotheses = new ArrayList<GeocodeHypothesis>();
        for (GeocodedCandidate l : geocodedCandidates) {
         // System.out.println(l.name+" "+ l.type+" "+ l.date);
          hypotheses.add(new GeocodeHypothesis(l));
        }
        EvidenceCodec<GeocodeHypothesis> codec = new DefaultCodec<GeocodeHypothesis>(hypotheses);
        MatchingProcess<TextualAdress, GeocodeHypothesis> matchingProcess = new MatchingProcess<TextualAdress, GeocodeHypothesis>(
            criteria, hypotheses, codec, closed);
        List<Pair<byte[], Float>> result = matchingProcess.combinationProcess(ad);
        CombinationAlgos.sortKernel(result);
        CombinationAlgos.deleteDoubles(result);
        float maxpig = 0.0f;
        byte[] maxpignistic = null;
        for (Pair<byte[], Float> hyp : result) {         
          //
          float bel = this.pignistic(hyp.getFirst(), result);
          // System.out.println("bel = " + bel + ", maxpig = " + maxpig);
          if (bel > maxpig) {
            maxpig = bel;
            maxpignistic = hyp.getFirst();
          }
        }
        Pair<byte[], Float> maxP =  new Pair<byte[], Float>(maxpignistic, maxpig);
        List<GeocodeHypothesis> decoded = codec.decode(maxP.getFirst());
        EvidenceResult<GeocodeHypothesis> result2 = new EvidenceResult<GeocodeHypothesis>(ChoiceType.PIGNISTIC, 0f, decoded, maxP.getSecond());
//      DecisionOp<GeocodeHypothesis> decisionOp = new DecisionOp<GeocodeHypothesis>(result, 0f, ChoiceType.PIGNISTIC,
//            codec, false);
//        EvidenceResult<GeocodeHypothesis> result2 = decisionOp.resolve();


        if(!result2.getHypothesis().isEmpty()){
          //candidat choisi
          GeocodedCandidate finalCandidate = result2.getHypothesis().get(0).getDecoratedFeature();
          ad.setCoord(finalCandidate.pos);
          ad.setGeocodeType(finalCandidate.type);
          ad.setTrust(result2.getValue());
          ad.setDate_geoc_ad(finalCandidate.date);
          IFeature f = new DefaultFeature(new GM_Point(ad.getCoord()));
          AttributeManager.addAttribute(f, "num", ad.getNum(), "String");
          AttributeManager.addAttribute(f, "name", ad.getName(), "String");
          AttributeManager.addAttribute(f, "type", finalCandidate.type, "String");
          AttributeManager.addAttribute(f, "score", result2.getValue(), "Float");
          AttributeManager.addAttribute(f, "date_geo_ad", finalCandidate.date.toString(), "String");
          out.add(f);
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }   

      System.out.println(" --- GEOCODING RESULT ---");
      System.out.println(ad.getGeocodeType() +" ; " +ad.getCoord()+ " ; "+ ad.getTrust());
    }
    //ShapefileWriter.write(out, "/home/bcostes/Bureau/test.shp");

    streetNamesDictionary.clear();
    streetNamesDictionary = null;
    
    System.out.println(cpt+" synonymes" );
  }

  public Set<GeocodedCandidate> stgeocode(TextualAdress textualAdress, Map<String, Set<String>> streetNamesDictionary ){
    System.out.println(" **** GEOCODING ... *****");

    List<String> l = textualAdress.getNameParsed();
    if(l == null || l.isEmpty()){
      return null;
    }
    System.out.println("Adress to geocode: " + textualAdress.getNum()+" "+ textualAdress.getName());

    //**************************************************************
    //***************** Recherche de candidats *********************
    //**************************************************************
    // on recherche des candidats (des points adresses) grace a un critère toponymique
    // Cependant, on ne cherche pas par rapport au nom des points de la base adresse
    // mais des noms des rues du STAG.
    // on va chercher des candidats en créeant dynamiquement un dictionnaire des
    // noms de rues, en "local" (i.e uniquement pour les noms de rues proches du nom recherché)
    Map<FuzzyTemporalInterval, Set<AdressPoint>> candidates = new HashMap<FuzzyTemporalInterval, Set<AdressPoint>>();
    for(FuzzyTemporalInterval t: this.adressesDataset.keySet()){
      Set<AdressPoint> adcandidates = new HashSet<AdressPoint>();
      // distance min
      Map<AdressPoint, Boolean> done = new HashMap<AdressPoint, Boolean>();
      for(AdressPoint adp: this.adressesDataset.get(t)){
        done.put(adp, false); 
      }
      for(AdressPoint adp: this.adressesDataset.get(t)){
        if(done.get(adp) || adp.getNameParsed().size()<=1){
          continue;
        }
        done.put(adp, true);
        //distance toponymique entre  le nom de adp ou un de ses synonyme dans le dictionnaire
        if (LexicalTools.dDamarauLevenshtein(textualAdress.getPhoneticName(),
            adp.getPhoneticName()) <= 1
            ||
            LexicalTools.dDamarauLevenshtein(textualAdress.getName(),
                adp.getName()) <= 1
                ||
                LexicalTools.dDamarauLevenshtein(textualAdress.getOriginalName(),
                    adp.getOriginalName()) <= 1
            /*|| LexicalTools.lexicalSimilarityCoeff(textualAdress.getNameParsed(),
                adp.getNameParsed()) < 0.2
                || LexicalTools
                .lexicalSimilarityCoeff(textualAdress.getPhoneticNameParsed(),
                    adp.getPhoneticNameParsed()) < 0.2*/
                    || (adp.getOriginalName().startsWith(textualAdress.getOriginalName()+" "))
                    || (textualAdress.getOriginalName().startsWith(adp.getOriginalName()+" "))
                    || (adp.getName().startsWith(textualAdress.getName()+" "))
                    || (textualAdress.getName().startsWith(adp.getName()+" "))) {
          //on a un match
          adcandidates.add(adp);  
          for(AdressPoint adp2: this.getClusters().get(t).get(adp.getName())){
            adcandidates.add(adp2);
            done.put(adp2, true);
          }
        }
        /* else{
          //on cherche un synonyme
          Set<String> syn = streetNamesDictionary.get(adp.getName());
          if(syn == null){
            continue;
          }

          for(String name: syn){
            if(LexicalTools.parse1(name).size()<=1){
              continue;
            }
            if (LexicalTools.dDamarauLevenshtein(textualAdress.getPhoneticName(),
                LexicalTools.phoneticName(name)) <= 2
                || LexicalTools.lexicalSimilarityCoeff(textualAdress.getNameParsed(),
                    LexicalTools.parse1(name)) <= 0.1
                    || LexicalTools
                    .lexicalSimilarityCoeff(textualAdress.getPhoneticNameParsed(),
                        LexicalTools.parsePhoneticName(name)) <= 0.1
                        || (name.startsWith(textualAdress.getName()))
                        || (textualAdress.getName().startsWith(name))) {
              adcandidates.add(adp);

              for(AdressPoint adp2: this.getClusters().get(t).get(adp.getName())){
                adcandidates.add(adp2);
                done.put(adp2, true);
              }
              break;
            }
            else{
              //on peut supprimer tous les autre pts adresses attachés à cet arcs à cette date
              for(AdressPoint adp2: this.getClusters().get(t).get(adp.getName())){
                done.put(adp2, true);
              }
            }
          }
        }*/

      }
      candidates.put(t, adcandidates);
    }

    Set<GeocodedCandidate> geocodedCandidates =  GeocoderProcess.geocode(textualAdress, candidates, this);

    return geocodedCandidates;
  }


  public Set<GeocodedCandidate> stgeocodeSyn(TextualAdress textualAdress, Map<String, Set<String>> streetNamesDictionary ){
    System.out.println(" **** GEOCODING SYN ... *****");

    List<String> l = textualAdress.getNameParsed();
    if(l == null || l.isEmpty()){
      return null;
    }
    System.out.println("Adress to geocode: " + textualAdress.getNum()+" "+ textualAdress.getName());

    //**************************************************************
    //***************** Recherche de candidats *********************
    //**************************************************************
    // on recherche des candidats (des points adresses) grace a un critère toponymique
    // Cependant, on ne cherche pas par rapport au nom des points de la base adresse
    // mais des noms des rues du STAG.
    // on va chercher des candidats en créeant dynamiquement un dictionnaire des
    // noms de rues, en "local" (i.e uniquement pour les noms de rues proches du nom recherché)
    Map<FuzzyTemporalInterval, Set<AdressPoint>> candidates = new HashMap<FuzzyTemporalInterval, Set<AdressPoint>>();
    for(FuzzyTemporalInterval t: this.adressesDataset.keySet()){
      Set<AdressPoint> adcandidates = new HashSet<AdressPoint>();
      // distance min
      Map<AdressPoint, Boolean> done = new HashMap<AdressPoint, Boolean>();
      for(AdressPoint adp: this.adressesDataset.get(t)){
        done.put(adp, false); 
      }
      for(AdressPoint adp: this.adressesDataset.get(t)){
        if(done.get(adp) || adp.getNameParsed().size()<=1){
          continue;
        }
        done.put(adp, true);
        //distance toponymique entre  le nom de adp ou un de ses synonyme dans le dictionnaire
        //on cherche un synonyme
        Set<String> syn = streetNamesDictionary.get(adp.getOriginalName());
        if(syn == null){
          continue;
        }

        for(String name: syn){
          if(LexicalTools.parse1(name).size()<=1){
            continue;
          }
          if (LexicalTools.dDamarauLevenshtein(textualAdress.getPhoneticName(),
              LexicalTools.phoneticName(name)) <= 1 ||
                  LexicalTools.dDamarauLevenshtein(textualAdress.getName(),
                      name) <= 1
                      ||
                      LexicalTools.dDamarauLevenshtein(textualAdress.getOriginalName(),
                          name) <= 1
             /* || LexicalTools.lexicalSimilarityCoeff(textualAdress.getNameParsed(),
                  LexicalTools.parse1(name)) <= 0.1
                  || LexicalTools
                  .lexicalSimilarityCoeff(textualAdress.getPhoneticNameParsed(),
                      LexicalTools.parsePhoneticName(name)) <= 0.1*/
                              || (name.startsWith(textualAdress.getOriginalName()+" "))
                              || (textualAdress.getOriginalName().startsWith(name+" "))
                              || (name.startsWith(textualAdress.getName()+" "))
                              || (textualAdress.getName().startsWith(name+" "))) {
            adcandidates.add(adp);

            for(AdressPoint adp2: this.getClusters().get(t).get(adp.getName())){
              adcandidates.add(adp2);
              done.put(adp2, true);
            }
            break;
          }
          else{
            //on peut supprimer tous les autre pts adresses attachés à cet arcs à cette date
            for(AdressPoint adp2: this.getClusters().get(t).get(adp.getName())){
              done.put(adp2, true);
            }
          }
        }

      }
      candidates.put(t, adcandidates);
    }

    Set<GeocodedCandidate> geocodedCandidates =  GeocoderProcess.geocode(textualAdress, candidates, this);

    return geocodedCandidates;
  }



  public STGraph getStag() {
    return stag;
  }



  public void setStag(STGraph stag) {
    this.stag = stag;
  }



  public Map<FuzzyTemporalInterval, Set<AdressPoint>> getAdressesDataset() {
    return adressesDataset;
  }



  public void setAdressesDataset(
      Map<FuzzyTemporalInterval, Set<AdressPoint>> adressesDataset) {
    this.adressesDataset = adressesDataset;
  }


  public Map<FuzzyTemporalInterval, Map<String, Set<AdressPoint>>> getClusters() {
    return clusters;
  }

  public void setClusters(
      Map<FuzzyTemporalInterval, Map<String, Set<AdressPoint>>> clusters) {
    this.clusters = clusters;
  }


  public Set<TextualAdress> getTextualAdresses() {
    return textualAdresses;
  }



  public void setTextualAdresses(Set<TextualAdress> textualAdresses) {
    this.textualAdresses = textualAdresses;
  }



  public Set<TextualAdress> getUngeocoded() {
    return ungeocoded;
  }



  public void setUngeocoded(Set<TextualAdress> ungeocoded) {
    this.ungeocoded = ungeocoded;
  }


  public String getPtAdName() {
    return ptAdName;
  }





  public static void main(String[] args) {


    // TODO Auto-generated method stub
    String host = "127.0.0.1";
    String port = "5432";
    String db = "these_source";
    String login = "postgres";
    String password = "postgres";
    String tableName = "bottin_1851_geocoded";

    Set<TextualAdress> add = STGeocoderIO.fromPgsql(host, port, db, login, password, tableName,
        "num_adr", "nom_adr", "date");

    String inputStg ="/home/bcostes/Bureau/stag_json/stag_json.tag";
    STGraph stg = TAGIoManager.deserialize(inputStg);

    STGeocoder stgeocoder = new STGeocoder(stg, add);
    stgeocoder.stgeocode();

    try {
      STGeocoderIO.exportDB(host, port, db, login, password, tableName,
          stgeocoder.getTextualAdresses());
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("NO GEOCODED :  " + stgeocoder.getUngeocoded().size());

  }
  
  
  private float pignistic(byte[] hyp, List<Pair<byte[], Float>> masspotentials) {
    float pignistic = 0.0f;
    float mvoid = masspotentials.get(0).getSecond();
    if (!Utils.isEmpty(masspotentials.get(0).getFirst())) {
      mvoid = 0.0f;
    }
    for (Pair<byte[], Float> value : masspotentials) {
      if (!Utils.isEmpty(Utils.byteIntersection(hyp, value.getFirst()))) {
        int cardinal = 0;
        for (byte b : value.getFirst()) {
          if (b == (byte) 1) {
            cardinal++;
          }
        }
        // logger.debug(value.getSecond() + ", " + cardinal + ", " + mvoid);
        pignistic += value.getSecond() / (cardinal * (1 - mvoid));
      }
    }
    // logger.debug("Pignistic value : " + pignistic + " for hypothesis" + Arrays.toString(hyp));
    return pignistic;
  }


}
