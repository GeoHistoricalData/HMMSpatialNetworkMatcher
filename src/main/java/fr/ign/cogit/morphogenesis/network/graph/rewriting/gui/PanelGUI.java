package fr.ign.cogit.morphogenesis.network.graph.rewriting.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.apache.batik.ext.awt.geom.Polyline2D;

import fr.ign.cogit.morphogenesis.network.graph.rewriting.io.ColorType;
import fr.ign.cogit.morphogenesis.network.utils.ColorScale;

public class PanelGUI extends JPanel {
  /**
   * 
   */
  private boolean init = true;
  private MainFrame f;
  private static final long serialVersionUID = 1L;
  private Map<Shape, Color> shapes;
  private Map<Shape, Double> values;
  private PanZoom zoomAndPanListener;
  private Stroke stroke;
  private int colorType = ColorType.VALUES;
  private double[] valuesSorted;
  private int INTERVAL_SIZE = 10;
  private boolean useDisticts = false;
  private List<Shape> districts;
  private Shape districtSelected;

  public PanelGUI(MainFrame f, Map<Shape, Color> shapes,
      Map<Shape, Double> values) {
    this.f = f;
    this.shapes = shapes;
    this.values = values;
    this.setDistrictSelected(null);
    this.districts = new ArrayList<Shape>();
    this.valuesSorted = new double[this.values.values().size()];
    int cpt = 0;
    List<Double> l = new ArrayList<Double>(this.values.values());
    Collections.sort(l);
    for (double d : l) {
      this.valuesSorted[cpt] = d;
      cpt++;
    }
    zoomAndPanListener = new PanZoom(this);
    this.addMouseListener(zoomAndPanListener);
    this.addMouseMotionListener(zoomAndPanListener);
    this.addMouseWheelListener(zoomAndPanListener);
  }

  public void paintComponent(Graphics g) {

    clear(g);
    Graphics2D g2d = (Graphics2D) g;
    if (init) {
      // Initialize the viewport by moving the origin to the center of the
      // window,
      // and inverting the y-axis to point upwards.
      init = false;
      Dimension d = this.getSize();
      g2d.translate(0, d.height);
      g2d.scale(1, -1);
      this.stroke = g2d.getStroke();
      // Save the viewport to be updated by the ZoomAndPanListener
      zoomAndPanListener.setCoordTransform(g2d.getTransform());
    } else {
      // Restore the viewport after it was updated by the ZoomAndPanListener
      g2d.setTransform(zoomAndPanListener.getCoordTransform());
    }

    if (zoomAndPanListener.getZoomLevel() == 0) {
      g2d.setStroke(new BasicStroke(1));
    } else if (zoomAndPanListener.getScaleFactor() == 0) {

    } else {
      g2d.setStroke(getInvertedZoomedStroke(stroke,
          zoomAndPanListener.getScaleFactor()));

      zoomAndPanListener.setScaleFactor(0);
    }
    this.stroke = g2d.getStroke();

    AffineTransform affineTransform = new AffineTransform();
    affineTransform.scale(1, -1);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    if (useDisticts) {
      for (Shape s : districts) {
        if (districtSelected == null || !s.equals(districtSelected)) {
          g2d.setPaint(new Color(1f, 0.67f, 0.5f, 0.4f));
          g2d.fill(s);
          g2d.setPaint(new Color(1f, 0.67f, 0.5f, 0.9f));
          g2d.draw(s);
        } else {
          g2d.setPaint(new Color(0f, 0.67f, 0.5f, 0.4f));
          g2d.fill(s);
          g2d.setPaint(new Color(0f, 0.67f, 0.5f, 0.9f));
          g2d.draw(s);
        }
      }
    }

    for (Shape s : shapes.keySet()) {
      g2d.setPaint(shapes.get(s));
      g2d.draw(s);
    }

  }

  private Stroke getInvertedZoomedStroke(Stroke stroke, double zoom) {
    if (stroke == null || !(stroke instanceof BasicStroke)) {
      return stroke;
    }

    BasicStroke bs = (BasicStroke) stroke;
    float[] dashArray = bs.getDashArray();

    float[] newDashArray = null;
    if (dashArray != null) {
      newDashArray = new float[dashArray.length];
      for (int i = 0; i < newDashArray.length; ++i) {
        newDashArray[i] = (float) (dashArray[i] / zoom);
      }
    }

    BasicStroke newStroke = new BasicStroke((float) (bs.getLineWidth() / zoom),
        bs.getEndCap(), bs.getLineJoin(), bs.getMiterLimit(),
        // (float)(bs.getMiterLimit() / zoom),
        newDashArray, (float) (bs.getDashPhase() / zoom));

    return newStroke;
  }

  protected void clear(Graphics g) {
    super.paintComponent(g);
  }

  public void center() {
    init = true;
    zoomAndPanListener.setZoomLevel(0);
    this.repaint();
  }

  public void changeColorType(int colorType) {

    System.out.println(colorType);

    this.colorType = colorType;
    Map<Shape, Color> shapes = new HashMap<Shape, Color>();
    switch (this.colorType) {
      case -1:
        for (Shape edge : this.values.keySet()) {
          Color c = new Color((float) 0., (float) 0., (float) 0., 0.6f);
          shapes.put(edge, c);
        }
        break;
      case ColorType.VALUES:
        double min = valuesSorted[0];
        double max = valuesSorted[valuesSorted.length - 1];
        for (Shape edge : this.values.keySet()) {

          double[] rgb = ColorScale.HSVtoRGB(
              (4.5 / (min - max)) * (this.values.get(edge) - max), 0.9, 1);
          Color c = new Color((float) rgb[0], (float) rgb[1], (float) rgb[2]);
          shapes.put(edge, c);
        }
        break;
      case ColorType.LOG_VALUES:
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        for (Shape edge : this.values.keySet()) {
          if (this.values.get(edge) == 0) {
            continue;
          }
          if (min > this.values.get(edge)) {
            min = this.values.get(edge);
          }
          if (max < this.values.get(edge)) {
            max = this.values.get(edge);
          }
        }
        min = Math.log(min);
        max = Math.log(max);

        for (Shape edge : this.values.keySet()) {

          if (this.values.get(edge) == 0) {
            double[] rgb = ColorScale.HSVtoRGB(4.5, 0.9, 1);
            Color c = new Color((float) rgb[0], (float) rgb[1], (float) rgb[2]);
            shapes.put(edge, c);
          } else {
            double[] rgb = ColorScale.HSVtoRGB(
                (4.5 / (min - max)) * (Math.log(this.values.get(edge)) - max),
                0.9, 1);
            Color c = new Color((float) rgb[0], (float) rgb[1], (float) rgb[2]);
            shapes.put(edge, c);
          }
        }
        break;
      case ColorType.INTERVAL:
        min = valuesSorted[0];
        max = valuesSorted[valuesSorted.length - 1];

        Color[] colors = new Color[INTERVAL_SIZE];
        for (int i = 0; i < INTERVAL_SIZE; i++) {
          double[] rgb = ColorScale.HSVtoRGB(4.5 - (4.5 / (INTERVAL_SIZE - 1))
              * (double) i, 0.9, 1);
          colors[i] = new Color((float) rgb[0], (float) rgb[1], (float) rgb[2]);
        }

        for (Shape edge : this.values.keySet()) {

          int index = (int) ((this.values.get(edge) - min) * this.INTERVAL_SIZE / (max - min));

          if (this.values.get(edge) == max) {
            index = INTERVAL_SIZE - 1;
          }
          shapes.put(edge, colors[index]);
        }
        break;
      case ColorType.LOG_INTERVAL:
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        for (Shape edge : this.values.keySet()) {
          if (this.values.get(edge) == 0) {
            continue;
          }
          if (min > this.values.get(edge)) {
            min = this.values.get(edge);
          }
          if (max < this.values.get(edge)) {
            max = this.values.get(edge);
          }
        }
        max = Math.log(max) + Math.abs(Math.log(min)); // translation

        colors = new Color[INTERVAL_SIZE];
        for (int i = 0; i < INTERVAL_SIZE; i++) {
          double[] rgb = ColorScale.HSVtoRGB(4.5 - (4.5 / (INTERVAL_SIZE - 1))
              * (double) i, 0.9, 1);
          colors[i] = new Color((float) rgb[0], (float) rgb[1], (float) rgb[2]);
        }

        for (Shape edge : this.values.keySet()) {

          if (this.values.get(edge) == 0) {
            shapes.put(edge, colors[0]);
          } else {
            int index = (int) ((Math.log(this.values.get(edge))
                + Math.abs(Math.log(min)) - 0)
                * this.INTERVAL_SIZE / (max - 0));

            if (Math.log(this.values.get(edge)) + Math.abs(Math.log(min)) == max) {
              index = INTERVAL_SIZE - 1;
            }
            shapes.put(edge, colors[index]);
          }

        }
        break;
      case ColorType.QUANTIL:
        colors = new Color[INTERVAL_SIZE];
        for (int i = 0; i < INTERVAL_SIZE; i++) {
          double[] rgb = ColorScale.HSVtoRGB(4.5 - (4.5 / (INTERVAL_SIZE - 1))
              * (double) i, 0.9, 1);
          colors[i] = new Color((float) rgb[0], (float) rgb[1], (float) rgb[2]);
        }

        int pas = (int) ((double) this.values.size() / (double) INTERVAL_SIZE);

        for (Shape edge : this.values.keySet()) {

          int id = -1;
          int cpt2 = 0;
          for (double d : valuesSorted) {
            if (d == this.values.get(edge)) {
              id = cpt2;
              break;
            }
            cpt2++;
          }

          int index = Math.min(id / pas, INTERVAL_SIZE - 1);
          shapes.put(edge, colors[index]);

        }
        break;
      case ColorType.MORE_CENTRAL:
        double[] rgb = ColorScale.HSVtoRGB(0, 0.9, 1);
        Color colorMax = new Color((float) rgb[0], (float) rgb[1],
            (float) rgb[2]);
        double valueMin = this.valuesSorted[valuesSorted.length
            - this.INTERVAL_SIZE];
        for (Shape edge : this.values.keySet()) {
          if (values.get(edge) >= valueMin) {
            shapes.put(edge, colorMax);
          } else {
            shapes.put(edge, new Color(0f, 0f, 0f, 0.5f));
          }
        }
        break;
      case ColorType.LESS_CENTRAL:
        rgb = ColorScale.HSVtoRGB(4.5, 0.9, 1);
        Color colorMin = new Color((float) rgb[0], (float) rgb[1],
            (float) rgb[2]);
        double valueMax = this.valuesSorted[this.INTERVAL_SIZE - 1];
        for (Shape edge : this.values.keySet()) {
          if (values.get(edge) <= valueMax) {
            shapes.put(edge, colorMin);
          } else {
            shapes.put(edge, new Color(0f, 0f, 0f, 0.5f));
          }
        }
        break;
      case ColorType.LENGTH_QUANTIL:
        Map<Shape, Double> lengths = new HashMap<Shape, Double>();
        double lenghtTot = 0;
        for (Shape s : this.values.keySet()) {
          Polyline2D p = (Polyline2D) s;
          double l = 0;
          for (int i = 0; i < p.xpoints.length - 1; i++) {
            l += Math.sqrt((p.xpoints[i] - p.xpoints[i + 1])
                * (p.xpoints[i] - p.xpoints[i + 1])
                + (p.ypoints[i] - p.ypoints[i + 1])
                * (p.ypoints[i] - p.ypoints[i + 1]));
          }
          lengths.put(s, l);
          lenghtTot += l;
        }
        colors = new Color[INTERVAL_SIZE];
        for (int i = 0; i < INTERVAL_SIZE; i++) {
          rgb = ColorScale.HSVtoRGB(4.5 - (4.5 / (INTERVAL_SIZE - 1))
              * (double) i, 0.9, 1);
          colors[i] = new Color((float) rgb[0], (float) rgb[1], (float) rgb[2]);
        }

        pas = (int) (lenghtTot / (double) INTERVAL_SIZE); // quantité de
                                                          // longueur a mettre
                                                          // dans chaque classe

        List<Shape> tmp = new ArrayList<Shape>(this.values.keySet());
        class ShapeComparator implements Comparator<Shape> {
          private PanelGUI p;

          public ShapeComparator(PanelGUI p) {
            this.p = p;
          }

          public int compare(Shape o1, Shape o2) {
            if (p.values.get(o1) > p.values.get(o2)) {
              return 1;
            } else if (p.values.get(o1) < p.values.get(o2)) {
              return -1;
            } else {
              return 0;
            }
          }
        }
        Collections.sort(tmp, new ShapeComparator(this));

        int cpt = 0;
        int length = 0;
        for (Shape edge : tmp) {
          length += lengths.get(edge);
          shapes.put(edge, colors[cpt]);
          if (length > pas) {
            cpt++;
            length = 0;
          }
        }
        break;
      default:
        break;
    }
    this.shapes = shapes;
    repaint();
  }

  public int getColorType() {
    return this.colorType;
  }

  public double[] getValuesSorted() {
    return this.valuesSorted;
  }

  public Map<Shape, Double> getValues() {
    return this.values;
  }

  public void setINTERVAL_SIZE(int iNTERVAL_SIZE) {
    INTERVAL_SIZE = iNTERVAL_SIZE;
  }

  public int getINTERVAL_SIZE() {
    return INTERVAL_SIZE;
  }

  public void setColorType(int i) {
    this.colorType = i;
  }

  public void setUseDistricts(boolean b) {
    this.useDisticts = b;
  }

  public boolean useDistrict() {
    return this.useDisticts;
  }

  public void setDistricts(List<Shape> districts) {
    this.districts = districts;
  }

  public List<Shape> getDistricts() {
    return this.districts;
  }

  public void setDistrictSelected(Shape districtSelected) {
    this.districtSelected = districtSelected;
    if (districtSelected != null) {
      this.f.enableStatButton(true);
    } else {
      this.f.enableStatButton(false);
    }
  }

  public Shape getDistrictSelected() {
    return districtSelected;
  }
}
