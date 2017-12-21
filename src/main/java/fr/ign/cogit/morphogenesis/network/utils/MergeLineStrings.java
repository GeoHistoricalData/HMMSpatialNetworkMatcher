package fr.ign.cogit.morphogenesis.network.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Distances;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;

public class MergeLineStrings {

  private static Logger logger = Logger.getLogger(MergeLineStrings.class
      .getName());

  public static ILineString merge(List<ILineString> lines) {
    IDirectPositionList final_points = new DirectPositionList();
    if (lines.isEmpty()) {
      MergeLineStrings.logger
          .error("ATTENTION. Erreur à la compilation de lignes : aucune ligne en entrée");
      return null;
    }
    if (lines.size() == 1) {
      return lines.get(0);
    }

    // on récupère une des extrémités
    List<IDirectPosition> point_start_list = new ArrayList<IDirectPosition>();
    List<ILineString> line_start_list = new ArrayList<ILineString>();
    List<ILineString> lines_set = new ArrayList<ILineString>();
    for (ILineString line : lines) {
      lines_set.add((ILineString) line.clone());
    }

    // for (ILineString line : lines) {
    /*
     * for (IDirectPosition p : line.coord()) { boolean alreadyIn = false; for
     * (IDirectPosition pin : points) { if (Distances.proche(pin, p, 0.5)) {
     * alreadyIn = true; break; } } if (!alreadyIn) { points.add(p); } }
     */
    // points.addAll(line.coord());
    // }
    for (ILineString line : lines_set) {
      IDirectPosition pstart = line.startPoint();
      IDirectPosition pend = line.endPoint();
      int cptStart = 0;
      int cptEnd = 0;
      for (ILineString line2 : lines_set) {
        if (line.equals(line2)) {
          continue;
        }
        for (IDirectPosition pp : line2.coord()) {
          if (pp.equals(pstart, 0.5)) {
            cptStart++;
            break;
          }
        }
        for (IDirectPosition pp : line2.coord()) {
          if (pp.equals(pend, 0.5)) {
            cptEnd++;
            break;
          }
        }
      }
      if (cptStart == 0) {
        point_start_list.add(pstart);
        line_start_list.add(line);
      }
      if (cptEnd == 0) {
        point_start_list.add(pend);
        line_start_list.add(line);
      }
    }
    IDirectPosition point_start = null;
    ILineString line_start = null;
    if (point_start_list.isEmpty()) {
      logger
          .warn("ATTENTION. Erreur à la compilation de lignes : boucle complète");
      point_start = lines_set.get(0).startPoint();
      line_start = lines_set.get(0);
    } else if (point_start_list.size() == 1) {
      logger
          .warn("ATTENTION. Erreur à la compilation de lignes : semi-boucle (bizarre)");
      point_start = point_start_list.get(0);
      line_start = line_start_list.get(0);
    } else if (point_start_list.size() > 2) {
      logger
          .error("ATTENTION. Erreur à la compilation de lignes : les lignes ne se touchent pas");
      System.out.println(lines_set.toString());
      return null;
    } else {
      point_start = point_start_list.get(0);
      line_start = line_start_list.get(0);
    }
    if (point_start.equals(line_start.startPoint(), 0.5)) {
      final_points.addAll(line_start.coord());
      point_start = line_start.endPoint();
    } else {
      final_points.addAll(line_start.coord().reverse());
      point_start = line_start.startPoint();
    }
    lines_set.remove(line_start);
    while (!lines_set.isEmpty()) {
      double dist_min = Double.MAX_VALUE;
      for (ILineString line : lines_set) {
        if (Distances.proche(point_start, line.startPoint(), dist_min)
            || Distances.proche(point_start, line.endPoint(), dist_min)) {
          dist_min = Math.min(point_start.distance(line.startPoint()),
              point_start.distance(line.endPoint()));
          line_start = line;
        }
      }
      if (point_start.equals(line_start.startPoint(), 0.5)) {
        IDirectPositionList l = line_start.coord();
        l.remove(0);
        final_points.addAll(l);
        point_start = line_start.endPoint();
      } else {
        IDirectPositionList l = line_start.coord().reverse();
        l.remove(0);
        final_points.addAll(l);
        point_start = line_start.startPoint();
      }
      lines_set.remove(line_start);
    }
    ILineString result = new GM_LineString(final_points);
    return result;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    IDirectPositionList l1 = new DirectPositionList();
    l1.add(new DirectPosition(0, 0));
    l1.add(new DirectPosition(1, 0));
    l1.add(new DirectPosition(2, 0));
    ILineString line1 = new GM_LineString(l1);

    IDirectPositionList l2 = new DirectPositionList();
    l2.add(new DirectPosition(2, 0));
    l2.add(new DirectPosition(3, 0));
    l2.add(new DirectPosition(4, 0));
    ILineString line2 = new GM_LineString(l2);

    IDirectPositionList l3 = new DirectPositionList();
    l3.add(new DirectPosition(4, 0));
    l3.add(new DirectPosition(5, 0));
    l3.add(new DirectPosition(0, 0));
    ILineString line3 = new GM_LineString(l3);

    List<ILineString> list = new ArrayList<ILineString>();
    list.add(line3);
    list.add(line1);
    list.add(line2);

    ILineString result = MergeLineStrings.merge(list);
    System.out.println(result.toString());

  }

}
