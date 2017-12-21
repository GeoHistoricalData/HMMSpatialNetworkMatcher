package fr.ign.cogit.v2.mergeProcess.hierarchicalMatching;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

/**
 * Sert à rien (juste pour faire des jeux de tests pour images explicatives de l'algo
 * @author bcostes
 *
 */
public class ProjectionManual {

  public static void main(String[] args) {
   
    
    
    IPopulation<IFeature> linesIn = ShapefileReader.read("/home/bcostes/Bureau/HMM_matching/test_images/lines_1871.shp");
    List<ILineString> lines = new ArrayList<ILineString>();
    for(IFeature f : linesIn){
      lines.add(new GM_LineString(f.getGeom().coord()));
    }
    ILineString lineM = Operateurs.union(lines);
    
    IPopulation<IFeature> inPts = ShapefileReader.read("/home/bcostes/Bureau/HMM_matching/test_images/pts_verniquet.shp");
    IPopulation<IFeature> out = new Population<IFeature>();
    for(IFeature f : inPts){
      IDirectPosition pproj = Operateurs.projection(f.getGeom().coord().get(0),lineM);
      IFeature fout = new DefaultFeature(new GM_Point(pproj));
      int value = Integer.parseInt(f.getAttribute("number").toString());
      AttributeManager.addAttribute(fout, "number", value, "Integer");
      out.add(fout);
    }
    ShapefileWriter.write(out, "/home/bcostes/Bureau/HMM_matching/test_images/projections.shp");

  }

}
