package fr.ign.cogit.morphogenesis.exploring_tool.view.legend;

import java.awt.Color;

public class NodesLegend extends Legend {

  private int nodes_size;
  private boolean propotional;

  /**
   * Default Constructor
   */
  public NodesLegend() {
    this.color = new Color(0.f, 0.f, 0.f, 0.5f);
    this.type = LegendType.VALUES;
    this.nodes_size = 5;
    this.setPropotional(false);
  }

  public void setNodes_size(int nodes_size) {
    this.nodes_size = nodes_size;
  }

  public int getNodes_size() {
    return nodes_size;
  }

  public void setPropotional(boolean propotional) {
    this.propotional = propotional;
  }

  public boolean isPropotional() {
    return propotional;
  }

}
