package fr.ign.cogit.morphogenesis.network.graph.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.morphogenesis.network.graph.AbstractGraph;
import fr.ign.cogit.morphogenesis.network.graph.DualTopologicalGraph;
import fr.ign.cogit.morphogenesis.network.graph.GeometricalGraph;
import fr.ign.cogit.morphogenesis.network.graph.PrimalTopologicalGraph;

/**
 * Permet d'exporter les centralités calculées pour un graph dans un fichier
 * commun
 * @author bcostes
 * 
 */
public class ExportCentralities {

  /**
   * Export des centralités calculées sur un graphe géométrique
   * @param graph
   * @param file
   */
  private static void exportGeometricalGraph(GeometricalGraph graph,
      String file, char c) {
    // création dossier temp
    String stmp = "DONT_TOUCH_ME_IM_GONNA_BE_DELETED_AUTOMATICALLY";
    File rep = new File(file.substring(0, file.lastIndexOf(File.separator) + 1)
        + stmp);

    if (rep.exists()) {
      File[] filesTmp = rep.listFiles();
      for (File f : filesTmp) {
        if (f.exists()) {
          f.delete();
        }
      }
      rep.delete();
    }
    rep.mkdir();
    // Export des fichiers temporaires
    String fileName = rep + "/";
    graph.exportDegreeCentrality(null, fileName + "degreeCentrality.shp");
    graph.exportClosenessCentrality(null, fileName + "closenessCentrality.shp");
    graph.exportBetweenessCentrality(null, fileName
        + "betweennessCentrality.shp");
    graph.exportPageRank(0.7, null, fileName + "pageRank.shp");
    graph.exportControlMetric(null, fileName + "controlMetric.shp");
    graph.exportStraightnessCentrality(null, fileName
        + "straightnessCentrality.shp");
    graph.exportRandomWalk(null, 1000, fileName + "randomWalk.shp");

    // table de mapping entre les géométries et leurs centralités
    Map<String, List<Double>> map = new HashMap<String, List<Double>>();

    // Lecture sequentielle des fichiers
    String s = "";
    File[] filesTmp = rep.listFiles();
    for (File f : filesTmp) {
      String fileType = f.toString().substring(
          f.toString().lastIndexOf('.') + 1, f.toString().length());
      if (!fileType.toLowerCase().equals("shp")) {
        continue;
      }

      // pour l'entete du fichier
      s += f.getName().substring(0, f.getName().lastIndexOf('.')) + c;

      IPopulation<IFeature> featTmp = ShapefileReader.read(f.toString());
      for (IFeature feat : featTmp) {
        double centrality = Double.parseDouble(feat.getAttribute("INDICATOR")
            .toString());
        if (!map.containsKey(feat.getGeom().toString())) {
          List<Double> l = new ArrayList<Double>();
          l.add(centrality);
          map.put(feat.getGeom().toString(), l);
        } else {
          List<Double> l = new ArrayList<Double>();
          l.addAll(map.get(feat.getGeom().toString()));
          l.add(centrality);
          map.put(feat.getGeom().toString(), l);
        }
      }
    }

    // on enlève le derniere séparateur
    s = s.substring(0, s.length() - 1);
    s += "\n";

    // Destruction des fichiers temporaires
    for (File f : filesTmp) {
      if (f.exists()) {
        f.delete();
      }
    }

    rep.delete();

    // sauvegarde dans un fichier
    int cpt = 1;
    for (String feat : map.keySet()) {
      s += Integer.toString(cpt) + c;
      for (Double centrality : map.get(feat.toString())) {
        s += Double.toString(centrality) + c;
      }
      s += "\n";
      cpt++;
    }
    s = s.substring(0, s.length() - 1); // on retire le dernier saut à la ligne

    FileWriter fr;
    try {
      fr = new FileWriter(file);
      BufferedWriter br = new BufferedWriter(fr);
      br.write(s);
      br.close();
      fr.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * Export des centralités calculées sur un graphe topologique primaire
   * @param graph
   * @param file
   */
  private static void exportPrimalTopologicalGraph(
      PrimalTopologicalGraph graph, String file, char c) {
    // création dossier temp
    String stmp = "DONT_TOUCH_ME_IM_GONNA_BE_DELETED_AUTOMATICALLY";
    File rep = new File(file.substring(0, file.lastIndexOf(File.separator) + 1)
        + stmp);

    if (rep.exists()) {
      File[] filesTmp = rep.listFiles();
      for (File f : filesTmp) {
        if (f.exists()) {
          f.delete();
        }
      }
      rep.delete();
    }
    rep.mkdir();
    // Export des fichiers temporaires
    String fileName = rep + "/";
    graph.exportDegreeCentrality(null, fileName + "degreeCentrality.shp");
    graph.exportClosenessCentrality(null, fileName + "closenessCentrality.shp");
    graph.exportBetweenessCentrality(null, fileName
        + "betweennessCentrality.shp");
    graph.exportPageRank(0.7, null, fileName + "pageRank.shp");
    graph.exportControlMetric(null, fileName + "controlMetric.shp");
    graph.exportGlobalIntegration(null, fileName + "globalIntegration.shp");
    graph.exportLocalIntegration(3, null, fileName + "localIntegration.shp");
    graph.exportSimplestCentrality(null, fileName + "simplestCentrality.shp");
    graph.exportRandomWalk(null, 1000, fileName + "randomWalk.shp");

    // table de mapping entre les géométries et leurs centralités
    Map<String, List<Double>> map = new HashMap<String, List<Double>>();

    // Lecture sequentielle des fichiers
    String s = "";
    File[] filesTmp = rep.listFiles();
    for (File f : filesTmp) {
      String fileType = f.toString().substring(
          f.toString().lastIndexOf('.') + 1, f.toString().length());
      if (!fileType.toLowerCase().equals("shp")) {
        continue;
      }

      // pour l'entete du fichier
      s += f.getName().substring(0, f.getName().lastIndexOf('.')) + c;

      IPopulation<IFeature> featTmp = ShapefileReader.read(f.toString());
      for (IFeature feat : featTmp) {
        double centrality = Double.parseDouble(feat.getAttribute("INDICATOR")
            .toString());
        if (!map.containsKey(feat.getGeom().toString())) {
          List<Double> l = new ArrayList<Double>();
          l.add(centrality);
          map.put(feat.getGeom().toString(), l);
        } else {
          List<Double> l = new ArrayList<Double>();
          l.addAll(map.get(feat.getGeom().toString()));
          l.add(centrality);
          map.put(feat.getGeom().toString(), l);
        }
      }
    }

    // on enlève le derniere séparateur
    s = s.substring(0, s.length() - 1);
    s += "\n";

    // Destruction des fichiers temporaires
    for (File f : filesTmp) {
      if (f.exists()) {
        f.delete();
      }
    }

    rep.delete();

    // sauvegarde dans un fichier
    int cpt = 1;
    for (String feat : map.keySet()) {
      s += Integer.toString(cpt) + c;
      for (Double centrality : map.get(feat.toString())) {
        s += Double.toString(centrality) + c;
      }
      s += "\n";
      cpt++;
    }
    s = s.substring(0, s.length() - 1); // on retire le dernier saut à la ligne

    FileWriter fr;
    try {
      fr = new FileWriter(file);
      BufferedWriter br = new BufferedWriter(fr);
      br.write(s);
      br.close();
      fr.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Export des centralités calculées sur un graphe topologique dual
   * @param graph
   * @param file
   */
  private static void exportDualTopologicalGraph(DualTopologicalGraph graph,
      String file, char c) {
    // création dossier temp
    String stmp = "DONT_TOUCH_ME_IM_GONNA_BE_DELETED_AUTOMATICALLY";
    File rep = new File(file.substring(0, file.lastIndexOf(File.separator) + 1)
        + stmp);

    if (rep.exists()) {
      File[] filesTmp = rep.listFiles();
      for (File f : filesTmp) {
        if (f.exists()) {
          f.delete();
        }
      }
      rep.delete();
    }
    rep.mkdir();
    // Export des fichiers temporaires
    String fileName = rep + "/";
    graph.exportDegreeCentrality(null, fileName + "degreeCentrality.shp");
    graph.exportClosenessCentrality(null, fileName + "closenessCentrality.shp");
    graph.exportBetweenessCentrality(null, fileName
        + "betweennessCentrality.shp");
    graph.exportPageRank(0.7, null, fileName + "pageRank.shp");
    graph.exportControlMetric(null, fileName + "controlMetric.shp");
    graph.exportGlobalIntegration(null, fileName + "globalIntegration.shp");
    graph.exportLocalIntegration(3, null, fileName + "localIntegration.shp");
    graph.exportRandomWalk(null, 1000, fileName + "randomWalk.shp");

    // table de mapping entre les géométries et leurs centralités
    Map<String, List<Double>> map = new HashMap<String, List<Double>>();

    // Lecture sequentielle des fichiers
    String s = "";
    File[] filesTmp = rep.listFiles();
    for (File f : filesTmp) {
      String fileType = f.toString().substring(
          f.toString().lastIndexOf('.') + 1, f.toString().length());
      if (!fileType.toLowerCase().equals("shp")) {
        continue;
      }

      // pour l'entete du fichier
      s += f.getName().substring(0, f.getName().lastIndexOf('.')) + c;

      IPopulation<IFeature> featTmp = ShapefileReader.read(f.toString());
      for (IFeature feat : featTmp) {
        double centrality = Double.parseDouble(feat.getAttribute("INDICATOR")
            .toString());
        if (!map.containsKey(feat.getGeom().toString())) {
          List<Double> l = new ArrayList<Double>();
          l.add(centrality);
          map.put(feat.getGeom().toString(), l);
        } else {
          List<Double> l = new ArrayList<Double>();
          l.addAll(map.get(feat.getGeom().toString()));
          l.add(centrality);
          map.put(feat.getGeom().toString(), l);
        }
      }
    }

    // on enlève le derniere séparateur
    s = s.substring(0, s.length() - 1);
    s += "\n";

    // Destruction des fichiers temporaires
    for (File f : filesTmp) {
      if (f.exists()) {
        f.delete();
      }
    }

    rep.delete();

    // sauvegarde dans un fichier
    int cpt = 1;
    for (String feat : map.keySet()) {
      s += Integer.toString(cpt) + c;
      for (Double centrality : map.get(feat.toString())) {
        s += Double.toString(centrality) + c;
      }
      s += "\n";
      cpt++;
    }
    s = s.substring(0, s.length() - 1); // on retire le dernier saut à la ligne

    FileWriter fr;
    try {
      fr = new FileWriter(file);
      BufferedWriter br = new BufferedWriter(fr);
      br.write(s);
      br.close();
      fr.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Export des centralités calculées sur un graphe topologique dual
   * @param graph
   * @param file
   */
  private static void exportAbstractGraph(File rep, String file, char c) {
    // table de mapping entre les géométries et leurs centralités
    Map<String, List<Double>> map = new HashMap<String, List<Double>>();

    // Lecture sequentielle des fichiers
    String s = "";
    File[] filesTmp = rep.listFiles();
    for (File f : filesTmp) {
      String fileType = f.toString().substring(
          f.toString().lastIndexOf('.') + 1, f.toString().length());
      if (!fileType.toLowerCase().equals("shp")) {
        continue;
      }

      // pour l'entete du fichier
      s += f.getName().substring(0, f.getName().lastIndexOf('.')) + c;

      IPopulation<IFeature> featTmp = ShapefileReader.read(f.toString());
      for (IFeature feat : featTmp) {
        double centrality = Double.parseDouble(feat.getAttribute("INDICATOR")
            .toString());
        if (!map.containsKey(feat.getGeom().toString())) {
          List<Double> l = new ArrayList<Double>();
          l.add(centrality);
          map.put(feat.getGeom().toString(), l);
        } else {
          List<Double> l = new ArrayList<Double>();
          l.addAll(map.get(feat.getGeom().toString()));
          l.add(centrality);
          map.put(feat.getGeom().toString(), l);
        }
      }
    }

    // on enlève le derniere séparateur
    s = s.substring(0, s.length() - 1);
    s += "\n";

    // sauvegarde dans un fichier
    int cpt = 1;
    for (String feat : map.keySet()) {
      s += Integer.toString(cpt) + c;
      for (Double centrality : map.get(feat.toString())) {
        s += Double.toString(centrality) + c;
      }
      s += "\n";
      cpt++;
    }
    s = s.substring(0, s.length() - 1); // on retire le dernier saut à la ligne

    FileWriter fr;
    try {
      fr = new FileWriter(file);
      BufferedWriter br = new BufferedWriter(fr);
      br.write(s);
      br.close();
      fr.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Méthode générique principale publique (mapping vers la méthode adéquate)
   * @param graph
   * @param file
   */
  public static void export(AbstractGraph graph, String file, char c) {
    if (graph instanceof GeometricalGraph) {
      exportGeometricalGraph((GeometricalGraph) graph, file, c);
    } else if (graph instanceof PrimalTopologicalGraph) {
      exportPrimalTopologicalGraph((PrimalTopologicalGraph) graph, file, c);
    } else {
      exportDualTopologicalGraph((DualTopologicalGraph) graph, file, c);
    }
  }

  public static void main(String args[]) {

    // String input =
    // "/media/Data/Benoit/thèse/analyses/centralites/FILAIRES_L93/1789_verniquet.shp";
    // String input =
    // "/media/Data/Benoit/thèse/analyses/centralites/strokes_L93/verniquet_strokes.shp";

    String output = "/home/bcostes/Bureau/test.txt";
    char c = '\t';

    // ! DON'T TOUCH !
    // IPopulation<IFeature> pop = ShapefileReader.read(input);

    // CASE GEOMETRY
    // AbstractGraph graph = GeometricalGraphFactory.getFactory().newGraph(pop,
    // 0);

    // CASE PRIMAL TOPOLOGY
    // AbstractGraph graph =
    // PrimalTopologicalGraphFactory.getFactory().newGraph(
    // pop, 0);

    // CASE DUAL TOPOLOGY
    // AbstractGraph graph = DualTopologicalGraphFactory.getFactory().newGraph(
    // pop, 0);

    // ! DON'T TOUCH !
    // ExportCentralities.export(graph, output, c);

    ExportCentralities.exportAbstractGraph(new File(
        "/home/bcostes/Bureau/tmp/primal"), output, c);
  }
}
