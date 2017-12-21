package fr.ign.cogit.v2.rasters;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.jdesktop.swingx.image.GaussianBlurFilter;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class MapTest {

    static final double XMAX = 656578.;
    static final double XMIN = 647298;

    static final double YMAX = 6865429;
    static final double YMIN = 6858728;

    private static int INTERVAL_SIZE = 5;

    private static List<Double> valuesSorted;
    private  static Color[] QUANTIL_COLOR;


    public static void main(String[] args) {

        String shp = "/home/bcostes/Bureau/tmp/betweenness.shp";

        String weightAttribute="betweennes";

        int width = 500;


        init_quantil_colors();



        Map<IFeature, Double> mappingValuesFeatures = new HashMap<IFeature, Double>();
        IPopulation<IFeature> features = ShapefileReader.read(shp);
        features.initSpatialIndex(Tiling.class, false);
        valuesSorted = new ArrayList<Double>();
        for (IFeature f : features) {
            mappingValuesFeatures.put(f,
                    Double.parseDouble(f.getAttribute(weightAttribute).toString()));
        }

        valuesSorted.addAll(mappingValuesFeatures.values());
        Collections.sort(valuesSorted);
        List<Double> l = new ArrayList<Double>();
        l.addAll(mappingValuesFeatures.values());
        Collections.sort(l);
        double min = l.get(0);
        double max = l.get(l.size()-1);
        l.clear();
        l = null;
        int height = (int) (((double) (width)) * (YMAX - YMIN) / (XMAX - XMIN)) + 1;

        // transfo global => local
        double ax = (XMAX - XMIN) / ((double) width);
        double bx = XMIN;
        double ay = (YMIN - YMAX) / ((double) height);
        double by = YMAX;


        Iterator<?> writers = ImageIO.getImageWritersByFormatName("png");
        ImageWriter writer = (ImageWriter)writers.next();


        File f = new File("/home/bcostes/Bureau/tmp/betweenness.png");
        ImageOutputStream ios;
        try {

            ios = ImageIO.createImageOutputStream(f);
            writer.setOutput(ios);
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            // Auxiliary counter.
            int cpt=0;
            int size  =height * width / 100;
            long tglobal = System.currentTimeMillis();
            for (int h = 0; h < height; h++)
                // Fill the array with a degradé pattern.
                for (int w = 0; w < width; w++)
                {
                    Point2D pt = new Point2D.Double(ax * w + bx, ay * h + by);
                    Color rgba = MapTest.kneighbor(features, pt,mappingValuesFeatures, min, max);
                    bi.setRGB(w, h, rgba.getRGB());
                    cpt++;
                    if(cpt%size == 0){
                        System.out.println(cpt / size  + "%");
                    }
                }
            // filtre gaussien de deux pixels
//            GaussianBlurFilter gfilter = new GaussianBlurFilter(5);
//            BufferedImage bfiltered = gfilter.filter(bi, null);
//            BufferedImage bfinal = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
//            bfinal.setData(bfiltered.getRaster());
            writer.write(bi);
            generate_pngw("/home/bcostes/Bureau/tmp/betweenness.png", width, height);
            long t= System.currentTimeMillis();
            System.out.println("time : " + (t-tglobal));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    private static Color kneighbor(IPopulation<IFeature> features, Point2D pt, Map<IFeature, Double> mappingValuesFeatures, double min, double max) {
        IGeometry geom = new GM_Point(new DirectPosition(pt.getX(), pt.getY()));

        Collection<IFeature> candidates = features.select(geom,4);
        if(!candidates.isEmpty()){
            //on regarde si on a qu'un seul candidat proche
            if(candidates.size() == 1){
                Color r= value2RGBQuantil(mappingValuesFeatures.get(candidates.iterator().next()));
                return r;
            }
            //plusieurs candidats => on va interpoler
            candidates = features.select(geom,16);
            //sinon on floute
            double resultR = 0.;
            double resultG = 0.;
            double resultB = 0.;
            double somme = 0.;
            for (IFeature ptf : candidates){
                double distance = ptf.getGeom().distance(geom);
                double weight =mappingValuesFeatures.get(ptf);
                int pas = (int) ((double) valuesSorted.size() / (double) INTERVAL_SIZE);
                int id = 0;
                for(Double d :valuesSorted){
                    if(d>weight){
                        break;
                    }
                    id++;
                }
                int index = Math.min(id / pas, INTERVAL_SIZE - 1);
                Color r = QUANTIL_COLOR[index];
                distance = 1. / (Math.pow(distance, 1.5));
                somme += distance;
                resultR += r.getRed() * distance;
                resultG += r.getGreen() * distance;
                resultB += r.getBlue() * distance;
            }
            resultR = resultR / somme;
            resultG = resultG / somme;
            resultB = resultB / somme;
            //Color r= QUANTIL_COLOR[(int)result -1];
            return new Color((int)resultR, (int)resultG, (int)resultB);
        }
        candidates = features.select(geom,250);
        if(!candidates.isEmpty()){
            //sinon on floute
            double dmin = 1000;
            double resultR = 0.;
            double resultG = 0.;
            double resultB = 0.;
            double somme = 0.;
            for (IFeature ptf : candidates){
                double distance = ptf.getGeom().distance(geom);
                if(distance<dmin){
                    dmin = distance;
                } 
                double weight =mappingValuesFeatures.get(ptf);
                int pas = (int) ((double) valuesSorted.size() / (double) INTERVAL_SIZE);
                int id = 0;
                for(Double d :valuesSorted){
                    if(d>weight){
                        break;
                    }
                    id++;
                }
                int index = Math.min(id / pas, INTERVAL_SIZE - 1);
                Color r = QUANTIL_COLOR[index];
                distance = 1. / (Math.pow(distance, 1.5));
                somme += distance;
                resultR += r.getRed() * distance;
                resultG += r.getGreen() * distance;
                resultB += r.getBlue() * distance;
            }
            resultR = resultR / somme;
            resultG = resultG / somme;
            resultB = resultB / somme;
            //Color r= QUANTIL_COLOR[(int)result -1];
            int alpha = Math.max(120 - (int)dmin, 0);
            return new Color((int)resultR, (int)resultG, (int)resultB, alpha);
        }
        return  new Color(0,0,0);
        //        Collection<IFeature> candidates = features.select(geom,250);
        //        if(!candidates.isEmpty()){
        //          //sinon on floute
        //            double dmin = Double.MAX_VALUE;
        //            double result = 0.;
        //            double somme = 0.;
        //            for (IFeature ptf : candidates){
        //                double distance = ptf.getGeom().distance(geom);
        //                if(distance<dmin){
        //                    dmin = distance;
        //                } 
        //                double weight = mappingValuesFeatures.get(ptf);
        //                distance = 1. / (Math.pow(distance, 1.5));
        //                somme += distance;
        //                result += weight * distance;
        //            }
        //            result = result / somme;
        //            
        //            Color r= value2RGBQuantil(result);
        //           // Color r= value2RGB(result, min, max);
        //            if(dmin < 4){
        //                return new Color(r.getRed(), r.getGreen(), r.getBlue());
        //            }
        //            int alpha = (int)Math.max(120 - dmin, 0);
        //            return new Color(r.getRed(), r.getGreen(), r.getBlue(), alpha);
        //        }
        //        return  new Color(0,0,0);
    }



    private static Color value2RGBQuantil(double value) {


        int pas = (int) ((double) valuesSorted.size() / (double) INTERVAL_SIZE);
        int id = 0;
        for(Double d :valuesSorted){
            if(d>value){
                break;
            }
            id++;
        }
        int index = Math.min(id / pas, INTERVAL_SIZE - 1);

        return QUANTIL_COLOR[index];
    }

    private static Color value2RGB(double value, double min, double max) {
        double a = ((250./360.)/ (min - max));
        double b = - a *max;
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
    //    private static Color HSVtoRGB(double value, double min, double max) {
    //
    //        if(value == 0){
    //            return new Color(0,0,0);
    //        }
    //        double a = (4.5/ (min - max));
    //        double b = - a *max;
    //        double h = a * value + b;          
    //
    //        // H is given on [0->6] or -1. S and V are given on [0->1].
    //        // RGB are each returned on [0->1].
    //        double m, n, f;
    //        int i;
    //
    //        final double[] hsv = new double[3];
    //        final float[] rgb = new float[3];
    //
    //        hsv[0] = h;
    //        hsv[1] = 1;
    //        hsv[2] = 1;
    //
    //        if (hsv[0] == -1) {
    //            rgb[0] = rgb[1] = rgb[2] = (float)hsv[2];
    //            return new Color((int)rgb[0], (int)rgb[1], (int)rgb[2]);
    //        }
    //        i = (int) (Math.floor(hsv[0]));
    //        f = hsv[0] - i;
    //        if (i % 2 == 0)
    //            f = 1 - f; // if i is even
    //        m = hsv[2] * (1 - hsv[1]);
    //        n = hsv[2] * (1 - hsv[1] * f);
    //        switch (i) {
    //        case 6:
    //        case 0:
    //            rgb[0] = (float)hsv[2];
    //            rgb[1] = (float)n;
    //            rgb[2] = (float)m;
    //            break;
    //        case 1:
    //            rgb[0] = (float)n;
    //            rgb[1] = (float)hsv[2];
    //            rgb[2] = (float)m;
    //            break;
    //        case 2:
    //            rgb[0] = (float)m;
    //            rgb[1] = (float)hsv[2];
    //            rgb[2] = (float)n;
    //            break;
    //        case 3:
    //            rgb[0] = (float)m;
    //            rgb[1] = (float)n;
    //            rgb[2] = (float)hsv[2];
    //            break;
    //        case 4:
    //            rgb[0] = (float)n;
    //            rgb[1] = (float)m;
    //            rgb[2] = (float)hsv[2];
    //            break;
    //        case 5:
    //            rgb[0] = (float)hsv[2];
    //            rgb[1] = (float)m;
    //            rgb[2] = (float)n;
    //            break;
    //        }
    //
    //        rgb[0] *=255;
    //        rgb[1] *=255;
    //        rgb[2] *=255;
    //        return new Color((int)rgb[0], (int)rgb[1],(int) rgb[2]);
    //    }

    private static void init_quantil_colors(){
        QUANTIL_COLOR = new Color[INTERVAL_SIZE];
        double a = ((250./360.)/ (0 - (INTERVAL_SIZE-1)));
        double b = - a *(INTERVAL_SIZE-1);

        for(int i=0; i< INTERVAL_SIZE; i++){
            double h = a * i + b;            
            h +=0.5;
            h *=-1;
            double r = Math.sin(Math.PI * h);
            double g = Math.sin(Math.PI * (h+1./3.));
            double bl = Math.sin(Math.PI * (h + 2./3.));       
            int[] result = new int[3];
            result[0] = (int) (255 * r*r);
            result[1] = (int) (255 * g*g);
            result[2] = (int)(255 * bl*bl);
            QUANTIL_COLOR[i] =  new Color(result[0], result[1], result[2]);
        }
    }
    
    private static void generate_pngw(String out, int width, int height){
        out = out + "w";
        String s ="";
        double pixelSize = (XMAX - XMIN) / ((double)width);
        s+=pixelSize  + "\n";
        s+="0\n";
        s+="0\n";
        s+= "-" + pixelSize + "\n";
        s+= XMIN + "\n";
        s+= YMAX;
        try {
            FileWriter fr = new FileWriter(out);
            BufferedWriter br = new BufferedWriter(fr);
            br.write(s);
            br.flush();
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

}
