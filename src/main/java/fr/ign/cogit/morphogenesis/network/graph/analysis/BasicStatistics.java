package fr.ign.cogit.morphogenesis.network.graph.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.morphogenesis.network.graph.AbstractGraph;
import fr.ign.cogit.oldies.network.graph.factory.AbstractGraphFactory;

public class BasicStatistics {

  public static void logLengths(String inputFile, String outputFile,
      int decimals, char del) {
    IPopulation<IFeature> pop = ShapefileReader.read(inputFile);
    String ouput = "";
    Map<Double, Integer> map = new HashMap<Double, Integer>();

    for (IFeature feat : pop) {
      double length = feat.getGeom().length();
      if (length < 1) {
        continue;
      }
      length = Math.log10(length);
      length = (Math.round(length * Math.pow(10, decimals)))
          / (Math.pow(10, decimals)); // arondi à un decimals chiffres après
                                      // la virgule
      if (map.containsKey(length)) {
        map.put(length, map.get(length) + 1);
      } else {
        map.put(length, 1);
      }
    }
    List<Double> classes = new ArrayList<Double>();
    classes.addAll(map.keySet());
    Collections.sort(classes);
    int tot = 0;
    for (int i = 0; i < classes.size(); i++) {
      tot += map.get(classes.get(i));
    }

    for (Double length : classes) {
      ouput += Double.toString(length) + del
          + Double.toString(100 * map.get(length) / (double) tot) + "\n";
    }

    try {
      FileWriter fw = new FileWriter(new File(outputFile));
      BufferedWriter bw = new BufferedWriter(fw);
      bw.write(ouput);
      bw.flush();
      bw.close();
      fw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public static void connectivities(String inputFile, String outputFile,
      char del) {
    IPopulation<IFeature> pop = ShapefileReader.read(inputFile);
    String ouput = "";
    AbstractGraph graph = AbstractGraphFactory.getFactory(
        AbstractGraphFactory.PRIMAL_TOPOLOGICAL_GRAPH).newGraph(pop, 0);

    Map<Integer, Integer> map = new HashMap<Integer, Integer>();

    for (IGeometry geom : graph.getVertices()) {
      int conn = graph.getNeighbors(geom).size();
      if (map.containsKey(conn)) {
        map.put(conn, map.get(conn) + 1);
      } else {
        map.put(conn, 1);
      }
    }
    List<Integer> classes = new ArrayList<Integer>();
    classes.addAll(map.keySet());
    Collections.sort(classes);
    int tot = 0;
    for (int i = 0; i < classes.size(); i++) {
      tot += map.get(classes.get(i));
    }

    for (Integer index : classes) {
      ouput += Integer.toString(index) + del
          + Double.toString(100 * map.get(index) / (double) tot) + "\n";
    }

    try {
      FileWriter fw = new FileWriter(new File(outputFile));
      BufferedWriter bw = new BufferedWriter(fw);
      bw.write(ouput);
      bw.flush();
      bw.close();
      fw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public static void main(String args[]) {

    BasicStatistics
        .logLengths(
            "/media/Data/Benoit/these/analyses/centralites/strokes_L93/poubelle_emprise_strokes.shp",
            "/home/bcostes/Bureau/test.txt", 1, ';');

    BasicStatistics
        .connectivities(
            "/media/Data/Benoit/these/analyses/centralites/FILAIRES_L93/1885_ALPHAND_POUBELLE_emprise.shp",
            "/home/bcostes/Bureau/test2.txt", ';');

    BasicStatistics
        .connectivities(
            "/media/Data/Benoit/these/analyses/centralites/strokes_L93/poubelle_emprise_strokes.shp",
            "/home/bcostes/Bureau/test3.txt", ';');

  }
}
