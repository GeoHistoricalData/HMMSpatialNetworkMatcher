package fr.ign.cogit.morphogenesis.exploring_tool.model.api;

public abstract class DrawableEntity {

  protected boolean drawMe;

  public DrawableEntity() {
    this.drawMe = true;
  }

  public boolean drawMe() {
    return this.drawMe;
  }

  public void drawMe(boolean drawMe) {
    this.drawMe = drawMe;
  }

}
