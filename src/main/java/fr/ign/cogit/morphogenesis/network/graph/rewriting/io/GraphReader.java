package fr.ign.cogit.morphogenesis.network.graph.rewriting.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.graph.util.Pair;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopoFactory;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.index.Tiling;
import fr.ign.cogit.morphogenesis.network.graph.rewriting.Edge;
import fr.ign.cogit.morphogenesis.network.graph.rewriting.GeometricalGraph;
import fr.ign.cogit.morphogenesis.network.graph.rewriting.Node;
import fr.ign.cogit.morphogenesis.network.graph.rewriting.TopologicalGraph;

public class GraphReader {

  private static Logger logger = Logger.getLogger(GraphReader.class);

  /**
   * @param thresold positif ou nul, on applique les corrections topologiques
   *          usuelles (rendre le graphe planaire, etc.) avec la précision
   *          thresold. Sinon on ne fait rien.
   * 
   */
  public static GeometricalGraph createGeometricalGraph(String file,
      double thresold) {

    if (logger.isInfoEnabled()) {
      logger.info("Création du graphe géométrique ...");
    }

    GeometricalGraph graph = new GeometricalGraph();

    CarteTopo map = null;
    IPopulation<IFeature> pop = ShapefileReader.read(file);
    // on cast les multi_line_string
    IFeatureCollection<IFeature> inputFeatureCollectionCorrected = new Population<IFeature>();
    // On simplifie les multi line string et on passe par des localFeature
    // pour conserver les attributs
    for (IFeature feat : pop) {
      if (feat.getGeom() instanceof IMultiCurve) {
        for (int i = 0; i < ((IMultiCurve<?>) feat.getGeom()).size(); i++) {
          inputFeatureCollectionCorrected.add(new DefaultFeature(
              ((IMultiCurve<?>) feat.getGeom()).get(i)));
        }
      } else {
        inputFeatureCollectionCorrected.add(new DefaultFeature(feat.getGeom()));
      }
    }

    graph.setPop(pop);

    // Correction topologiques
    if (thresold >= 0) {
      map = CarteTopoFactory.newCarteTopo("", inputFeatureCollectionCorrected,
          thresold);
      map.filtreDoublons(thresold);
      map.filtreArcsDoublons();
      map.fusionNoeuds(thresold);
      map.filtreNoeudsSimples();
      map.filtreNoeudsIsoles();
    } else {
      map = CarteTopoFactory.newCarteTopo(inputFeatureCollectionCorrected);
    }

    List<Edge> listEdges = new ArrayList<Edge>();
    List<Node> listNodes = new ArrayList<Node>();
    for (Arc arc : map.getListeArcs()) {
      Edge edge = new Edge(arc.getGeom().coord());
      listEdges.add(edge);
    }
    for (Noeud n : map.getListeNoeuds()) {
      Node node = new Node(n.getCoord().getX(), n.getCoord().getY());
      listNodes.add(node);
    }

    for (Noeud n : map.getListeNoeuds()) {
      Node node = new Node(n.getCoord().getX(), n.getCoord().getY());
      listNodes.add(node);
    }

    // On créer les arcs et les noeuds

    for (Edge edge : listEdges) {
      Node node1 = new Node(edge.coords().get(0).getX(), edge.coords().get(0)
          .getY());
      Node node2 = new Node(edge.coords().get(edge.coords().size() - 1).getX(),
          edge.coords().get(edge.coords().size() - 1).getY());

      for (Node n : listNodes) {
        if (n.equals(node1)) {
          node1 = n;
        }
        if (n.equals(node2)) {
          node2 = n;
        }
      }

      edge.setFisrt(node1);
      edge.setLast(node2);

      graph.addEdge(edge, node1, node2);

    }

    // on enlève les strokes isolés
    List<Node> toRemove = new ArrayList<Node>();
    for (Edge edge : graph.getEdges()) {
      Pair<Node> nodes = graph.getEndpoints(edge);
      if (graph.getNeighborCount(nodes.getFirst())
          + graph.getNeighborCount(nodes.getSecond()) == 2) {
        toRemove.add(nodes.getFirst());
        toRemove.add(nodes.getSecond());
      }
    }
    for (Node geom : toRemove) {
      graph.removeVertex(geom);
    }

    if (logger.isInfoEnabled()) {
      logger.info("Graphe géométrique créé.");
    }
    return graph;
  }

  public static TopologicalGraph createTopologicalGraph(String file) {

    if (logger.isInfoEnabled()) {
      logger.info("Création du graphe topologique ...");
    }

    TopologicalGraph graph = new TopologicalGraph();
    IPopulation<IFeature> pop = ShapefileReader.read(file);

    graph.setPop(pop);

    List<Edge> listEdges = new ArrayList<Edge>();
    for (IFeature feat : pop) {
      Edge edge = new Edge(feat.getGeom().coord());
      listEdges.add(edge);
    }

    List<IFeature> alreadyDone = new ArrayList<IFeature>();

    pop.initSpatialIndex(Tiling.class, false);

    for (IFeature feat : pop) {
      alreadyDone.add(feat);
      Collection<IFeature> candidats = pop.select(feat.getGeom(), 5);
      for (IFeature feat2 : candidats) {
        if (feat.equals(feat2) || alreadyDone.contains(feat2)) {
          continue;
        }
        if (feat.getGeom().intersects(feat2.getGeom())) {
          // les arcs s'intersectent
          Edge edge1 = listEdges.get(listEdges.indexOf(new Edge(feat.getGeom()
              .coord())));
          Edge edge2 = listEdges.get(listEdges.indexOf(new Edge(feat2.getGeom()
              .coord())));

          // Un noeud fictif à l'intersection des deux
          String n = edge1.toString() + ", " + edge2.toString();
          graph.addEdge(n, edge1, edge2);

        }
      }

    }

    if (logger.isInfoEnabled()) {
      logger.info("Graphe topologique créé.");
    }
    return graph;
  }
}
