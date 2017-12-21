package fr.ign.cogit.morphogenesis.exploring_tool.controler;

import java.util.List;

import fr.ign.cogit.morphogenesis.exploring_tool.model.layers.GraphLayer;
import fr.ign.cogit.morphogenesis.exploring_tool.view.MainFrame;
import fr.ign.cogit.morphogenesis.exploring_tool.view.PanelLayer;

public class CalculateIndicatorControler implements Runnable {

  private MainFrame mainFrame;
  private List<String> indicators;
  private int indicatorType;

  String calculting = "Calcul de "; //$NON-NLS-1$
  String graph = "graphe "; //$NON-NLS-1$

  public CalculateIndicatorControler(MainFrame frame, List<String> indicators,
      int indicatorType) {
    this.mainFrame = frame;
    this.indicators = indicators;
    this.indicatorType = indicatorType;
  }

  public void run() {
    PanelLayer panelLayer = this.mainFrame.getCurrentPanel();
    switch (this.indicatorType) {
      case 1:
        // indicateur global
        for (String indicator : indicators) {
          for (GraphLayer gL : panelLayer.getGraphLayer()) {
            this.mainFrame.updateProcessingInfo(calculting + indicator + ", "
                + graph + gL.getG().getDate(), true);
            gL.updateMorphologicalIndicator(indicator);
          }
        }
        this.mainFrame.updateProcessingInfo("", false);
        this.mainFrame.updateMorphologicalMesures();
        this.mainFrame.setEnabled(true);
        break;
      case 2:
        // indicateur local
        for (String indicator : indicators) {
          for (GraphLayer gL : panelLayer.getGraphLayer()) {
            this.mainFrame.updateProcessingInfo(calculting + indicator + ", "
                + gL.getG().getDate(), true);
            gL.updateLocalMorphologicalIndicator(indicator);
            this.mainFrame.updateClassificationPanel(indicator);
          }
        }
        this.mainFrame.updateProcessingInfo("", false);
        this.mainFrame.updateMorphologicalMesures();
        this.mainFrame.enableClassificationPanel(true);
        this.mainFrame.setEnabled(true);
        break;
      default:
        break;
    }

  }

}
