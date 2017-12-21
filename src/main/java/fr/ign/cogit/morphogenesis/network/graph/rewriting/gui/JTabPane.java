package fr.ign.cogit.morphogenesis.network.graph.rewriting.gui;

import java.awt.Color;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;

import fr.ign.cogit.morphogenesis.network.utils.ColorScale;

public class JTabPane {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private JComboBox listeCentralities;
  private Map<String, PanelGUI> centralities;
  private String currentCentrality;
  private Map<Integer, String> mappingListPanel;
  private int id;
  private String idGraph;

  public JTabPane(final MainFrame f,
      Map<String, Map<Shape, Double>> centralities, String idGraph) {

    super();
    this.mappingListPanel = new HashMap<Integer, String>();
    this.centralities = new HashMap<String, PanelGUI>();
    this.idGraph = idGraph;

    int cpt = 0;
    for (String centralityName : centralities.keySet()) {
      Map<Shape, Double> values = centralities.get(centralityName);
      Map<Shape, Color> shapes = new HashMap<Shape, Color>();
      double max = 0;
      double min = Double.MAX_VALUE;
      for (Shape v : values.keySet()) {
        if (max < values.get(v)) {
          max = values.get(v);
        }
        if (min > values.get(v)) {
          min = values.get(v);
        }
      }
      for (Shape edge : values.keySet()) {

        double[] rgb = ColorScale.HSVtoRGB(
            (4 / (min - max)) * (values.get(edge) - max), 0.9, 1);
        Color c = new Color((float) rgb[0], (float) rgb[1], (float) rgb[2]);
        shapes.put(edge, c);
      }
      PanelGUI panelGui = new PanelGUI(f, shapes, values);
      this.centralities.put(centralityName, panelGui);
    }

    // Le tableau de choix
    listeCentralities = new JComboBox();
    for (String centralityName : this.centralities.keySet()) {
      listeCentralities.addItem(centralityName);
      this.mappingListPanel.put(cpt, centralityName);
      cpt++;
    }

    this.currentCentrality = mappingListPanel.get(0);
    this.listeCentralities.setSelectedIndex(0);

    listeCentralities.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JComboBox list = (JComboBox) evt.getSource();
        int index = list.getSelectedIndex();
        String centralityName = mappingListPanel.get(index);
        currentCentrality = centralityName;
        f.updateOnglet();
      }
    });
  }

  public void addPanelGui() {

  }

  public PanelGUI getPanelGui() {
    return this.centralities.get(currentCentrality);
  }

  public JComboBox getPanelList() {
    return listeCentralities;
  }

  public void setIdGraph(String idGraph) {
    this.idGraph = idGraph;
  }

  public String getIdGraph() {
    return idGraph;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public Map<String, PanelGUI> getCentralities() {
    return this.centralities;
  }

}
