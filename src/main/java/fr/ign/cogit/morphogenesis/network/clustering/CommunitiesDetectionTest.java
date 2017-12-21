package fr.ign.cogit.morphogenesis.network.clustering;

import java.util.Set;

import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.AttributeType;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.FeatureType;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.morphogenesis.network.graph.Edge;
import fr.ign.cogit.morphogenesis.network.graph.GeometricalGraph;
import fr.ign.cogit.morphogenesis.network.graph.Node;
import fr.ign.cogit.morphogenesis.network.graph.io.GraphReader;

public class CommunitiesDetectionTest {

  /**
   * @param args
   */
  public static void main(String[] args) {

    GeometricalGraph g = GraphReader
        .createGeometricalGraph(
            "/media/Data/Benoit/these/donnees/vecteur/FILAIRES_L93_OK/1885_ALPHAND_POUBELLE_emprise_topologieOk.shp",
            0);

    /*
     * BicomponentClusterer<Node, Edge> cluster1 = new
     * BicomponentClusterer<Node, Edge>(); Set<Set<Node>> biConnectedComponentns
     * = cluster1.transform(g);
     * System.out.println(biConnectedComponentns.size());
     * 
     * IFeatureCollection<IFeature> col = new Population<IFeature>();
     * 
     * FeatureType ft = new FeatureType(); AttributeType att = new
     * AttributeType(); att.setMemberName("community");
     * att.setNomField("community"); att.setValueType("Integer");
     * ft.addFeatureAttribute(att); int cpt = 1; for (Set<Node> community :
     * biConnectedComponentns) { for (Node n : community) {
     * LocalFeatureNodeCommunities f = new LocalFeatureNodeCommunities( new
     * GM_Point(new DirectPosition(n.getX(), n.getY()))); f.setFeatureType(ft);
     * f.setAttribute(ft.getFeatureAttributeByName("community"), cpt);
     * col.add(f); } cpt++; }
     */

    /*
     * WeakComponentClusterer<Node, Edge> cluster1 = new
     * WeakComponentClusterer<Node, Edge>(); Set<Set<Node>>
     * biConnectedComponentns = cluster1.transform(g);
     * System.out.println(biConnectedComponentns.size());
     * 
     * IFeatureCollection<IFeature> col = new Population<IFeature>();
     * 
     * FeatureType ft = new FeatureType(); AttributeType att = new
     * AttributeType(); att.setMemberName("community");
     * att.setNomField("community"); att.setValueType("Integer");
     * ft.addFeatureAttribute(att); int cpt = 1; for (Set<Node> community :
     * biConnectedComponentns) { for (Node n : community) {
     * LocalFeatureNodeCommunities f = new LocalFeatureNodeCommunities( new
     * GM_Point(new DirectPosition(n.getX(), n.getY()))); f.setFeatureType(ft);
     * f.setAttribute(ft.getFeatureAttributeByName("community"), cpt);
     * col.add(f); } cpt++; }
     * 
     * ShapefileWriter .write(col,
     * "/home/bcostes/Bureau/communities_detection/WeakComponentClusterer.shp");
     */

    EdgeBetweennessClusterer<Node, Edge> cluster1 = new EdgeBetweennessClusterer<Node, Edge>(
        10);
    Set<Set<Node>> biConnectedComponentns = cluster1.transform(g);
    System.out.println(biConnectedComponentns.size());

    IFeatureCollection<IFeature> col = new Population<IFeature>();

    FeatureType ft = new FeatureType();
    AttributeType att = new AttributeType();
    att.setMemberName("community");
    att.setNomField("community");
    att.setValueType("Integer");
    ft.addFeatureAttribute(att);
    int cpt = 1;
    for (Set<Node> community : biConnectedComponentns) {
      for (Node n : community) {
        LocalFeatureNodeCommunities f = new LocalFeatureNodeCommunities(
            new GM_Point(new DirectPosition(n.getX(), n.getY())));
        f.setFeatureType(ft);
        f.setAttribute(ft.getFeatureAttributeByName("community"), cpt);
        col.add(f);
      }
      cpt++;
    }

    ShapefileWriter
        .write(col,
            "/home/bcostes/Bureau/communities_detection/EdgeBetweennessClusterer.shp");

  }

}
