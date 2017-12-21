package fr.ign.cogit.morphogenesis.exploring_tool.model.api;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point2d;

public class Node extends Point2d {

  private static final long serialVersionUID = 1L;
  private Map<String, Double> centralityValues;

  public Node(Node n) {
    super(n.x, n.y);
    this.centralityValues = new HashMap<String, Double>(n.getCentralityValues());
  }

  public Node(double x, double y) {
    super(x, y);
    this.centralityValues = new HashMap<String, Double>();
  }

  public void setCentralityValues(Map<String, Double> centralityValues) {
    this.centralityValues = centralityValues;
  }

  public Map<String, Double> getCentralityValues() {
    return centralityValues;
  }

  public boolean equals(Node n) {
    return (this.x == n.x && this.y == n.y);
  }

}
