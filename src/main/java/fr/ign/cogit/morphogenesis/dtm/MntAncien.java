package fr.ign.cogit.morphogenesis.dtm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import com.vividsolutions.jts.io.ParseException;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class MntAncien {
  public static void main(String args[]) throws ParseException {

    IPopulation<IFeature> poly = ShapefileReader
        .read("/media/Data/Benoit/these/donnees/mnt/mathieu_fernandez/Lignes_de_Niveau_P_S_Girard.shp");

    poly.initSpatialIndex(Tiling.class, true);

    double xmin = Math.round(poly.envelope().getLowerCorner().getX());
    double ymin = Math.round(poly.envelope().getLowerCorner().getY());
    double xmax = Math.round(poly.envelope().getUpperCorner().getX());
    double ymax = Math.round(poly.envelope().getUpperCorner().getY());

    double y = ymax;

    /*
     * FeatureType ft = new FeatureType(); AttributeType att = new
     * AttributeType(); att.setNomField("Z"); att.setMemberName("Z");
     * att.setValueType("DOUBLE"); ft.addFeatureAttribute(att);
     */

    int pas = 20;
    String output = "";
    int sx = 0;

    int sy = 0;

    output += "xllcorner     " + xmin + "\n";
    output += "yllcorner     " + ymin + "\n";
    output += "cellsize      " + pas + "\n";
    output += "NODATA_value  -9999\n";

    while (y >= ymin - pas) {
      System.out.println(y + " / " + ymax);
      double x = xmin;
      sx = 0;
      while (x <= xmax + pas) {

        DefaultFeature f = new DefaultFeature();
        f.setGeom(new GM_Point(new DirectPosition(x, y)));
        Collection<IFeature> c = poly.select(f.getGeom(), 0);
        if (c.isEmpty()) {
          /*
           * f.setFeatureType(ft);
           * f.setAttribute(ft.getFeatureAttributeByName("Z"), -9999.);
           * lines.add(f);
           */
          x += pas;
          output += -9999 + " ";
          sx++;
          continue;
        }
        String value = c.iterator().next().getAttribute("Z").toString();
        output += value + " ";
        /*
         * f.setFeatureType(ft);
         * f.setAttribute(ft.getFeatureAttributeByName("Z"),
         * Double.parseDouble(value)); lines.add(f);
         */
        x += pas;
        sx++;
      }
      y -= pas;
      output = output.substring(0, output.length());
      output += "\n";
      sy++;
    }

    String s = "ncols         " + sx + "\n";
    s += "nrows         " + sy + "\n";
    output = s + output;

    /*
     * ShapefileWriter.write(lines,
     * "/media/Data/Benoit/these/donnees/mnt/mathieu_fernandez/lignes.shp");
     */

    try {
      FileWriter fr = new FileWriter("/home/bcostes/Bureau/t.asc");
      BufferedWriter br = new BufferedWriter(fr);
      br.write(output);
      br.close();
      fr.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
