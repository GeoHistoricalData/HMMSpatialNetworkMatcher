package fr.ign.cogit.morphogenesis.network.graph.rewriting;

import fr.ign.cogit.morphogenesis.network.graph.rewriting.io.GraphReader;

public class Main {

  public static void main(String args[]) {

    String file = "/media/Data/Benoit/these/analyses/centralites/FILAIRES_L93/1789_verniquet.shp";
    String fileStyrokes = "/media/Data/Benoit/these/analyses/centralites/strokes_L93/verniquet_strokes.shp";

    GeometricalGraph g1 = GraphReader.createGeometricalGraph(file, -1);

    System.out.println(g1.getEdgeCount() + " " + g1.getVertexCount());

    /*
     * g1.exportClosenessCentrality(
     * "/home/bcostes/Bureau/tmp_centralities_rewriting/geometrical_closeness.png"
     * , Format.PNG);
     */

    TopologicalGraph g2 = GraphReader.createTopologicalGraph(fileStyrokes);

  }
}
