package fr.ign.cogit.morphogenesis.network.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.AttributeType;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.FeatureType;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.morphogenesis.network.clustering.AbstractClustering;
import fr.ign.cogit.morphogenesis.network.graph.randomWalk.RandomWalk;
import fr.ign.cogit.oldies.network.graph.factory.LocalFeature;

public class PrimalTopologicalGraph extends TopologicalGraph {

  private static final long serialVersionUID = 1L;
  private static Logger logger = Logger.getLogger(PrimalTopologicalGraph.class);

  public PrimalTopologicalGraph() {
    super();
  }

  /**
   * Exporte les centralités relatives aux chemins les plus simples
   * @param clustering
   * @param file
   */
  public void exportSimplestCentrality(AbstractClustering clustering,
      String file) {
    if (logger.isInfoEnabled()) {
      logger
          .info("Calcul des centralité relatives aux chemins les plus simples ...");
    }

    IFeatureCollection<IFeature> col = new Population<IFeature>();

    Map<IGeometry, Double> simplestCentrality = this.getSimplestCentrality();

    FeatureType featT = new FeatureType();
    AttributeType att = new AttributeType();
    att.setMemberName("INDICATOR");
    att.setNomField("INDICATOR");
    att.setValueType("Double");
    featT.addFeatureAttribute(att);

    if (logger.isInfoEnabled()) {
      logger
          .info("Calcul des centralité relatives aux chemins les plus simples terminé.");
    }

    List<List<Double>> clusters = null;
    if (clustering != null) {
      // si le nombre de classe demandé est suffisant on fait une classification
      List<Double> nodesScore = new ArrayList<Double>();
      for (IGeometry node : simplestCentrality.keySet()) {
        nodesScore.add((double) simplestCentrality.get(node));
      }
      if (logger.isInfoEnabled()) {
        logger.info("Clustering des indicateurs par " + clustering.getName()
            + " ...");
      }
      clusters = clustering.cluster(nodesScore);
      if (logger.isInfoEnabled()) {
        logger.info("Clustering terminé.");
      }
    }

    if (logger.isInfoEnabled()) {
      logger.info("Export en cours ...");
    }

    for (IGeometry pos : this.getVertices()) {
      LocalFeature feat = new LocalFeature(pos);
      feat.setFeatureType(featT);
      double value = simplestCentrality.get(pos);
      if (clustering != null) {
        // si il y a eu clustering
        int indexCluster = -1;
        int cpt = 1;
        for (List<Double> cluster : clusters) {
          if (cluster.contains(value)) {
            indexCluster = cpt;
            break;
          }
          cpt++;
        }
        if (indexCluster == -1) {
          logger
              .warn("indexCLuster = -1 : la valeur n'est pas trouvée dans les clusters de la classification.");
          System.exit(-1);
        }
        value = indexCluster;
      }
      feat.setAttribute(featT.getFeatureAttributeByName("INDICATOR"), value);
      col.add(feat);
    }
    ShapefileWriter.write(col, file);
  }

  /**
   * Écrit dans un fichier shape la cartographie des pagerank
   * @param clustering la méthode de partitionnement. Peut-être null
   * @param alpha random jump probability
   * @param file fichier shape en sortie
   */
  public void exportPageRank(double alpha, AbstractClustering clustering,
      String file) {
    if (logger.isInfoEnabled()) {
      logger.info("Calcul des page rank en cours ...");
    }

    // calcul des centralités
    IFeatureCollection<IFeature> col = new Population<IFeature>();
    PageRank<IGeometry, IGeometry> pageRank = this.getPageRank(alpha);

    if (logger.isInfoEnabled()) {
      logger.info("Calcul des page rank terminé");
    }

    // création du schéma de sortie
    FeatureType featT = new FeatureType();
    AttributeType att = new AttributeType();
    att.setMemberName("INDICATOR");
    att.setNomField("INDICATOR");
    att.setValueType("Double");
    featT.addFeatureAttribute(att);

    List<List<Double>> clusters = null;
    if (clustering != null) {
      // si le nombre de classe demandé est suffisant on fait une classification
      List<Double> nodesScore = new ArrayList<Double>();
      for (IGeometry node : this.getVertices()) {
        nodesScore.add(Math.log10(pageRank.getVertexScore(node)));
      }
      if (logger.isInfoEnabled()) {
        logger.info("Clustering des centralités de proximité par "
            + clustering.getName() + "...");
      }
      clusters = clustering.cluster(nodesScore);
      if (logger.isInfoEnabled()) {
        logger.info("Clustering terminé.");
      }
    }

    if (logger.isInfoEnabled()) {
      logger.info("Export en cours ... ");
    }

    for (IGeometry pos : this.getVertices()) {
      LocalFeature feat = new LocalFeature(pos);
      feat.setFeatureType(featT);
      double value = Math.log10(pageRank.getVertexScore(pos));
      if (clustering != null) {
        // si il y a eu clustering
        int indexCluster = -1;
        int cpt = 1;
        for (List<Double> cluster : clusters) {
          if (cluster.contains(value)) {
            indexCluster = cpt;
            break;
          }
          cpt++;
        }
        if (indexCluster == -1) {
          logger
              .warn("indexCLuster = -1 : la valeur n'est pas trouvée dans les clusters de la classification.");
          System.exit(-1);
        }
        value = indexCluster;
      }
      feat.setAttribute(featT.getFeatureAttributeByName("INDICATOR"), value);
      col.add(feat);
    }
    ShapefileWriter.write(col, file);
  }

  /**
   * Écrit dans un fichier shape la cartographie des mesures de contrôle
   * @param clustering la méthode de partitionnement. Peut-être null
   * @param file fichier shape en sortie
   */
  public void exportControlMetric(AbstractClustering clustering, String file) {
    if (logger.isInfoEnabled()) {
      logger.info("Calcul des mesures de contrôle ...");
    }

    // calcul des centralités
    IFeatureCollection<IFeature> col = new Population<IFeature>();
    Map<IGeometry, Double> controlMetric = this.getControlMetric();

    if (logger.isInfoEnabled()) {
      logger.info("Calcul des cmesures de contrôle terminé.");
    }

    // création du schéma de sortie
    FeatureType featT = new FeatureType();
    AttributeType att = new AttributeType();
    att.setMemberName("INDICATOR");
    att.setNomField("INDICATOR");
    att.setValueType("Double");
    featT.addFeatureAttribute(att);

    List<List<Double>> clusters = null;
    if (clustering != null) {
      // si le nombre de classe demandé est suffisant on fait une classification
      List<Double> nodesScore = new ArrayList<Double>();
      for (IGeometry node : this.getVertices()) {
        nodesScore.add(Math.log10(controlMetric.get(node)));
      }
      if (logger.isInfoEnabled()) {
        logger.info("Clustering des centralités de degré par "
            + clustering.getName() + "...");
      }
      clusters = clustering.cluster(nodesScore);
      if (logger.isInfoEnabled()) {
        logger.info("Clustering terminé.");
      }
    }

    if (logger.isInfoEnabled()) {
      logger.info("Export en cours ... ");
    }

    for (IGeometry pos : this.getVertices()) {
      LocalFeature feat = new LocalFeature(pos);
      feat.setFeatureType(featT);
      double value = Math.log10(controlMetric.get(pos));
      if (clustering != null) {
        // si il y a eu clustering
        int indexCluster = -1;
        int cpt = 1;
        for (List<Double> cluster : clusters) {
          if (cluster.contains(value)) {
            indexCluster = cpt;
            break;
          }
          cpt++;
        }
        if (indexCluster == -1) {
          logger
              .warn("indexCLuster = -1 : la valeur n'est pas trouvée dans les clusters de la classification.");
          System.exit(-1);
        }
        value = indexCluster;
      }
      feat.setAttribute(featT.getFeatureAttributeByName("INDICATOR"), value);
      col.add(feat);
    }

    ShapefileWriter.write(col, file);
  }

  /**
   * Exporte la carte topologique
   * @param clustering
   * @param file
   */
  public void exportTopologicalMap(AbstractClustering clustering, String file) {

    if (logger.isInfoEnabled()) {
      logger.info("Détermination de la carte topologique ...");
    }

    IFeatureCollection<IFeature> col = new Population<IFeature>();

    Map<IGeometry, Integer> indicatorMap = this.getTopologicalMap();

    FeatureType featT = new FeatureType();
    AttributeType att = new AttributeType();
    att.setMemberName("INDICATOR");
    att.setNomField("INDICATOR");
    att.setValueType("Double");
    featT.addFeatureAttribute(att);

    if (logger.isInfoEnabled()) {
      logger.info("Carte topologique calculée.");
    }

    List<List<Double>> clusters = null;
    if (clustering != null) {
      // si le nombre de classe demandé est suffisant on fait une classification
      List<Double> nodesScore = new ArrayList<Double>();
      for (IGeometry node : indicatorMap.keySet()) {
        nodesScore.add((double) indicatorMap.get(node));
      }
      if (logger.isInfoEnabled()) {
        logger.info("Clustering des indicateurs par " + clustering.getName()
            + " ...");
      }
      clusters = clustering.cluster(nodesScore);
      if (logger.isInfoEnabled()) {
        logger.info("Clustering terminé.");
      }
    }

    if (logger.isInfoEnabled()) {
      logger.info("Export en cours ...");
    }

    for (IGeometry pos : this.getVertices()) {
      LocalFeature feat = new LocalFeature(pos);
      feat.setFeatureType(featT);
      double value = indicatorMap.get(pos);
      if (clustering != null) {
        // si il y a eu clustering
        int indexCluster = -1;
        int cpt = 1;
        for (List<Double> cluster : clusters) {
          if (cluster.contains(value)) {
            indexCluster = cpt;
            break;
          }
          cpt++;
        }
        if (indexCluster == -1) {
          logger
              .warn("indexCLuster = -1 : la valeur n'est pas trouvée dans les clusters de la classification.");
          System.exit(-1);
        }
        value = indexCluster;
      }
      if (indicatorMap.get(pos) == 0) {
        feat.setAttribute(featT.getFeatureAttributeByName("INDICATOR"), 0.);
      } else {
        feat.setAttribute(featT.getFeatureAttributeByName("INDICATOR"), value);
      }
      col.add(feat);
    }
    ShapefileWriter.write(col, file);
  }

  private Map<IGeometry, Double> getSimplestCentrality() {
    Map<IGeometry, Double> simplestCentrality = new HashMap<IGeometry, Double>();
    if (logger.isInfoEnabled()) {
      logger.info("Calcul de tout les plus courts chemins ...");
    }
    UnweightedShortestPath<IGeometry, IGeometry> path = new UnweightedShortestPath<IGeometry, IGeometry>(
        this);
    for (IGeometry road : this.getVertices()) {
      double normalizer = 0;
      for (IGeometry otherRoad : this.getVertices()) {
        normalizer += otherRoad.length();
      }
      Map<IGeometry, Number> m = path.getDistanceMap(road);
      double centrality = 0;
      for (IGeometry otherRoad : this.getVertices()) {
        centrality += otherRoad.length() * distance_topo(m, otherRoad);
      }
      centrality = normalizer * (1. / centrality);
      simplestCentrality.put(road, centrality);
      path.reset(road);
    }
    return simplestCentrality;
  }

  public void exportGlobalIntegration(AbstractClustering clustering, String file) {
    if (logger.isInfoEnabled()) {
      logger.info("Détermination des intégrations globales ...");
    }

    IFeatureCollection<IFeature> col = new Population<IFeature>();

    Map<IGeometry, Double> globalIntegration = this.getGlobalIntegration();

    FeatureType featT = new FeatureType();
    AttributeType att = new AttributeType();
    att.setMemberName("INDICATOR");
    att.setNomField("INDICATOR");
    att.setValueType("Double");
    featT.addFeatureAttribute(att);

    if (logger.isInfoEnabled()) {
      logger.info("Intégrations globales calculées.");
    }

    List<List<Double>> clusters = null;
    if (clustering != null) {
      // si le nombre de classe demandé est suffisant on fait une classification
      List<Double> nodesScore = new ArrayList<Double>();
      for (IGeometry node : globalIntegration.keySet()) {
        nodesScore.add((double) globalIntegration.get(node));
      }
      if (logger.isInfoEnabled()) {
        logger.info("Clustering des indicateurs par " + clustering.getName()
            + " ...");
      }
      clusters = clustering.cluster(nodesScore);
      if (logger.isInfoEnabled()) {
        logger.info("Clustering terminé.");
      }
    }

    if (logger.isInfoEnabled()) {
      logger.info("Export en cours ...");
    }

    for (IGeometry pos : this.getVertices()) {
      LocalFeature feat = new LocalFeature(pos);
      feat.setFeatureType(featT);
      double value = globalIntegration.get(pos);
      if (clustering != null) {
        // si il y a eu clustering
        int indexCluster = -1;
        int cpt = 1;
        for (List<Double> cluster : clusters) {
          if (cluster.contains(value)) {
            indexCluster = cpt;
            break;
          }
          cpt++;
        }
        if (indexCluster == -1) {
          logger
              .warn("indexCLuster = -1 : la valeur n'est pas trouvée dans les clusters de la classification.");
          System.exit(-1);
        }
        value = indexCluster;
      }
      feat.setAttribute(featT.getFeatureAttributeByName("INDICATOR"), value);
      col.add(feat);
    }
    ShapefileWriter.write(col, file);
  }

  /**
   * Détermination de la carte topologique (cf thèse courtat) i.e les distances
   * topologique par rapport au centre topologique
   * @param graph
   * @return
   */
  private Map<IGeometry, Integer> getTopologicalMap() {
    Map<IGeometry, Integer> map = new HashMap<IGeometry, Integer>();

    UnweightedShortestPath<IGeometry, IGeometry> path = new UnweightedShortestPath<IGeometry, IGeometry>(
        this);

    if (logger.isInfoEnabled()) {
      logger.info("Détermination du centre topologique de la ville ...");
    }

    // détermination du centre topologique de la ville
    // on cherche la rue qui minimise distance_topo_total_weighted
    IGeometry city_center = null;
    double dist_center_min = Double.MAX_VALUE;
    for (IGeometry road : this.getVertices()) {
      double dist_road = distance_topo_total_weighted(road, path);
      if (dist_road < dist_center_min) {
        dist_center_min = dist_road;
        city_center = road;
      }
    }

    if (logger.isInfoEnabled()) {
      logger.info("Calcul des distances au centre topologique ...");
    }

    path = new UnweightedShortestPath<IGeometry, IGeometry>(this);

    // on calcul ensuite la distance topologique entre toutes les rues et le
    // centre topo
    for (IGeometry road : this.getVertices()) {
      Map<IGeometry, Number> m = path.getDistanceMap(road);
      map.put(road, distance_topo(m, city_center));
      path.reset(road);
    }
    return map;
  }

  /**
   * Écrit dans un fichier shape la cartographie des centralités de proximité
   * @param clustering la méthode de partitionnement. Peut-être null
   * @param file fichier shape en sortie
   */
  public void exportClosenessCentrality(AbstractClustering clustering,
      String file) {
    if (logger.isInfoEnabled()) {
      logger.info("Calcul des centralités de proximité en cours ...");
    }

    // calcul des centralités
    IFeatureCollection<IFeature> col = new Population<IFeature>();
    ClosenessCentrality<IGeometry, IGeometry> closenessCentrality = this
        .getClosenessCentrality();

    if (logger.isInfoEnabled()) {
      logger.info("Calcul des centralités de proximité terminé");
    }

    // création du schéma de sortie
    FeatureType featT = new FeatureType();
    AttributeType att = new AttributeType();
    att.setMemberName("INDICATOR");
    att.setNomField("INDICATOR");
    att.setValueType("Double");
    featT.addFeatureAttribute(att);

    List<List<Double>> clusters = null;
    if (clustering != null) {
      // si le nombre de classe demandé est suffisant on fait une classification
      List<Double> nodesScore = new ArrayList<Double>();
      for (IGeometry node : this.getVertices()) {
        nodesScore.add(closenessCentrality.getVertexScore(node));
      }
      if (logger.isInfoEnabled()) {
        logger.info("Clustering des centralités de proximité par "
            + clustering.getName() + "...");
      }
      clusters = clustering.cluster(nodesScore);
      if (logger.isInfoEnabled()) {
        logger.info("Clustering terminé.");
      }
    }

    if (logger.isInfoEnabled()) {
      logger.info("Export en cours ... ");
    }

    for (IGeometry pos : this.getVertices()) {
      LocalFeature feat = new LocalFeature(pos);
      feat.setFeatureType(featT);
      double value = closenessCentrality.getVertexScore(pos);
      if (clustering != null) {
        // si il y a eu clustering
        int indexCluster = -1;
        int cpt = 1;
        for (List<Double> cluster : clusters) {
          if (cluster.contains(value)) {
            indexCluster = cpt;
            break;
          }
          cpt++;
        }
        if (indexCluster == -1) {
          logger
              .warn("indexCLuster = -1 : la valeur n'est pas trouvée dans les clusters de la classification.");
          System.exit(-1);
        }
        value = indexCluster;
      }
      feat.setAttribute(featT.getFeatureAttributeByName("INDICATOR"), value);
      col.add(feat);
    }

    ShapefileWriter.write(col, file);
  }

  public void exportLocalIntegration(int depth, AbstractClustering clustering,
      String file) {
    if (logger.isInfoEnabled()) {
      logger.info("Détermination des intégrations locales ...");
    }

    IFeatureCollection<IFeature> col = new Population<IFeature>();

    Map<IGeometry, Double> localIntegration = this.getLocalIntegration(depth);

    FeatureType featT = new FeatureType();
    AttributeType att = new AttributeType();
    att.setMemberName("INDICATOR");
    att.setNomField("INDICATOR");
    att.setValueType("Double");
    featT.addFeatureAttribute(att);

    if (logger.isInfoEnabled()) {
      logger.info("Intégrations locales calculées.");
    }

    List<List<Double>> clusters = null;
    if (clustering != null) {
      // si le nombre de classe demandé est suffisant on fait une classification
      List<Double> nodesScore = new ArrayList<Double>();
      for (IGeometry node : localIntegration.keySet()) {
        nodesScore.add(Math.log10((double) localIntegration.get(node)));
      }
      if (logger.isInfoEnabled()) {
        logger.info("Clustering des indicateurs par " + clustering.getName()
            + " ...");
      }
      clusters = clustering.cluster(nodesScore);
      if (logger.isInfoEnabled()) {
        logger.info("Clustering terminé.");
      }
    }

    if (logger.isInfoEnabled()) {
      logger.info("Export en cours ...");
    }

    for (IGeometry pos : this.getVertices()) {
      LocalFeature feat = new LocalFeature(pos);
      feat.setFeatureType(featT);
      double value = Math.log10(localIntegration.get(pos));
      if (clustering != null) {
        // si il y a eu clustering
        int indexCluster = -1;
        int cpt = 1;
        for (List<Double> cluster : clusters) {
          if (cluster.contains(value)) {
            indexCluster = cpt;
            break;
          }
          cpt++;
        }
        if (indexCluster == -1) {
          logger
              .warn("indexCLuster = -1 : la valeur n'est pas trouvée dans les clusters de la classification.");
          System.exit(-1);
        }
        value = indexCluster;
      }
      feat.setAttribute(featT.getFeatureAttributeByName("INDICATOR"), value);
      col.add(feat);
    }
    ShapefileWriter.write(col, file);
  }

  public void exportRandomWalk(AbstractClustering clustering, int nb_troopers,
      String file) {
    if (logger.isInfoEnabled()) {
      logger.info("Calcul des random walk en cours ...");
    }

    // calcul des centralités
    IFeatureCollection<IFeature> col = new Population<IFeature>();
    RandomWalk randomWalk = this.getRandomWalk(nb_troopers);

    if (logger.isInfoEnabled()) {
      logger.info("Calcul des random walk terminé");
    }

    // création du schéma de sortie
    FeatureType featT = new FeatureType();
    AttributeType att = new AttributeType();
    att.setMemberName("INDICATOR");
    att.setNomField("INDICATOR");
    att.setValueType("Double");
    featT.addFeatureAttribute(att);

    List<List<Double>> clusters = null;
    if (clustering != null) {
      // si le nombre de classe demandé est suffisant on fait une classification
      List<Double> nodesScore = new ArrayList<Double>();
      for (IGeometry node : this.getVertices()) {
        nodesScore.add(Math.log10(randomWalk.getVertexScore(node)));
      }
      if (logger.isInfoEnabled()) {
        logger.info("Clustering des centralités de proximité par "
            + clustering.getName() + "...");
      }
      clusters = clustering.cluster(nodesScore);
      if (logger.isInfoEnabled()) {
        logger.info("Clustering terminé.");
      }
    }

    if (logger.isInfoEnabled()) {
      logger.info("Export en cours ... ");
    }

    for (IGeometry pos : this.getVertices()) {
      LocalFeature feat = new LocalFeature(pos);
      feat.setFeatureType(featT);
      double value = Math.log10(randomWalk.getVertexScore(pos));
      if (clustering != null) {
        // si il y a eu clustering
        int indexCluster = -1;
        int cpt = 1;
        for (List<Double> cluster : clusters) {
          if (cluster.contains(value)) {
            indexCluster = cpt;
            break;
          }
          cpt++;
        }
        if (indexCluster == -1) {
          logger
              .warn("indexCLuster = -1 : la valeur n'est pas trouvée dans les clusters de la classification.");
          System.exit(-1);
        }
        value = indexCluster;
      }
      feat.setAttribute(featT.getFeatureAttributeByName("INDICATOR"),
          (double) value);
      col.add(feat);
    }

    ShapefileWriter.write(col, file);
  }

}
