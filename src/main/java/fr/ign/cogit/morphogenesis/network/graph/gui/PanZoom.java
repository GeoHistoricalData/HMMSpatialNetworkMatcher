package fr.ign.cogit.morphogenesis.network.graph.gui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class PanZoom implements MouseListener, MouseMotionListener,
    MouseWheelListener {
  public static final int DEFAULT_MIN_ZOOM_LEVEL = -5;
  public static final int DEFAULT_MAX_ZOOM_LEVEL = 10;
  public static final double DEFAULT_ZOOM_MULTIPLICATION_FACTOR = 2;

  private Component targetComponent;
  private PanelGUI panelGui;

  private int zoomLevel = 0;
  private int scaleFactor = 1;
  private int minZoomLevel = DEFAULT_MIN_ZOOM_LEVEL;
  private int maxZoomLevel = DEFAULT_MAX_ZOOM_LEVEL;
  private double zoomMultiplicationFactor = DEFAULT_ZOOM_MULTIPLICATION_FACTOR;

  private Point dragStartScreen;
  private Point dragEndScreen;
  private AffineTransform coordTransform = new AffineTransform();

  public PanZoom(Component targetComponent) {
    this.targetComponent = targetComponent;
    this.panelGui = (PanelGUI) targetComponent;
  }

  public PanZoom(Component targetComponent, int minZoomLevel, int maxZoomLevel,
      double zoomMultiplicationFactor) {
    this.targetComponent = targetComponent;
    this.minZoomLevel = minZoomLevel;
    this.maxZoomLevel = maxZoomLevel;
    this.zoomMultiplicationFactor = zoomMultiplicationFactor;
  }

  public void mouseClicked(MouseEvent e) {
    if (panelGui.useDistrict()) {
      /*
       * Point pt = new Point(); pt.setLocation(e.getPoint().getX(),
       * panelGui.getSize().height - e.getPoint().getY());
       */
      Point2D.Float pt;
      try {
        pt = transformPoint(e.getPoint());
        boolean selection = false;
        for (Shape s : panelGui.getDistricts()) {
          if (s.contains(pt)) {
            panelGui.setDistrictSelected(s);
            selection = true;
            break;
          }
        }
        if (!selection) {
          panelGui.setDistrictSelected(null);
        }
        targetComponent.repaint();

      } catch (NoninvertibleTransformException e1) {
        e1.printStackTrace();
      }

    } else {
      Point2D.Float pt;

      try {
        pt = transformPoint(e.getPoint());
        System.out.println(e.getPoint().toString() + " " + pt.toString());
      } catch (NoninvertibleTransformException e1) {
        e1.printStackTrace();
      }

    }
  }

  public void mousePressed(MouseEvent e) {
    dragStartScreen = e.getPoint();
    dragEndScreen = null;
  }

  public void mouseReleased(MouseEvent e) {
    // moveCamera(e);
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseMoved(MouseEvent e) {
  }

  public void mouseDragged(MouseEvent e) {
    moveCamera(e);
  }

  public void mouseWheelMoved(MouseWheelEvent e) {
    zoomCamera(e);
  }

  private void moveCamera(MouseEvent e) {
    try {
      scaleFactor = 0;
      dragEndScreen = e.getPoint();
      Point2D.Float dragStart = transformPoint(dragStartScreen);
      Point2D.Float dragEnd = transformPoint(dragEndScreen);
      double dx = dragEnd.getX() - dragStart.getX();
      double dy = dragEnd.getY() - dragStart.getY();
      coordTransform.translate(dx, dy);
      dragStartScreen = dragEndScreen;
      dragEndScreen = null;
      targetComponent.repaint();
    } catch (NoninvertibleTransformException ex) {
      ex.printStackTrace();
    }
  }

  private void zoomCamera(MouseWheelEvent e) {
    try {
      int wheelRotation = e.getWheelRotation();
      Point p = e.getPoint();
      if (wheelRotation < 0) {
        if (zoomLevel < maxZoomLevel) {
          scaleFactor = 1;
          zoomLevel++;
          Point2D p1 = transformPoint(p);
          coordTransform.scale(zoomMultiplicationFactor,
              zoomMultiplicationFactor);
          Point2D p2 = transformPoint(p);
          coordTransform
              .translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
          targetComponent.repaint();
        }
      } else {
        if (zoomLevel > minZoomLevel) {
          zoomLevel--;
          scaleFactor = -1;
          Point2D p1 = transformPoint(p);
          coordTransform.scale(1 / zoomMultiplicationFactor,
              1 / zoomMultiplicationFactor);
          Point2D p2 = transformPoint(p);
          coordTransform
              .translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
          targetComponent.repaint();
        }
      }
    } catch (NoninvertibleTransformException ex) {
      ex.printStackTrace();
    }
  }

  private Point2D.Float transformPoint(Point p1)
      throws NoninvertibleTransformException {
    // System.out.println("Model -> Screen Transformation:");
    // showMatrix(coordTransform);
    AffineTransform inverse = coordTransform.createInverse();
    // System.out.println("Screen -> Model Transformation:");
    // showMatrix(inverse);

    Point2D.Float p2 = new Point2D.Float();
    inverse.transform(p1, p2);
    return p2;
  }

  private void showMatrix(AffineTransform at) {
    double[] matrix = new double[6];
    at.getMatrix(matrix); // { m00 m10 m01 m11 m02 m12 }
    int[] loRow = { 0, 0, 1 };
    for (int i = 0; i < 2; i++) {
      System.out.print("[ ");
      for (int j = i; j < matrix.length; j += 2) {
        System.out.printf("%5.1f ", matrix[j]);
      }
      System.out.print("]\n");
    }
    System.out.print("[ ");
    for (int i = 0; i < loRow.length; i++) {
      System.out.printf("%3d   ", loRow);

    }

    System.out.print("]\n");

    System.out.println("---------------------");

  }

  public double getZoomLevel() {
    return zoomLevel;
  }

  public double getScaleFactor() {
    if (scaleFactor == 1) {
      return this.zoomMultiplicationFactor;
    } else if (scaleFactor == 0) {
      return 1;
    } else {
      return (1. / this.zoomMultiplicationFactor);
    }
  }

  public void setZoomLevel(int zoomLevel) {

    this.zoomLevel = zoomLevel;

  }

  public AffineTransform getCoordTransform() {

    return coordTransform;

  }

  public void setCoordTransform(AffineTransform coordTransform) {

    this.coordTransform = coordTransform;

  }

  public void setScaleFactor(int i) {
    this.scaleFactor = i;
  }

}
