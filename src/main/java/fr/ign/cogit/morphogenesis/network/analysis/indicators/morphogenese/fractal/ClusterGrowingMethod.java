package fr.ign.cogit.morphogenesis.network.analysis.indicators.morphogenese.fractal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class ClusterGrowingMethod {

  public static int count(Noeud pt, IPopulation<Noeud> nodes, double radius) {
    if (!nodes.hasSpatialIndex()) {
      nodes.initSpatialIndex(Tiling.class, false);
    }
    // int result = nodes.select(pt.getGeometrie().buffer(radius)).size();
    int result = nodes.select(pt.getCoord(), radius).size();

    return result;
  }

  public static int count(Noeud pt, IPopulation<Noeud> nodes, double radius,
      Map<Noeud, Boolean> flag) {
    if (!nodes.hasSpatialIndex()) {
      nodes.initSpatialIndex(Tiling.class, false);
    }
    Collection<Noeud> selection = nodes
        .select(pt.getGeometrie().buffer(radius));
    int result = selection.size();
    for (Noeud node : selection) {
      if (!flag.get(node)) {
        flag.put(node, true);
      }
    }
    return result;
  }

  public static double count(IPopulation<Noeud> nodes, double radius) {
    if (!nodes.hasSpatialIndex()) {
      nodes.initSpatialIndex(Tiling.class, false);
    }

    Map<Noeud, Boolean> flag = new HashMap<Noeud, Boolean>();
    for (Noeud node : nodes) {
      flag.put(node, false);
    }

    List<Noeud> centers = new ArrayList<Noeud>();
    Random r = new Random();
    double countMean = 0;
    while (!allPointsFlaged(flag)) {

      List<Noeud> enables = new ArrayList<Noeud>();
      enables.addAll(nodes);
      enables.removeAll(centers);
      Noeud newCenter = enables.get(r.nextInt(enables.size()));
      centers.add(newCenter);
      countMean += ClusterGrowingMethod.count(newCenter, nodes, radius, flag);

    }

    countMean /= (double) centers.size();
    return countMean;
  }

  private static boolean allPointsFlaged(Map<Noeud, Boolean> flag) {
    for (Noeud pt : flag.keySet()) {
      if (!flag.get(pt)) {
        return false;
      } else {
      }
    }
    return true;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    IPopulation<IFeature> pop = ShapefileReader
        .read("/media/Data/Benoit/these/donnees/vecteur/FILAIRES_L93_OK/1810_1836_VASSEROT_accentsOK_topologieOk_PONTS.shp");

    String size = "";
    CarteTopo ct = new CarteTopo("void"); //$NON-NLS-1$
    IPopulation<Arc> arcs = ct.getPopArcs();
    for (IFeature feature : pop) {
      Arc arc = arcs.nouvelElement();
      try {
        GM_LineString line = new GM_LineString(feature.getGeom().coord());
        arc.setGeometrie(line);
        arc.addCorrespondant((IFeature) feature);
      } catch (ClassCastException e) {
        e.printStackTrace();
      }
    }
    ct.creeNoeudsManquants(0);
    ct.fusionNoeuds(0);
    ct.filtreArcsDoublons();
    ct.filtreNoeudsIsoles();
    ct.rendPlanaire(0);
    ct.fusionNoeuds(0);
    ct.filtreArcsDoublons();

    for (double radius = 0; radius <= 2500; radius += 10) {
      System.out.println(radius);
      double count = ClusterGrowingMethod.countAll(ct.getPopNoeuds(), radius);
      size += radius + " , " + count + "\n";
    }

    /*
     * IDirectPosition bary = Operateurs.barycentre2D(pop.envelope().getGeom());
     * Noeud n = new Noeud(bary); System.out.println(n.toString());
     * System.out.println(bary.toString()); for (double radius = 10; radius <=
     * 5000; radius += 10) { int count = ClusterGrowingMethod.count(n,
     * ct.getPopNoeuds(), radius); size += (int) radius + " , " + count + "\n";
     * }
     */
    System.out.println(size);

  }

  private static double countAll(IPopulation<Noeud> nodes, double radius) {
    if (!nodes.hasSpatialIndex()) {
      nodes.initSpatialIndex(Tiling.class, false);
    }

    double countMean = 0;

    for (Noeud node : nodes) {
      countMean += ClusterGrowingMethod.count(node, nodes, radius);
    }

    countMean /= (double) nodes.size();
    return countMean;

  }

}
