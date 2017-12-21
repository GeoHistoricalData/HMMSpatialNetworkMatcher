package fr.ign.cogit.morphogenesis.network.utils.delaunay;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.core.util.Primitives;

/**
 * Incremental Delaunay Triangulation
 * 
 * @author Java-code by X.Philippeau - Pseudo-code by Guibas and Stolfi
 * 
 * @see Primitives for the Manipulation of General Subdivisions and the
 *      Computation of Voronoi Diagrams (Leonidas Guibas,Jorge Stolfi)
 */
public class Delaunay {

  // starting edge for walk (see locate() method)
  private QuadEdge startingEdge = null;

  // list of quadEdge belonging to Delaunay triangulation
  private List<QuadEdge> quadEdge = new ArrayList<QuadEdge>();

  // Bounding box of the triangulation
  class BoundingBox {
    int minx, miny, maxx, maxy;
    Point a = new Point(); // lower left
    Point b = new Point(); // lower right
    Point c = new Point(); // upper right
    Point d = new Point(); // upper left
  }

  private BoundingBox bbox = new BoundingBox();

  /**
   * Constuctor:
   */
  public Delaunay() {

    bbox.minx = Integer.MAX_VALUE;
    bbox.maxx = Integer.MIN_VALUE;
    bbox.miny = Integer.MAX_VALUE;
    bbox.maxy = Integer.MIN_VALUE;

    // create the QuadEdge graph of the bounding box
    QuadEdge ab = QuadEdge.makeEdge(bbox.a, bbox.b);
    QuadEdge bc = QuadEdge.makeEdge(bbox.b, bbox.c);
    QuadEdge cd = QuadEdge.makeEdge(bbox.c, bbox.d);
    QuadEdge da = QuadEdge.makeEdge(bbox.d, bbox.a);
    QuadEdge.splice(ab.sym(), bc);
    QuadEdge.splice(bc.sym(), cd);
    QuadEdge.splice(cd.sym(), da);
    QuadEdge.splice(da.sym(), ab);

    this.startingEdge = ab;
  }

  /**
   * update the dimension of the bounding box
   * 
   * @param minx,miny,maxx,maxy summits of the rectangle
   */
  public void setBoundigBox(int minx, int miny, int maxx, int maxy) {
    // update saved values
    bbox.minx = minx;
    bbox.maxx = maxx;
    bbox.miny = miny;
    bbox.maxy = maxy;

    // extend the bounding-box to surround min/max
    int centerx = (minx + maxx) / 2;
    int centery = (miny + maxy) / 2;
    int x_min = (int) ((minx - centerx - 1) * 10 + centerx);
    int x_max = (int) ((maxx - centerx + 1) * 10 + centerx);
    int y_min = (int) ((miny - centery - 1) * 10 + centery);
    int y_max = (int) ((maxy - centery + 1) * 10 + centery);

    // set new positions
    bbox.a.x = x_min;
    bbox.a.y = y_min;
    bbox.b.x = x_max;
    bbox.b.y = y_min;
    bbox.c.x = x_max;
    bbox.c.y = y_max;
    bbox.d.x = x_min;
    bbox.d.y = y_max;
  }

  // update the size of the bounding box (cf locate() method)
  private void updateBoundigBox(Point p) {
    int minx = Math.min(bbox.minx, p.x);
    int maxx = Math.max(bbox.maxx, p.x);
    int miny = Math.min(bbox.miny, p.y);
    int maxy = Math.max(bbox.maxy, p.y);
    setBoundigBox(minx, miny, maxx, maxy);
    // System.out.println("resizing bounding-box: "+minx+" "+miny+" "+maxx+" "+maxy);
  }

  /**
   * Returns an edge e of the triangle containing the point p (Guibas and
   * Stolfi)
   * 
   * @param p the point to localte
   * @return the edge of the triangle
   */
  private QuadEdge locate(Point p) {

    /* outside the bounding box ? */
    if (p.x < bbox.minx || p.x > bbox.maxx || p.y < bbox.miny
        || p.y > bbox.maxy) {
      updateBoundigBox(p);
    }

    QuadEdge e = startingEdge;
    while (true) {
      /* duplicate point ? */
      if (p.x == e.orig().x && p.y == e.orig().y)
        return e;
      if (p.x == e.dest().x && p.y == e.dest().y)
        return e;

      /* walk */
      if (QuadEdge.isAtRightOf(e, p))
        e = e.sym();
      else if (!QuadEdge.isAtRightOf(e.onext(), p))
        e = e.onext();
      else if (!QuadEdge.isAtRightOf(e.dprev(), p))
        e = e.dprev();
      else
        return e;
    }
  }

  /**
   * Inserts a new point into a Delaunay triangulation (Guibas and Stolfi)
   * 
   * @param p the point to insert
   */
  public void insertPoint(Point p) {
    System.out.println("00");

    QuadEdge e = locate(p);

    System.out.println("01");

    // point is a duplicate -> nothing to do
    if (p.x == e.orig().x && p.y == e.orig().y)
      return;
    if (p.x == e.dest().x && p.y == e.dest().y)
      return;

    System.out.println("1");

    // point is on an existing edge -> remove the edge
    if (QuadEdge.isOnLine(e, p)) {
      e = e.oprev();
      this.quadEdge.remove(e.onext().sym());
      this.quadEdge.remove(e.onext());
      QuadEdge.deleteEdge(e.onext());
    }

    System.out.println("2");

    // Connect the new point to the vertices of the containing triangle
    // (or quadrilateral in case of the point is on an existing edge)
    QuadEdge base = QuadEdge.makeEdge(e.orig(), p);
    this.quadEdge.add(base);

    System.out.println("3");

    QuadEdge.splice(base, e);
    this.startingEdge = base;

    System.out.println("4");

    do {
      base = QuadEdge.connect(e, base.sym());
      this.quadEdge.add(base);
      e = base.oprev();
    } while (e.lnext() != startingEdge);

    System.out.println("5");

    // Examine suspect edges to ensure that the Delaunay condition is satisfied.
    do {
      QuadEdge t = e.oprev();

      if (QuadEdge.isAtRightOf(e, t.dest())
          && QuadEdge.inCircle(e.orig(), t.dest(), e.dest(), p)) {
        // flip triangles
        QuadEdge.swapEdge(e);
        e = e.oprev();
      } else if (e.onext() == startingEdge)
        return; // no more suspect edges
      else
        e = e.onext().lprev(); // next suspect edge
    } while (true);
  }

  /**
   * compute and return the list of edges
   */
  public List<Point[]> computeEdges() {
    List<Point[]> edges = new ArrayList<Point[]>();
    // do not return edges pointing to/from surrouding triangle
    for (QuadEdge q : this.quadEdge) {
      if (q.orig() == bbox.a || q.orig() == bbox.b || q.orig() == bbox.c
          || q.orig() == bbox.d)
        continue;
      if (q.dest() == bbox.a || q.dest() == bbox.b || q.dest() == bbox.c
          || q.dest() == bbox.d)
        continue;
      edges.add(new Point[] { q.orig(), q.dest() });
    }
    return edges;
  }

  /**
   * compute and return the list of triangles
   */
  public List<Point[]> computeTriangles() {
    List<Point[]> triangles = new ArrayList<Point[]>();

    // do not process edges pointing to/from surrouding triangle
    // --> mark them as already computed
    for (QuadEdge q : this.quadEdge) {
      q.mark = false;
      q.sym().mark = false;
      if (q.orig() == bbox.a || q.orig() == bbox.b || q.orig() == bbox.c
          || q.orig() == bbox.d)
        q.mark = true;
      if (q.dest() == bbox.a || q.dest() == bbox.b || q.dest() == bbox.c
          || q.dest() == bbox.d)
        q.sym().mark = true;
    }

    // compute the 2 triangles associated to each quadEdge
    for (QuadEdge qe : quadEdge) {
      // first triangle
      QuadEdge q1 = qe;
      QuadEdge q2 = q1.lnext();
      QuadEdge q3 = q2.lnext();
      if (!q1.mark && !q2.mark && !q3.mark) {
        triangles.add(new Point[] { q1.orig(), q2.orig(), q3.orig() });
      }

      // second triangle
      QuadEdge qsym1 = qe.sym();
      QuadEdge qsym2 = qsym1.lnext();
      QuadEdge qsym3 = qsym2.lnext();
      if (!qsym1.mark && !qsym2.mark && !qsym3.mark) {
        triangles.add(new Point[] { qsym1.orig(), qsym2.orig(), qsym3.orig() });
      }

      // mark as used
      qe.mark = true;
      qe.sym().mark = true;
    }

    return triangles;
  }

  public List<Point[]> computeVoronoi() {
    List<Point[]> voronoi = new ArrayList<Point[]>();

    // do not process edges pointing to/from surrouding triangle
    // --> mark them as already computed
    for (QuadEdge q : this.quadEdge) {
      q.mark = false;
      q.sym().mark = false;
      if (q.orig() == bbox.a || q.orig() == bbox.b || q.orig() == bbox.c
          || q.orig() == bbox.d)
        q.mark = true;
      if (q.dest() == bbox.a || q.dest() == bbox.b || q.dest() == bbox.c
          || q.dest() == bbox.d)
        q.sym().mark = true;
    }

    for (QuadEdge qe : quadEdge) {

      // walk throught left and right region
      for (int b = 0; b <= 1; b++) {
        QuadEdge qstart = (b == 0) ? qe : qe.sym();
        if (qstart.mark)
          continue;

        // new region start
        List<Point> poly = new ArrayList<Point>();

        // walk around region
        QuadEdge qregion = qstart;
        while (true) {
          qregion.mark = true;

          // compute CircumCenter if needed
          if (qregion.rot().orig() == null) {
            QuadEdge q1 = qregion;
            Point p0 = q1.orig();
            QuadEdge q2 = q1.lnext();
            Point p1 = q2.orig();
            QuadEdge q3 = q2.lnext();
            Point p2 = q3.orig();

            double ex = p1.x - p0.x, ey = p1.y - p0.y;
            double nx = p2.y - p1.y, ny = p1.x - p2.x;
            double dx = (p0.x - p2.x) * 0.5, dy = (p0.y - p2.y) * 0.5;
            double s = (ex * dx + ey * dy) / (ex * nx + ey * ny);
            double cx = (p1.x + p2.x) * 0.5 + s * nx;
            double cy = (p1.y + p2.y) * 0.5 + s * ny;

            Point p = new Point((int) cx, (int) cy);
            qregion.rot().setOrig(p);
          }

          poly.add(qregion.rot().orig());

          qregion = qregion.onext();
          if (qregion == qstart)
            break;
        }

        // add region to output list
        voronoi.add(poly.toArray(new Point[0]));
      }
    }
    return voronoi;
  }
}
