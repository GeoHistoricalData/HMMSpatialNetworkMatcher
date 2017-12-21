package fr.ign.cogit.v2.mergeProcess.mergeGeometries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.mergeProcess.mergeGeometries.snapping.AbstractSnappingAlgorithm;
import fr.ign.cogit.v2.mergeProcess.mergeGeometries.snapping.ElasticTransformation;
import fr.ign.cogit.v2.mergeProcess.mergeGeometries.snapping.HelmertSnapping;

public class MergeGeometriesTest {

  public static void main(String[] args) {
    IPopulation<IFeature> pop = ShapefileReader.read("/home/bcostes/Bureau/lines.shp");
    Set<ILineString> lines = new HashSet<ILineString>();
    Map<ILineString, Double> weights = new HashMap<ILineString, Double>();

    for(IFeature f: pop){
      lines.add(new GM_LineString(f.getGeom().coord()));
    }
    weights.put(lines.iterator().next(),5. );

    //    MatchingHomologousPointsMerge merge = new MatchingHomologousPointsMerge(lines, 10);
    //    ILineString lineMerged = merge.merge();
    //    HelmertSnapping hSNap = new HelmertSnapping(lines,lines.iterator().next().startPoint(),
    //        lines.iterator().next().endPoint());
    //    lineMerged = hSNap.transform(lineMerged);
    //    IPopulation<IFeature> out = new Population<IFeature>();
    //    out.add(new DefaultFeature(lineMerged));
    //    System.out.println(lineMerged);
    //    ShapefileWriter.write(out, "/home/bcostes/Bureau/test_merge/merge.shp");

    pop = ShapefileReader.read("/home/bcostes/Bureau/points.shp");
    List<IDirectPosition> points = new ArrayList<IDirectPosition>();
    for(IFeature f: pop){
      points.add(f.getGeom().coord().get(0));
    }

    AbstractSnappingAlgorithm hSNap = new ElasticTransformation(lines,points.get(0),
        points.get(1));
    ILineString l=   hSNap.transform(Operateurs.resampling(lines.iterator().next(),0.05));
//    
    //AbstractMergeAlgorithm merge = new MCMCMerge(lines);
   // ILineString lineMerged = merge.merge();

    IPopulation<IFeature> out = new Population<IFeature>();
    out.add(new DefaultFeature(l));

   // for(ILineString l : lines){
   // out.add(new DefaultFeature(l));
   // }
    ShapefileWriter.write(out, "/home/bcostes/Bureau/merge2.shp");


    


  }

}
