package fr.ign.cogit.v2.rasters;

import java.awt.Color;
import java.util.Collection;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;

public class SHP2RasterValuesClassification extends AbstractSHP2Raster{

    public SHP2RasterValuesClassification(String inputShp, String weightAttribute, String outputImgSrc, AbstractSHP2Raster.FORMAT format, double bufSize, int width){
        super(inputShp, weightAttribute, outputImgSrc, format, width, bufSize);
    }

    @Override
    protected Color value2Color(double value) {
        double a = ((250./360.)/ (this.getMinValue() - this.getMaxValue()));
        double b = - a * this.getMaxValue();
        double h = a * value + b;            
        h +=0.5;
        h *=-1;
        double r = Math.sin(Math.PI * h);
        double g = Math.sin(Math.PI * (h+1./3.));
        double bl = Math.sin(Math.PI * (h + 2./3.));       
        int[] result = new int[3];
        result[0] = (int) (255 * r*r);
        result[1] = (int) (255 * g*g);
        result[2] = (int)(255 * bl*bl);
        return new Color(result[0], result[1], result[2]);
    }

    @Override
    protected Color getOutlyingPixelsColor(Collection<IFeature> candidates, IGeometry geom) {
        //sinon on floute
        double dmin = Double.MAX_VALUE;
        double result = 0.;
        double somme = 0.;
        for (IFeature ptf : candidates){
            double distance = ptf.getGeom().distance(geom);
            if(distance<dmin){
                dmin = distance;
            } 
            double weight = this.getMappingValuesFeatures().get(ptf);
            distance = 1. / (Math.pow(distance, this.getAlpha()));
            somme += distance;
            result += weight * distance;
        }
        result = result / somme;
        Color r= this.value2Color(result);
        int alpha = (int)Math.max(120 - dmin, 0);
        return new Color(r.getRed(), r.getGreen(), r.getBlue(), alpha);
    }

}
