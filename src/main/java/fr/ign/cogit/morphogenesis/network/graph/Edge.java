package fr.ign.cogit.morphogenesis.network.graph;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;

public class Edge {

  private List<Point2D.Double> points;
  private Node ini, end; // utilise lorsque les arcs du graphe sont bien des
                         // arcs

  public Edge() {
    this.points = new ArrayList<Point2D.Double>();
  }

  public Edge(Edge e) {
    this.points = new ArrayList<Point2D.Double>(e.points);
    this.ini = new Node(e.first());
    this.end = new Node(e.last());
  }

  public Edge(Node ini, Node end) {
    this.points = new ArrayList<Point2D.Double>();
    this.ini = ini;
    this.end = end;
  }

  public Edge(List<Point2D.Double> points) {
    this.points = points;
  }

  public Edge(IDirectPositionList l) {
    this.points = new ArrayList<Point2D.Double>();
    for (IDirectPosition pos : l) {
      points.add(new Point2D.Double(pos.getX(), pos.getY()));
    }
  }

  public Node first() {
    return this.ini;
  }

  public Node last() {
    return this.end;
  }

  public List<Point2D.Double> coords() {
    return this.points;
  }

  public void add(Point2D.Double pt) {
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
    double max = 0;
    for (Point2D.Double pt : this.points) {
      if (pt.getX() > max) {
        max = pt.getX();
      }
    }
    return max;
  }

  public double getMaxY() {
    double max = 0;
    for (Point2D.Double pt : this.points) {
      if (pt.getY() > max) {
        max = pt.getY();
      }
    }
    return max;
  }

  public double getMinX() {
    double min = Double.MAX_VALUE;
    for (Point2D.Double pt : this.points) {
      if (pt.getX() < min) {
        min = pt.getX();
      }
    }
    return min;
  }

  public double getMinY() {
    double min = Double.MAX_VALUE;
    for (Point2D.Double pt : this.points) {
      if (pt.getY() < min) {
        min = pt.getY();
      }
    }
    return min;
  }

}
