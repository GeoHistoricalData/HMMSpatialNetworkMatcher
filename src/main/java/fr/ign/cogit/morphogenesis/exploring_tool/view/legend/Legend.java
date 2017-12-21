package fr.ign.cogit.morphogenesis.exploring_tool.view.legend;

import java.awt.Color;

public class Legend {

  protected Color color = new Color(0.f, 0.f, 0.f, 1f);
  protected String type = LegendType.VALUES;
  protected int NB_CLASSES = 5;

  public Color getColor() {
    return this.color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public String getLegendType() {
    return this.type;
  }

  public void setLegendType(String type) {
    this.type = type;
  }

  public int getNB_CLASSES() {
    return this.NB_CLASSES;
  }

  public void setNB_CLASSES(int NB_CLASSES) {
    this.NB_CLASSES = NB_CLASSES;
  }

}
