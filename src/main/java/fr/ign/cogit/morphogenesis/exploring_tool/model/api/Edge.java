package fr.ign.cogit.morphogenesis.exploring_tool.model.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;

public class Edge {
  private List<Point2d> points;
  private double length;
  private Node ini, end; // utilise lorsque les arcs du graphe sont bien des
  private double maxX, maxY, minX, minY;
  private Map<String, Double> centralityValues;

  // arcs

  public Edge() {
    this.points = new ArrayList<Point2d>();
    this.centralityValues = new HashMap<String, Double>();
  }

  public Edge(Edge e) {
    this.points = new ArrayList<Point2d>(e.points);
    this.length = e.length;
    this.ini = new Node(e.first());
    this.end = new Node(e.last());
    this.centralityValues = new HashMap<String, Double>(e.getCentralityValues());
    initBounds();
  }

  public Edge(Node ini, Node end) {
    this.points = new ArrayList<Point2d>();
    this.ini = ini;
    this.end = end;
    this.centralityValues = new HashMap<String, Double>();
  }

  public Edge(List<Point2d> points) {
    this.points = points;
    this.length = length();
    this.centralityValues = new HashMap<String, Double>();
    initBounds();
  }

  public Node first() {
    return this.ini;
  }

  public Node last() {
    return this.end;
  }

  public List<Point2d> coords() {
    return this.points;
  }

  public void add(Point2d pt) {
    this.points.add(pt);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Edge other = (Edge) obj;
    if (points == null) {
      if (other.points != null)
        return false;
    } else if (!points.equals(other.points))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Edge [points=" + points + "]";
  }

  public void setFisrt(Node ini) {
    this.ini = ini;
  }

  public void setLast(Node end) {
    this.end = end;
  }

  public double getMaxX() {
    return this.maxX;
  }

  public double getMaxY() {
    return this.maxY;

  }

  public double getMinX() {
    return this.minX;
  }

  public double getMinY() {
    return this.minY;
  }

  public void setLength(double length) {
    this.length = length;
  }

  public double getLength() {
    return length;
  }

  private double length() {
    double l = 0;
    for (int i = 0; i < this.points.size() - 1; i++) {
      Point2d pt1 = this.points.get(i);
      Point2d pt2 = this.points.get(i + 1);
      l += pt1.distance(pt2);
    }
    return l;
  }

  private void initBounds() {
    // maxX
    this.maxX = 0;
    for (Point2d pt : this.points) {
      if (pt.x > this.maxX) {
        this.maxX = pt.x;
      }
    }
    // max y
    this.maxY = 0;
    for (Point2d pt : this.points) {
      if (pt.y > this.maxY) {
        this.maxY = pt.y;
      }
    }
    // min x
    this.minX = Double.MAX_VALUE;
    for (Point2d pt : this.points) {
      if (pt.x < this.minX) {
        this.minX = pt.x;
      }
    }
    // min y
    this.minY = Double.MAX_VALUE;
    for (Point2d pt : this.points) {
      if (pt.y < this.minY) {
        this.minX = pt.y;
      }
    }
  }

  public void setCentralityValues(Map<String, Double> centralityValues) {
    this.centralityValues = centralityValues;
  }

  public Map<String, Double> getCentralityValues() {
    return centralityValues;
  }
}
