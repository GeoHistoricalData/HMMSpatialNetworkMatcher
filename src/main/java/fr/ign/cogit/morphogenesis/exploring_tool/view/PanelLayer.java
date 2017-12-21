package fr.ign.cogit.morphogenesis.exploring_tool.view;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IEnvelope;
import fr.ign.cogit.morphogenesis.exploring_tool.controler.MouseControler;
import fr.ign.cogit.morphogenesis.exploring_tool.model.layers.GraphLayer;
import fr.ign.cogit.morphogenesis.exploring_tool.utils.Parameters;

public class PanelLayer extends JPanel implements MouseListener,
    MouseMotionListener, MouseWheelListener, ChangeListener {

  private static final long serialVersionUID = 1L;
  private MainFrame mainFrame;
  private boolean init = true;
  private String title = "";
  private String mode; // mode géométrique, topoligique, etc.
  private String drawnCentrality = ""; // la centralité à cartographier
  private String drawableEntities;
  private List<GraphLayer> graphLayer;
  private int idCurrentGraphLayer;
  private double xmin, xmax, ymin, ymax;
  private double offsetX, offsetY; // décalage de la vue
  private double scale; // échelle de la vue
  private double SCALE_BASE = 100.; // échelle de base
  private int zoomLevel = 3;
  private MouseControler mouseControler;
  private JSlider slider; // le slider spatio temporel

  // gestionnaire de dessin
  PanelLayerDrawManager drawManager;

  // gestion deu rafraichissement
  private boolean TIMER_FPS_STATE = false;
  private boolean TIMER_FPS_STARTED = false;
  private Timer timerFPS = new Timer();
  private TimerTask timerFpsTask;
  private static final int FPS = 70;

  // zoom ST
  private boolean STZOOM_ON = false;
  private Rectangle2D zoomStRectangle;
  private int zoomDepth; // profondeur du zoom ST
  private int zoomDepthMax; // profondeur du zoom ST
  private String zoomDirection;

  public void paintComponent(Graphics g) {

    clear(g);
    Graphics2D g2d = (Graphics2D) g;

    // intialisation du gestionnaire de dessin
    this.drawManager.init(g2d);

    // Premier appel à paint (initialisation de la vue)
    if (init) {
      init = false;

      double newScale = 9;
      double newOffsetX = offsetX
          + getRealCoordX(this.getSize().getWidth() / 2.) / SCALE_BASE
          * (scale - newScale);
      double newOffsetY = offsetY
          - getRealCoordY(this.getSize().getHeight() / 2.) / SCALE_BASE
          * (scale - newScale);
      offsetX = newOffsetX;
      offsetY = newOffsetY;
      this.scale = newScale;
    }

    // Zoom ST ?
    if (zoomStRectangle != null) {
      this.drawManager.drawAreaZoomSt(this.zoomStRectangle);
    }

    // si on est en mode zoom ST, on dessine
    if (this.STZOOM_ON || zoomStRectangle != null) {
      this.drawManager.drawZoomStClip(this.zoomStRectangle);
    }

    // Affichage du graphe courrant
    GraphLayer graphLayer = this.graphLayer.get(this.idCurrentGraphLayer);
    this.drawManager.drawCurrentGraphLayer(graphLayer);

  }

  protected void clear(Graphics g) {
    super.paintComponent(g);
  }

  public PanelLayer(MainFrame mainFrame, List<GraphLayer> graphLayer,
      String mode) {
    super();

    this.mainFrame = mainFrame;
    this.drawManager = new PanelLayerDrawManager(this);
    this.setGraphLayer(graphLayer);
    this.setIdCurrentGraphLayer(0);
    this.getBoundsArea();
    this.mode = mode;
    this.idCurrentGraphLayer = 0;
    this.scale = SCALE_BASE;
    // slider
    this.slider = new JSlider(JSlider.HORIZONTAL, 0,
        this.graphLayer.size() - 1, 0);
    this.slider.setMajorTickSpacing(1);
    this.slider.setMinorTickSpacing(1);
    this.slider.setPaintTicks(false);
    int w = Math.min(750, 100 * graphLayer.size());
    this.slider.setPreferredSize(new Dimension(w, 30));
    Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
    for (int i = 0; i < this.graphLayer.size(); i++) {
      JLabel l = new JLabel(graphLayer.get(i).getG().getDate());
      l.setFont(new Font("Arial", Font.ITALIC, 10));
      labelTable.put(i, l);
    }
    this.slider.setLabelTable(labelTable);
    this.slider.setPaintLabels(true);
    this.slider.setSnapToTicks(true);
    this.slider.addChangeListener(this);

    // zoom st
    this.zoomDepth = this.graphLayer.size() - 1;
    this.zoomDepthMax = this.zoomDepth;

    mouseControler = new MouseControler(this);
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    this.addMouseWheelListener(this);

  }

  private void getBoundsArea() {
    IEnvelope e = this.graphLayer.get(idCurrentGraphLayer).getG().getPop()
        .getEnvelope();
    this.xmin = e.minX();
    this.ymin = e.minY();
    this.xmax = e.maxX();
    this.ymax = e.maxY();
    this.offsetX = -(xmin + (xmax - xmin) / 2.);
    this.offsetY = ymin + (ymax - ymin) / 2.;
  }

  public void setGraphLayer(List<GraphLayer> graphLayer) {
    this.graphLayer = graphLayer;
  }

  public List<GraphLayer> getGraphLayer() {
    return graphLayer;
  }

  public void setIdCurrentGraphLayer(int idCurrentGraphLayer) {
    this.idCurrentGraphLayer = idCurrentGraphLayer;
  }

  public int getIdCurrentGraphLayer() {
    return idCurrentGraphLayer;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public int getLocalCoordX(double x) {
    return (int) Math.round(this.getWidth() / 2 + this.offsetX + x * this.scale
        / SCALE_BASE);
  }

  /**
   * Convertit une ordonnée réelle en ordonnée locale (repère JPanel)
   * @param y l'ordonnée réelle
   * @return l'ordonnée locale
   */
  public int getLocalCoordY(double y) {
    return (int) Math.round(this.getHeight() / 2 + this.offsetY - y
        * this.scale / SCALE_BASE);
  }

  /**
   * Convertit une abscisse locale (repère JPanel) en abscisse réelle
   * @param x l'abscisse locale
   * @return l'abscisse réelle
   */
  public double getRealCoordX(double x) {
    return (x - (this.getWidth() / 2 + this.offsetX)) * SCALE_BASE / this.scale;
  }

  /**
   * Convertit une ordonnée locale (repère JPanel) en ordonnée réelle
   * @param y l'ordonnée locale
   * @return l'ordonnée réelle
   */
  public double getRealCoordY(double y) {
    return (y - (this.getHeight() / 2 + this.offsetY)) * -SCALE_BASE
        / this.scale;
  }

  public void setSCALE_BASE(double sCALE_BASE) {
    this.SCALE_BASE = sCALE_BASE;
  }

  public double getXmin() {
    return xmin;
  }

  public void setXmin(double xmin) {
    this.xmin = xmin;
  }

  public double getXmax() {
    return xmax;
  }

  public void setXmax(double xmax) {
    this.xmax = xmax;
  }

  public double getYmin() {
    return ymin;
  }

  public void setYmin(double ymin) {
    this.ymin = ymin;
  }

  public double getYmax() {
    return ymax;
  }

  public void setYmax(double ymax) {
    this.ymax = ymax;
  }

  public double getOffsetX() {
    return offsetX;
  }

  public void setOffsetX(double offsetX) {
    this.offsetX = offsetX;
  }

  public double getOffsetY() {
    return offsetY;
  }

  public void setOffsetY(double offsetY) {
    this.offsetY = offsetY;
  }

  public double getSCALE_BASE() {
    return this.SCALE_BASE;
  }

  public void setScale(double scale) {
    this.scale = scale;
  }

  public double getScale() {
    return scale;
  }

  public void setZoomLevel(int zoomLevel) {
    this.zoomLevel = zoomLevel;
  }

  public int getZoomLevel() {
    return zoomLevel;
  }

  public void mouseDragged(MouseEvent e) {
    Point pt = e.getPoint();

    if (this.STZOOM_ON) {
      // mode zoom st
      this.mouseControler.updateRectangleSize(this.zoomStRectangle, pt);
    }
    // pour l'instant! déplacement

    else {
      this.mainFrame.updateCoordinates(getRealCoordX(pt.getX()),
          getRealCoordY(pt.getY()));
      if (TIMER_FPS_STARTED) {

        // Verrou permettant de synchronise l'appel de cette fonction avec le
        // timer de rafraichissement
        if (TIMER_FPS_STATE) {
          // On verrouille --> tant que TIMER_FPS_STATE n'est pas remis à true
          // par
          // le timer de rafraichissement,
          // on ne rentre pas dans la fonction ci-dessous
          TIMER_FPS_STATE = false;
          this.mouseControler.translate(pt);
        }

      } else {
        // On déclenche le timer
        timerFPS = new Timer();
        timerFpsTask = new TimerTask() {
          public void run() {
            TIMER_FPS_STATE = true; // On met le flag à true
          }
        };
        timerFPS.scheduleAtFixedRate(timerFpsTask, 0, FPS);
        TIMER_FPS_STARTED = true;

      }
    }
  }

  public void mouseMoved(MouseEvent e) {
    Point pt = e.getPoint();
    this.mainFrame.updateCoordinates(getRealCoordX(pt.getX()),
        getRealCoordY(pt.getY()));
  }

  public void mouseClicked(MouseEvent e) {
    if (e.getButton() == MouseEvent.BUTTON3 && STZOOM_ON
        || e.getButton() == MouseEvent.BUTTON3 && zoomStRectangle != null) {
      this.STZOOM_ON = false;
      this.zoomStRectangle = null;
      @SuppressWarnings("unchecked")
      Hashtable<Integer, JLabel> tableLabel = (Hashtable<Integer, JLabel>) this.slider
          .getLabelTable();
      for (Integer i : tableLabel.keySet()) {
        JLabel label = (JLabel) this.slider.getLabelTable().get(i);
        label.setBorder(null);
      }
      this.getSlider().repaint();
      this.repaint();

    }
  }

  public void mousePressed(MouseEvent e) {
    Point pt = e.getPoint();
    this.mouseControler.updateMouseCoord(pt);
    if (e.getButton() == MouseEvent.BUTTON3) {
      this.STZOOM_ON = true;
      this.zoomStRectangle = this.mouseControler.udapteRectanle(e.getPoint());
    }

  }

  public void mouseReleased(MouseEvent e) {
    if (e.getButton() == MouseEvent.BUTTON1) {
      timerFPS.cancel();
      timerFPS.purge();
      TIMER_FPS_STARTED = false;
    } else if (e.getButton() == MouseEvent.BUTTON3) {
      this.STZOOM_ON = false;
      // bouton droit

      double x = this.zoomStRectangle.getX();
      double y = this.zoomStRectangle.getY();
      double width = this.zoomStRectangle.getWidth();
      double height = this.zoomStRectangle.getHeight();

      this.zoomStRectangle = new Rectangle2D.Double(getRealCoordX(x),
          getRealCoordY(y), getRealCoordX(x + width), getRealCoordY(y + height));

      this.repaint();
    }
  }

  public void mouseEntered(MouseEvent e) {

  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseWheelMoved(MouseWheelEvent e) {
    int wheelRotation = e.getWheelRotation();
    Point p = e.getPoint();
    if (wheelRotation < 0) {
      int zini = this.getZoomLevel();
      this.mouseControler.zoomIn(p);
      int zfin = this.getZoomLevel();
      if (zini != zfin) {
        this.mainFrame.updateScale(this.getScaleInCentimeters());
      }
    } else {
      int zini = this.getZoomLevel();
      this.mouseControler.zoomOut(p);
      int zfin = this.getZoomLevel();
      if (zini != zfin) {
        this.mainFrame.updateScale(this.getScaleInCentimeters());
      }
    }
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getMode() {
    return mode;
  }

  public JSlider getSlider() {
    return this.slider;
  }

  /**
   * Changement d'état avec le slider
   */
  public void stateChanged(ChangeEvent e) {
    JSlider source = (JSlider) e.getSource();
    if (!source.getValueIsAdjusting()) {
      int index = (int) source.getValue();
      if (index == this.idCurrentGraphLayer) {
        return;
      }
      @SuppressWarnings("unchecked")
      Hashtable<Integer, JLabel> tableLabel = (Hashtable<Integer, JLabel>) this
          .getSlider().getLabelTable();
      for (Integer i : tableLabel.keySet()) {
        JLabel label = (JLabel) this.getSlider().getLabelTable().get(i);
        label.setBorder(null);
      }
      this.getSlider().repaint();
      this.mouseControler.changeGraphLayer(index);
      // mise à jour de la profondeur du zoom
      if (this.getZoomDirection().equals(Parameters.ZOOM_FORWARD)) {
        int depthMax = this.graphLayer.size() - (this.idCurrentGraphLayer + 1);
        this.setZoomDepth(depthMax);
        this.setZoomDepthMax(this.getZoomDepth());
        this.mainFrame.updateZoomStPanel(this.getZoomDepth(),
            this.getZoomDepthMax());
      }
      if (this.getZoomDirection().equals(Parameters.ZOOM_BACKWARD)) {
        int depthMax = this.idCurrentGraphLayer;
        this.setZoomDepth(depthMax);
        this.setZoomDepthMax(this.getZoomDepth());
        this.mainFrame.updateZoomStPanel(this.getZoomDepth(),
            this.getZoomDepthMax());
      }
    }
  }

  public void delete() {
    for (GraphLayer g : this.graphLayer) {
      g.delete();
    }
    this.graphLayer.clear();
    this.graphLayer = null;
  }

  public void setDrawableEntities(String drawableEntities) {
    this.drawableEntities = drawableEntities;
    for (GraphLayer gl : this.graphLayer) {
      gl.setDrawableEntities(this.drawableEntities);
    }
  }

  public String getDrawableEntities() {
    return drawableEntities;
  }

  public int getScaleInCentimeters() {
    int screenDpi = Toolkit.getDefaultToolkit().getScreenResolution();
    double dpiCm = screenDpi / 2.54; // conversion en cm
    return ((int) (getRealCoordX(dpiCm) - getRealCoordX(0)));
  }

  public void setZoomDirection(String zoomDirection) {
    this.zoomDirection = zoomDirection;
  }

  public String getZoomDirection() {
    return zoomDirection;
  }

  public void setZoomDepth(int zoomDepth) {
    this.zoomDepth = zoomDepth;
  }

  public int getZoomDepth() {
    return zoomDepth;
  }

  public void setZoomDepthMax(int zoomDepthMax) {
    this.zoomDepthMax = zoomDepthMax;
  }

  public int getZoomDepthMax() {
    return zoomDepthMax;
  }

  public void setDrawnCentrality(String drawnCentrality) {
    this.drawnCentrality = drawnCentrality;
  }

  public String getDrawnCentrality() {
    return drawnCentrality;
  }
}
