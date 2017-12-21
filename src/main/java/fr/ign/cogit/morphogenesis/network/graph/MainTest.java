package fr.ign.cogit.morphogenesis.network.graph;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.oldies.network.graph.factory.AbstractGraphFactory;

public class MainTest {

  public static void main(String[] args) {

    String inputSegments = "/media/Data/Benoit/thèse/analyses/centralites/FILAIRES_L93/1885_ALPHAND_POUBELLE_emprise.shp";
    String inputStrokes = "/media/Data/Benoit/thèse/analyses/centralites/strokes_L93/poubelle_emprise_strokes.shp";

    String output = "/media/Data/Benoit/thèse/analyses/centralites/indicateurs/1885_poubelle_emprise/";
    String outputStegments = "segments/";
    String outputStrokes = "primal/";
    String outputStrokes2 = "dual/";

    String basename = "/1885_";

    IPopulation<IFeature> popStrokes = ShapefileReader.read(inputStrokes);
    IPopulation<IFeature> popSegments = ShapefileReader.read(inputSegments);

    /*
     * GeometricalGraph g1 = GeometricalGraphFactory.getFactory().newGraph(
     * popSegments, 0);
     * 
     * g1.exportClosenessCentrality(null, output + outputStegments + basename +
     * "closeness.shp"); g1.exportDegreeCentrality(null, output +
     * outputStegments + basename + "degree.shp");
     * g1.exportEdgesBetweenessCentrality(null, output + outputStegments +
     * basename + "betweeness_log.shp"); g1.exportPageRank(0.7, null, output +
     * outputStegments + basename + "pagerank.shp");
     * g1.exportControlMetric(null, output + outputStegments + basename +
     * "control.shp"); g1.exportStraightnessCentrality(null, output +
     * outputStegments + basename + "straightness.shp");
     * g1.exportRandomWalk(null, 1000, output + outputStegments + basename +
     * "random.shp");
     * 
     * g1 = null; System.gc();
     * 
     * // **********************************************************************
     * 
     * PrimalTopologicalGraph g11 = PrimalTopologicalGraphFactory.getFactory()
     * .newGraph(popStrokes, 0);
     * 
     * g11.exportControlMetric(null, output + outputStrokes + basename +
     * "control_log.shp"); g11.exportGlobalIntegration(null, output +
     * outputStrokes + basename + "global_integration.shp");
     * g11.exportDegreeCentrality(null, output + outputStrokes + basename +
     * "degree_log.shp"); g11.exportLocalIntegration(3, null, output +
     * outputStrokes + basename + "local_integration_log.shp");
     * g11.exportClosenessCentrality(null, output + outputStrokes + basename +
     * "closeness.shp"); g11.exportBetweenessCentrality(null, output +
     * outputStrokes + basename + "beetweeness_log.shp");
     * g11.exportPageRank(0.8, null, output + outputStrokes + basename +
     * "pagerank_log.shp"); g11.exportTopologicalMap(null, output +
     * outputStrokes + basename + "topological_map.shp");
     * g11.exportRandomWalk(null, 1000, output + outputStrokes + basename +
     * "random_log.shp");
     * 
     * g11 = null; System.gc();
     */
    // **********************************************************************

    AbstractGraph g111 = AbstractGraphFactory.getFactory(
        AbstractGraphFactory.GEOMETRICAL_GRAPH).newGraph(popStrokes, 0);

    /*
     * g111.exportControlMetric(null, output + outputStrokes2 + basename +
     * "control.shp"); g111.exportDegreeCentrality(null, output + outputStrokes2
     * + basename + "degree.shp"); g111.exportGlobalIntegration(null, output +
     * outputStrokes2 + basename + "global_integration.shp");
     */
    g111.exportClosenessCentrality(null, output + outputStrokes2 + basename
        + "closeness.shp");
    g111.exportBetweenessCentrality(null, output + outputStrokes2 + basename
        + "betweeness.shp");
    g111.exportPageRank(0.70, null, output + outputStrokes2 + basename
        + "pagerank.shp");
    g111.exportRandomWalk(null, 1000, output + outputStrokes2 + basename
        + "random.shp");
    g111.exportLocalIntegration(3, null, output + outputStrokes2 + basename
        + "local_integration.shp");

  }
}
