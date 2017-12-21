package fr.ign.cogit.morphogenesis.network.graph.rewriting.io;

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
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class GraphWriter {

  public static void graph2Img(IFeatureCollection<IFeature> col, String file,
      int format) {

    int sizex = 1000;

    Graph<IDirectPosition, IGeometry> graph = new UndirectedSparseMultigraph<IDirectPosition, IGeometry>();

    for (IFeature arc : col) {
      if (arc.getGeom().coord().size() == 2) {
        graph.addEdge(arc.getGeom(), arc.getGeom().coord().get(0), arc
            .getGeom().coord().get(1));
      } else {
        for (int i = 0; i < arc.getGeom().coord().size() - 1; i++) {
          IDirectPositionList l = new DirectPositionList();
          l.add(arc.getGeom().coord().get(i));
          l.add(arc.getGeom().coord().get(i + 1));

          graph.addEdge(new GM_LineString(l), arc.getGeom().coord().get(i), arc
              .getGeom().coord().get(i + 1));
        }
      }

    }

    double xmax = col.envelope().maxX();
    double xmin = col.envelope().minX();
    double ymax = col.envelope().maxY();
    double ymin = col.envelope().minY();

    int sizey = (int) (sizex * (ymax - ymin) / (xmax - xmin)) + 10;

    final double ax = (double) sizex / (xmax - xmin);
    final double bx = -ax * xmin;
    final double ay = -(double) sizey / (ymax - ymin);
    final double by = -ay * ymax;

    StaticLayout<IDirectPosition, IGeometry> layout = new StaticLayout<IDirectPosition, IGeometry>(
        graph, new Transformer<IDirectPosition, Point2D>() {
          public Point2D transform(IDirectPosition arg0) {
            Point2D pt = new Point();
            pt.setLocation(ax * arg0.getX() + bx, ay * arg0.getY() + by);
            return pt;
          }
        });

    layout.setSize(new Dimension(sizex + 10, sizey));

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

  public static void graph2Shp(IFeatureCollection<IFeature> col, String file) {
    ShapefileWriter.write(col, file);
  }

}
