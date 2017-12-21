package hmmmatching.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;

public class HMMMatchingLauncher {

  public static IPopulation<IFeature> run(String fileNetwork1, String fileNetwork2, double selection, int stroke_length, boolean resampling, boolean lpsolving, long seed) {
    HMMParameters parameters = new HMMParameters(selection, stroke_length, resampling, lpsolving);
    CarteTopo netRef = new CarteTopo("");
    CarteTopo netComp = new CarteTopo("");
    HMMMatchingPreProcess hmmPreProcess = new HMMMatchingPreProcess();
    hmmPreProcess.loadAndPrepareNetworks(netRef, fileNetwork1, netComp, fileNetwork2, parameters);
    HMMMatchingProcess hmmProcess = new HMMMatchingProcess();
    Random rng = new Random(seed);
    Map<Arc, Set<Arc>> matchingF = hmmProcess.matchNetworks(netRef, netComp, parameters, rng);
    return Utils.exportMatchinLinks2(matchingF);
  }

  public static List<List<String>> runString(String fileNetwork1, String fileNetwork2, String idAttribute1, String idAttribute2, double selection, int stroke_length,
      boolean resampling, boolean lpsolving, long seed) {
    HMMParameters parameters = new HMMParameters(selection, stroke_length, resampling, lpsolving);
    CarteTopo netRef = new CarteTopo("");
    CarteTopo netComp = new CarteTopo("");
    HMMMatchingPreProcess hmmPreProcess = new HMMMatchingPreProcess();
    Random rng = new Random(seed);
    hmmPreProcess.loadAndPrepareNetworks(netRef, fileNetwork1, netComp, fileNetwork2, parameters);
    HMMMatchingProcess hmmProcess = new HMMMatchingProcess();
    Map<Arc, Set<Arc>> matchingF = hmmProcess.matchNetworks(netRef, netComp, parameters, rng);
    Map<IFeature, Set<IFeature>> matching = new HashMap<>();
    for (Arc a : matchingF.keySet()) {
      for (IFeature f1 : a.getCorrespondants()) {
        for (Arc a2 : matchingF.get(a)) {
          for (IFeature f2 : a2.getCorrespondants()) {
            if (matching.containsKey(f1)) {
              if (!matching.get(f1).contains(f2)) {
                matching.get(f1).add(f2);
              }
            } else {
              matching.put(f1, new HashSet<>(Arrays.asList(f2)));
            }
          }
        }

      }
    }
    List<List<String>> results = new ArrayList<>();
    for (IFeature a : matching.keySet()) {
      String id1 = a.getAttribute(idAttribute1).toString();
      for (IFeature b : matching.get(a)) {
        String id2 = b.getAttribute(idAttribute2).toString();
        results.add(Arrays.asList(id1, id2));
      }
    }
    return results;
  }

  public static List<List<String>> runStringFromDouble(String fileNetwork1, String fileNetwork2, String idAttribute1, String idAttribute2, double selection, double stroke_length,
      double resampling, double lpsolving, long seed) {
    return runString(fileNetwork1, fileNetwork2, idAttribute1, idAttribute2, selection, new Double(stroke_length).intValue(), resampling > 0.5, lpsolving > 0.5, seed);
  }

  public static void main(String args[]) {

    /*
     * Lectures
     */
    // String fileNetwork1
    // ="/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/verniquet_l93_utf8_corr.shp";
    // String fileNetwork2 =
    // "/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/jacoubet_l93_utf8.shp";
    // String inComp = "/home/bcostes/Bureau/t.shp";
    //
    // String inRef = "/home/bcostes/Bureau/matching_hmm/osm_railways.shp";
    // String inComp = "/home/bcostes/Bureau/matching_hmm/bdcarto_railways.shp";

    //
    // String inRef = "/home/bcostes/Bureau/matching_hmm/roads_osm2.shp";
    // String inComp = "/home/bcostes/Bureau/matching_hmm/roads_bdcarto2.shp";
    //
    // String inRef = "/home/bcostes/Bureau/matching_hmm/hydro_bdcarto.shp";
    // String inComp = "/home/bcostes/Bureau/matching_hmm/hydro_bdtopo.shp";
    //////
    // String inRef = "/home/bcostes/Bureau/osm1.shp";
    // String inComp = "/home/bcostes/Bureau/bdcarto1.shp";
    //
    String fileNetwork1 = "networkmatching/matching/snapshot_1825.0_1836.0_edges.shp";
    String fileNetwork2 = "networkmatching/matching/snapshot_1784.0_1791.0_edges.shp";

    /*
     * Parametres
     */

    double selection = 25;
    double stroke_length = 5;
    double resampling = 0;
    double lpsolving = 1.0;
    long seed = 42L;
    List<List<String>> l = runStringFromDouble(fileNetwork1, fileNetwork2, "ID", "ID", selection, stroke_length, resampling, lpsolving, seed);
    System.out.println(l);
    // IPopulation<IFeature> out = runStringFromDouble(fileNetwork1, fileNetwork2, selection, stroke_length, resampling, lpsolving);
    // ShapefileWriter.write(out, "results/result_paris_lpsolve_25_resampling.shp");
  }
}
