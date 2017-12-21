package fr.ign.cogit.morphogenesis.exploring_tool.utils.interpolation;

import java.util.Collection;
import java.util.Collections;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IEnvelope;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class HeatMapParametrization {


    public static void study(IFeatureCollection<IFeature> pop, double width){
        pop.initSpatialIndex(Tiling.class,false);
        IEnvelope env = pop.getEnvelope();
        double xmin = env.minX();
        double ymin = env.minY();
        double xmax = env.maxX();
        double ymax = env.maxY();
        int height = (int) (((double) (width)) * (ymax - ymin) / (xmax - xmin)) + 1;
        // transfo global => local
        double ax = (xmax - xmin) / ((double) width);
        double bx = xmin;
        double ay = (ymin - ymax) / ((double) height);
        double by = ymax;
        for(double d = 120; d<=250; d+=10){
            double mean = 0.;
            for (int h = 0; h < height; h++){
                // Fill the array with a degradé pattern.
                for (int w = 0; w < width; w++){
                    IDirectPosition pt = new DirectPosition(ax * w + bx, ay * h + by);
                    Collection<IFeature> candidates = pop.select(new GM_Point(pt),d+100);
                    for(IFeature f : candidates){
                        if(f.getGeom().distance(new GM_Point(pt))<=d){
                            mean ++;
                        }
                    }
                }
            }
            mean /= ((double)width * height);
            System.out.println(d+" " + mean);
        }

    }
    
    public static void main(String args[]){
        IPopulation<IFeature> pop = ShapefileReader.read("/home/bcostes/Bureau/tmp/betweenness.shp");
        HeatMapParametrization.study(pop, 500);
    }

}
