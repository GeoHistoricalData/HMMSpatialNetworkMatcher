package fr.ign.cogit.morphogenesis.network.graph.randomWalk;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Random;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.algorithms.scoring.VertexScorer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class RandomGraph {

  public static double squared_dist(IDirectPosition p, IDirectPosition q) {
    double dx = p.getX() - q.getX();
    double dy = p.getY() - q.getY();
    return dx * dx + dy * dy;
  }

  public static void main(String args[]) {
    final Graph<IDirectPosition, Integer> g = new UndirectedSparseGraph<IDirectPosition, Integer>();

    int N = 100;
    double MD2 = (0.03) * (0.03), D2 = (0.035) * (0.035);
    IDirectPosition[] pts = new DirectPosition[N];

    for (int i = 0; i < N; i++) {
      Random r = new Random();
      IDirectPosition pt = new DirectPosition();
      if (i == 0) {
        pt.setX(r.nextDouble());
        pt.setY(r.nextDouble());
        pts[i] = pt;
        continue;
      }
      b1: while (true) {
        pt.setX(r.nextDouble());
        pt.setY(r.nextDouble());
        for (int j = 0; j < i; j++) {
          if (squared_dist(pt, pts[j]) < MD2) {
            break b1;
          }
        }
      }
      pts[i] = pt;
    }

    int cpt = 0;

    for (int i = 0; i < N - 1; i++) {
      for (int j = i + 1; j < N; j++) {
        if (squared_dist(pts[i], pts[j]) < D2) {
          if (g.containsVertex(pts[i]) && g.getNeighborCount(pts[i]) > 5) {
            continue;
          }
          if (g.containsVertex(pts[j]) && g.getNeighborCount(pts[j]) > 5) {
            continue;
          }
          g.addEdge(cpt, pts[i], pts[j]);
          cpt++;
        }
      }
    }

    IFeatureCollection<IFeature> out = new Population<IFeature>();
    for (Integer i : g.getEdges()) {
      IDirectPositionList l = new DirectPositionList();
      l.add(g.getEndpoints(i).getFirst());
      l.add(g.getEndpoints(i).getSecond());
      out.add(new DefaultFeature(new GM_LineString(l)));
    }
    ShapefileWriter.write(out, "/home/bcostes/Bureau/test.shp");
    System.exit(0);

    final VertexScorer<IDirectPosition, Double> c = new ClosenessCentrality<IDirectPosition, Integer>(
        g);
    final VertexScorer<IDirectPosition, Integer> d = new DegreeScorer<IDirectPosition>(
        g);
    final BetweennessCentrality<IDirectPosition, Integer> b = new BetweennessCentrality<IDirectPosition, Integer>(
        g);
    b.setRemoveRankScoresOnFinalize(false);
    b.evaluate();

    RandomGraph.show(c, g);
    RandomGraph.show3(b, g);
    RandomGraph.show2(d, g);

  }

  public static void show2(final VertexScorer<IDirectPosition, Integer> scorer,
      final Graph<IDirectPosition, Integer> g) {
    Transformer<IDirectPosition, Paint> vertexPaint = new Transformer<IDirectPosition, Paint>() {
      private final Color[] palette = { Color.GREEN, Color.BLUE, Color.RED };

      public Paint transform(IDirectPosition i) {
        double max = 0;
        double min = Double.MAX_VALUE;
        for (IDirectPosition v : g.getVertices()) {
          if (max < scorer.getVertexScore(v)) {
            max = scorer.getVertexScore(v);
          }
          if (min > scorer.getVertexScore(v)) {
            min = scorer.getVertexScore(v);
          }
        }

        double[] rgb = HSVtoRGB((4 / (min - max))
            * (scorer.getVertexScore(i) - max), 0.9, 1);
        Color c = new Color((float) rgb[0], (float) rgb[1], (float) rgb[2]);
        return c;

      }
    };
    Layout<IDirectPosition, Integer> layout = new StaticLayout<IDirectPosition, Integer>(
        g, new Transformer<IDirectPosition, Point2D>() {
          public Point2D transform(IDirectPosition arg0) {
            Point2D pt = new Point();
            pt.setLocation(arg0.getX(), arg0.getY());
            return pt;
          }
        });
    layout.setSize(new Dimension(1000, 1000));
    BasicVisualizationServer<IDirectPosition, Integer> vv = new BasicVisualizationServer<IDirectPosition, Integer>(
        layout);
    vv.setBackground(Color.WHITE);
    vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
    vv.getRenderContext().setEdgeShapeTransformer(
        new EdgeShape.Line<IDirectPosition, Integer>());
    vv.getRenderContext().setVertexShapeTransformer(
        new ConstantTransformer(new Ellipse2D.Float(-5, -5, 11, 11)));
    vv.setPreferredSize(new Dimension(1000, 1000)); // Sets the viewing area
    // size
    JFrame frame = new JFrame("Simple Graph View");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(vv);
    frame.pack();
    frame.setVisible(true);
  }

  public static void show3(
      final BetweennessCentrality<IDirectPosition, Integer> scorer,
      final Graph<IDirectPosition, Integer> g) {
    Transformer<IDirectPosition, Paint> vertexPaint = new Transformer<IDirectPosition, Paint>() {
      private final Color[] palette = { Color.GREEN, Color.BLUE, Color.RED };

      public Paint transform(IDirectPosition i) {
        double max = 0;
        double min = Double.MAX_VALUE;
        for (IDirectPosition v : g.getVertices()) {
          if (max < scorer.getVertexRankScore(v)) {
            max = scorer.getVertexRankScore(v);
          }
          if (min > scorer.getVertexRankScore(v)) {
            min = scorer.getVertexRankScore(v);
          }
        }

        double[] rgb = HSVtoRGB(
            (4 / (min - max)) * (scorer.getVertexRankScore(i) - max), 0.9, 1);
        Color c = new Color((float) rgb[0], (float) rgb[1], (float) rgb[2]);
        return c;

      }
    };
    Layout<IDirectPosition, Integer> layout = new StaticLayout<IDirectPosition, Integer>(
        g, new Transformer<IDirectPosition, Point2D>() {
          public Point2D transform(IDirectPosition arg0) {
            Point2D pt = new Point();
            pt.setLocation(arg0.getX(), arg0.getY());
            return pt;
          }
        });
    layout.setSize(new Dimension(1000, 1000));
    BasicVisualizationServer<IDirectPosition, Integer> vv = new BasicVisualizationServer<IDirectPosition, Integer>(
        layout);
    vv.setBackground(Color.WHITE);
    vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
    vv.getRenderContext().setEdgeShapeTransformer(
        new EdgeShape.Line<IDirectPosition, Integer>());
    vv.getRenderContext().setVertexShapeTransformer(
        new ConstantTransformer(new Ellipse2D.Float(-5, -5, 11, 11)));
    vv.setPreferredSize(new Dimension(1000, 1000)); // Sets the viewing area
    // size
    JFrame frame = new JFrame("Simple Graph View");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(vv);
    frame.pack();
    frame.setVisible(true);
  }

  public static void show(final VertexScorer<IDirectPosition, Double> scorer,
      final Graph<IDirectPosition, Integer> g) {
    Transformer<IDirectPosition, Paint> vertexPaint = new Transformer<IDirectPosition, Paint>() {
      private final Color[] palette = { Color.GREEN, Color.BLUE, Color.RED };

      public Paint transform(IDirectPosition i) {
        double max = 0;
        double min = Double.MAX_VALUE;
        for (IDirectPosition v : g.getVertices()) {
          if (max < scorer.getVertexScore(v)) {
            max = scorer.getVertexScore(v);
          }
          if (min > scorer.getVertexScore(v)) {
            min = scorer.getVertexScore(v);
          }
        }

        double[] rgb = HSVtoRGB((4 / (min - max))
            * (scorer.getVertexScore(i) - max), 0.9, 1);
        Color c = new Color((float) rgb[0], (float) rgb[1], (float) rgb[2]);
        return c;

      }
    };
    Layout<IDirectPosition, Integer> layout = new StaticLayout<IDirectPosition, Integer>(
        g, new Transformer<IDirectPosition, Point2D>() {
          public Point2D transform(IDirectPosition arg0) {
            Point2D pt = new Point();
            pt.setLocation(arg0.getX(), arg0.getY());
            return pt;
          }
        });
    layout.setSize(new Dimension(1000, 1000));
    BasicVisualizationServer<IDirectPosition, Integer> vv = new BasicVisualizationServer<IDirectPosition, Integer>(
        layout);
    vv.setBackground(Color.WHITE);
    vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
    vv.getRenderContext().setEdgeShapeTransformer(
        new EdgeShape.Line<IDirectPosition, Integer>());
    vv.getRenderContext().setVertexShapeTransformer(
        new ConstantTransformer(new Ellipse2D.Float(-5, -5, 11, 11)));
    vv.setPreferredSize(new Dimension(1000, 1000)); // Sets the viewing area
    // size
    JFrame frame = new JFrame("Simple Graph View");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(vv);
    frame.pack();
    frame.setVisible(true);
  }

  public static double[] HSVtoRGB(final double h, final double s, final double v) {
    // H is given on [0->6] or -1. S and V are given on [0->1].
    // RGB are each returned on [0->1].
    double m, n, f;
    int i;

    final double[] hsv = new double[3];
    final double[] rgb = new double[3];

    hsv[0] = h;
    hsv[1] = s;
    hsv[2] = v;

    if (hsv[0] == -1) {
      rgb[0] = rgb[1] = rgb[2] = hsv[2];
      return rgb;
    }
    i = (int) (Math.floor(hsv[0]));
    f = hsv[0] - i;
    if (i % 2 == 0)
      f = 1 - f; // if i is even
    m = hsv[2] * (1 - hsv[1]);
    n = hsv[2] * (1 - hsv[1] * f);
    switch (i) {
      case 6:
      case 0:
        rgb[0] = hsv[2];
        rgb[1] = n;
        rgb[2] = m;
        break;
      case 1:
        rgb[0] = n;
        rgb[1] = hsv[2];
        rgb[2] = m;
        break;
      case 2:
        rgb[0] = m;
        rgb[1] = hsv[2];
        rgb[2] = n;
        break;
      case 3:
        rgb[0] = m;
        rgb[1] = n;
        rgb[2] = hsv[2];
        break;
      case 4:
        rgb[0] = n;
        rgb[1] = m;
        rgb[2] = hsv[2];
        break;
      case 5:
        rgb[0] = hsv[2];
        rgb[1] = m;
        rgb[2] = n;
        break;
    }

    return rgb;

  }
}
