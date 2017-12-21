package fr.ign.cogit.v2.tag.enrichment.stgeocoding;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.conversion.WktGeOxygene;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.enrichment.JSonAttribute;
import geocodage.IOUtils;
import geocodage.LexicalTools;
import geocodage.namedStreet.NamedStreet;
import geocodage.namedStreet.NamedStreetBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

/**
 * Pour charger les données à géocoder
 * @author bcostes
 *
 */
public class STGeocoderIO {

  // pour chaque nom, on compte le nombre d'arcs qui ont un nom synonyme différent
  static double THRESHOLD_SYNONYMS = 0.06; // au moins x% d'arcs commun pour les synonymes


  public static Set<TextualAdress> fromPgsql(String host, String port,
      String db, String login, String password, String tableName, String columnNumber, String columnName,
      String columnDate){
    Set<TextualAdress> textualAdresses = new HashSet<TextualAdress>();
    Connection destDbConnection = null;
    try {
      // initialisation des connexions aux bases (source puis dest)
      destDbConnection = IOUtils.createDBConnection(host, port, db, login,
          password);
      PreparedStatement stSelectData = destDbConnection
          .prepareStatement("SELECT id, "+columnNumber+", "+columnName+", "+columnDate+" FROM " + tableName);
      // initialise le résultat de la requète
      ResultSet query = null;
      // exécution de la requete de sélection
      query = stSelectData.executeQuery();

      if (query != null) {
        while (query.next()) {

          int id = query.getInt("id");

          String num = query.getString(columnNumber);
          num = num == null ? "" : num.toLowerCase();

          String nom = query.getString(columnName);
          nom = nom == null ? "" : nom.toLowerCase();

          double date = Double.parseDouble(query.getString(columnDate));

          List<String> adParsed = LexicalTools.parse2(nom);
          String type = "";
          if(LexicalTools.typeDictionary.contains(adParsed.get(0))){
            type = adParsed.get(0);
            adParsed.remove(0);
          }
          String name ="";
          if(!adParsed.isEmpty()){
            for(String sss : adParsed){
              name += sss +" ";
            }
            name = name.substring(0, name.length()-1);
          }

          TextualAdress adS = new TextualAdress(id, num, type, name, nom);

          adS.setDate(date);
          adS.setNameParsed(adParsed);


          List<String> phoneticNameParsed = new ArrayList<String>();
          for(String s: adS.getNameParsed()){
            phoneticNameParsed.add(Soundex2.soundex2(s));
          }
          adS.setPhoneticNameParsed(phoneticNameParsed);
          adS.setPhoneticName("    ");
          if(!phoneticNameParsed.isEmpty()){
            String ss ="";
            for(String sss : phoneticNameParsed){
              ss += sss +" ";
            }
            ss = ss.substring(0, ss.length()-1);
            adS.setPhoneticName(ss);
          }

          textualAdresses.add(adS);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        IOUtils.closeDBConnection(destDbConnection);
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return textualAdresses;
  }

  /**
   * Chercher les points adresses ratachés aux stedges.
   * Remplie la map du stgeocoder adressesPointMap en créeant des points adresses de base
   * à partir des éventuels attributs JSon rattachés au STAG.
   * Effectue le parsing du nom des points adresses
   */
  public static void clustering(STGeocoder stgeocoder){    
    stgeocoder.setAdressesDataset(new HashMap<FuzzyTemporalInterval, Set<AdressPoint>>());
    for(STEntity edge: stgeocoder.getStag().getEdges()){
      for(JSonAttribute att: JSonAttribute.getJsonAttributeByName(stgeocoder.getPtAdName().toLowerCase(), edge.getJsonAttributes())){
        //on cherche des points adresses attachées à ce stedge
        try {
          String num = att.getJson().get("num").toString().toLowerCase();
          num = num.replace(" ", "");
          String s = LexicalTools.digitSuppression(num);
          if (s.length() != 0) {
            // il y a autre chose que des chiffres
            s = "";
            for (int i = 0; i < num.length(); i++) {
              if (LexicalTools.containsDigit(num.substring(i, i + 1))) {
                s += num.substring(i, i + 1);
              } else {
                break;
              }
            }
            num = s;
          }
          if(num.equals("")){
            continue;
          }
          String nom = att.getJson().get("name").toString().toLowerCase();

          if(nom.equals("")){
            continue;
          }

          
          List<String> adParsed = LexicalTools.parse2(nom);
          String type = "";
          if(LexicalTools.typeDictionary.contains(adParsed.get(0))){
            type = adParsed.get(0);
            adParsed.remove(0);
          }
          String name ="";
          if(!adParsed.isEmpty()){
            for(String sss : adParsed){
              name += sss +" ";
            }
            name = name.substring(0, name.length()-1);
          }

          
          AdressPoint adp = new AdressPoint(num, type, name,
              att.decodeGeometry().coord().get(0));

          adp.setNameParsed(adParsed);


          List<String> phoneticNameParsed = new ArrayList<String>();
          for(String ss: adp.getNameParsed()){
            phoneticNameParsed.add(Soundex2.soundex2(ss));
          }
          adp.setPhoneticNameParsed(phoneticNameParsed);
          adp.setPhoneticName("    ");
          if(!phoneticNameParsed.isEmpty()){
            String ss ="";
            for(String sss : phoneticNameParsed){
              ss += sss +" ";
            }
            ss = ss.substring(0, ss.length()-1);
            adp.setPhoneticName(ss);
          }
          
          
          JSONObject time = att.getJson().getJSONObject("date");
          double[] dd = new double[4];
          if(time.getInt("time_size") == 3){
            dd[0] = time.getJSONArray("fuzzyset").getDouble(0);
            dd[1] = time.getJSONArray("fuzzyset").getDouble(1);
            dd[2] = dd[1];
            dd[3] = time.getJSONArray("fuzzyset").getDouble(2);
          }
          else{
            for(int i=0; i< time.getInt("time_size"); i++){
              dd[i] = time.getJSONArray("fuzzyset").getDouble(i);
            }
          }

          FuzzyTemporalInterval t = new FuzzyTemporalInterval(dd ,new double[]{0,1,1,0}, 4);
          adp.setTime(t);






          //          
          //          
          //          
          //          
          //          
          //          
          //
          //          //TODO: DELETE FOLLOWING CODE
          //          try {
          //            FuzzyTemporalInterval date = new FuzzyTemporalInterval(new double[]{2009,2010,2010,2011},new double[]{0,1,1,0}, 4);
          //
          //            if(!t.equals(date)){
          //              continue;
          //            }
          //
          //          } catch (XValuesOutOfOrderException e) {
          //            // TODO Auto-generated catch block
          //            e.printStackTrace();
          //          } catch (YValueOutOfRangeException e) {
          //            // TODO Auto-generated catch block
          //            e.printStackTrace();
          //          }
          //          //------ TODO : end delete
          //
          //          
          //          












          if(stgeocoder.getAdressesDataset().containsKey(t)){
            stgeocoder.getAdressesDataset().get(t).add(adp);
          }
          else{
            Set<AdressPoint> set = new HashSet<AdressPoint>();
            set.add(adp);
            stgeocoder.getAdressesDataset().put(t, set);
          }
        } catch (JSONException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (XValuesOutOfOrderException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (YValueOutOfRangeException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } 
      }
    }


    //CLUSTERING
    stgeocoder.setClusters(new HashMap<FuzzyTemporalInterval, Map<String,Set<AdressPoint>>>());

    for(FuzzyTemporalInterval t: stgeocoder.getAdressesDataset().keySet()){
      Map<String, Set<AdressPoint>> clusters = new HashMap<String, Set<AdressPoint>>();
      //************************* Clustering toponymique **************************
      //on va regrouper par aggrégation des stedge associés aux pts adresses
      for(AdressPoint adp: stgeocoder.getAdressesDataset().get(t)){
        String name = adp.getName();
        boolean found = false;
        for(String othername: clusters.keySet()){
          if(othername.equals(name)){
            found = true;
            clusters.get(othername).add(adp);
            break;
          }
        }
        if(!found){
          Set<AdressPoint> set = new HashSet<AdressPoint>();
          set.add(adp);
          clusters.put(name, set);
        }
      }
      stgeocoder.getClusters().put(t, clusters);
    }    
  }

  /**
   * Crée un dictionnaire de noms de rues
   * Deux noms sont dans le meme set si ils correspondent à la meme rue
   * @param stGeocoder
   * @return
   */
  public static Map<String, Set<String>> getStreetNamesDictionary(STGeocoder stGeocoder){
    Map<String, Set<String>> streetNamesDictionary = new HashMap<String, Set<String>>();



    for(FuzzyTemporalInterval t:stGeocoder.getStag().getTemporalDomain().asList()){
      //on récupère le snapshot le plus proche
      Map<String, Map<FuzzyTemporalInterval, Map<String, Integer>>> map = new HashMap<String, Map<FuzzyTemporalInterval,Map<String,Integer>>>();
      Map<String, Integer> mapI = new HashMap<String, Integer>();

      for(STEntity e: stGeocoder.getStag().getEdgesAt(t)){
        String name = e.getTAttributeByName("name").getValueAt(t);
        if(name == null || name.equals("")){
          continue;
        }
        name = name.toLowerCase();
        String[] snameSplited = name.split(";");
        for(String s: snameSplited){
          s = s.trim();
          List<String> l = LexicalTools.parse2(s);
          String ss="";
          for(String sss: l){
            ss += sss + " ";
          }
          s = ss.substring(0, ss.length()-1);
          if(mapI.containsKey(s)){
            mapI.put(s, mapI.get(s)+1);
          }
          else{
            mapI.put(s, 1);
          }
          if(!map.containsKey(s)){
            map.put(s, new HashMap<FuzzyTemporalInterval, Map<String,Integer>>());
          }
          for(FuzzyTemporalInterval t2:  stGeocoder.getStag().getTemporalDomain().asList()){
            if(t.equals(t2) || !e.existsAt(t2)){
              continue;
            }
            String name2 = e.getTAttributeByName("name").getValueAt(t2);
            if(name2 == null || name2.equals("")){
              continue;
            }
            name2 =name2.toLowerCase();
            String[] snameSplited2 = name2.split(";");

            for(String s2: snameSplited2){
              s2 = s2.trim();
              List<String> l2 = LexicalTools.parse2(s2);
              String ss2="";
              for(String sss: l2){
                ss2 += sss + " ";
              }
              s2 = ss2.substring(0, ss2.length()-1);
              if(s.equals(s2)){
                continue;
              }
              if(map.get(s).containsKey(t2)){
                if(map.get(s).get(t2).containsKey(s2)){
                  map.get(s).get(t2).put(s2, map.get(s).get(t2).get(s2)+1);
                }
                else{
                  map.get(s).get(t2).put(s2, 1);
                }
              }
              else{
                map.get(s).put(t2, new HashMap<String, Integer>());
                map.get(s).get(t2).put(s2, 1);
              }
            }
          }
        }
      }
      //Construction des namedStreet
      //on va supprimer les petites rues (moins de 3 tronçons
      for(String name: mapI.keySet()){
        if(mapI.get(name)<2){
          continue;
        }
        if(!streetNamesDictionary.containsKey(name)){
          streetNamesDictionary.put(name, new HashSet<String>());
        }
        double cpt = (double) mapI.get(name);
        for(FuzzyTemporalInterval t2: map.get(name).keySet()){
          for(String name2: map.get(name).get(t2).keySet()){
            double cpt2 = (double)map.get(name).get(t2).get(name2);
            cpt2 =  (cpt2 / cpt);
            if(cpt2 > STGeocoderIO.THRESHOLD_SYNONYMS){
              streetNamesDictionary.get(name).add(name2);
            }
          }
        }
      }
    }














    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //    Set<String> adnames = new HashSet<String>();
    //    for(FuzzyTemporalInterval t: stGeocoder.getAdressesDataset().keySet()){
    //      for(AdressPoint adp:stGeocoder.getAdressesDataset().get(t)){  
    //        adnames.add(adp.getName());
    //      }
    //    }
    //
    //    for(STEntity edge: stGeocoder.getStag().getEdges()){     
    //      Set<String> edgeNames = new HashSet<String>();
    //      for(FuzzyTemporalInterval t: stGeocoder.getStag().getTemporalDomain().asList()){
    //        String sname = edge.getTAttributeByName("name").getValueAt(t);
    //        if( sname!= null && !sname.equals("")){
    //          sname = sname.toLowerCase();
    //          String[] snameSplited = sname.split(";");
    //          for(String s: snameSplited){
    //            s = s.trim();
    //            List<String> l = LexicalTools.parse1(s);
    //            String ss="";
    //            for(String sss: l){
    //              ss += sss + " ";
    //            }
    //            s = ss.substring(0, ss.length()-1);
    //            edgeNames.add(s);
    //          }
    //        }
    //      }
    //      for(String edgename: edgeNames){
    //        if(adnames.contains(edgename)){
    //          if(!streetNamesDictionary.containsKey(edgename)){
    //            Set<String> set = new HashSet<String>();
    //            set.addAll(edgeNames);
    //            set.remove(edgename);
    //            streetNamesDictionary.put(edgename, set);
    //          }
    //          else{
    //            Set<String> set = new HashSet<String>();
    //            set.addAll(edgeNames);
    //            set.remove(edgename);
    //            streetNamesDictionary.get(edgename).addAll(set);
    //          }
    //        }
    //      }
    //    }

    return streetNamesDictionary;
  }

  public static void loadNamedStreets(STGeocoder stgeocoder){
    stgeocoder.setNamedStreet(new HashMap<FuzzyTemporalInterval, Set<NamedStreet>>());
    List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>(stgeocoder.getStag().getTemporalDomain().asList());
    Collections.sort(times);
    for(FuzzyTemporalInterval t: stgeocoder.getAdressesDataset().keySet()){
      double trank = FuzzyTemporalInterval.ChengFuzzyRank(t);
      IFeatureCollection<IFeature> pop = new Population<IFeature>();
      //on récupère le snapshot le plus proche
      FuzzyTemporalInterval tclosest = null;
      double dmin = Double.MAX_VALUE;
      for(FuzzyTemporalInterval t2: times){
        double d =Math.abs(trank - FuzzyTemporalInterval.ChengFuzzyRank(t2));
        if(d<dmin){
          dmin = d;
          tclosest = t2;
        }
      }
      for(STEntity e: stgeocoder.getStag().getEdgesAt(tclosest)){
        IFeature f = new DefaultFeature(e.getGeometryAt(tclosest).toGeoxGeometry());
        String name = e.getTAttributeByName("name").getValueAt(tclosest);
        name = (name == null) ? "": name;
        String ss ="";
        for(String sss : LexicalTools.parse2(name)){
          ss += sss +" ";
        }
        if(!ss.equals("")){
          ss = ss.substring(0, ss.length()-1);
        }
        name = ss;
        AttributeManager.addAttribute(f, "name", name, "String");
        pop.add(f);
      }
      //Construction des namedStreet
      Set<NamedStreet> namedtreets = new HashSet<NamedStreet>();
      namedtreets.addAll(NamedStreetBuilder.buildNamedStreetsFeatures(pop, "name"));


      stgeocoder.getNamedStreet().put(t, namedtreets);
    }
  }

  public static void mapNamedStreet(STGeocoder stgeocoder){
    if (stgeocoder.getNamedStreet() != null && !stgeocoder.getNamedStreet().isEmpty()) {
      stgeocoder.setNamedStreetMapping(new HashMap<AdressPoint, NamedStreet>());
      for(FuzzyTemporalInterval t: stgeocoder.getAdressesDataset().keySet()){
        for(AdressPoint adBase: stgeocoder.getAdressesDataset().get(t)){
          List<NamedStreet> potentialNamedStreet = new ArrayList<NamedStreet>();
          for (NamedStreet steet : stgeocoder.getNamedStreet().get(adBase.getTime())) {
            if (steet.getName().equals("")) {
              continue;
            }
            if (steet.getGeom()
                .distance(new GM_Point(adBase.getPosition())) > 50) {
              continue;
            }

            if (LexicalTools.dDamarauLevenshtein(adBase.getPhoneticName(),
                LexicalTools.phoneticName(steet.getName())) <= 1
                || LexicalTools.lexicalSimilarityCoeff2(
                    adBase.getNameParsed(),
                    LexicalTools.parse2(steet.getName())) < 0.1
                    || (adBase.getName().startsWith(
                        steet.getName().toLowerCase()) && steet.getName()
                        .length() > 1)
                        || (steet.getName().toLowerCase()
                            .startsWith(adBase.getName()) && adBase.getName()
                            .length() > 1)) {
              potentialNamedStreet.add(steet);
            }
          }


          if (!potentialNamedStreet.isEmpty()) {
            if (potentialNamedStreet.size() == 1) {
              stgeocoder.getNamedStreetMapping().put(adBase,potentialNamedStreet.get(0));
            } else {
              // si plusieurs named street potentielle on choisi la plus
              // proche
              double dmin = Double.MAX_VALUE;
              NamedStreet goodStreet = null;
              for (NamedStreet st : potentialNamedStreet) {
                double d = st.getGeom().distance(
                    new GM_Point(adBase.getPosition()));
                if (d < dmin) {
                  d = dmin;
                  goodStreet = st;
                }
              }
              stgeocoder.getNamedStreetMapping().put(adBase,goodStreet);
              continue;
            }
          } else {
            // System.out.println(adBase.getNom());
          }
        }
      }
    }
  }

  public static void exportDB(String host, String port, String db,
      String login, String password,  String tableGis,
      Set<TextualAdress> textualAdresses)
          throws SQLException {
    Connection destDbConnection = null;

    // Insertion du géocodage

    try {
      // initialisation des connexions aux bases (source puis dest)
      destDbConnection = IOUtils.createDBConnection(host, port, db, login,
          password);

      // Préparation du mapping




      PreparedStatement stInsertData = destDbConnection
          .prepareStatement("UPDATE "
              + tableGis
              + " SET the_geom = ST_GEOMETRYFROMTEXT(?), method = ?, score = ?, date_ad_geo = ? WHERE id = ?");
      // compteur de réponses à la première requète
      int nb_transfert = 0;

      for (TextualAdress ads : textualAdresses) {

        if(ads.getCoord() == null){
          continue;
        }


        // on indique que le premier "?" est renseigné par l'entier "idbati"
        // lu de la précédente requète

        stInsertData.setString(1,  WktGeOxygene.makeWkt(new GM_Point(ads.getCoord())));
        stInsertData.setString(2, ads.getGeocodeType().toString());
        stInsertData.setDouble(3, ads.getTrust());
        stInsertData.setDouble(4, FuzzyTemporalInterval.ChengFuzzyRank(ads.getDate_geoc_ad()));
        stInsertData.setInt(5, ads.getId());

        stInsertData.addBatch();

        // increment du compteur
        nb_transfert++;

        // uncomment this part in case of big data
        if (nb_transfert % 50 == 0) {
          // vide le buffer de requètes
          int[] status = new int[50];
          try {
            status = stInsertData.executeBatch();
          } catch (SQLException e) {
            e.getNextException().printStackTrace();
          }
          for (int i = 0; i < status.length; i++) {
            // status[i] == 1 : tout OK,
            if (status[i] == Statement.EXECUTE_FAILED) {
              System.out.println("Erreur sur la requète " + i);
              // si tu veux connaitre la requète i, il faut sauver la requète
              // dans un tableau avant...
            }
          }
        }

      }

      System.out.println("Found " + nb_transfert + " data to transfert.");

      // initialise le tableau des codes de retour pour chaque requète.
      int[] status = new int[nb_transfert];
      // execute l'insert multiple (tout le buffer de requètes est lu)
      // NOTE : lorsque beaucoup de géométries (les contours MULTIPOLYGON des
      // communes par exemples) sont dans le buffer
      // celui ci devient vite trop gros pour la taille de la JVM -
      // il ne faut pas hésiter à vider ce buffer (par exécution) toutes les 50
      // communes par exemple
      status = stInsertData.executeBatch();

      // affiche si tout s'est bien passé dans l'insertion.
      for (int i = 0; i < status.length; i++) {
        // status[i] == 1 : tout OK,
        if (status[i] == Statement.EXECUTE_FAILED) {
          System.out.println("Erreur sur la requète " + i);
          // si tu veux connaitre la requète i, il faut sauver la requète dans
          // un tableau avant...
        }
      }

    } catch (SQLException e) {
      // afficher la stack trace pour comprendre l'erreur
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      // on passe toujours dans un finally, qu'il y ait eu Exception ou non.
      // ceci garantit de toujours fermer une base après l'avoir ouvert
      // autrement on peut se retrouver à bouffer toutes les ressources de sa
      // base
      // (et c'est pas drole quand vous avez excédé les 100 connections max
      // simultanées possibles)
      IOUtils.closeDBConnection(destDbConnection);
    }
  }
}
