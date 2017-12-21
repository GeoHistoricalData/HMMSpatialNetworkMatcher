package fr.ign.cogit.morphogenesis.exploring_tool.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;

import fr.ign.cogit.morphogenesis.exploring_tool.controler.MainFrameControler;
import fr.ign.cogit.morphogenesis.exploring_tool.model.layers.GraphLayer;
import fr.ign.cogit.morphogenesis.exploring_tool.utils.Parameters;
import fr.ign.cogit.morphogenesis.network.graph.gui.ParamFrame;

public class MainFrame extends JFrame {

  private static final long serialVersionUID = 1L;
  // main panels
  private JPanel mainPanel, northPanel, westPanel, southPanel;
  private JTabbedPane centerPanel;
  private JMenuBar menuBar;
  private JToolBar toolBar;
  private JXTaskPaneContainer panelWest1, panelWest2;

  private JLabel labelImgProcessing, labelInfoProcessing, labelCoordinates,
      labelScale;

  // couche de graphe
  private JRadioButton radioButonEdgesAndNodes, radioButtonEdgesOnly,
      radioButtonNodesOnly;

  // classification
  private JComboBox comboClassification, comboMethod;
  private JSpinner spinNbClasses;
  private JCheckBox boxAllIndicators, boxAllDates;
  private JButton butAdvancedClassifiSettings;

  // zoom spatio-temporel
  private JSlider sliderDepthZoomSt;
  private JRadioButton zoomStFuture, zoomStPast;

  // menu west
  private JXTaskPane panelGraphLayers, panelDrawing, panelAdditonalLayers,
      panelGlobalMorphoIndicators, paneldynamics, panelLocalMorphoIndicators;;
  // statistiques
  JLabel labelNbNodes, labelNbEdges;

  // menus
  private JMenu fileMenu;
  private JMenu newFileMenu;
  private JMenuItem closeMenu;
  private JMenuItem closeAllMenu;
  private JMenu newFileGeometricalMenu;
  private JMenuItem newFileGeometricalShpMenuItem;

  // panneau indicateurs
  JPanel panelLocalGlobalMorphoIndicators, panelLocalLocalMorphoIndicators;
  JComboBox comboBoxGlobalMorphoIndicators,
      comboBoxLocaGloballMorphoIndicators, comboBoxLocaLocalMorphoIndicators;
  JButton butCalculateGlobalMorphoIndicators,
      butCalculateLocalGlobalMorphoIndicators,
      butCalculateLocalLocalMorphoIndicators;
  JList listOkGlobalMorpoIndicators, listOkLocalLocalMorpoIndicators,
      listOkLocalGlobalMorpoIndicators;

  // tool bar

  // Liste des Panel
  private List<PanelLayer> panelLayers;

  //
  private boolean ENABLE = true;

  // Various strings
  private final String warning = "Attention"; //$NON-NLS-1$
  private final String frameTitle = "ST-Network Exploring Tool"; //$NON-NLS-1$
  private final String fileMenuTitle = "Fichier"; //$NON-NLS-1$
  private final String newFileMenuTitle = "Nouvelle exploration"; //$NON-NLS-1$
  private final String newFileGeometricalMenuTitle = "Analyse Géométrique"; //$NON-NLS-1$
  private final String newFileMenuItemTitle = "Charger des shapefiles"; //$NON-NLS-1$
  private final String repertoryChoiceText = "Répertoire des fichiers de graphe"; //$NON-NLS-1$
  private final String closeMenuText = "Fermer"; //$NON-NLS-1$
  private final String closeAllMenuText = "Tout fermer"; //$NON-NLS-1$
  private final String closeMenuWarning = "Fermer l'onglet courrant ?"; //$NON-NLS-1$
  private final String closeAllMenuWarning = "Fermer tous les onglets ?"; //$NON-NLS-1$
  private final String analysis = "Analyse structurelle"; //$NON-NLS-1$
  private final String drawing = "Affichage"; //$NON-NLS-1$
  private final String layers = "Couches de graphe"; //$NON-NLS-1$
  private final String properties = "Propriétés"; //$NON-NLS-1$
  private final String data = "Données"; //$NON-NLS-1$
  private final String globalMorphologicalIndicatorsText = "Mesures globales"; //$NON-NLS-1$
  private final String localMorphologicalIndicatorsText = "Mesures locales"; //$NON-NLS-1$
  private final String localLocalMorphologicalIndicatorsText = "Mesures locales de voisinage"; //$NON-NLS-1$
  private final String localGlobalMorphologicalIndicatorsText = "Mesures locales d'ensemble"; //$NON-NLS-1$
  private final String morphogeneticalIndicators = "Morphogénèse"; //$NON-NLS-1$
  private final String edges = "Arcs"; //$NON-NLS-1$
  private final String nodes = "Noeuds"; //$NON-NLS-1$
  private final String edgesAndNodes = "Arcs et Noeuds"; //$NON-NLS-1$
  private final String scaleText = "Échelle : "; //$NON-NLS-1$
  private final String coordinatesText = "Coordonnées : "; //$NON-NLS-1$
  private final String zoomStPanelText = "Zoom ST"; //$NON-NLS-1$
  private final String zoomStFutureText = "Futur"; //$NON-NLS-1$
  private final String zoomStPastText = "Passé"; //$NON-NLS-1$
  private final String zoomDepthText = "    Profondeur du zoom ST"; //$NON-NLS-1$
  private final String calculatedIndicators = "  Indicateurs calculés"; //$NON-NLS-1$
  private final String classificationText = "Classification"; //$NON-NLS-1$
  private final String classificationText2 = "Aucun"; //$NON-NLS-1$

  /**
   * Default constructor
   */
  public MainFrame() {
    initComponents();
    Dimension screen = new Dimension(GraphicsEnvironment
        .getLocalGraphicsEnvironment().getMaximumWindowBounds().width,
        GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getMaximumWindowBounds().height);
    this.panelLayers = new ArrayList<PanelLayer>();
    // placement et redimension
    this.setSize(screen);
    this.setPreferredSize(screen);
    // Titre de la fenêtre
    this.setTitle(this.frameTitle);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setResizable(false);
    this.pack();
    this.setVisible(true);
  }

  /**
   * Initialization of graphics components
   */
  private void initComponents() {

    // menu bar
    this.initMenuBar();
    // north panel (tool bar)
    this.initNorthPanel();

    // west panel
    this.initWestPanel();

    // south panel
    this.initSouthPanel();

    // center panel
    this.centerPanel = new JTabbedPane();
    this.centerPanel.addChangeListener(MainFrameControler.newTabSelected(this));

    // main panel
    this.mainPanel = new JPanel(new BorderLayout());
    this.mainPanel.add(this.northPanel, BorderLayout.NORTH);
    this.mainPanel.add(this.westPanel, BorderLayout.WEST);
    this.mainPanel.add(this.centerPanel, BorderLayout.CENTER);
    this.mainPanel.add(this.southPanel, BorderLayout.SOUTH);

    this.setContentPane(this.mainPanel);

  }

  // ****************************** IHM ***************************

  /**
   * Initialisation du menu
   */
  private void initMenuBar() {
    this.menuBar = new JMenuBar();

    // menu fichier
    this.fileMenu = new JMenu(this.fileMenuTitle);
    // nouveau
    this.newFileMenu = new JMenu(this.newFileMenuTitle);
    this.newFileGeometricalMenu = new JMenu(this.newFileGeometricalMenuTitle);
    this.newFileGeometricalShpMenuItem = new JMenuItem(
        this.newFileMenuItemTitle);
    this.newFileGeometricalShpMenuItem
        .addActionListener(new newGraphProjectActionListener(this,
            Parameters.SHAPEFILE, Parameters.GEOMETRICAL_MODE));
    this.newFileGeometricalMenu.add(newFileGeometricalShpMenuItem);
    this.newFileMenu.add(newFileGeometricalMenu);
    this.fileMenu.add(newFileMenu);
    // fermer
    this.closeMenu = new JMenuItem(this.closeMenuText);
    this.closeMenu.setEnabled(false);
    this.closeMenu.addActionListener(new closeActionListener(this));
    this.closeAllMenu = new JMenuItem(this.closeAllMenuText);
    this.closeAllMenu.setEnabled(false);
    this.fileMenu.add(closeMenu);
    this.fileMenu.add(closeAllMenu);

    this.menuBar.add(fileMenu);

    this.setJMenuBar(menuBar);
  }

  /**
   * Initialisation du panneau nord (tool bar)
   */
  private void initNorthPanel() {
    this.northPanel = new JPanel();
    // tool bar
    this.toolBar = new JToolBar();
    this.northPanel.add(this.toolBar);
  }

  public void addPanelLayer(PanelLayer panelLayer) {
    panelLayer.setTitle(panelLayer.getMode().toLowerCase() + " "
        + (this.panelLayers.size() + 1));
    this.panelLayers.add(panelLayer);
    JPanel newPanelCentral = new JPanel(new BorderLayout());
    newPanelCentral.add(panelLayer, BorderLayout.CENTER);
    if (panelLayer.getGraphLayer().size() > 1) {
      JPanel panelSlider = new JPanel();
      panelSlider.setBorder(BorderFactory.createLineBorder(new Color(0f, 0f,
          0f, 0.3f)));
      panelSlider.add(panelLayer.getSlider());
      newPanelCentral.add(panelSlider, BorderLayout.SOUTH);
    }

    this.centerPanel.add(newPanelCentral, panelLayer.getTitle());
    this.centerPanel.setSelectedIndex(this.panelLayers.size() - 1);

  }

  /**
   * panel ouest
   */
  private void initWestPanel() {
    this.westPanel = new JPanel(new GridLayout(2, 1, 0, 5));
    this.westPanel.setPreferredSize(new Dimension(250, 500));
    this.westPanel.setBorder(BorderFactory
        .createEtchedBorder(EtchedBorder.LOWERED));

    // panel du haut
    this.panelWest1 = new JXTaskPaneContainer();
    this.panelWest1.setBorder(BorderFactory.createTitledBorder(drawing));

    // couches de graphe
    this.panelGraphLayers = new JXTaskPane();
    this.panelGraphLayers.setCollapsed(true);
    this.panelGraphLayers.setEnabled(false);
    this.panelGraphLayers.setTitle(layers);
    this.radioButonEdgesAndNodes = new JRadioButton(edgesAndNodes);
    this.radioButonEdgesAndNodes.setEnabled(false);
    this.radioButonEdgesAndNodes.addActionListener(MainFrameControler
        .getRadioButonGraphLayerActivated(Parameters.NODES_EDGES, this));
    this.radioButtonEdgesOnly = new JRadioButton(edges);
    this.radioButtonEdgesOnly.setEnabled(false);
    this.radioButtonEdgesOnly.setSelected(true);
    this.radioButtonEdgesOnly.addActionListener(MainFrameControler
        .getRadioButonGraphLayerActivated(Parameters.EDGES_ONLY, this));
    this.radioButtonNodesOnly = new JRadioButton(nodes);
    this.radioButtonNodesOnly.setEnabled(false);
    this.radioButtonNodesOnly.addActionListener(MainFrameControler
        .getRadioButonGraphLayerActivated(Parameters.NODES_ONLY, this));
    ButtonGroup group = new ButtonGroup();
    group.add(this.radioButonEdgesAndNodes);
    group.add(this.radioButtonEdgesOnly);
    group.add(this.radioButtonNodesOnly);
    this.panelGraphLayers.add(this.radioButonEdgesAndNodes);
    this.panelGraphLayers.add(this.radioButtonEdgesOnly);
    this.panelGraphLayers.add(this.radioButtonNodesOnly);
    this.panelWest1.add(panelGraphLayers);

    // affichage
    this.panelDrawing = new JXTaskPane();
    this.panelDrawing.setCollapsed(true);
    this.panelDrawing.setTitle(properties);

    // classification
    JPanel panelClassification = new JPanel(new VerticalLayout(8));
    panelClassification.setBorder(BorderFactory
        .createTitledBorder(classificationText));

    this.comboClassification = new JComboBox();
    this.comboClassification.addItem(classificationText2);
    this.comboClassification.setEnabled(false);
    panelClassification.add(comboClassification);
    this.comboMethod = new JComboBox();
    this.comboMethod.addItem("Valeurs");
    this.comboMethod.addItem("Quantiles");
    this.comboMethod.addItem("Intervalles");
    this.comboMethod.addItem("Log-Quantiles");
    this.comboMethod.addItem("Log-Intervalles");
    this.comboMethod.addItem("Longueur-Quantiles");
    this.comboMethod.setSelectedIndex(0);
    this.comboClassification.addActionListener(MainFrameControler
        .chooseIndicatorForClassification(this, comboClassification,
            comboMethod));
    this.comboMethod.addActionListener(MainFrameControler
        .chooseTypeForClassification(this, comboClassification, comboMethod));
    panelClassification.add(comboClassification);
    this.comboMethod.setEnabled(false);
    panelClassification.add(comboMethod);
    SpinnerModel spinModel = new SpinnerNumberModel(5, 1, 50, 1);
    this.spinNbClasses = new JSpinner(spinModel);
    this.spinNbClasses.setEnabled(false);
    this.spinNbClasses.addChangeListener(MainFrameControler
        .chooseNbClassesForClassification(this, this.spinNbClasses,
            this.comboClassification));
    JPanel panelSpinNbClasses = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panelSpinNbClasses.add(new JLabel("Classes : "));
    panelSpinNbClasses.add(spinNbClasses);
    panelClassification.add(panelSpinNbClasses);
    this.boxAllIndicators = new JCheckBox("Tous les indicateurs");
    this.boxAllIndicators.setEnabled(false);
    panelClassification.add(boxAllIndicators);
    this.boxAllDates = new JCheckBox("Toutes les dates");
    this.boxAllDates.setEnabled(false);
    panelClassification.add(boxAllDates);

    this.panelDrawing.add(panelClassification);

    // zoomSt
    JPanel panelZoomSt = new JPanel(new VerticalLayout());
    panelZoomSt.setBorder(BorderFactory.createTitledBorder(zoomStPanelText));

    // direction du zoom
    this.zoomStFuture = new JRadioButton(zoomStFutureText);
    this.zoomStFuture.setEnabled(false);
    this.zoomStFuture.addActionListener(MainFrameControler
        .directionForwardZoomStActivated(this, true));
    this.zoomStFuture.setSelected(true);
    this.zoomStPast = new JRadioButton(zoomStPastText);
    this.zoomStPast.addActionListener(MainFrameControler
        .directionForwardZoomStActivated(this, false));
    this.zoomStPast.setEnabled(false);
    ButtonGroup groupZoomSt = new ButtonGroup();
    groupZoomSt.add(this.zoomStFuture);
    groupZoomSt.add(this.zoomStPast);
    JPanel panelZoomStDirection = new JPanel();
    panelZoomStDirection.add(this.zoomStFuture);
    panelZoomStDirection.add(this.zoomStPast);
    panelZoomSt.add(panelZoomStDirection);

    // profondeur du zoom
    this.sliderDepthZoomSt = new JSlider(JSlider.HORIZONTAL);
    this.sliderDepthZoomSt.setEnabled(false);
    this.sliderDepthZoomSt.addChangeListener(MainFrameControler
        .sliderZoomStActivated(this));
    panelZoomSt.add(new JLabel(zoomDepthText));
    panelZoomSt.add(this.sliderDepthZoomSt);

    this.panelDrawing.add(panelZoomSt);

    this.panelWest1.add(panelDrawing);

    // couches supplémentaires
    this.panelAdditonalLayers = new JXTaskPane();
    this.panelAdditonalLayers.setCollapsed(true);
    this.panelAdditonalLayers.setTitle(data);
    this.panelWest1.add(panelAdditonalLayers);

    // panel du bas
    this.panelWest2 = new JXTaskPaneContainer();
    this.panelWest2.setBorder(BorderFactory.createTitledBorder(analysis));

    // *****************************************************
    // indicateurs structuraux globaux
    this.panelGlobalMorphoIndicators = new JXTaskPane();
    this.panelGlobalMorphoIndicators.setCollapsed(true);
    this.panelGlobalMorphoIndicators
        .setTitle(globalMorphologicalIndicatorsText);
    this.panelWest2.add(panelGlobalMorphoIndicators);

    this.comboBoxGlobalMorphoIndicators = new JComboBox();
    this.comboBoxGlobalMorphoIndicators.setEnabled(false);
    this.comboBoxGlobalMorphoIndicators
        .setPreferredSize(new Dimension(160, 25));
    this.comboBoxGlobalMorphoIndicators.addItem(" --- ");

    this.butCalculateGlobalMorphoIndicators = new JButton(new ImageIcon(
        /*MainFrame.class.getResource("/images/go.png")*/));
    this.butCalculateGlobalMorphoIndicators.setPreferredSize(new Dimension(25,
        25));
    this.butCalculateGlobalMorphoIndicators.setEnabled(false);
    this.butCalculateGlobalMorphoIndicators
        .addActionListener(MainFrameControler.calculateIndicator(this, 1,
            this.comboBoxGlobalMorphoIndicators));

    DefaultListModel modelGlobal = new DefaultListModel();
    this.listOkGlobalMorpoIndicators = new JList(modelGlobal);
    this.listOkGlobalMorpoIndicators
        .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    this.listOkGlobalMorpoIndicators.setLayoutOrientation(JList.VERTICAL);
    this.listOkGlobalMorpoIndicators.addMouseListener(MainFrameControler
        .viewIndicatorDistribution(this, 1));
    JScrollPane scollPaneListGlobal = new JScrollPane(
        listOkGlobalMorpoIndicators);
    scollPaneListGlobal.setPreferredSize(new Dimension(220, 150));

    JPanel panelMorpho1 = new JPanel(new FlowLayout());
    panelMorpho1.add(comboBoxGlobalMorphoIndicators);
    panelMorpho1.add(butCalculateGlobalMorphoIndicators);
    this.panelGlobalMorphoIndicators.add(panelMorpho1);
    JLabel labelGLobalMorpho = new JLabel(calculatedIndicators);
    labelGLobalMorpho.setPreferredSize(new Dimension(220, 25));
    labelGLobalMorpho.setFont(new Font("Arial", Font.ITALIC, 11));
    this.panelGlobalMorphoIndicators.add(labelGLobalMorpho);
    this.panelGlobalMorphoIndicators.add(scollPaneListGlobal);

    // ****************************************************
    // indicateurs structuraux locaux

    this.panelLocalMorphoIndicators = new JXTaskPane();
    this.panelLocalMorphoIndicators.setCollapsed(true);
    this.panelLocalMorphoIndicators.setTitle(localMorphologicalIndicatorsText);

    // ****************************************************
    // indicateur locaux d'ensemble
    this.panelLocalGlobalMorphoIndicators = new JPanel(new VerticalLayout());
    JLabel labelLocalGlobal = new JLabel(localGlobalMorphologicalIndicatorsText);
    labelLocalGlobal.setFont(new Font("Arial", Font.ITALIC, 11));
    this.panelLocalGlobalMorphoIndicators.add(labelLocalGlobal);

    this.comboBoxLocaGloballMorphoIndicators = new JComboBox();
    this.comboBoxLocaGloballMorphoIndicators.setEnabled(false);
    this.comboBoxLocaGloballMorphoIndicators.setPreferredSize(new Dimension(
        160, 25));
    this.comboBoxLocaGloballMorphoIndicators.addItem(" --- ");

    this.butCalculateLocalGlobalMorphoIndicators = new JButton(new ImageIcon(
       /* MainFrame.class.getResource("/images/go.png")*/));
    this.butCalculateLocalGlobalMorphoIndicators
        .setPreferredSize(new Dimension(25, 25));
    this.butCalculateLocalGlobalMorphoIndicators.setEnabled(false);
    this.butCalculateLocalGlobalMorphoIndicators
        .addActionListener(MainFrameControler.calculateIndicator(this, 2,
            this.comboBoxLocaGloballMorphoIndicators));

    DefaultListModel modelGlobal2 = new DefaultListModel();
    this.listOkLocalGlobalMorpoIndicators = new JList(modelGlobal2);
    this.listOkLocalGlobalMorpoIndicators
        .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    this.listOkLocalGlobalMorpoIndicators.setLayoutOrientation(JList.VERTICAL);
    JScrollPane scollPaneListGlobal2 = new JScrollPane(
        listOkLocalGlobalMorpoIndicators);
    this.listOkLocalGlobalMorpoIndicators.addMouseListener(MainFrameControler
        .viewIndicatorDistribution(this, 2));
    scollPaneListGlobal2.setPreferredSize(new Dimension(220, 150));

    JPanel panelMorpho2 = new JPanel(new FlowLayout());
    panelMorpho2.add(comboBoxLocaGloballMorphoIndicators);
    panelMorpho2.add(butCalculateLocalGlobalMorphoIndicators);
    this.panelLocalGlobalMorphoIndicators.add(panelMorpho2);
    JLabel labelLocalGlobalMorpho = new JLabel(calculatedIndicators);
    labelLocalGlobalMorpho.setPreferredSize(new Dimension(220, 25));
    labelLocalGlobalMorpho.setFont(new Font("Arial", Font.ITALIC, 11));
    this.panelLocalGlobalMorphoIndicators.add(labelLocalGlobalMorpho);
    this.panelLocalGlobalMorphoIndicators.add(scollPaneListGlobal2);

    this.panelLocalMorphoIndicators.add(panelLocalGlobalMorphoIndicators);
    // ****************************************************
    // ****************************************************
    // indicateur locaux de voisinage
    this.panelLocalLocalMorphoIndicators = new JPanel(new VerticalLayout());
    JLabel labelLocalLocal = new JLabel(localLocalMorphologicalIndicatorsText);
    labelLocalLocal.setFont(new Font("Arial", Font.ITALIC, 11));
    this.panelLocalLocalMorphoIndicators.add(labelLocalLocal);

    this.comboBoxLocaLocalMorphoIndicators = new JComboBox();
    this.comboBoxLocaLocalMorphoIndicators.setEnabled(false);
    this.comboBoxLocaLocalMorphoIndicators.setPreferredSize(new Dimension(160,
        25));
    this.comboBoxLocaLocalMorphoIndicators.addItem(" --- ");

    this.butCalculateLocalLocalMorphoIndicators = new JButton(new ImageIcon(
       /* MainFrame.class.getResource("/images/go.png")*/));
    this.butCalculateLocalLocalMorphoIndicators.setPreferredSize(new Dimension(
        25, 25));
    this.butCalculateLocalLocalMorphoIndicators.setEnabled(false);
    this.butCalculateLocalLocalMorphoIndicators
        .addActionListener(MainFrameControler.calculateIndicator(this, 2,
            this.comboBoxLocaLocalMorphoIndicators));

    DefaultListModel modelGlobal3 = new DefaultListModel();
    this.listOkLocalLocalMorpoIndicators = new JList(modelGlobal3);
    this.listOkLocalLocalMorpoIndicators
        .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    this.listOkLocalLocalMorpoIndicators.setLayoutOrientation(JList.VERTICAL);
    JScrollPane scollPaneListGlobal3 = new JScrollPane(
        listOkLocalLocalMorpoIndicators);
    this.listOkLocalLocalMorpoIndicators.addMouseListener(MainFrameControler
        .viewIndicatorDistribution(this, 2));
    scollPaneListGlobal3.setPreferredSize(new Dimension(220, 150));

    JPanel panelMorpho3 = new JPanel(new FlowLayout());
    panelMorpho3.add(comboBoxLocaLocalMorphoIndicators);
    panelMorpho3.add(butCalculateLocalLocalMorphoIndicators);
    this.panelLocalLocalMorphoIndicators.add(panelMorpho3);
    JLabel labelLocalLocalMorpho = new JLabel(calculatedIndicators);
    labelLocalLocalMorpho.setPreferredSize(new Dimension(220, 25));
    labelLocalLocalMorpho.setFont(new Font("Arial", Font.ITALIC, 11));
    this.panelLocalLocalMorphoIndicators.add(labelLocalLocalMorpho);
    this.panelLocalLocalMorphoIndicators.add(scollPaneListGlobal3);

    this.panelLocalMorphoIndicators.add(Box.createVerticalStrut(25));
    this.panelLocalMorphoIndicators.add(new JSeparator());
    this.panelLocalMorphoIndicators.add(Box.createVerticalStrut(15));
    this.panelLocalMorphoIndicators.add(panelLocalLocalMorphoIndicators);
    // ****************************************************
    this.panelWest2.add(panelLocalMorphoIndicators);

    // indicateurs morphogénétiques
    this.paneldynamics = new JXTaskPane();
    this.paneldynamics.setCollapsed(true);
    this.paneldynamics.setTitle(morphogeneticalIndicators);
    // this.panelWest2.add(paneldynamics);

    westPanel.add(new JScrollPane(panelWest1));
    westPanel.add(new JScrollPane(panelWest2));

  }

  /**
   * Panel Sud
   */
  private void initSouthPanel() {
    this.southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    this.southPanel.setPreferredSize(new Dimension(1200, 25));
    this.labelImgProcessing = new JLabel();
    this.labelImgProcessing.setPreferredSize(new Dimension(30, 25));
    this.labelInfoProcessing = new JLabel();
    this.labelInfoProcessing.setPreferredSize(new Dimension(775, 25));
    this.labelScale = new JLabel(scaleText);
    this.labelScale.setPreferredSize(new Dimension(200, 25));
    this.labelCoordinates = new JLabel(coordinatesText);
    this.labelCoordinates.setPreferredSize(new Dimension(250, 25));

    labelInfoProcessing.setFont(new Font("Arial", Font.ITALIC, 11));
    labelScale.setFont(new Font("Arial", Font.ITALIC, 11));
    labelCoordinates.setFont(new Font("Arial", Font.ITALIC, 11));

    this.southPanel.add(labelImgProcessing);
    this.southPanel.add(labelInfoProcessing);
    this.southPanel.add(labelScale);
    this.southPanel.add(labelCoordinates);
  }

  public void updateProcessingInfo(String info, boolean showIcon) {

    if (showIcon) {
      this.labelImgProcessing.setIcon(new ImageIcon(/*MainFrame.class
          .getResource("/images/busy.gif")*/));
    } else {
      this.labelImgProcessing.setIcon(null);
    }
    this.labelInfoProcessing.setText(info);
    this.southPanel.revalidate();
  }

  public void updateScale(int scale) {
    this.labelScale.setText(scaleText + "1 / " + Integer.toString(scale));
    this.southPanel.revalidate();
  }

  public void updateCoordinates(double x, double y) {
    this.labelCoordinates.setText(coordinatesText + "("
        + Integer.toString((int) x) + ", " + Integer.toString((int) y) + ")");
    this.southPanel.revalidate();
  }

  // ****************************** Autres méthodes ***************************

  public int getCurrentPanelId() {
    return this.centerPanel.getSelectedIndex();
  }

  public PanelLayer getCurrentPanel() {
    return this.panelLayers.get(this.centerPanel.getSelectedIndex());
  }

  public void enableCloseMenu(boolean enable) {
    this.closeMenu.setEnabled(enable);
    this.closeAllMenu.setEnabled(enable);
  }

  public void enableWestPanel(boolean enable) {
    this.radioButonEdgesAndNodes.setEnabled(enable);
    this.radioButtonEdgesOnly.setEnabled(enable);
    this.radioButtonNodesOnly.setEnabled(enable);
    this.zoomStFuture.setEnabled(enable);
    this.zoomStPast.setEnabled(enable);
    this.sliderDepthZoomSt.setEnabled(enable);
    this.comboBoxGlobalMorphoIndicators.setEnabled(true);
    this.comboBoxLocaGloballMorphoIndicators.setEnabled(true);
    this.comboBoxLocaLocalMorphoIndicators.setEnabled(true);
    this.butCalculateGlobalMorphoIndicators.setEnabled(true);
    this.butCalculateLocalGlobalMorphoIndicators.setEnabled(true);
    this.butCalculateLocalLocalMorphoIndicators.setEnabled(true);
  }

  public void enableClassificationPanel(boolean enable) {
    if ((enable && this.comboClassification.isEnabled())
        || (!enable && !this.comboClassification.isEnabled())) {
      return;
    }
    this.comboClassification.setEnabled(enable);
  }

  public void enableClassificationPanelComplete(boolean enable) {
    this.comboClassification.setEnabled(enable);
    this.comboMethod.setEnabled(enable);
    this.spinNbClasses.setEnabled(enable);
    this.boxAllIndicators.setEnabled(enable);
    this.boxAllDates.setEnabled(enable);
  }

  public void enableSpinnerNbClasses(boolean enable) {
    this.spinNbClasses.setEnabled(enable);
  }

  /**
   * Met à jour les radio bouton pour les couches de graph (arc, noeud, etc.)
   * @param drawableEntities
   */
  public void updateDrawablesEntities(String drawableEntities) {
    if (drawableEntities.equals(Parameters.EDGES_ONLY)) {
      this.radioButtonEdgesOnly.setSelected(true);
    } else if (drawableEntities.equals(Parameters.NODES_ONLY)) {
      this.radioButtonNodesOnly.setSelected(true);
    } else {
      this.radioButonEdgesAndNodes.setSelected(true);
    }
  }

  /**
   * mise à jour du panel pour lz oom St
   */
  public void updateZoomStPanel(int zoomDepth, int zoomMax) {
    this.sliderDepthZoomSt.setMinimum(0);
    this.sliderDepthZoomSt.setMaximum(zoomMax);
    this.sliderDepthZoomSt.setValue(zoomDepth);
    this.sliderDepthZoomSt.setMajorTickSpacing(1);
    this.sliderDepthZoomSt.setMinorTickSpacing(1);
    this.sliderDepthZoomSt.setPaintTicks(true);
    this.sliderDepthZoomSt.setSnapToTicks(true);
    this.sliderDepthZoomSt.setPaintLabels(true);
  }

  public void updateClassificationPanel() {
    PanelLayer panelLayer = this.getCurrentPanel();
    GraphLayer graphLayer = panelLayer.getGraphLayer().get(
        panelLayer.getIdCurrentGraphLayer());
    String drawableEntities = panelLayer.getDrawableEntities();
    List<String> centralities = new ArrayList<String>();
    if (drawableEntities.equals(Parameters.NODES_ONLY)
        || drawableEntities.equals(Parameters.NODES_EDGES)) {
      centralities.addAll(graphLayer.getLegendNodesIndicators().keySet());
    } else {
      centralities.addAll(graphLayer.getLegendEdgesIndicators().keySet());
    }
    this.comboClassification.removeAllItems();
    this.comboClassification.addItem(classificationText2);
    for (String c : centralities) {
      this.comboClassification.addItem(c);
    }
    this.enableClassificationPanelComplete(false);
    if (this.comboClassification.getItemCount() == 1) {
      this.enableClassificationPanel(false);
    } else {
      this.enableClassificationPanel(true);
    }

  }

  public void updateClassificationPanel(String newCentrality) {
    for (int i = 0; i < this.comboClassification.getItemCount(); i++) {
      if (((String) this.comboClassification.getItemAt(i))
          .equals(newCentrality)) {
        return;
      }
    }
    PanelLayer panelLayer = this.getCurrentPanel();
    GraphLayer graphLayer = panelLayer.getGraphLayer().get(
        panelLayer.getIdCurrentGraphLayer());
    String drawableEntities = panelLayer.getDrawableEntities();
    if (drawableEntities.equals(Parameters.NODES_ONLY)
        || drawableEntities.equals(Parameters.NODES_EDGES)) {
      if (graphLayer.getLegendNodesIndicators().containsKey(newCentrality)) {
        this.comboClassification.addItem(newCentrality);
      }

    } else {
      if (graphLayer.getLegendEdgesIndicators().containsKey(newCentrality)) {
        this.comboClassification.addItem(newCentrality);
      }
    }

  }

  public void updateSpinnerClasses(int value) {
    this.spinNbClasses.setValue(value);
  }

  /**
   * Mise à jour des indicateurs calculés / à calculer
   */
  public void updateMorphologicalMesures() {

    int currentPanel = this.getCurrentPanelId();
    PanelLayer panelLayer = this.panelLayers.get(currentPanel);
    GraphLayer graphLayer = panelLayer.getGraphLayer().get(
        panelLayer.getIdCurrentGraphLayer());

    List<String> globalMorphoIndicators = graphLayer
        .getGlobalMorphologicalIndicators();
    List<String> localGlobalMorphoIndicators = new ArrayList<String>();
    List<String> localLocalMorphoIndicators = new ArrayList<String>();

    // **********************************************************
    // les indicateur globaux
    File file = new File(ParamFrame.class.getResource(
        "/config/global_morphological_indicators").getFile());
    try {
      FileReader fr = new FileReader(file);
      BufferedReader br = new BufferedReader(fr);
      String line = "";
      this.comboBoxGlobalMorphoIndicators.removeAllItems();
      this.comboBoxGlobalMorphoIndicators.addItem(" --- ");
      while ((line = br.readLine()) != null) {
        if (globalMorphoIndicators.contains(line)) {
          continue;
        }
        this.comboBoxGlobalMorphoIndicators.addItem(line);
      }
      br.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (this.comboBoxGlobalMorphoIndicators.getItemCount() == 0) {
      this.comboBoxGlobalMorphoIndicators.setEnabled(false);
      this.butCalculateGlobalMorphoIndicators.setEnabled(false);
    } else {
      this.comboBoxGlobalMorphoIndicators.setEnabled(true);
      this.butCalculateGlobalMorphoIndicators.setEnabled(true);
    }
    // mise àjour des listes
    int cpt = 0;
    ((DefaultListModel) this.listOkGlobalMorpoIndicators.getModel())
        .removeAllElements();
    for (String indicator : globalMorphoIndicators) {
      ((DefaultListModel) this.listOkGlobalMorpoIndicators.getModel()).add(cpt,
          indicator);
      cpt++;
    }

    // **********************************************************
    // les indicateur locaux d'ensemble
    file = new File(ParamFrame.class.getResource(
        "/config/local_global_morphological_indicators").getFile());
    try {
      FileReader fr = new FileReader(file);
      BufferedReader br = new BufferedReader(fr);
      String line = "";
      this.comboBoxLocaGloballMorphoIndicators.removeAllItems();
      this.comboBoxLocaGloballMorphoIndicators.addItem(" --- ");
      while ((line = br.readLine()) != null) {
        if (graphLayer.getLocalMorphologicalIndicators().contains(line)) {
          localGlobalMorphoIndicators.add(line);
          continue;
        }
        this.comboBoxLocaGloballMorphoIndicators.addItem(line);
      }
      br.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    // mise àjour des listes
    ((DefaultListModel) this.listOkLocalGlobalMorpoIndicators.getModel())
        .removeAllElements();
    cpt = 0;
    if (this.comboBoxLocaGloballMorphoIndicators.getItemCount() == 0) {
      this.comboBoxLocaGloballMorphoIndicators.setEnabled(false);
      this.butCalculateLocalGlobalMorphoIndicators.setEnabled(false);
    } else {
      this.comboBoxLocaGloballMorphoIndicators.setEnabled(true);
      this.butCalculateLocalGlobalMorphoIndicators.setEnabled(true);
    }
    for (String indicator : localGlobalMorphoIndicators) {
      ((DefaultListModel) this.listOkLocalGlobalMorpoIndicators.getModel())
          .add(cpt, indicator);
      cpt++;
    }
    // **********************************************************
    // les indicateur locaux de voisinage
    file = new File(ParamFrame.class.getResource(
        "/config/local_local_morphological_indicators").getFile());
    try {
      FileReader fr = new FileReader(file);
      BufferedReader br = new BufferedReader(fr);
      String line = "";
      this.comboBoxLocaLocalMorphoIndicators.removeAllItems();
      this.comboBoxLocaLocalMorphoIndicators.addItem(" --- ");
      while ((line = br.readLine()) != null) {
        if (graphLayer.getLocalMorphologicalIndicators().contains(line)) {
          localLocalMorphoIndicators.add(line);
          continue;
        }
        this.comboBoxLocaLocalMorphoIndicators.addItem(line);
      }
      br.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    // mise àjour des listes
    cpt = 0;
    if (this.comboBoxLocaLocalMorphoIndicators.getItemCount() == 0) {
      this.comboBoxLocaLocalMorphoIndicators.setEnabled(false);
      this.butCalculateLocalLocalMorphoIndicators.setEnabled(false);
    } else {
      this.comboBoxLocaLocalMorphoIndicators.setEnabled(true);
      this.butCalculateLocalLocalMorphoIndicators.setEnabled(true);
    }
    ((DefaultListModel) this.listOkLocalLocalMorpoIndicators.getModel())
        .removeAllElements();
    for (String indicator : localLocalMorphoIndicators) {
      ((DefaultListModel) this.listOkLocalLocalMorpoIndicators.getModel()).add(
          cpt, indicator);
      cpt++;
    }

  }

  // mise à jour des infos du panneau west
  public void updateWestPanel() {
    int currentPanel = this.getCurrentPanelId();
    PanelLayer panelLayer = this.panelLayers.get(currentPanel);

    // couche de graphe
    String drawableEntities = panelLayer.getDrawableEntities();
    this.updateDrawablesEntities(drawableEntities);

    // zoom St
    updateZoomStPanel(panelLayer.getZoomDepth(), panelLayer.getZoomDepthMax());

    // indicateurs
    updateMorphologicalMesures();
    // classification
    this.updateClassificationPanel();
  }

  // ****************************** Action Listeners ***************************

  public void isEnable(boolean eNABLE) {
    ENABLE = eNABLE;
    this.setEnabled(false);
  }

  public boolean isEnable() {
    return ENABLE;
  }

  public class newGraphProjectActionListener implements ActionListener {

    private MainFrame frame;
    private String mode;
    private String format;

    public newGraphProjectActionListener(MainFrame frame, String format,
        String mode) {
      this.frame = frame;
      this.format = format;
      this.mode = mode;
    }

    public void actionPerformed(ActionEvent e) {
      JFileChooser jfc = new JFileChooser();
      jfc = new JFileChooser();
      jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      jfc.setMultiSelectionEnabled(false);
      jfc.setDialogTitle(repertoryChoiceText);
      jfc.setCurrentDirectory(new File(
          "/media/Data/Benoit/these/donnees/vecteur/filaires/FILAIRES_L93_OK/"));

      int result = jfc.showOpenDialog(null);
      if (result == JFileChooser.APPROVE_OPTION) {
        String directory = jfc.getSelectedFile().getAbsolutePath();
        this.frame.isEnable(false);
        NewProjectFrame newFrame = new NewProjectFrame(this.frame, directory,
            format, mode);
        this.frame.setEnabled(false);
        newFrame.setVisible(true);
      } else {
        return;
      }
    }
  }

  public class closeActionListener implements ActionListener {

    private MainFrame frame;

    public closeActionListener(MainFrame frame) {
      this.frame = frame;
    }

    public void actionPerformed(ActionEvent e) {
      int currentPanel = this.frame.getCurrentPanelId();
      // on ferme le projet courrant
      int dialogButton = JOptionPane.YES_NO_OPTION;
      JOptionPane.showConfirmDialog(null, closeMenuWarning, warning,
          dialogButton);
      if (dialogButton == JOptionPane.NO_OPTION) {
        return;
      }
      // c'est ok
      PanelLayer panelLayer = this.frame.panelLayers.get(currentPanel);
      panelLayer.delete();
      this.frame.centerPanel.remove(currentPanel);
      this.frame.panelLayers.remove(panelLayer);
      panelLayer = null;
      if (!this.frame.panelLayers.isEmpty()) {
        this.frame.centerPanel.setSelectedIndex(0);
        this.frame.updateWestPanel();
      } else {
        this.frame.enableCloseMenu(false);
        this.frame.enableWestPanel(false);
      }
      this.frame.centerPanel.revalidate();
      this.frame.centerPanel.repaint();
      System.gc();
    }
  }

}
