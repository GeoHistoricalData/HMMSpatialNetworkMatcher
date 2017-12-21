package fr.ign.cogit.morphogenesis.network.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * Algorithme de classification des k-moyennes
 * @author bcostes
 * 
 */
public class KMeanClustering extends AbstractClustering {

  private Logger logger = Logger.getLogger(KMeanClustering.class);
  private static final int MAX = 10000;

  public KMeanClustering(int nbclasses) {
    super(nbclasses);
  }

  public List<List<Double>> cluster(List<? extends Number> values) {

    // on cast en double
    List<Double> values2Cluster = new ArrayList<Double>();
    for (Number value : values) {
      values2Cluster.add(Double.parseDouble(value.toString()));
    }
    List<List<Double>> clusters = new ArrayList<List<Double>>();

    // vecteurs des moyennes
    List<Double> means = new ArrayList<Double>();

    // initialisation: on crée k classes (k listes)
    Random r = new Random();
    for (int i = 0; i < this.nbclasses; i++) {
      List<Double> cluster = new ArrayList<Double>();
      // on prend un objet au hasard dans la liste en entrée
      while (true) {
        double mean = values2Cluster.get(r.nextInt(values.size() - 1));
        if (means.contains(mean)) {
          continue;
        }
        means.add(mean);
        break;
      }
      clusters.add(cluster);
    }

    // algo
    int cpt = 0;
    while (cpt < MAX) { // au bout d'un moment on abandonne
      // on vide les clusters
      for (List<Double> cluster : clusters) {
        cluster.clear();
      }
      boolean change = false;
      for (double value : values2Cluster) {
        double distMin = Math.abs(value - means.get(0));
        int iMin = 0;
        for (int i = 1; i < this.nbclasses; i++) {
          double dist = Math.abs(value - means.get(i));
          if (dist < distMin) {
            distMin = dist;
            iMin = i;
          }
        }
        // on a la moyenne la plus proche
        // on associe la valeur au cluster correspondant
        clusters.get(iMin).add(value);
      }
      // on met à jour les moyennes
      for (int i = 0; i < this.nbclasses; i++) {
        List<Double> cluster = clusters.get(i);
        double mean = 0;
        for (double value : cluster) {
          mean += value;
        }
        mean /= (double) cluster.size();
        if (mean != means.get(i)) {
          change = true;
        }
        means.set(i, mean);
      }
      // il y a convergence quand il n'y a plus de changement dans la
      // classification
      if (!change) {
        break;
      }
      cpt++;
    }

    if (cpt == MAX) {
      logger.error("Nombre d'itérations maximal dépassé.");
      System.exit(-1);
    }

    // on tri par ordre croissant
    Collections.sort(clusters, new Comparator<List<Double>>() {

      public int compare(List<Double> o1, List<Double> o2) {
        if (o1.get(0) > o2.get(0)) {
          return 1;
        } else {
          return -1;
        }
      }
    });
    return clusters;
  }
}
