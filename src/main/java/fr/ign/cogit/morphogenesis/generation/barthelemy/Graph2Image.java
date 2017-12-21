package fr.ign.cogit.morphogenesis.generation.barthelemy;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;

public class Graph2Image {

  private static final int size = (int) (3 * Parameters.WINDOWSIZE);

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static void export(IPopulation<IFeature> network, String file) {
    Graph<IDirectPosition, IGeometry> graph = new UndirectedSparseMultigraph<IDirectPosition, IGeometry>();

    for (IFeature arc : network) {
      graph.addEdge(arc.getGeom(), arc.getGeom().coord().get(0), arc.getGeom()
          .coord().get(arc.getGeom().coord().size() - 1));
    }

    StaticLayout<IDirectPosition, IGeometry> layout = new StaticLayout<IDirectPosition, IGeometry>(
        graph, new Transformer<IDirectPosition, Point2D>() {
          public Point2D transform(IDirectPosition arg0) {
            Point2D pt = new Point();
            pt.setLocation(arg0.getX(), arg0.getY());
            return pt;
          }
        });

    /*
     * CircleLayout<Point2D, SimpleEdge> layout = new CircleLayout<Point2D,
     * SimpleEdge>( graph);
     */

    layout.setSize(new Dimension(size, size));

    VisualizationImageServer<IDirectPosition, IGeometry> vis = new VisualizationImageServer<IDirectPosition, IGeometry>(
        layout, layout.getSize());
    vis.setBackground(Color.WHITE);
    vis.getRenderContext().setEdgeShapeTransformer(
        new EdgeShape.Line<IDirectPosition, IGeometry>());
    vis.getRenderContext().setVertexShapeTransformer(
        new ConstantTransformer(new Ellipse2D.Float(0, 0, 0, 0)));

    BufferedImage image = (BufferedImage) vis.getImage(new Point2D.Double(vis
        .getGraphLayout().getSize().getWidth() / 2, vis.getGraphLayout()
        .getSize().getHeight() / 2), new Dimension(vis.getGraphLayout()
        .getSize()));

    Graphics2D g = image.createGraphics();

    g.dispose();
    // this.viewFrame.paintComponents(g);
    // try{Thread.sleep(1000);} catch(Exception e) {throw new
    // RuntimeException(e);} // Sleeping doesn't help.
    try {
      File f = new File(file);
      ImageIO.write(image, "png", f);
      // try{Thread.sleep(500);} catch(Exception e) {throw new
      // RuntimeException(e);} // Doesn't help
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
