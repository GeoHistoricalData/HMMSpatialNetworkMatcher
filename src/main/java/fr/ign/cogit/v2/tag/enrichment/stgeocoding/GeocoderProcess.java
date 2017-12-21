package fr.ign.cogit.v2.tag.enrichment.stgeocoding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import geocodage.LexicalTools;
import geocodage.namedStreet.NamedStreet;

public class GeocoderProcess {

  public static Set<GeocodedCandidate> geocode(TextualAdress textualAdress,  Map<FuzzyTemporalInterval, Set<AdressPoint>>  pointsAdresses, STGeocoder stgeocoder){

    Set<GeocodedCandidate> geocodedCandidates = new HashSet<GeocodedCandidate>();
    // clusters
    Map<FuzzyTemporalInterval, Map<String,Set<AdressPoint>>> clusters = GeocoderProcess.clustering(pointsAdresses);


    //On va déterminer un candidat pour chaque lcuster
    for(FuzzyTemporalInterval t: clusters.keySet()){
      if(clusters.get(t).isEmpty()){
        //pas de candidat, on géocode à la rue
        // TODO : geocode street
        continue;
      }
      if(!textualAdress.getNum().equals("")){
        // il y a un numéro attaché à l'adresse qu'on cherche à géocoder
        try {
          // conversion en entier
          int numI = Integer.parseInt(textualAdress.getNum());

          for(String clusterName: clusters.get(t).keySet()){
            Set<AdressPoint> adressesMatch = clusters.get(t).get(clusterName);
            // pour chaque cluster de points à cette date

            AdressPoint adrFound = null;
            for (AdressPoint ad : adressesMatch) {
              try {
                int numAd = Integer.parseInt(ad.getNum());
                if (numAd == numI) {
                  adrFound = ad;
                  break;
                }
              } catch (NumberFormatException e) {
                continue;
              }
            }
            if (adrFound != null) {
              // on a troucé l'adresse exacte !
              GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
              adresseGeocoded.type = GeocodeType.EXACT;
              adresseGeocoded.pos = adrFound.getPosition();
              adresseGeocoded.name = adrFound.getOriginalName();
              adresseGeocoded.date = t;
              geocodedCandidates.add(adresseGeocoded);
              continue;
            }

            // au moins une adresse de base trouvée dans la bonne rue pour ce cluster
            if (adressesMatch.size()== 1) {
              // 1 adresse de base précisémment
              // Comme on ne peut pas interpoler avec un seul numéro, on place à cet endroit et on indique que
              // le géocodage se fait à la rue
              GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
              adresseGeocoded.type = GeocodeType.STREET;
              adresseGeocoded.pos = adressesMatch.iterator().next().getPosition();
              adresseGeocoded.name = adressesMatch.iterator().next().getOriginalName();
              adresseGeocoded.date = t;
              geocodedCandidates.add(adresseGeocoded);
              continue;
            } 
            // au moins deux adresses de base dans la bonne rue
            int minSup = 10000;
            AdressPoint adrSup = null;
            int maxInf = -1;
            AdressPoint adrInf = null;

            boolean evenNumber = (numI % 2 == 0);
            Map<Integer, AdressPoint> adrSamePariteSup = new HashMap<Integer, AdressPoint>();
            Map<Integer, AdressPoint> adrSamePariteInf = new HashMap<Integer, AdressPoint>();
            List<Integer> numberSamePariteSup = new ArrayList<Integer>();
            List<Integer> numberSamePariteInf = new ArrayList<Integer>();

            for (AdressPoint ad : adressesMatch) {
              try {
                int numAd = Integer.parseInt(ad.getNum());
                if (evenNumber == (numAd % 2 == 0)) {
                  // meme paritée
                  if (numAd > numI) {
                    adrSamePariteSup.put(numAd, ad);
                    numberSamePariteSup.add(numAd);
                  } else {
                    adrSamePariteInf.put(numAd, ad);
                    numberSamePariteInf.add(numAd);
                  }
                }

              } catch (NumberFormatException e) {
                continue;
              }
            }

            // tri ordre croissant
            Collections.sort(numberSamePariteInf);
            Collections.sort(numberSamePariteSup);
            if (!numberSamePariteInf.isEmpty()) {
              // adresse meme paritée inférieure => on prend la plus grande, donc la
              // dernière de la liste
              maxInf = numberSamePariteInf.get(numberSamePariteInf.size() - 1);
              adrInf = adrSamePariteInf.get(maxInf);
            }
            if (!numberSamePariteSup.isEmpty()) {
              // adresse meme paritée supérieure => on prend la plus petite, donc la
              // première de la liste
              minSup = numberSamePariteSup.get(0);
              adrSup = adrSamePariteSup.get(minSup);
            }

            // a t'on une borne inf et sup ?
            if (adrSup != null && adrInf != null) {
              // pas de borne sup et inf
              // Interpolation avec minSup et maxInf, quelque soit la parité
              GeocodedCandidate adresseGeocoded =  interpolationSupInf(adrSup, adrInf, numI, maxInf,
                  minSup, stgeocoder);
              geocodedCandidates.add(adresseGeocoded);

            } else if (adrSup != null) {
              // pas de borne inf mais une borne sup
              // on va interpoler en recréant la nameStreet associée à cette adresse
              NamedStreet namedStreet = stgeocoder.getNamedStreetMapping().get(adrSup);
              // y a t(il une named street ?
              if (namedStreet == null) {
                // pas de NamedStreet associée à la borne sup, on le place sur la borne sup
                GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
                adresseGeocoded.type = GeocodeType.STREET_SUP;
                adresseGeocoded.pos = adrSup.getPosition();
                adresseGeocoded.name = adrSup.getOriginalName();
                adresseGeocoded.date = t;
                geocodedCandidates.add(adresseGeocoded);
                continue;
              }
              //sinon one le place en interpolant
              if (adrSamePariteSup.size() <= 1) {
                // on le place sur la borne sup car interpolation pas possible
                GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
                adresseGeocoded.type = GeocodeType.STREET_SUP;
                adresseGeocoded.pos = adrSup.getPosition();
                adresseGeocoded.name = adrSup.getOriginalName();
                adresseGeocoded.date = t;
                geocodedCandidates.add(adresseGeocoded);
                continue;
              }
              GeocodedCandidate adresseGeocoded =  interpolationSup(adrSup, numI, minSup,
                  adrSamePariteSup, numberSamePariteSup, stgeocoder);
              geocodedCandidates.add(adresseGeocoded);
              continue;
            } else if (adrInf != null) {
              // pas de borne sup mais une borne inf
              // on place entre la borne sup et le début de la rue
              NamedStreet namedStreet = stgeocoder.getNamedStreetMapping().get(adrInf);
              if (namedStreet == null) {
                // on le place sur la borne inf
                GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
                adresseGeocoded.type = GeocodeType.STREET_INF;
                adresseGeocoded.pos = adrInf.getPosition();
                adresseGeocoded.name = adrInf.getOriginalName();
                adresseGeocoded.date = t;
                geocodedCandidates.add(adresseGeocoded);
                continue;
              }
              if (adrSamePariteInf.size() <= 1) {
                // on le place sur la borne inf
                GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
                adresseGeocoded.type = GeocodeType.STREET_INF;
                adresseGeocoded.pos = adrInf.getPosition();
                adresseGeocoded.name = adrInf.getOriginalName();
                adresseGeocoded.date = t;
                geocodedCandidates.add(adresseGeocoded);
                continue;
              }
              GeocodedCandidate adresseGeocoded = interpolationInf(adrInf, numI, maxInf,
                  adrSamePariteInf, numberSamePariteInf, stgeocoder);
              geocodedCandidates.add(adresseGeocoded);
              continue;
            } else {
              // on essaye de placer à la rue
              GeocodedCandidate adresseGeocoded= geocodeStreet(adressesMatch.iterator().next(), t, stgeocoder);
              if(adresseGeocoded == null){
                continue;
              }
              geocodedCandidates.add(adresseGeocoded);
              continue;
            }      
          }

        } catch (NumberFormatException e) {
          continue;
        }
      }
      else {
        for(String clusterName: clusters.get(t).keySet()){
          Set<AdressPoint> adressesMatch = clusters.get(t).get(clusterName);
          // aucun numéro correspondant à ce nom de rue
          // si il y a des strokes comme base, on place au milieu du stroke
          // si il ya plusieur named street de meme nom en en choisi une et on
          // insique que c'est incertain
          NamedStreet street = stgeocoder.getNamedStreetMapping().get(adressesMatch.iterator().next());
          if (street == null) {
            continue;
          }
          String name = street.getName().toLowerCase();
          int cpt = 0;
          for (NamedStreet s : stgeocoder.getNamedStreet().get(t)) {
            if (s.getName().toLowerCase().equals(name)) {
              cpt++;
            }
          }
          if (cpt > 1) {
            street = stgeocoder.getNamedStreetMapping().get(adressesMatch.iterator().next());
            if (street == null) {
              continue;
            }
            GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
            adresseGeocoded.type = GeocodeType.UNCERTAIN;
            adresseGeocoded.pos= Operateurs.milieu(street.getGeom());
            adresseGeocoded.date = t;
            adresseGeocoded.name = adressesMatch.iterator().next().getOriginalName();
            geocodedCandidates.add(adresseGeocoded);
            continue;
          } else if (cpt == 1) {
            street =stgeocoder.getNamedStreetMapping().get(adressesMatch.iterator().next());
            if (street == null) {
              continue;
            }
            GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
            adresseGeocoded.type = GeocodeType.STREET;
            adresseGeocoded.pos= Operateurs.milieu(street.getGeom());
            adresseGeocoded.date = t;
            adresseGeocoded.name = adressesMatch.iterator().next().getOriginalName();
            geocodedCandidates.add(adresseGeocoded);
            continue;

          } else {
            continue;
          }
        }
      }

    }

    return geocodedCandidates;
  }

  /**
   * Regroupe les points adresses pour constituer des "traces gps"
   * Utilise le nom de la rue et un critère de distance
   * @return
   */
  private static Map<FuzzyTemporalInterval, Map<String,Set<AdressPoint>>> clustering( Map<FuzzyTemporalInterval, Set<AdressPoint>>  temporalClusters){
    //************************* Clustering toponymique **************************
    //on va regrouper par aggrégation des stedge associés aux pts adresses
    Map<FuzzyTemporalInterval, Map<String,Set<AdressPoint>>> finalClusters = new HashMap<FuzzyTemporalInterval, Map<String,Set<AdressPoint>>>();
    for(FuzzyTemporalInterval t: temporalClusters.keySet()){
      Map<String,Set<AdressPoint>> clusters= new HashMap<String, Set<AdressPoint>>();
      for(AdressPoint adp: temporalClusters.get(t)){
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
      finalClusters.put(t, clusters);
    }    
    return finalClusters;
  }


  /**
   * Interpolation si minSup et maxInf ok, meme paritée
   * @param adrSup
   * @param adrInf
   * @param numI
   * @param maxInf
   * @param minSup
   * @return
   */
  private static GeocodedCandidate interpolationSupInf(AdressPoint adrSup,
      AdressPoint adrInf, int numI, int maxInf, int minSup, STGeocoder stgeocoder) {
    
        
    
    // interpolation entre les position des num en prnanten compte
    // leur valeur
    NamedStreet streetSup = stgeocoder.getNamedStreetMapping().get(adrSup);
    NamedStreet streetInf = stgeocoder.getNamedStreetMapping().get(adrInf);
    

    

    if (streetSup == null || streetInf == null) {
      // sinon on le place au milieu ...
      double x = (adrSup.getPosition().getX() + adrInf.getPosition().getX()) / 2.;
      double y = (adrSup.getPosition().getY() +adrInf.getPosition().getY()) / 2.;

      GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
      adresseGeocoded.type = GeocodeType.UNCERTAIN;
      adresseGeocoded.pos = new DirectPosition(x, y);
      adresseGeocoded.name = adrSup.getOriginalName();
      adresseGeocoded.date = adrSup.getTime();
      return adresseGeocoded;
    }

    // si il ne sont pas sur la meme named street aggregate, les deux
    // adresses sont probablement
    // trop éloignée, on les place sur un des aggregat, en indiquant que
    // c'est incertain

    if (!streetSup.equals(streetInf)) {
      GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
      adresseGeocoded.type = GeocodeType.UNCERTAIN;
      adresseGeocoded.pos =  Operateurs.milieu(streetSup.getGeom());
      adresseGeocoded.name = adrSup.getOriginalName();
      adresseGeocoded.date = adrSup.getTime();
      return adresseGeocoded;
    }

    NamedStreet street = streetSup;

    // rééchantillonage de la ligne
    ILineString lineEch = Operateurs.echantillone(street.getGeom(), 1.);
    // on récupre le point de controle le plus proche de chaque point
    // projeté
    int iSup = -1;
    int iInf = -1;
    IDirectPosition supProj = Operateurs.projection(adrSup.getPosition(),
        lineEch);
    IDirectPosition infProj = Operateurs.projection(adrInf.getPosition(),
        lineEch);
    double dmin = Double.MAX_VALUE;
    int cpt = 0;
    for (IDirectPosition pos : lineEch.getControlPoint()) {
      double d = pos.distance(supProj);
      if (d < dmin) {
        dmin = d;
        iSup = cpt;
      }
      cpt++;
    }
    dmin = Double.MAX_VALUE;
    cpt = 0;
    for (IDirectPosition pos : lineEch.getControlPoint()) {
      double d = pos.distance(infProj);
      if (d < dmin) {
        dmin = d;
        iInf = cpt;
      }
      cpt++;
    }
    // récupération de l'asbcisse curviligne des deux points
    double absSup = Operateurs.abscisseCurviligne(lineEch, iSup);
    double absInf = Operateurs.abscisseCurviligne(lineEch, iInf);
    // interpolation en fonction du num
    double a = (double) (absSup - absInf) / (double) (minSup - maxInf);
    double b = absInf - a * maxInf;
    double absPondere = a * numI + b;
    // Récupération des coordonnées du points correspondant
    IDirectPosition posMilieu = Operateurs.pointEnAbscisseCurviligne(lineEch,
        absPondere);

    GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
    adresseGeocoded.type = GeocodeType.INTERPOLATION;
    adresseGeocoded.pos =  posMilieu;
    adresseGeocoded.name = adrSup.getOriginalName();
    adresseGeocoded.date = adrSup.getTime();
    return adresseGeocoded; 
  }

  /**
   * Interpolation lorsqu'une adresse inférieure seulement est présente
   * @param adrSup
   * @param numI
   * @param minSup
   * @param adrSamePariteSup
   * @param adrDiffPariteSup
   * @return
   */
  private static GeocodedCandidate interpolationInf(AdressPoint adrInf, int numI,
      int maxInf, Map<Integer, AdressPoint> mapAdrInf, List<Integer> numberInf, STGeocoder stgeocoder) {

    NamedStreet streetInf = stgeocoder.getNamedStreetMapping().get(adrInf);

    // On commence par récupérer une autre adresse, forcémment
    // inférieure
    // à la projeter et a calculer son abscisse curviligne

    AdressPoint otherAdr = null;
    int max = -1;
    // si il ya d'autres adresses ?
    if (mapAdrInf.size() > 1) {
      // on prend la seconde
      otherAdr = mapAdrInf.get(numberInf.get(numberInf.size() - 2));
      max = numberInf.get(numberInf.size() - 2);
    } else {
      // sinon on prend place sur sup
      GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
      adresseGeocoded.type = GeocodeType.STREET;
      adresseGeocoded.pos =  Operateurs.milieu(streetInf.getGeom());
      adresseGeocoded.name = mapAdrInf.get(numberInf.get(0)).getOriginalName();
      adresseGeocoded.date = mapAdrInf.get(numberInf.get(0)).getTime();
      return adresseGeocoded;
    }

    if (otherAdr == null) {
      GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
      adresseGeocoded.type = GeocodeType.UNCERTAIN;
      adresseGeocoded.pos =  Operateurs.milieu(streetInf.getGeom());
      adresseGeocoded.name = mapAdrInf.get(numberInf.get(0)).getOriginalName();
      adresseGeocoded.date = mapAdrInf.get(numberInf.get(0)).getTime();
      return adresseGeocoded;
    }

    NamedStreet streetOther =stgeocoder.getNamedStreetMapping().get(otherAdr);

    if (streetOther == null) {
      GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
      adresseGeocoded.type = GeocodeType.UNCERTAIN;
      adresseGeocoded.pos =  Operateurs.milieu(streetInf.getGeom());
      adresseGeocoded.name = mapAdrInf.get(numberInf.get(0)).getOriginalName();
      adresseGeocoded.date = mapAdrInf.get(numberInf.get(0)).getTime();
      return adresseGeocoded;
    }

    // si il ne sont pas sur la meme named street, on place
    // arbitrairement le point sur l'une d'elle en indiquant que c'est
    // incertain
    if (!streetInf.equals(streetOther)) {
      GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
      adresseGeocoded.type = GeocodeType.UNCERTAIN;
      adresseGeocoded.pos =  Operateurs.milieu(streetInf.getGeom());
      adresseGeocoded.name = mapAdrInf.get(numberInf.get(0)).getOriginalName();
      adresseGeocoded.date = mapAdrInf.get(numberInf.get(0)).getTime();
      return adresseGeocoded;
    }

    ILineString lineEch = Operateurs.echantillone(streetInf.getGeom(), 1.);
    // Projection de la bornse sup
    IDirectPosition infProj = Operateurs.projection(adrInf.getPosition(),
        lineEch);
    int iInf = -1;
    double dmin = Double.MAX_VALUE;
    int cpt = 0;
    for (IDirectPosition pos : lineEch.getControlPoint()) {
      double d = pos.distance(infProj);
      if (d < dmin) {
        dmin = d;
        iInf = cpt;
      }
      cpt++;
    }
    // récupération de l'asbcisse curviligne de la borne sup
    double absInf = Operateurs.abscisseCurviligne(lineEch, iInf);
    // récupération du sens du stroke ...

    IDirectPosition otherAdrProj = Operateurs.projection(
        otherAdr.getPosition(), lineEch);
    int iotherAdrProj = -1;
    dmin = Double.MAX_VALUE;
    cpt = 0;
    for (IDirectPosition pos : lineEch.getControlPoint()) {
      double d = pos.distance(otherAdrProj);
      if (d < dmin) {
        dmin = d;
        iotherAdrProj = cpt;
      }
      cpt++;
    }
    // récupération de l'asbcisse curviligne de la borne sup
    double absOtherAdrProj = Operateurs.abscisseCurviligne(lineEch,
        iotherAdrProj);

    // si abscisse superieur à abscisse de la borne inf => premier point
    // de controle
    double absStreetStart = 0;
    if (absOtherAdrProj > absInf) {
      absStreetStart = Operateurs.abscisseCurviligne(lineEch, 0);
    } else {
      absStreetStart = Operateurs.abscisseCurviligne(lineEch, lineEch
          .getControlPoint().size() - 1);
    }
    // Récupération du point au milieu
    double a = (double) (absInf - absOtherAdrProj) / (double) (maxInf - max);
    double b = absInf - a * maxInf;
    double absPondere = a * numI + b;
    if (absStreetStart == 0 && absPondere < 0) {
      absPondere = 0;
    } else if (absStreetStart != 0 && absPondere > absStreetStart) {
      absPondere = absStreetStart;
    }
    // Récupération des coordonnées du points correspondant
    IDirectPosition posMilieu = Operateurs.pointEnAbscisseCurviligne(lineEch,
        absPondere);


    GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
    adresseGeocoded.type = GeocodeType.POOR_INTERPOLATION;
    adresseGeocoded.pos =posMilieu;
    adresseGeocoded.name = mapAdrInf.get(numberInf.get(0)).getOriginalName();
    adresseGeocoded.date = mapAdrInf.get(numberInf.get(0)).getTime();
    return adresseGeocoded;
  }

  private static GeocodedCandidate geocodeStreet(AdressPoint adSource, FuzzyTemporalInterval t, STGeocoder stgeocoder) {

    List<NamedStreet> potentialNamedStreet = new ArrayList<NamedStreet>();
    for (NamedStreet steet : stgeocoder.getNamedStreet().get(t)) {
      if (steet.getName().equals("")) {
        continue;
      }

      if (LexicalTools.dDamarauLevenshtein(adSource.getPhoneticName(),
          LexicalTools.phoneticName(steet.getName())) <= 1
          || (adSource.getName().startsWith(steet.getName().toLowerCase()) && steet
              .getName().length() > 1)
              || (steet.getName().toLowerCase().startsWith(adSource.getName()) && adSource
                  .getName().length() > 1)) {

        potentialNamedStreet.add(steet);

      }
    }

    if (!potentialNamedStreet.isEmpty()) {
      if (potentialNamedStreet.size() == 1) {
        NamedStreet street = potentialNamedStreet.get(0);
        GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
        adresseGeocoded.type = GeocodeType.STREET;
        adresseGeocoded.pos = Operateurs.milieu(street.getGeom());
        adresseGeocoded.name = adSource.getOriginalName();
        adresseGeocoded.date = t;
        return adresseGeocoded;

      } else {
        // si plusieurs named street potentielle on choisi la plus
        // proche au sens de levens
        double dmin = Double.MAX_VALUE;
        NamedStreet goodStreet = null;
        for (NamedStreet st : potentialNamedStreet) {
          double d = LexicalTools.dDamarauLevenshteinNormalized(st.getName(),
              adSource.getName());
          if (d < dmin) {
            d = dmin;
            goodStreet = st;
          }
        }

        GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
        adresseGeocoded.type = GeocodeType.STREET;
        adresseGeocoded.pos =  Operateurs.milieu(goodStreet.getGeom());
        adresseGeocoded.name = adSource.getOriginalName();
        adresseGeocoded.date = t;
        return adresseGeocoded;
      }
    } else {
      return null;
    }
  }

  /**
   * Interpolation lorsqu'une adresse supérieur seulement est présente
   * @param adrSup
   * @param numI
   * @param minSup
   * @param adrSamePariteSup
   * @param adrDiffPariteSup
   * @return
   */
  private static GeocodedCandidate interpolationSup(AdressPoint adrSup, int numI,
      int minSup, Map<Integer, AdressPoint> mapAdrSup, List<Integer> numberSup, STGeocoder stgeocoder) {
    // on place entre la borne sup et le début de la rue
    NamedStreet streetSup = stgeocoder.getNamedStreetMapping().get(adrSup);

    // On commence par récupérer une autre adresse, forcémment
    // supérieure
    // à la projeter et a calculer son abscisse curviligne

    AdressPoint otherAdr = null;
    int min = -1;
    // si il ya d'autres adresses ?
    if (mapAdrSup.size() > 1) {
      // on prend la seconde
      otherAdr = mapAdrSup.get(numberSup.get(1));
      min = numberSup.get(1);
    } else {
      // sinon on prend place a la rue
      GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
      adresseGeocoded.type = GeocodeType.STREET;
      adresseGeocoded.pos =Operateurs.milieu(streetSup.getGeom());
      adresseGeocoded.name = mapAdrSup.get(numberSup.get(0)).getOriginalName();
      adresseGeocoded.date = mapAdrSup.get(numberSup.get(0)).getTime();
      return adresseGeocoded;   
    }

    NamedStreet streetOther = stgeocoder.getNamedStreetMapping().get(otherAdr);

    if (streetOther == null) {
      // on place
      // arbitrairement le point sur l'une d'elle en indiquant que c'est
      // incertain
      GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
      adresseGeocoded.type = GeocodeType.UNCERTAIN;
      adresseGeocoded.pos =Operateurs.milieu(streetSup.getGeom());
      adresseGeocoded.name = mapAdrSup.get(numberSup.get(0)).getOriginalName();
      adresseGeocoded.date = mapAdrSup.get(numberSup.get(0)).getTime();
      return adresseGeocoded; 
    }

    // si il ne sont pas sur la meme named street, on place
    // arbitrairement le point sur l'une d'elle en indiquant que c'est
    // incertain

    if (!streetSup.equals(streetOther)) {
      GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
      adresseGeocoded.type = GeocodeType.UNCERTAIN;
      adresseGeocoded.pos =Operateurs.milieu(streetSup.getGeom());
      adresseGeocoded.name = mapAdrSup.get(numberSup.get(0)).getOriginalName();
      adresseGeocoded.date = mapAdrSup.get(numberSup.get(0)).getTime();
      return adresseGeocoded; 
    }

    ILineString lineEch = Operateurs.echantillone(streetSup.getGeom(), 1.);
    // Projection de la borne sup
    IDirectPosition supProj = Operateurs.projection(adrSup.getPosition(),
        lineEch);
    int iSup = -1;
    double dmin = Double.MAX_VALUE;
    int cpt = 0;
    for (IDirectPosition pos : lineEch.getControlPoint()) {
      double d = pos.distance(supProj);
      if (d < dmin) {
        dmin = d;
        iSup = cpt;
      }
      cpt++;
    }
    // récupération de l'asbcisse curviligne de la borne sup
    double absSup = Operateurs.abscisseCurviligne(lineEch, iSup);
    // récupération du sens du stroke ...

    IDirectPosition otherAdrProj = Operateurs.projection(
        otherAdr.getPosition(), lineEch);
    int iotherAdrProj = -1;
    dmin = Double.MAX_VALUE;
    cpt = 0;
    for (IDirectPosition pos : lineEch.getControlPoint()) {
      double d = pos.distance(otherAdrProj);
      if (d < dmin) {
        dmin = d;
        iotherAdrProj = cpt;
      }
      cpt++;
    }
    // récupération de l'asbcisse curviligne de la borne sup
    double absOtherAdrProj = Operateurs.abscisseCurviligne(lineEch,
        iotherAdrProj);

    // si abscisse supérieur à abscisse de la borne sup => premier point
    // de controle
    double absStreetStart = 0;
    if (absOtherAdrProj > absSup) {
      absStreetStart = Operateurs.abscisseCurviligne(lineEch, 0);
    } else {
      absStreetStart = Operateurs.abscisseCurviligne(lineEch, lineEch
          .getControlPoint().size() - 1);
    }
    // Récupération du point pondéré en fonction du num de l'adresse
    double a = (double) (absSup - absOtherAdrProj) / (double) (minSup - min);
    double b = absSup - a * minSup;
    double absPondere = a * numI + b;
    if (absStreetStart == 0 && absPondere < 0) {
      absPondere = 0;
    } else if (absStreetStart != 0 && absPondere > absStreetStart) {
      absPondere = absStreetStart;
    }
    // Récupération des coordonnées du points correspondant
    IDirectPosition posMilieu = Operateurs.pointEnAbscisseCurviligne(lineEch,
        absPondere);


    GeocodedCandidate adresseGeocoded = new GeocodedCandidate();
    adresseGeocoded.type = GeocodeType.POOR_INTERPOLATION;
    adresseGeocoded.pos =posMilieu;
    adresseGeocoded.name = mapAdrSup.get(numberSup.get(0)).getOriginalName();
    adresseGeocoded.date = mapAdrSup.get(numberSup.get(0)).getTime();
    return adresseGeocoded; 
  }

}
