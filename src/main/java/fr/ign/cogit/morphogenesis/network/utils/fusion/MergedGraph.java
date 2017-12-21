package fr.ign.cogit.morphogenesis.network.utils.fusion;

import java.util.ArrayList;
import java.util.List;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;

public class MergedGraph extends
    UndirectedSparseMultigraph<MergesNode, MergesEdge> {

  public MergedGraph() {
  }

  /**
   * 
   */
  private static final long serialVersionUID = 1108755014771823184L;

  public static MergedGraph buildMergesGraph(List<IPopulation<IFeature>> data,
      IPopulation<IFeature> match) {
    MergedGraph mergedGraph = new MergedGraph();

    // création des graphes
    List<Graph> graphs = new ArrayList<Graph>();
    for (IPopulation<IFeature> pop : data) {
      graphs.add(Graph.buildGraph(pop));
    }

    // création des liens
    List<Matching> matchings = new ArrayList<Matching>();
    for (int i = 0; i < graphs.size() - 1; i++) {
      matchings.add(Matching.buildMatching(graphs.get(i), graphs.get(i + 1),
          match));
    }

    // Construction du graph aggrégé topologique
    // on part de graph1
    for (ILineString l : graphs.get(0).getEdges()) {
      MergesEdge e = new MergesEdge();
      e.lines.add(l);
      MergesNode n1 = new MergesNode();
      n1.points.add(l.coord().get(0));
      MergesNode n2 = new MergesNode();
      n2.points.add(l.coord().get(l.coord().size() - 1));

      mergedGraph.addEdge(e, new Pair<MergesNode>(n1, n2));
    }

    for (int i = 1; i < graphs.size(); i++) {
      Graph g = graphs.get(i);
      List<ILineString> newEntities = new ArrayList<ILineString>(); // nouvelles
                                                                    // entités
      List<ILineString> oldEntities = new ArrayList<ILineString>(); // entités
                                                                    // qui
                                                                    // disparaissent

      Matching m = matchings.get(i - 1);
      for (ILineString edge : g.getEdges()) {
        // appariement
        List<ILineString> matchs = new ArrayList<ILineString>();
        for (MergesEdge mergesEdge : mergedGraph.getEdges()) {
          ILineString line = mergesEdge.lines.get(i - 1);
          if (line == null) {
            continue;
          }
          // filiations et scission
          matchs.addAll(m.map.get(line));
        }
        if (matchs.isEmpty()) {
          // nouvelle entitées
          newEntities.add(edge);
          continue;
        }
        // sinon, appariement
        if (matchs.size() == 1) {
          // filiation ou scission
          ILineString e = matchs.get(0);
          List<ILineString> matchsE = m.map.get(e);
          if (matchsE.size() == 1) {
            // filliation

          } else {
            // scission
          }
        } else {
          // fusion
        }

      }
    }

    System.out.println(mergedGraph.getVertexCount() + " "
        + mergedGraph.getEdgeCount());

    return mergedGraph;
  }

  public static void main(String[] args) {
    List<String> files = new ArrayList<String>();
    files.add("/home/bcostes/Bureau/tmp/fusion/file1.shp");
    files.add("/home/bcostes/Bureau/tmp/fusion/file2.shp");
    files.add("/home/bcostes/Bureau/tmp/fusion/file3.shp");

    String mapping = "/home/bcostes/Bureau/tmp/fusion/matching.shp";

    List<IPopulation<IFeature>> data = new ArrayList<IPopulation<IFeature>>();
    for (String s : files) {
      data.add(ShapefileReader.read(s));
    }
    IPopulation<IFeature> match = ShapefileReader.read(mapping);

    MergedGraph mergesGraph = MergedGraph.buildMergesGraph(data, match);
  }
}
