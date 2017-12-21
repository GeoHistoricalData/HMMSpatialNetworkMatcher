package fr.ign.cogit.morphogenesis.generation.barthelemy;

public class Center {

  private static final long serialVersionUID = 1L;
  private boolean activated;
  private double x, y;

  public Center(double x, double y) {
    this.setX(x);
    this.setY(y);
    this.activated = true;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getX() {
    return x;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getY() {
    return y;
  }

  public void isActivated(boolean activated) {
    this.activated = activated;
  }

  public boolean isActivated() {
    return this.activated;
  }

}
