package fr.ign.cogit.morphogenesis.exploring_tool.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.vecmath.Point2d;

import org.apache.batik.ext.awt.geom.Polyline2D;

import fr.ign.cogit.morphogenesis.exploring_tool.model.api.AbstractGraph;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.Edge;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.GeometricalGraph;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.Node;
import fr.ign.cogit.morphogenesis.exploring_tool.model.layers.GraphLayer;
import fr.ign.cogit.morphogenesis.exploring_tool.utils.ColorScale;
import fr.ign.cogit.morphogenesis.exploring_tool.utils.Parameters;
import fr.ign.cogit.morphogenesis.exploring_tool.view.legend.Legend;
import fr.ign.cogit.morphogenesis.exploring_tool.view.legend.LegendType;

public class PanelLayerDrawManager {

  private PanelLayer panelLayer;
  private Graphics2D g2d;

  public PanelLayerDrawManager(PanelLayer panelLayer) {
    this.panelLayer = panelLayer;
  }

  public void init(Graphics2D g2d) {

    this.g2d = g2d;

    // Anti-aliasing et rendu graphique
    RenderingHints rh = new RenderingHints(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    rh.add(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON));
    rh.add(new RenderingHints(RenderingHints.KEY_RENDERING,
        RenderingHints.VALUE_RENDER_SPEED));
    this.g2d.setRenderingHints(rh);
  }

  /**
   * Dessin du zoom ST (loupe seulement)
   * @param zoomStRectangle
   */
  public void drawAreaZoomSt(Rectangle2D zoomStRectangle) {
    g2d.setPaint(new Color(100, 150, 235, 25));
    Rectangle2D localRectangle = new Rectangle2D.Double(
        panelLayer.getLocalCoordX(zoomStRectangle.getX()),
        panelLayer.getLocalCoordY(zoomStRectangle.getY()),
        panelLayer.getLocalCoordX(zoomStRectangle.getWidth())
            - panelLayer.getLocalCoordX(zoomStRectangle.getX()),
        panelLayer.getLocalCoordY(zoomStRectangle.getHeight())
            - panelLayer.getLocalCoordY(zoomStRectangle.getY()));
    g2d.fill(localRectangle);
    g2d.setPaint(new Color(100, 150, 235, 255));
    g2d.draw(localRectangle);
  }

  /**
   * Dessin du graphe courrant
   * @param graphLayer
   */
  public void drawCurrentGraphLayer(GraphLayer graphLayer) {
    // récupération du graph abstrait
    AbstractGraph<?, ?> graph = graphLayer.getG();

    // **************************** GEOMETRICAL MODE ***********************
    if (graph instanceof GeometricalGraph) {
      // mode géométrique
      // le mapping pour les couleurs
      Map<Edge, Color> mappingValueColorEdges = getPaintEdges(
          this.panelLayer.getDrawnCentrality(),
          ((GeometricalGraph) graphLayer.getG()).getEdges(), graphLayer
              .getLegendEdgesIndicators().get(panelLayer.getDrawnCentrality()));
      if (graphLayer.getDrawableEntities().equals(Parameters.EDGES_ONLY)
          || graphLayer.getDrawableEntities().equals(Parameters.NODES_EDGES)) {
        // mode affichage des arcs activé
        for (Edge e : ((GeometricalGraph) graph).getEdges()) {
          // on ne dessine que les arcs présents sur la fenetre
          if (panelLayer.getLocalCoordX(e.getMaxX()) > panelLayer.getSize()
              .getWidth() + 50
              && panelLayer.getLocalCoordX(e.getMaxY()) > panelLayer.getSize()
                  .getHeight() + 50
              && panelLayer.getLocalCoordX(e.getMinX()) < -50
              && panelLayer.getLocalCoordX(e.getMinY()) < -50) {
            continue;
          }

          // application du style carto
          if (mappingValueColorEdges == null) {
            g2d.setPaint(graphLayer.getLegendEdges().getColor());
          } else {
            g2d.setPaint(mappingValueColorEdges.get(e));
          }

          int size = e.coords().size();
          float[] x = new float[size];
          float[] y = new float[size];
          int cpt = 0;
          for (Point2d pt : e.coords()) {
            x[cpt] = (float) (panelLayer.getLocalCoordX(pt.x));
            y[cpt] = (float) (panelLayer.getLocalCoordY(pt.y));
            cpt++;
          }
          // dessin des polylignes
          Polyline2D s = new Polyline2D(x, y, size);
          g2d.draw(s);
        }
      }
      Map<Node, Color> mappingValueColorNodes = getPaintNodes(
          this.panelLayer.getDrawnCentrality(),
          ((GeometricalGraph) graphLayer.getG()).getVertices(), graphLayer
              .getLegendNodesIndicators().get(panelLayer.getDrawnCentrality()));
      /*
       * Map<Node, Integer> mappingValueSizeNodes = getSizeNodes(
       * this.panelLayer.getDrawnCentrality(), ((GeometricalGraph)
       * graphLayer.getG()).getVertices(), graphLayer
       * .getLegendEdgesIndicators().get(panelLayer.getDrawnCentrality())
       * .getLegendType());
       */
      if (graphLayer.getDrawableEntities().equals(Parameters.NODES_ONLY)
          || graphLayer.getDrawableEntities().equals(Parameters.NODES_EDGES)) {
        // modes affichage des noeuds activé
        for (Node n : ((GeometricalGraph) graph).getVertices()) {
          // on ne dessine que les noeuds présent sur la fenetre
          if ((panelLayer.getLocalCoordX(n.x) > panelLayer.getSize()
              .getWidth() + 50 || panelLayer.getLocalCoordY(n.y) > panelLayer
              .getSize().getHeight() + 50)
              || (panelLayer.getLocalCoordX(n.x) < -50 || panelLayer
                  .getLocalCoordY(n.y) < -50)) {
            continue;
          }

          // application du style carto
          if (mappingValueColorNodes == null) {
            g2d.setPaint(graphLayer.getLegendNodes().getColor());
          } else {
            g2d.setPaint(mappingValueColorNodes.get(n));
          }

          int nodeSize = graphLayer.getLegendNodes().getNodes_size();
          // dessin des cercle (ellipse)
          Ellipse2D s = new Ellipse2D.Float(panelLayer.getLocalCoordX(n.x)
              - ((float) nodeSize) / 2.f, panelLayer.getLocalCoordY(n.y)
              - ((float) nodeSize) / 2.f, nodeSize, nodeSize);
          g2d.fill(s);
        }
      }
    }
    // ******************* GEOMETRICAL MODE END ***********************
  }

  /*
   * private Map<Node, Integer> getSizeNodes(String drawnCentrality,
   * Collection<Node> vertices, String legendType) { Map<Node, Integer> result =
   * new HashMap<Node, Integer>(); if
   * (this.panelLayer.getDrawnCentrality().equals("") || !((Node)
   * vertices.iterator().next()).getCentralityValues()
   * .containsKey(drawnCentrality)) { return null; } else { List<Double> values
   * = new ArrayList<Double>(); if (legendType.equals(LegendType.VALUES)) { for
   * (Node e : vertices) {
   * values.add(e.getCentralityValues().get(drawnCentrality)); }
   * Collections.sort(values); for (Node e : vertices) { result.put( e,
   * valuesLegendType(e.getCentralityValues().get(drawnCentrality),
   * values.get(0), values.get(values.size() - 1))); } } else { }
   * 
   * return result; } }
   */

  private Map<Edge, Color> getPaintEdges(String drawnCentrality,
      Collection<Edge> edges, Legend legend) {

    Map<Edge, Color> result = new HashMap<Edge, Color>();
    if (this.panelLayer.getDrawnCentrality().equals("")
        || this.panelLayer.getDrawableEntities().equals(Parameters.NODES_ONLY)
        || this.panelLayer.getDrawableEntities().equals(Parameters.NODES_EDGES)) {
      return null;
    } else {
      List<Double> values = new ArrayList<Double>();
      if (legend.getLegendType().equals(LegendType.VALUES)) {
        for (Edge e : edges) {
          values.add(e.getCentralityValues().get(drawnCentrality));
        }
        Collections.sort(values);
        for (Edge e : edges) {
          result.put(
              e,
              valuesLegendType(e.getCentralityValues().get(drawnCentrality),
                  values.get(0), values.get(values.size() - 1)));
        }
      } else if (legend.getLegendType().equals(LegendType.QUANTIL)) {
        for (Edge e : edges) {
          values.add(e.getCentralityValues().get(drawnCentrality));
        }
        Collections.sort(values);
        for (Edge e : edges) {
          result.put(
              e,
              quantilLegendType(e.getCentralityValues().get(drawnCentrality),
                  values, legend.getNB_CLASSES()));
        }
      } else {
      }

      return result;
    }
  }

  private Color valuesLegendType(double d, double min, double max) {
    double[] rgb = ColorScale.HSVtoRGB((4.5 / (min - max)) * (d - max), 0.9, 1);
    Color c = new Color((float) rgb[0], (float) rgb[1], (float) rgb[2]);
    return c;
  }

  private Color quantilLegendType(double d, Collection<Double> values,
      int nb_classes) {
    int pas = (int) ((double) values.size() / (double) nb_classes);
    int id = -1;
    int cpt2 = 0;
    for (double dd : values) {
      if (d == dd) {
        id = cpt2;
        break;
      }
      cpt2++;
    }

    int index = Math.min(id / pas, nb_classes - 1);
    double[] rgb = ColorScale.HSVtoRGB(4.5 - (4.5 / (nb_classes - 1))
        * (double) index, 0.9, 1);
    Color color = new Color((float) rgb[0], (float) rgb[1], (float) rgb[2]);
    return color;
  }

  private Map<Node, Color> getPaintNodes(String drawnCentrality,
      Collection<Node> nodes, Legend legend) {
    Map<Node, Color> result = new HashMap<Node, Color>();
    if (this.panelLayer.getDrawnCentrality().equals("")
        || !((Node) nodes.iterator().next()).getCentralityValues().containsKey(
            drawnCentrality)) {
      return null;
    } else {
      List<Double> values = new ArrayList<Double>();
      if (legend.getLegendType().equals(LegendType.VALUES)) {

        for (Node e : nodes) {
          values.add(e.getCentralityValues().get(drawnCentrality));
        }
        Collections.sort(values);
        for (Node e : nodes) {
          result.put(
              e,
              valuesLegendType(e.getCentralityValues().get(drawnCentrality),
                  values.get(0), values.get(values.size() - 1)));
        }
      } else if (legend.getLegendType().equals(LegendType.QUANTIL)) {
        for (Node e : nodes) {
          values.add(e.getCentralityValues().get(drawnCentrality));
        }
        Collections.sort(values);
        for (Node e : nodes) {
          result.put(
              e,
              quantilLegendType(e.getCentralityValues().get(drawnCentrality),
                  values, legend.getNB_CLASSES()));
        }
      } else {
      }

      return result;
    }
  }

  /**
   * dessin des entités du zoom ST
   */
  public void drawZoomStClip(Rectangle2D zoomStRectangle) {

    if (panelLayer.getZoomDirection().equals(Parameters.ZOOM_FORWARD)) {
      for (int i = this.panelLayer.getIdCurrentGraphLayer() + 1; i <= this.panelLayer
          .getIdCurrentGraphLayer() + this.panelLayer.getZoomDepth(); i++) {

        double[] rgb = ColorScale
            .HSVtoRGB(
                (4.5 / (-(this.panelLayer.getIdCurrentGraphLayer() + 1) + (this.panelLayer
                    .getIdCurrentGraphLayer() + this.panelLayer.getZoomDepth())))
                    * (i - (this.panelLayer.getIdCurrentGraphLayer() + 1)),
                0.9, 1);
        Color c = new Color((float) rgb[0], (float) rgb[1], (float) rgb[2],
            0.7f);

        JLabel label = (JLabel) panelLayer.getSlider().getLabelTable().get(i);
        label.setBorder(BorderFactory.createLineBorder(c));
        panelLayer.getSlider().repaint();

        GraphLayer gL = this.panelLayer.getGraphLayer().get(i);
        AbstractGraph<?, ?> gg = gL.getG();
        Rectangle2D localRectangle = new Rectangle2D.Double(
            this.panelLayer.getLocalCoordX(zoomStRectangle.getX()),
            this.panelLayer.getLocalCoordY(zoomStRectangle.getY()),
            this.panelLayer.getLocalCoordX(zoomStRectangle.getWidth())
                - this.panelLayer.getLocalCoordX(zoomStRectangle.getX()),
            this.panelLayer.getLocalCoordY(zoomStRectangle.getHeight())
                - this.panelLayer.getLocalCoordY(zoomStRectangle.getY()));
        g2d.setClip(localRectangle);

        if (gg instanceof GeometricalGraph) {
          if (gL.getDrawableEntities().equals(Parameters.EDGES_ONLY)
              || gL.getDrawableEntities().equals(Parameters.NODES_EDGES)) {
            for (Edge e : ((GeometricalGraph) gL.getG()).getEdges()) {
              if (this.panelLayer.getLocalCoordX(e.getMaxX()) > this.panelLayer
                  .getSize().getWidth() + 50
                  && this.panelLayer.getLocalCoordX(e.getMaxY()) > this.panelLayer
                      .getSize().getHeight() + 50
                  && this.panelLayer.getLocalCoordX(e.getMinX()) < -50
                  && this.panelLayer.getLocalCoordX(e.getMinY()) < -50) {
                continue;
              }
              g2d.setPaint(c);
              int size = e.coords().size();
              float[] x = new float[size];
              float[] y = new float[size];
              int cpt = 0;
              for (Point2d pt : e.coords()) {
                x[cpt] = (float) (this.panelLayer.getLocalCoordX(pt.x));
                y[cpt] = (float) (this.panelLayer.getLocalCoordY(pt.y));
                cpt++;
              }
              Polyline2D s = new Polyline2D(x, y, size);
              g2d.draw(s);
            }
          }
        }
      }
    } else {
      for (int i = this.panelLayer.getIdCurrentGraphLayer() - 1; i >= this.panelLayer
          .getIdCurrentGraphLayer() - this.panelLayer.getZoomDepth(); i--) {

        double[] rgb = ColorScale.HSVtoRGB(
            (4.5 / (this.panelLayer.getIdCurrentGraphLayer()
                - this.panelLayer.getZoomDepth() - (this.panelLayer
                .getIdCurrentGraphLayer() - 1)) * (i - (this.panelLayer
                .getIdCurrentGraphLayer() - 1))), 0.9, 1);
        Color c = new Color((float) rgb[0], (float) rgb[1], (float) rgb[2],
            0.7f);

        JLabel label = (JLabel) panelLayer.getSlider().getLabelTable().get(i);
        label.setBorder(BorderFactory.createLineBorder(c));
        panelLayer.getSlider().repaint();

        GraphLayer gL = this.panelLayer.getGraphLayer().get(i);
        AbstractGraph<?, ?> gg = gL.getG();
        Rectangle2D localRectangle = new Rectangle2D.Double(
            this.panelLayer.getLocalCoordX(zoomStRectangle.getX()),
            this.panelLayer.getLocalCoordY(zoomStRectangle.getY()),
            this.panelLayer.getLocalCoordX(zoomStRectangle.getWidth())
                - this.panelLayer.getLocalCoordX(zoomStRectangle.getX()),
            this.panelLayer.getLocalCoordY(zoomStRectangle.getHeight())
                - this.panelLayer.getLocalCoordY(zoomStRectangle.getY()));
        g2d.setClip(localRectangle);

        if (gg instanceof GeometricalGraph) {
          if (gL.getDrawableEntities().equals(Parameters.EDGES_ONLY)
              || gL.getDrawableEntities().equals(Parameters.NODES_EDGES)) {
            for (Edge e : ((GeometricalGraph) gL.getG()).getEdges()) {
              if (this.panelLayer.getLocalCoordX(e.getMaxX()) > this.panelLayer
                  .getSize().getWidth() + 50
                  && this.panelLayer.getLocalCoordX(e.getMaxY()) > this.panelLayer
                      .getSize().getHeight() + 50
                  && this.panelLayer.getLocalCoordX(e.getMinX()) < -50
                  && this.panelLayer.getLocalCoordX(e.getMinY()) < -50) {
                continue;
              }
              g2d.setPaint(c);
              int size = e.coords().size();
              float[] x = new float[size];
              float[] y = new float[size];
              int cpt = 0;
              for (Point2d pt : e.coords()) {
                x[cpt] = (float) (this.panelLayer.getLocalCoordX(pt.x));
                y[cpt] = (float) (this.panelLayer.getLocalCoordY(pt.y));
                cpt++;
              }
              Polyline2D s = new Polyline2D(x, y, size);
              g2d.draw(s);
            }
          }
        }
      }
    }
    g2d.setClip(null);
  }
}
