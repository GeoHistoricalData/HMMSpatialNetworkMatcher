package fr.ign.cogit.morphogenesis.network.utils.delaunay;

import java.awt.Point;

import org.geotools.graph.structure.Graph;
import org.geotools.graph.util.delaunay.DelaunayNode;
import org.geotools.graph.util.delaunay.DelaunayTriangulator;

import com.vividsolutions.jts.geom.Coordinate;

import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.GeometricalGraph;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.GraphReader;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.Node;

public class DelaunayTest {

  /**
   * @param args
   */
  public static void main(String[] args) {

    /* creation de l'instance */

    GeometricalGraph g = GraphReader
        .createGeometricalGraph(
            "/media/Data/Benoit/these/donnees/vecteur/filaires/FILAIRES_L93_OK/1810_1836_VASSEROT_accentsOK_topologieOk_PONTS.shp",
            0);

    DelaunayNode[] tabDeNoeuds 
    = new DelaunayNode[g.getVertexCount()];
    int i = 0;
//    for (Node n : g.getVertices()) {
//      DelaunayNode newDelaunayNode = new DelaunayNode();
//      newDelaunayNode.setCoordinate(new Coordinate(n.getX(), n.getY()));
//      tabDeNoeuds[i++] = newDelaunayNode;
//    }

    DelaunayTriangulator maTriangulationDeDelauney = new DelaunayTriangulator();
    maTriangulationDeDelauney.setNodeArray(tabDeNoeuds);
    Graph grapheTrianguleGeoTools = maTriangulationDeDelauney
        .getTriangulation();

//    for (Object unNoeudDuGrapheGeoTools : grapheTrianguleGeoTools.getNodes()) {
//        Point p = new Point(new Double(((DelaunayNode) unNoeudDuGrapheGeoTools).getCoordinate().x).intValue(), new Double(((DelaunayNode) unNoeudDuGrapheGeoTools).getCoordinate().y).intValue());
//        
//        Node n = new Noeud(unNoeudDuGrapheGeoTools.toString(), p);
//        
//        this.grapheDeReference.addNoeud(nouveauNoeud);
//    }
//    
      
    }
}
