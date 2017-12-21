package fr.ign.cogit.v2.rasters;

import java.awt.Color;
import java.util.Collection;

import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.appli.plugin.VoronoiDiagramJTSPlugin;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;

public class SHP2RasterMeanInterpolation extends AbstractSHP2Raster{

    public SHP2RasterMeanInterpolation(String inputShp, String weightAttribute,
            String outputImgSrc, FORMAT format, double bufSize, int width) {
        super(inputShp, weightAttribute, outputImgSrc, format, width, bufSize);

    }


    @Override
    protected Color kneighbor(IDirectPosition pt) {
        IGeometry geom = new GM_Point(pt);
        Collection<IFeature> candidates = this.getFeatures().select(geom, this.getBufSize());
        if(!candidates.isEmpty()){
            //plusieurs candidats => on va interpoler
            double result = 0.;
            double somme = 0.;
            double dmin = Double.MAX_VALUE;
            for (IFeature ptf : candidates){
                double distance = ptf.getGeom().distance(geom);
                if(distance<dmin){
                    dmin = distance;
                } 
                double weight = this.getMappingValuesFeatures().get(ptf);
                distance = 1. / (Math.pow(distance, this.getAlpha()));
                //somme += distance;
                result += weight * distance;
            }
            result = result / somme;

            candidates = this.getFeatures().select(geom, this.getObjectSize());
            if(!candidates.isEmpty()){              
                Color r= value2Color(this.getMappingValuesFeatures().get(candidates.iterator().next()));
                return r;
            }
            Color r= this.value2Color(result);
            int alpha = (int)Math.max(120 - dmin, 0);
            return new Color(r.getRed(), r.getGreen(), r.getBlue(), alpha);
        }
        return  new Color(0,0,0);
    }

    @Override
    protected Color value2Color(double value) {
//        double a = (1./ (this.getMaxValue() - this.getMinValue()));
//        double b = - a * this.getMinValue();
//        double h = a * value + b;            
//        h +=0.5;
//        h *=-1;
//        double r = Math.sin(Math.PI * h);
//        double g = Math.sin(Math.PI * (h+1./3.));
//        double bl = Math.sin(Math.PI * (h + 2./3.));       
//        int[] result = new int[3];
//        result[0] = (int) (255 * r*r);
//        result[1] = (int) (255 * g*g);
//        result[2] = (int)(255 * bl*bl);
//        return new Color(result[0], result[1], result[2]);
//        double a = ((0.8- 0.0)/ (this.getMaxValue() - this.getMinValue()));
//        double b = 0.0 -  a * this.getMinValue();
//        double h = a * value + b;            
//        h +=0.5;
//        h *=-1;
//        double r = Math.sin(Math.PI * h);
//        double g = Math.sin(Math.PI * (h+1./4.));
//        double bl = Math.sin(Math.PI * (h +1./3.));       
//        int[] result = new int[3];
//        result[0] = (int) (255 * r*r);
//        result[1] = (int) (255 * g*g);
//        result[2] = (int)(255 * bl*bl);
//        return new Color(result[0], result[1], result[2]);
        
        
//      double a = ((0.83- 0.15)/ (this.getMaxValue() - this.getMinValue()));
//      double b = 0.15 -  a * this.getMinValue();
//      double h = a * value + b;            
//     // h +=0.5;
//      //h *=-1;
//      double r = Math.sin(Math.PI * (h) );
//      double g = Math.sin(Math.PI * (h+0.1));
//      double bl = Math.sin(Math.PI * (h +0.45));       
//      int[] result = new int[3];
//      result[0] = (int) (255 * r*r);
//      result[1] = (int) (255 * g*g);
//      result[2] = (int)(255 * bl*bl);
//      return new Color(result[0], result[1], result[2]);
        
        
        double a = ((0.77- 0.1)/ (this.getMaxValue() - this.getMinValue()));
        double b = 0.1 -  a * this.getMinValue();
        double h = a * value + b;            
       // h +=0.5;
        //h *=-1;
        double r = Math.sin(Math.PI * (h+0.05) );
        double g = Math.sin(Math.PI * (h+0.2));
        double bl = Math.sin(Math.PI * (h +0.45));       
        int[] result = new int[3];
        result[0] = (int) (255 * r*r);
        result[1] = (int) (255 * g*g);
        result[2] = (int)(255 * bl*bl);
        return new Color(result[0], result[1], result[2]);

    }

    @Override
    protected Color getOutlyingPixelsColor(Collection<IFeature> candidates,
            IGeometry geom) {
        return null;
    }

}
