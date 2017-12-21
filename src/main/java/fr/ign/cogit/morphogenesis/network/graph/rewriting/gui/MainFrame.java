package fr.ign.cogit.morphogenesis.network.graph.rewriting.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.batik.ext.awt.geom.Polygon2D;
import org.apache.batik.ext.awt.geom.Polyline2D;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.morphogenesis.network.graph.rewriting.AbstractGraph;
import fr.ign.cogit.morphogenesis.network.graph.rewriting.Edge;
import fr.ign.cogit.morphogenesis.network.graph.rewriting.GeometricalGraph;
import fr.ign.cogit.morphogenesis.network.graph.rewriting.TopologicalGraph;
import fr.ign.cogit.morphogenesis.network.graph.rewriting.io.GraphReader;

public class MainFrame extends JFrame {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private JTabbedPane mainPanel;
  private Dimension sizePanel;
  private JButton newProject, closeProject, param, stat, colors, screenshot,
      correlation, zoom;
  private List<AbstractGraph<?, ?>> graphs;
  private Map<String, IFeatureCollection<IFeature>> populations;
  private List<JTabPane> onglets;
  private boolean dualAnalysis, primalAnalysis, districtAnalysis;
  private double ax = -1, bx = -1, ay = -1, by = -1;
  private PanelGUI panelGuiGlobal;
  private String filePrimal, fileDual;

  public MainFrame() {

    init();
    Dimension screen = new Dimension(GraphicsEnvironment
        .getLocalGraphicsEnvironment().getMaximumWindowBounds().width,
        GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getMaximumWindowBounds().height);
    // placement et redimension
    this.setSize(screen);
    this.setPreferredSize(screen);
    // Titre de la fenêtre
    this.setTitle("Caractérisation");
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setResizable(false);
    this.pack();

    this.setSizePanel(new Dimension(this.mainPanel.getWidth(), this.mainPanel
        .getHeight()));
    this.populations = new HashMap<String, IFeatureCollection<IFeature>>();

  }

  private void init() {

    this.onglets = new ArrayList<JTabPane>();
    this.graphs = new ArrayList<AbstractGraph<?, ?>>();
    this.getContentPane().setLayout(new BorderLayout());

    // barre d'outils
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);

    newProject = new JButton(new ImageIcon(
        MainFrame.class.getResource("/images/new_repository.png")));
    newProject.addActionListener(new NewProjectAction(this));
    newProject.setToolTipText("");

    closeProject = new JButton(new ImageIcon(
        MainFrame.class.getResource("/images/fermer.png")));
    closeProject.addActionListener(new CloseAction(this));
    closeProject.setToolTipText("");
    closeProject.setEnabled(false);

    param = new JButton(new ImageIcon(
        MainFrame.class.getResource("/images/parametres.png")));
    param.addActionListener(new ParamAction(this));
    param.setToolTipText("");
    param.setEnabled(false);

    stat = new JButton(new ImageIcon(
        MainFrame.class.getResource("/images/stat.png")));
    stat.addActionListener(new statButtonActionPerformed(this));
    stat.setToolTipText("");
    stat.setEnabled(false);

    correlation = new JButton(new ImageIcon(
        MainFrame.class.getResource("/images/correlation.png")));
    correlation.addActionListener(new correlationButtonActionPerformed(this));
    correlation.setToolTipText("");
    correlation.setEnabled(false);

    colors = new JButton(new ImageIcon(
        MainFrame.class.getResource("/images/couleurs.png")));
    colors.addActionListener(new ColorAction(this));
    colors.setToolTipText("");
    colors.setEnabled(false);

    screenshot = new JButton(new ImageIcon(
        MainFrame.class.getResource("/images/screenshot.png")));
    screenshot.addActionListener(new ScreenShotAction(this));
    screenshot.setToolTipText("");
    screenshot.setEnabled(false);

    zoom = new JButton(new ImageIcon(
        MainFrame.class.getResource("/images/zoom.png")));
    zoom.addActionListener(new CenterAction(this));
    zoom.setToolTipText("");
    zoom.setEnabled(false);

    toolBar.add(newProject);
    toolBar.add(new JToolBar.Separator());
    toolBar.add(closeProject);
    toolBar.add(new JToolBar.Separator());
    toolBar.add(param);
    toolBar.add(new JToolBar.Separator());
    toolBar.add(stat);
    toolBar.add(new JToolBar.Separator());
    toolBar.add(correlation);
    toolBar.add(new JToolBar.Separator());
    toolBar.add(colors);
    toolBar.add(new JToolBar.Separator());
    toolBar.add(screenshot);
    toolBar.add(new JToolBar.Separator());
    toolBar.add(zoom);

    // panneau latéral
    // panneau central
    mainPanel = new JTabbedPane();
    // mainPanel.addChangeListener(new tabbedChangeListener(this));
    mainPanel.setBackground(Color.white);
    mainPanel.addChangeListener(new ChangeTabLister(this));

    this.getContentPane().add(toolBar, BorderLayout.NORTH);
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);

  }

  class CenterAction implements ActionListener {

    private MainFrame f;

    public CenterAction(MainFrame f) {
      super();
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      if (f.mainPanel.getSelectedIndex() != 0) {
        PanelGUI p = (PanelGUI) f.onglets.get(mainPanel.getSelectedIndex() - 1)
            .getPanelGui();
        p.center();
      }
    }
  }

  class CloseAction implements ActionListener {

    private MainFrame f;

    public CloseAction(MainFrame f) {
      super();
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      int n = JOptionPane.showConfirmDialog(f,
          "Voulez-vous vraiment fermer ce projet ?", "Attention",
          JOptionPane.YES_NO_OPTION);
      if (n == JOptionPane.YES_OPTION) {
        f.graphs.clear();
        f.mainPanel.removeAll();
        f.newProject.setEnabled(true);
        f.closeProject.setEnabled(false);
        f.correlation.setEnabled(false);
        f.param.setEnabled(false);
        f.stat.setEnabled(false);
        f.colors.setEnabled(false);
        f.screenshot.setEnabled(false);
        f.zoom.setEnabled(false);

      }
    }
  }

  class NewProjectAction implements ActionListener {

    private MainFrame f;

    public NewProjectAction(MainFrame f) {
      super();
      this.f = f;
    }

    public void actionPerformed(ActionEvent ee) {
      NewProjectFrame ff = new NewProjectFrame(f);
      ff.setVisible(true);
      f.setEnabled(false);
    }
  }

  class ParamAction implements ActionListener {

    private MainFrame f;

    public ParamAction(MainFrame f) {
      super();
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      if (mainPanel.getSelectedIndex() == 0) {
        // ParamFrame = new ParamFrame(this.f);
        ParamFrame ff = new ParamFrame(this.f);
        ff.setVisible(true);
        this.f.setEnabled(false);
      }
    }
  }

  class statButtonActionPerformed implements ActionListener {

    private MainFrame f;

    public statButtonActionPerformed(MainFrame f) {
      super();
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      if (this.f.mainPanel.getSelectedIndex() > 0) {
        this.f.setEnabled(false);
        AbstractGraph<?, ?> graph = this.f.graphs.get(f.mainPanel
            .getSelectedIndex() - 1);
        IndicatorsFrame ff = new IndicatorsFrame(this.f, graph);
        ff.setVisible(true);
      }
    }
  }

  class correlationButtonActionPerformed implements ActionListener {

    private MainFrame f;

    public correlationButtonActionPerformed(MainFrame f) {
      super();
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {

      CorrelationFrame ff = new CorrelationFrame(this.f,
          this.f.onglets.get(this.f.mainPanel.getSelectedIndex() - 1));
      ff.setVisible(true);
    }
  }

  class ColorAction implements ActionListener {

    private MainFrame f;

    public ColorAction(MainFrame f) {
      super();
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      if (mainPanel.getSelectedIndex() != 0) {
        PanelGUI p = (PanelGUI) f.onglets.get(
            f.mainPanel.getSelectedIndex() - 1).getPanelGui();
        ChangeColorTypeFrame ff = new ChangeColorTypeFrame(f, p);
        ff.setVisible(true);
      }
    }
  }

  class ScreenShotAction implements ActionListener {

    private MainFrame f;

    public ScreenShotAction(MainFrame f) {
      super();
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      JPanel p = (JPanel) f.mainPanel.getComponentAt(500, 500);
      BufferedImage bufImage = new BufferedImage(p.getSize().width,
          p.getSize().height, BufferedImage.TYPE_INT_RGB);
      p.paint(bufImage.createGraphics());
      JFileChooser dialogue = new JFileChooser();
      dialogue.showOpenDialog(null);
      File imageFile = dialogue.getSelectedFile();
      try {
        imageFile.createNewFile();
        ImageIO.write(bufImage, "png", imageFile);
      } catch (Exception ex) {
      }
    }
  }

  public void addOnglet(JTabPane p, String title) {
    this.onglets.add(p);
    JPanel panel = new JPanel(new BorderLayout());
    PanelGUI panelGui = p.getPanelGui();
    panel.add(p.getPanelList(), BorderLayout.NORTH);
    panel.add(panelGui, BorderLayout.CENTER);
    this.mainPanel.add(panel, title);
    this.mainPanel.setSelectedIndex(mainPanel.getTabCount() - 1);
    p.setId(this.mainPanel.getSelectedIndex() - 1);
  }

  public void updateOnglet() {
    JPanel panel = new JPanel(new BorderLayout());
    PanelGUI panelGui = this.onglets.get(mainPanel.getSelectedIndex() - 1)
        .getPanelGui();
    panel.add(
        this.onglets.get(mainPanel.getSelectedIndex() - 1).getPanelList(),
        BorderLayout.NORTH);
    panel.add(panelGui, BorderLayout.CENTER);
    this.mainPanel.setComponentAt(this.mainPanel.getSelectedIndex(), panel);
    this.repaint();
  }

  public void setSizePanel(Dimension sizePanel) {
    this.sizePanel = sizePanel;
  }

  public Dimension getSizePanel() {
    return sizePanel;
  }

  public static void main(String args[]) {
    MainFrame f = new MainFrame();
    f.setVisible(true);
  }

  public void createGeometricalGraph(String file) {

    GeometricalGraph g = GraphReader.createGeometricalGraph(file, 0);
    this.populations.put(g.getId(), g.getPop());

    if (this.ax == -1) {
      getCenters();
    }

    this.graphs.add(g);

  }

  public void createTopologicalGraph(String file) {

    TopologicalGraph g = GraphReader.createTopologicalGraph(file);
    this.populations.put(g.getId(), g.getPop());

    if (this.ax == -1) {
      getCenters();
    }

    this.graphs.add(g);

  }

  public void createDistricts(String f, PanelGUI p) {
    IPopulation<IFeature> pop = ShapefileReader.read(f);
    this.populations.put("DISTRICTS", pop);
    List<Shape> districts = new ArrayList<Shape>();

    for (IFeature ff : pop) {
      float[] ptx = new float[ff.getGeom().coord().size() - 1];
      float[] pty = new float[ff.getGeom().coord().size() - 1];

      int cpt = 0;
      for (IDirectPosition pos : ff.getGeom().coord()) {
        ptx[cpt] = (float) (ax * pos.getX() + bx);
        pty[cpt] = (float) (ay * pos.getY() + by);
        cpt++;
        if (cpt == ff.getGeom().coord().size() - 1) {
          break;
        }
      }
      Shape s = new Polygon2D(ptx, pty, ff.getGeom().coord().size() - 1);
      districts.add(s);
    }
    p.setDistricts(districts);
  }

  public void calculateCentralities(TopologicalGraph g,
      List<String> centralities) {
    Map<String, Map<Shape, Double>> map = new HashMap<String, Map<Shape, Double>>();

    for (String centralityName : centralities) {
      Map<Shape, Double> valuesShape = new HashMap<Shape, Double>();
      Map<Edge, Double> values = g.getCentrality(centralityName);
      for (Edge edge : g.getVertices()) {
        int size = edge.coords().size();
        float[] x = new float[size];
        float[] y = new float[size];
        int cpt = 0;
        for (Point2D.Double pos : edge.coords()) {
          x[cpt] = (float) (ax * pos.getX() + bx);
          y[cpt] = (float) (ay * pos.getY() + by);
          cpt++;
        }
        Polyline2D s = new Polyline2D(x, y, size);
        valuesShape.put(s, values.get(edge));
      }

      map.put(centralityName, valuesShape);
    }

    JTabPane p = new JTabPane(this, map, g.getId());
    this.addOnglet(p, "Graphe dual");

    this.param.setEnabled(true);
    this.stat.setEnabled(true);
    this.correlation.setEnabled(true);
    this.colors.setEnabled(true);
    this.screenshot.setEnabled(true);
    this.zoom.setEnabled(true);
    this.setEnabled(true);
  }

  public void calculateCentralities(GeometricalGraph g,
      List<String> centralities) {
    Map<String, Map<Shape, Double>> map = new HashMap<String, Map<Shape, Double>>();

    for (String centralityName : centralities) {
      Map<Shape, Double> valuesShape = new HashMap<Shape, Double>();
      Map<Edge, Double> values = g.getCentrality(centralityName);
      for (Edge edge : g.getEdges()) {
        int size = edge.coords().size();
        float[] x = new float[size];
        float[] y = new float[size];
        int cpt = 0;
        for (Point2D.Double pos : edge.coords()) {
          x[cpt] = (float) (ax * pos.getX() + bx);
          y[cpt] = (float) (ay * pos.getY() + by);
          cpt++;
        }
        Polyline2D s = new Polyline2D(x, y, size);
        valuesShape.put(s, values.get(edge));
      }

      map.put(centralityName, valuesShape);
    }
    JTabPane p = new JTabPane(this, map, g.getId());
    this.addOnglet(p, "Graphe primal");

    this.newProject.setEnabled(false);
    this.closeProject.setEnabled(true);
    this.param.setEnabled(true);
    this.stat.setEnabled(true);
    this.correlation.setEnabled(true);
    this.colors.setEnabled(true);
    this.screenshot.setEnabled(true);
    this.zoom.setEnabled(true);
    this.setEnabled(true);
  }

  public void setPrimalAnalysis(boolean primalAnaysis) {
    this.primalAnalysis = primalAnaysis;
  }

  public boolean isPrimalAnalysis() {
    return primalAnalysis;
  }

  public void setDualAnalysis(boolean dualAnalysis) {
    this.dualAnalysis = dualAnalysis;
  }

  public boolean isDualAnalysis() {
    return dualAnalysis;
  }

  public AbstractGraph<?, ?> getGraphById(String s) {
    for (AbstractGraph<?, ?> g : this.graphs) {
      if (g.getId().equals(s)) {
        return g;
      }
    }
    return null;
  }

  public void setDistrictAnalysis(boolean districtAnalysis) {
    this.districtAnalysis = districtAnalysis;
  }

  public boolean isDistrictAnalysis() {
    return districtAnalysis;
  }

  public void initMap(String fileDistricts) {
    Map<Shape, Color> shapes = new HashMap<Shape, Color>();
    Map<Shape, Double> values = new HashMap<Shape, Double>();

    if (this.primalAnalysis) {
      for (Edge edge : ((GeometricalGraph) this
          .getGraphById("Global Geometrical Graph")).getEdges()) {
        int size = edge.coords().size();
        float[] x = new float[size];
        float[] y = new float[size];
        int cpt = 0;
        for (Point2D.Double pos : edge.coords()) {
          x[cpt] = (float) (ax * pos.getX() + bx);
          y[cpt] = (float) (ay * pos.getY() + by);
          cpt++;
        }
        Polyline2D s = new Polyline2D(x, y, size);
        Color c = new Color((float) 0.5, (float) 0.5, (float) 0.5, 0.6f);
        shapes.put(s, c);
        values.put(s, 0.);
      }
    } else {
      for (Edge edge : ((TopologicalGraph) this
          .getGraphById("Global Topological Graph")).getVertices()) {
        int size = edge.coords().size();
        float[] x = new float[size];
        float[] y = new float[size];
        int cpt = 0;
        for (Point2D.Double pos : edge.coords()) {
          x[cpt] = (float) (ax * pos.getX() + bx);
          y[cpt] = (float) (ay * pos.getY() + by);
          cpt++;
        }
        Polyline2D s = new Polyline2D(x, y, size);
        Color c = new Color((float) 0.5, (float) 0.5, (float) 0.5, 0.6f);
        shapes.put(s, c);
        values.put(s, 0.);
      }
    }

    PanelGUI p = new PanelGUI(this, shapes, values);
    this.setPanelGuiGlobal(p);
    p.changeColorType(-1);
    if (this.districtAnalysis) {
      p.setUseDistricts(true);
      createDistricts(fileDistricts, p);
    }
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(p, BorderLayout.CENTER);
    this.mainPanel.add(panel, "Data");
    this.mainPanel.setSelectedIndex(mainPanel.getTabCount() - 1);
  }

  public void getCenters() {

    IFeatureCollection<IFeature> pop = this.populations.values().iterator()
        .next();
    double xmax = pop.getEnvelope().getUpperCorner().getX();
    double xmin = pop.getEnvelope().getLowerCorner().getX();
    double ymax = pop.getEnvelope().getUpperCorner().getY();
    double ymin = pop.getEnvelope().getLowerCorner().getY();

    int sizeFramex = (int) this.getSizePanel().getWidth();
    int sizeFramey = (int) this.getSizePanel().getHeight();
    int sizex = (int) (xmax - xmin) + 1;
    int sizey = (int) (ymax - ymin) + 1;

    if (sizex > sizey) {
      sizex = sizeFramex;
      sizey = (int) (sizex * (ymax - ymin) / (xmax - xmin)) + 1;
    } else {
      sizey = sizeFramey;
      sizex = (int) (sizey * (xmax - xmin) / (ymax - ymin)) + 1;
    }

    ax = (double) sizex / (xmax - xmin);
    bx = -ax * xmin;
    ay = (double) sizey / (ymax - ymin);
    by = -ay * ymin;

    this.newProject.setEnabled(false);
    this.closeProject.setEnabled(true);
    this.param.setEnabled(true);

  }

  class ChangeTabLister implements ChangeListener {

    private MainFrame f;

    public ChangeTabLister(MainFrame f) {
      this.f = f;
    }

    public void stateChanged(ChangeEvent e) {
      JTabbedPane panel = (JTabbedPane) e.getSource();
      if (panel.getSelectedIndex() == 0) {
        this.f.zoom.setEnabled(false);
        this.f.colors.setEnabled(false);
        if (panelGuiGlobal.getDistrictSelected() != null) {
          this.f.stat.setEnabled(true);
          this.f.correlation.setEnabled(false);
        } else {
          this.f.stat.setEnabled(false);
          this.f.correlation.setEnabled(false);
        }
        this.f.param.setEnabled(true);
        this.f.screenshot.setEnabled(false);
      } else {
        this.f.zoom.setEnabled(true);
        this.f.colors.setEnabled(true);
        this.f.stat.setEnabled(true);
        this.f.correlation.setEnabled(true);
        this.f.param.setEnabled(false);
        this.f.screenshot.setEnabled(true);
      }
    }

  }

  public boolean usePrimalAnalysis() {
    return this.primalAnalysis;
  }

  public boolean useDualAnalysis() {
    return this.dualAnalysis;
  }

  public boolean useDistrictAnalysis() {
    return this.districtAnalysis;
  }

  public void setFilePrimal(String filePrimal) {
    this.filePrimal = filePrimal;
  }

  public String getFilePrimal() {
    return filePrimal;
  }

  public void setFileDual(String fileDual) {
    this.fileDual = fileDual;
  }

  public String getFileDual() {
    return fileDual;
  }

  public void setPanelGuiGlobal(PanelGUI panelGuiGlobal) {
    this.panelGuiGlobal = panelGuiGlobal;
  }

  public PanelGUI getPanelGuiGlobal() {
    return panelGuiGlobal;
  }

  public void enableStatButton(boolean b) {
    this.stat.setEnabled(b);
  }

  /*
   * class tabbedChangeListener implements ChangeListener {
   * 
   * private MainFrame f;
   * 
   * public tabbedChangeListener(MainFrame f) { this.f = f; }
   * 
   * public void stateChanged(ChangeEvent e) { // Panneau latéral f.panelLayer =
   * new JPanel(); f.panelLayer.add(f.onglets.get(mainPanel.getSelectedIndex())
   * .getPanelList()); f.panelLayer.setPreferredSize(new Dimension(120, 150));
   * panelLayer.setAutoscrolls(true);
   * panelLayer.setBorder(BorderFactory.createEtchedBorder());
   * f.getContentPane().add(panelLayer, BorderLayout.EAST); } }
   */
}
