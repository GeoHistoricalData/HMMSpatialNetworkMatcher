package fr.ign.cogit.morphogenesis.exploring_tool.controler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import fr.ign.cogit.morphogenesis.exploring_tool.model.api.AbstractGraph;
import fr.ign.cogit.morphogenesis.exploring_tool.model.api.GraphReader;
import fr.ign.cogit.morphogenesis.exploring_tool.model.layers.GraphLayer;
import fr.ign.cogit.morphogenesis.exploring_tool.utils.Parameters;
import fr.ign.cogit.morphogenesis.exploring_tool.view.MainFrame;
import fr.ign.cogit.morphogenesis.exploring_tool.view.PanelLayer;

public class NewGraphPanelControler implements Runnable {

  private Map<String, List<File>> datesAndFiles;
  private String mode;
  private JFrame frame; // la frame qui a lancé la création d'un nouvel onglet
  private MainFrame mainFrame;

  String fileLoadingGeom = "Création du graphe géométrique: "; //$NON-NLS-1$

  public NewGraphPanelControler(JFrame frame, MainFrame mainFrame, String mode) {
    this.mode = mode;
    this.mainFrame = mainFrame;
    this.frame = frame;
    this.setFiles(new HashMap<String, List<File>>());
  }

  public void setFiles(Map<String, List<File>> datesAndFiles) {
    this.datesAndFiles = datesAndFiles;
  }

  public Map<String, List<File>> getFiles() {
    return datesAndFiles;
  }

  public void run() {
    List<GraphLayer> graphLayers = new ArrayList<GraphLayer>();
    this.frame.setVisible(false);
    this.frame.dispose();
    // Tri par date
    List<String> dates = new ArrayList<String>(datesAndFiles.keySet());

    Collections.sort(dates, new Comparator<String>() {
      public int compare(String o1, String o2) {
        // on sait qu'on a des dates
        Integer date1 = Integer.parseInt(o1);
        Integer date2 = Integer.parseInt(o2);

        if (date1.intValue() > date2.intValue()) {
          return 1;
        } else if (date1.intValue() < date2.intValue()) {
          return -1;
        } else {
          return 0;
        }

      }
    });
    for (String date : dates) {
      for (File f : datesAndFiles.get(date)) {
        if (this.mode.equals(Parameters.GEOMETRICAL_MODE)) {
          this.mainFrame.updateProcessingInfo(fileLoadingGeom + f.getName(),
              true);
          AbstractGraph<?, ?> newGraph = GraphReader.createGeometricalGraph(
              f.toString(), 0.);
          newGraph.setName(f.getName());
          newGraph.setDate(date);
          // nouvelle couche
          GraphLayer graphLayer = new GraphLayer(newGraph);
          graphLayers.add(graphLayer);
        }
      }
    }
    PanelLayer panelLayer = new PanelLayer(this.mainFrame, graphLayers,
        this.mode);
    panelLayer.setDrawableEntities(Parameters.EDGES_ONLY);
    panelLayer.setZoomDirection(Parameters.ZOOM_FORWARD);
    this.mainFrame.updateProcessingInfo("", false);
    this.mainFrame.addPanelLayer(panelLayer);
    this.mainFrame.setEnabled(true);
    this.mainFrame.enableCloseMenu(true);
    this.mainFrame.enableWestPanel(true);
    this.mainFrame.updateWestPanel();
  }
}
