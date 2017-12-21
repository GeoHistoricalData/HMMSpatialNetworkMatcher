package fr.ign.cogit.morphogenesis.network.graph;

import java.util.Random;

public class Node {

  /**
   * 
   */

  private double x, y;
  private static final long serialVersionUID = 6707783403977922814L;

  public Node() {
    Random r = new Random();
    this.setX(r.nextDouble());
    this.setY(r.nextDouble());

  }

  public Node(Node n) {
    this.x = n.getX();
    this.y = n.getY();
  }

  public Node(double x, double y) {
    this.x = x;
    this.y = y;
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

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Node other = (Node) obj;
    if (Math.abs(this.x - other.getX()) > 0.0000001) {
      return false;
    }
    if (Math.abs(this.y - other.getY()) > 0.0000001) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Node [x=" + x + ", y=" + y + "]";
  }

}
