package fr.ign.cogit.morphogenesis.exploring_tool.controler;

import java.awt.Point;
import java.awt.geom.Rectangle2D;

import fr.ign.cogit.morphogenesis.exploring_tool.view.PanelLayer;

public class MouseControler {

  private static final int DEFAULT_MIN_ZOOM_LEVEL = 0;
  private static final int DEFAULT_MAX_ZOOM_LEVEL = 12;
  private static final double DEFAULT_ZOOM_MULTIPLICATION_FACTOR = 2;
  private double xold, yold; // coordonnées du point local précédent

  private PanelLayer panelLayer;

  public MouseControler(PanelLayer panelLayer) {
    this.setPanelLayer(panelLayer);
  }

  public void zoomIn(Point p) {
    if (panelLayer.getZoomLevel() < DEFAULT_MAX_ZOOM_LEVEL) {
      double newScale = this.panelLayer.getScale()
          * DEFAULT_ZOOM_MULTIPLICATION_FACTOR;
      double newOffsetX = this.panelLayer.getOffsetX()
          + this.panelLayer.getRealCoordX(p.getX())
          / this.panelLayer.getSCALE_BASE()
          * (this.panelLayer.getScale() - newScale);
      double newOffsetY = this.panelLayer.getOffsetY()
          - this.panelLayer.getRealCoordY(p.getY())
          / this.panelLayer.getSCALE_BASE()
          * (this.panelLayer.getScale() - newScale);
      this.panelLayer.setOffsetX(newOffsetX);
      this.panelLayer.setOffsetY(newOffsetY);
      this.panelLayer.setScale(newScale);
      this.panelLayer.setZoomLevel(this.panelLayer.getZoomLevel() + 1);
      this.panelLayer.repaint();
    }
  }

  public void zoomOut(Point p) {
    if (panelLayer.getZoomLevel() > DEFAULT_MIN_ZOOM_LEVEL) {
      double newScale = this.panelLayer.getScale() * 1.
          / DEFAULT_ZOOM_MULTIPLICATION_FACTOR;
      double newOffsetX = this.panelLayer.getOffsetX()
          + this.panelLayer.getRealCoordX(p.getX())
          / this.panelLayer.getSCALE_BASE()
          * (this.panelLayer.getScale() - newScale);
      double newOffsetY = this.panelLayer.getOffsetY()
          - this.panelLayer.getRealCoordY(p.getY())
          / this.panelLayer.getSCALE_BASE()
          * (this.panelLayer.getScale() - newScale);
      this.panelLayer.setOffsetX(newOffsetX);
      this.panelLayer.setOffsetY(newOffsetY);
      this.panelLayer.setScale(newScale);
      this.panelLayer.setZoomLevel(this.panelLayer.getZoomLevel() - 1);
      this.panelLayer.repaint();
    }
  }

  public void translate(Point pt) {
    double x = pt.getX();
    double y = pt.getY();
    if (x == xold && y == yold) {
      return;
    }
    this.panelLayer.setOffsetX(this.panelLayer.getOffsetX() + (x - this.xold));
    this.panelLayer.setOffsetY(this.panelLayer.getOffsetY() + (y - this.yold));
    this.xold = x;
    this.yold = y;
    this.panelLayer.repaint();
  }

  public void setPanelLayer(PanelLayer panelLayer) {
    this.panelLayer = panelLayer;
  }

  public PanelLayer getPanelLayer() {
    return panelLayer;
  }

  public void updateMouseCoord(Point pt) {
    this.xold = pt.getX();
    this.yold = pt.getY();
  }

  /**
   * Changement de couche de graphe
   * @param index
   */
  public void changeGraphLayer(int index) {
    this.panelLayer.setIdCurrentGraphLayer(index);
    this.panelLayer.repaint();
  }

  public Rectangle2D udapteRectanle(Point point) {
    Rectangle2D r = new Rectangle2D.Double(point.getX(), point.getY(), 0, 0);
    return r;
  }

  public boolean checkIfMouseHasMoved(Point pt) {
    double x = pt.getX();
    double y = pt.getY();
    if (x == xold && y == yold) {
      return false;
    } else {
      return true;
    }
  }

  public void updateRectangleSize(Rectangle2D zoomStRectangle, Point pt) {
    double x = pt.getX();
    double y = pt.getY();
    if (x == xold && y == yold) {
      return;
    }

    zoomStRectangle.setFrame(zoomStRectangle.getX(), zoomStRectangle.getY(),
        zoomStRectangle.getWidth() + x - xold, zoomStRectangle.getHeight() + y
            - yold);
    this.xold = x;
    this.yold = y;

  }
}
