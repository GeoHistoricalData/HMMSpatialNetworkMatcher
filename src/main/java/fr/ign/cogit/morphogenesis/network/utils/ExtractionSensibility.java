package fr.ign.cogit.morphogenesis.network.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.GeometricalGraph;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.GraphReader;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.Node;
import fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.IndicatorVisitorBuilder;
import fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.local.LocalMorphologicalIndicator;
import fr.ign.cogit.morphogenesis.exploring_tool.utils.LocalMorphologicalndicatorList;

public class ExtractionSensibility {

  public static void main(String args[]) {
    String shp = "/media/Data/Benoit/these/donnees/vecteur/filaires/FILAIRES_L93_OK/1885_ALPHAND_POUBELLE_emprise_topologieOk.shp";
    IPopulation<IFeature> features = ShapefileReader.read(shp);
    IDirectPosition ptCenterFalse = new DirectPosition((features.getEnvelope()
        .maxX() + features.getEnvelope().minX()) / 2., (features.getEnvelope()
        .maxY() + features.getEnvelope().minY()) / 2.);
    Collection<IFeature> closestPoints = features.select(ptCenterFalse, 500);
    IDirectPosition ptCenter = null;
    double dmin = Double.MAX_VALUE;
    for (IFeature f : closestPoints) {
      double distance = f.getGeom().distance(new GM_Point(ptCenterFalse));
      if (distance < dmin) {
        dmin = distance;
        ptCenter = f.getGeom().coord().get(0);
      }
    }
    Map<Double, Double> centralityEvolution1 = new HashMap<Double, Double>();
    Map<Double, Double> centralityEvolution2 = new HashMap<Double, Double>();

    Map<Double, Double> MeanCentralityEvolution1 = new HashMap<Double, Double>();
    Map<Double, Double> MeanCentralityEvolution2 = new HashMap<Double, Double>();

    Map<Double, Double> MeanNeighborEvolution1 = new HashMap<Double, Double>();
    Map<Double, Double> MeanNeighborEvolution2 = new HashMap<Double, Double>();

    double Rmax = Math.max(
        Math.abs(ptCenter.getX() - features.getEnvelope().maxX()),
        ptCenter.getX() - features.getEnvelope().minX());
    for (double r = Rmax; r >= 250; r -= 200) {
      System.out.println("rayon : " + r);
      IGeometry buffer = (new GM_Point(ptCenter)).buffer(r);
      IFeatureCollection<IFeature> popReduct = new Population<IFeature>();
      for (IFeature f : features) {
        if (f.getGeom().intersects(buffer)) {
          popReduct.add(f);
        }
      }
      GeometricalGraph g = GraphReader.createGeometricalGraph(popReduct, 0.);
      LocalMorphologicalIndicator centralityInd1 = IndicatorVisitorBuilder
          .createLocalMorphologicalIndicator(LocalMorphologicalndicatorList.CLOSENESS);
      centralityInd1.calculate(g);
      LocalMorphologicalIndicator centralityInd2 = IndicatorVisitorBuilder
          .createLocalMorphologicalIndicator(LocalMorphologicalndicatorList.BETWEENNESS);
      centralityInd2.calculate(g);
      Node nodeCenter = null;
      for (Node n : g.getVertices()) {
        if ((new DirectPosition(n.getX(), n.getY()).equals(ptCenter, 0.05))) {
          nodeCenter = n;
          break;
        }
      }
      double meanC1 = 0;
      double meanC2 = 0;
      for (Node n : g.getVertices()) {
        meanC1 += n.getCentralityValues().get(
            LocalMorphologicalndicatorList.CLOSENESS);
        meanC2 += n.getCentralityValues().get(
            LocalMorphologicalndicatorList.BETWEENNESS);
      }
      meanC1 = meanC1 / (double) g.getVertexCount();
      meanC2 = meanC2 / (double) g.getVertexCount();

      /*
       * double meanNeighbor1 = 0; double meanNeighbor2 = 0;
       * 
       * for (Node n : g.getNeighbors(nodeCenter)) { meanNeighbor1 +=
       * n.getCentralityValues().get(
       * 
       * meanNeighbor2 += n.getCentralityValues().get(
       * LocalMorphologicalndicatorList.BETWEENNESS); } meanNeighbor1 =
       * meanNeighbor1 / (double) g.getNeighborCount(nodeCenter); meanNeighbor2
       * = meanNeighbor2 / (double) g.getNeighborCount(nodeCenter);
       */

      double centrality1 = nodeCenter.getCentralityValues().get(
          LocalMorphologicalndicatorList.CLOSENESS);
      double centrality2 = nodeCenter.getCentralityValues().get(
          LocalMorphologicalndicatorList.BETWEENNESS);
      centralityEvolution1.put(r, centrality1);
      centralityEvolution2.put(r, centrality2);

      MeanCentralityEvolution1.put(r, meanC1);
      MeanCentralityEvolution2.put(r, meanC2);

      /*
       * MeanNeighborEvolution1.put(r, meanNeighbor1);
       * MeanNeighborEvolution2.put(r, meanNeighbor2);
       */
    }

    System.out.println();
    System.out.println();
    System.out.println();
    System.out.println();

    for (double r : centralityEvolution1.keySet()) {
      System.out.println(r + " , " + centralityEvolution1.get(r));
    }
    System.out.println("-------------------------------------");
    for (double r : centralityEvolution2.keySet()) {
      System.out.println(r + " , " + centralityEvolution2.get(r));
    }
    System.out.println("###################################");

    for (double r : MeanCentralityEvolution1.keySet()) {
      System.out.println(r + " , " + MeanCentralityEvolution1.get(r));
    }
    System.out.println("-------------------------------------");
    for (double r : MeanCentralityEvolution2.keySet()) {
      System.out.println(r + " , " + MeanCentralityEvolution2.get(r));
    }
  }
}
