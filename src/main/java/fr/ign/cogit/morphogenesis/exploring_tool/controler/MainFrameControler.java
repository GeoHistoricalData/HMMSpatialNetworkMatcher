package fr.ign.cogit.morphogenesis.exploring_tool.controler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.ign.cogit.morphogenesis.exploring_tool.model.layers.GraphLayer;
import fr.ign.cogit.morphogenesis.exploring_tool.utils.Parameters;
import fr.ign.cogit.morphogenesis.exploring_tool.view.MainFrame;
import fr.ign.cogit.morphogenesis.exploring_tool.view.PanelLayer;
import fr.ign.cogit.morphogenesis.exploring_tool.view.ViewGlobalMorphologicalIndicator;
import fr.ign.cogit.morphogenesis.exploring_tool.view.ViewLocalMorphologicalIndicator;
import fr.ign.cogit.morphogenesis.exploring_tool.view.legend.LegendType;

public class MainFrameControler {

  private static MainFrameControler controler = new MainFrameControler();

  // *************** Radio bouton couches de graphe (arc, noeuds, etc.)

  public static ActionListener getRadioButonGraphLayerActivated(String mode,
      MainFrame f) {
    GraphLayerAction r = controler.new GraphLayerAction(mode, f);
    return r;
  }

  public class GraphLayerAction implements ActionListener {
    private String mode;
    private MainFrame frame;

    public GraphLayerAction(String mode, MainFrame frame) {
      this.mode = mode;
      this.frame = frame;
    }

    public void actionPerformed(ActionEvent e) {
      PanelLayer panelLayer = frame.getCurrentPanel();
      if (panelLayer.getDrawableEntities().equals(this.mode)) {
        return;
      }
      panelLayer.setDrawableEntities(this.mode);
      panelLayer.repaint();
    }
  }

  // *************** sélection nouvel onglet

  public static ChangeListener newTabSelected(MainFrame mainFrame) {
    NewTabSelected r = controler.new NewTabSelected(mainFrame);
    return r;
  }

  public class NewTabSelected implements ChangeListener {
    private MainFrame frame;

    public NewTabSelected(MainFrame frame) {
      this.frame = frame;
    }

    public void stateChanged(ChangeEvent e) {
      if (((JTabbedPane) e.getSource()).getTabCount() != 0) {
        this.frame.updateWestPanel();
        this.frame.updateScale(this.frame.getCurrentPanel()
            .getScaleInCentimeters());
      }
    }
  }

  // *************** slider zoom st
  public static ChangeListener sliderZoomStActivated(MainFrame mainFrame) {
    SliderZoomStActivated r = controler.new SliderZoomStActivated(mainFrame);
    return r;
  }

  public class SliderZoomStActivated implements ChangeListener {
    private MainFrame frame;

    public SliderZoomStActivated(MainFrame frame) {
      this.frame = frame;
    }

    public void stateChanged(ChangeEvent e) {
      JSlider source = (JSlider) e.getSource();

      if (!source.getValueIsAdjusting()) {
        int index = (int) source.getValue();
        if (index == this.frame.getCurrentPanel().getZoomDepth()) {
          return;
        }
        @SuppressWarnings("unchecked")
        Hashtable<Integer, JLabel> tableLabel = (Hashtable<Integer, JLabel>) this.frame
            .getCurrentPanel().getSlider().getLabelTable();
        for (Integer i : tableLabel.keySet()) {
          JLabel label = (JLabel) this.frame.getCurrentPanel().getSlider()
              .getLabelTable().get(i);
          label.setBorder(null);
        }
        this.frame.getCurrentPanel().getSlider().repaint();
        this.frame.getCurrentPanel().setZoomDepth(source.getValue());
        this.frame.getCurrentPanel().repaint();
      }
    }
  }

  // *************** direction zoom st
  public static ActionListener directionForwardZoomStActivated(
      MainFrame mainFrame, boolean forward) {
    DirectionZoomStActivated r = controler.new DirectionZoomStActivated(
        mainFrame, forward);
    return r;
  }

  public class DirectionZoomStActivated implements ActionListener {
    private MainFrame frame;
    private boolean forward;

    public DirectionZoomStActivated(MainFrame frame, boolean forward) {
      this.frame = frame;
      this.forward = forward;
    }

    public void actionPerformed(ActionEvent e) {
      if (forward) {
        // zoom futur
        if (this.frame.getCurrentPanel().getZoomDirection()
            .equals(Parameters.ZOOM_FORWARD)) {
          return;
        }
        @SuppressWarnings("unchecked")
        Hashtable<Integer, JLabel> tableLabel = (Hashtable<Integer, JLabel>) this.frame
            .getCurrentPanel().getSlider().getLabelTable();
        for (Integer i : tableLabel.keySet()) {
          JLabel label = (JLabel) this.frame.getCurrentPanel().getSlider()
              .getLabelTable().get(i);
          label.setBorder(null);
        }
        this.frame.getCurrentPanel().getSlider().repaint();
        this.frame.getCurrentPanel().setZoomDirection(Parameters.ZOOM_FORWARD);
        int depthMax = this.frame.getCurrentPanel().getGraphLayer().size()
            - (frame.getCurrentPanel().getIdCurrentGraphLayer() + 1);
        this.frame.getCurrentPanel().setZoomDepth(depthMax);
        this.frame.getCurrentPanel().setZoomDepthMax(
            this.frame.getCurrentPanel().getZoomDepth());
        this.frame.updateZoomStPanel(depthMax, depthMax);
        this.frame.getCurrentPanel().repaint();
      } else {
        if (this.frame.getCurrentPanel().getZoomDirection()
            .equals(Parameters.ZOOM_BACKWARD)) {
          return;
        }
        @SuppressWarnings("unchecked")
        Hashtable<Integer, JLabel> tableLabel = (Hashtable<Integer, JLabel>) this.frame
            .getCurrentPanel().getSlider().getLabelTable();
        for (Integer i : tableLabel.keySet()) {
          JLabel label = (JLabel) this.frame.getCurrentPanel().getSlider()
              .getLabelTable().get(i);
          label.setBorder(null);
        }
        this.frame.getCurrentPanel().getSlider().repaint();
        this.frame.getCurrentPanel().setZoomDirection(Parameters.ZOOM_BACKWARD);
        int depthMax = this.frame.getCurrentPanel().getIdCurrentGraphLayer();
        this.frame.getCurrentPanel().setZoomDepth(depthMax);
        this.frame.getCurrentPanel().setZoomDepthMax(
            this.frame.getCurrentPanel().getZoomDepth());
        this.frame.updateZoomStPanel(depthMax, depthMax);
        this.frame.getCurrentPanel().repaint();
      }
    }

  }

  // *************** calcul indicateur
  /**
   * Type : 1 mesure globale, 2 mesure locale d'ensemble
   */
  // TODO : pour le moment, l'indicateur est calculé pour l'ensemble des graphes
  // du panelLayer
  public static ActionListener calculateIndicator(MainFrame mainFrame,
      int indicatorType, JComboBox comboBox) {
    CalculateIndicator r = controler.new CalculateIndicator(mainFrame,
        indicatorType, comboBox);
    return r;
  }

  public class CalculateIndicator implements ActionListener {
    private MainFrame frame;
    private int indicatorType;
    private JComboBox comboBox;

    public CalculateIndicator(MainFrame frame, int indicatorType,
        JComboBox comboBox) {
      this.frame = frame;
      this.indicatorType = indicatorType;
      this.comboBox = comboBox;
    }

    public void actionPerformed(ActionEvent e) {
      List<String> indicators = new ArrayList<String>();
      if (comboBox.getSelectedIndex() == 0) {
        return;
      }
      for (Object o : comboBox.getSelectedObjects()) {
        indicators.add((String) o);
      }
      this.frame.setEnabled(false);
      Thread t = new Thread(new CalculateIndicatorControler(frame, indicators,
          indicatorType));
      t.start();
    }
  }

  // *************** frame visu distribtuion indicateur
  /**
   * type : 1 global indicateur, 2 local indicateur
   */
  public static MouseListener viewIndicatorDistribution(MainFrame mainFrame,
      int indicatorType) {
    ViewIndicatorDistribution r = controler.new ViewIndicatorDistribution(
        mainFrame, indicatorType);
    return r;
  }

  public class ViewIndicatorDistribution implements MouseListener {
    private MainFrame frame;
    private int indicatorType;

    public ViewIndicatorDistribution(MainFrame frame, int indicatorType) {
      this.frame = frame;
      this.indicatorType = indicatorType;
    }

    public void mouseClicked(MouseEvent evt) {
      JList list = (JList) evt.getSource();
      if (evt.getClickCount() == 2) {
        int index = list.locationToIndex(evt.getPoint());
        if (index == -1) {
          return;
        }
        String indicator = (String) list.getModel().getElementAt(index);
        switch (indicatorType) {
          case 1:
            ViewGlobalMorphologicalIndicator indF = new ViewGlobalMorphologicalIndicator(
                this.frame, indicator);
            indF.setVisible(true);
            break;
          case 2:
            ViewLocalMorphologicalIndicator indFL = new ViewLocalMorphologicalIndicator(
                this.frame, indicator);
            indFL.setVisible(true);
            break;
          default:
            break;
        }

      }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
  }

  // *************** choix d'un indicateur de classif

  public static ActionListener chooseIndicatorForClassification(
      MainFrame mainFrame, JComboBox comboBoxClassif, JComboBox comboBoxType) {
    ChooseIndicatorForClassification r = controler.new ChooseIndicatorForClassification(
        mainFrame, comboBoxClassif, comboBoxType);
    return r;
  }

  public class ChooseIndicatorForClassification implements ActionListener {
    private MainFrame frame;
    private JComboBox comboBoxClassif;
    private JComboBox comboBoxType;

    public ChooseIndicatorForClassification(MainFrame frame,
        JComboBox comboBoxClassif, JComboBox comboBoxType) {
      this.frame = frame;
      this.comboBoxClassif = comboBoxClassif;
      this.comboBoxType = comboBoxType;
    }

    public void actionPerformed(ActionEvent e) {
      if (this.comboBoxClassif.getItemCount() == 0) {
        return;
      }
      if (comboBoxClassif.getSelectedIndex() == 0) {
        // aucun indicateur
        this.frame.getCurrentPanel().setDrawnCentrality("");
        this.frame.enableClassificationPanelComplete(false);
        this.frame.enableClassificationPanel(true);

      } else {
        String indicator = (String) comboBoxClassif.getSelectedItem();
        String type = this.frame.getCurrentPanel().getGraphLayer()
            .get(this.frame.getCurrentPanel().getIdCurrentGraphLayer())
            .getLegendEdgesIndicators().get(indicator).getLegendType();
        int nbClasses = this.frame.getCurrentPanel().getGraphLayer()
            .get(this.frame.getCurrentPanel().getIdCurrentGraphLayer())
            .getLegendEdgesIndicators().get(indicator).getNB_CLASSES();

        this.comboBoxType.setSelectedItem(type);
        this.frame.getCurrentPanel().setDrawnCentrality(indicator);
        this.frame.updateSpinnerClasses(nbClasses);
        this.frame.enableClassificationPanelComplete(true);
      }
      this.frame.getCurrentPanel().repaint();
    }
  }

  // *************** choix d'un type de classification

  public static ActionListener chooseTypeForClassification(MainFrame mainFrame,
      JComboBox comboBoxClassif, JComboBox comboBoxType) {
    ChooseTypeForClassification r = controler.new ChooseTypeForClassification(
        mainFrame, comboBoxClassif, comboBoxType);
    return r;
  }

  public class ChooseTypeForClassification implements ActionListener {
    private MainFrame frame;
    private JComboBox comboBoxClassif;
    private JComboBox comboBoxType;

    public ChooseTypeForClassification(MainFrame frame,
        JComboBox comboBoxClassif, JComboBox comboBoxType) {
      this.frame = frame;
      this.comboBoxClassif = comboBoxClassif;
      this.comboBoxType = comboBoxType;
    }

    public void actionPerformed(ActionEvent e) {
      String type = (String) comboBoxType.getSelectedItem();
      GraphLayer graphLayer = this.frame.getCurrentPanel().getGraphLayer()
          .get(this.frame.getCurrentPanel().getIdCurrentGraphLayer());
      String centrality = (String) comboBoxClassif.getSelectedItem();

      if (graphLayer.getLegendEdgesIndicators().get(centrality).getLegendType()
          .equals(type)) {
        return;
      } else {
        if (type.equals(LegendType.VALUES)) {
          this.frame.enableSpinnerNbClasses(false);
        } else {
          this.frame.enableSpinnerNbClasses(true);
        }
        graphLayer.getLegendEdgesIndicators().get(centrality)
            .setLegendType(type);
        if (graphLayer.getLegendNodesIndicators().containsKey(centrality)) {
          graphLayer.getLegendEdgesIndicators().get(centrality)
              .setLegendType(type);
        }
      }

      this.frame.getCurrentPanel().repaint();
    }
  }

  // *************** choix du nombre de classes pour la classif

  public static ChangeListener chooseNbClassesForClassification(
      MainFrame mainFrame, JSpinner spinner, JComboBox comboBoxClassif) {
    ChooseNbClassesForClassification r = controler.new ChooseNbClassesForClassification(
        mainFrame, spinner, comboBoxClassif);
    return r;
  }

  public class ChooseNbClassesForClassification implements ChangeListener {
    private MainFrame frame;
    private JSpinner spinner;
    private JComboBox comboBoxClassif;

    public ChooseNbClassesForClassification(MainFrame frame, JSpinner spinner,
        JComboBox comboBoxClassif) {
      this.frame = frame;
      this.spinner = spinner;
      this.comboBoxClassif = comboBoxClassif;
    }

    public void stateChanged(ChangeEvent e) {
      int value = Integer.parseInt(spinner.getValue().toString());
      String centrality = (String) comboBoxClassif.getSelectedItem();
      this.frame.getCurrentPanel().getGraphLayer()
          .get(this.frame.getCurrentPanel().getIdCurrentGraphLayer())
          .getLegendEdgesIndicators().get(centrality).setNB_CLASSES(value);
      if (this.frame.getCurrentPanel().getGraphLayer()
          .get(this.frame.getCurrentPanel().getIdCurrentGraphLayer())
          .getLegendNodesIndicators().containsKey(centrality)) {
        this.frame.getCurrentPanel().getGraphLayer()
            .get(this.frame.getCurrentPanel().getIdCurrentGraphLayer())
            .getLegendNodesIndicators().get(centrality).setNB_CLASSES(value);
      }
      this.frame.getCurrentPanel().repaint();
    }
  }
}
