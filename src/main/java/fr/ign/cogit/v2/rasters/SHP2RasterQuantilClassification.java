package fr.ign.cogit.v2.rasters;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;

public class SHP2RasterQuantilClassification  extends AbstractSHP2Raster{

    private int INTERVAL_SIZE;
    private Color[] QUANTIL_COLORS;


    private List<Double> valuesSorted;

    public SHP2RasterQuantilClassification(String inputShp, String weightAttribute, String outputImgSrc, AbstractSHP2Raster.FORMAT format, double bufSize, int width){
        super(inputShp, weightAttribute, outputImgSrc, format,width, bufSize);
        this.INTERVAL_SIZE = 5;
        this.finalizeInitialisation();
    }

    public SHP2RasterQuantilClassification(String inputShp, String weightAttribute, String outputImgSrc, AbstractSHP2Raster.FORMAT format, double bufSize, int width, int intervalSize){
        super(inputShp, weightAttribute, outputImgSrc, format, bufSize, width);
        this.INTERVAL_SIZE = intervalSize;
        this.finalizeInitialisation();
    }


    @Override
    protected Color value2Color(double value) {
        int pas = (int) ((double) this.getValuesSorted().size() / (double) this.getINTERVAL_SIZE());
        int id = 0;
        for(Double d :this.getValuesSorted()){
            if(d>value){
                break;
            }
            id++;
        }
        int index = Math.min(id / pas, this.getINTERVAL_SIZE() - 1);
        return this.getQUANTIL_COLORS()[index];
    }

    @Override
    protected Color getOutlyingPixelsColor(Collection<IFeature> candidates, IGeometry geom) {
        //on floute
        double dmin = Double.MAX_VALUE;
        double resultR = 0.;
        double resultG = 0.;
        double resultB = 0.;
        double somme = 0.;
        for (IFeature ptf : candidates){
            double distance = ptf.getGeom().distance(geom);
            if(distance<dmin){
                dmin = distance;
            } 
            double weight = this.getMappingValuesFeatures().get(ptf);
            int pas = (int) ((double) this.getValuesSorted().size() / (double) this.getINTERVAL_SIZE());
            int id = 0;
            for(Double d :this.getValuesSorted()){
                if(d>weight){
                    break;
                }
                id++;
            }
            int index = Math.min(id / pas, this.getINTERVAL_SIZE() - 1);
            Color r = this.getQUANTIL_COLORS()[index];
            distance = 1. / (Math.pow(distance, this.getAlpha()));
            somme += distance;
            resultR += r.getRed() * distance;
            resultG += r.getGreen() * distance;
            resultB += r.getBlue() * distance;
        }
        resultR = resultR / somme;
        resultG = resultG / somme;
        resultB = resultB / somme;
        int a = (int)Math.max(120 - dmin, 0);
        return new Color((int)resultR, (int)resultG, (int)resultB, a);
    }


    private void finalizeInitialisation() {
        
       
        
         
        this.QUANTIL_COLORS = new Color[this.getINTERVAL_SIZE()];
        double a = ((0.77- 0.1)/ (this.getINTERVAL_SIZE()-1));
        double b = - a *0.;

        for(int i=0; i< this.getINTERVAL_SIZE(); i++){
            double h = a * i + b;            
            h +=0.5;
            h *=-1;
            double r = Math.sin(Math.PI * (h+0.05) );
            double g = Math.sin(Math.PI * (h+0.2));
            double bl = Math.sin(Math.PI * (h +0.45));          
            int[] result = new int[3];
            result[0] = (int) (255 * r*r);
            result[1] = (int) (255 * g*g);
            result[2] = (int)(255 * bl*bl);
            this.QUANTIL_COLORS[i] =  new Color(result[0], result[1], result[2]);
        }

        this.valuesSorted = new ArrayList<Double>();
        this.valuesSorted.addAll(this.getMappingValuesFeatures().values());
        Collections.sort(this.getValuesSorted());
        
//        this.QUANTIL_COLORS = new Color[this.getINTERVAL_SIZE()];
//        double a = ((250./360.)/ (0 - (this.getINTERVAL_SIZE()-1)));
//        double b = - a *(this.getINTERVAL_SIZE()-1);
//
//        for(int i=0; i< this.getINTERVAL_SIZE(); i++){
//            double h = a * i + b;            
//            h +=0.5;
//            h *=-1;
//            double r = Math.sin(Math.PI * h);
//            double g = Math.sin(Math.PI * (h+1./3.));
//            double bl = Math.sin(Math.PI * (h + 2./3.));       
//            int[] result = new int[3];
//            result[0] = (int) (255 * r*r);
//            result[1] = (int) (255 * g*g);
//            result[2] = (int)(255 * bl*bl);
//            this.QUANTIL_COLORS[i] =  new Color(result[0], result[1], result[2]);
//        }
//
//        this.valuesSorted = new ArrayList<Double>();
//        this.valuesSorted.addAll(this.getMappingValuesFeatures().values());
//        Collections.sort(this.getValuesSorted());
    }

    public int getINTERVAL_SIZE() {
        return INTERVAL_SIZE;
    }
    public List<Double> getValuesSorted() {
        return valuesSorted;
    }
    public Color[] getQUANTIL_COLORS() {
        return QUANTIL_COLORS;
    }

}
