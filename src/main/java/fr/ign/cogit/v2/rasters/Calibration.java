package fr.ign.cogit.v2.rasters;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.jdesktop.swingx.image.GaussianBlurFilter;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;

public class Calibration {

    public static void main(String args[]){
        String output = "/home/bcostes/Bureau/calibration3.png";
        
        
        
        Iterator<?> writers = ImageIO.getImageWritersByFormatName("png");
        ImageWriter writer = (ImageWriter)writers.next();

        File f = new File(output);
        ImageOutputStream ios;
        try {

            ios = ImageIO.createImageOutputStream(f);
            writer.setOutput(ios);
            BufferedImage bi = new BufferedImage(200,10, BufferedImage.TYPE_INT_ARGB);
            // Auxiliary counter.
//            int cpt=0;
//            for(double dr = 0; dr<0.5; dr += 0.05){
//                for(double dg = 0; dg<0.5; dg += 0.05){
//                    for(double db = 0; db<0.5; db += 0.05){
                        for (int h = 0; h < 10; h++){
                            //cpt++;
                            // Fill the array with a degradé pattern.
                            for (int w = 0; w < 200; w++)
                            {
//                                double a = ((1.- 0.0)/ (200. - 0.));
//                                double b = 0.0 -  a * 0.;
//                                double hh = a * w + b;            
//                               // h +=0.5;
//                                //h *=-1;
//                                double r = Math.sin(Math.PI * (hh+dr) );
//                                double g = Math.sin(Math.PI * (hh+dg));
//                                double bl = Math.sin(Math.PI * (hh +db));       
//                                int[] result = new int[3];
//                                result[0] = (int) (255 * r*r);
//                                result[1] = (int) (255 * g*g);
//                                result[2] = (int)(255 * bl*bl);
                                Color rgba = Calibration.value2Color(w);
                                bi.setRGB(w, h, rgba.getRGB());
                                
                            }
                        }
//                        for (int h = 0; h < 3; h++){
//                            cpt++;
//                            // Fill the array with a degradé pattern.
//                            for (int w = 0; w < 200; w++)
//                            {
//                                Color rgba = new Color(0,0,0);
//                                bi.setRGB(w, cpt, rgba.getRGB());
//                                
//                            }
//                        }
//                    }
//                }
 //           }
            
            // filtre gaussien de deux pixels
          
                writer.write(bi);
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static Color value2Color(double value) {
//        double a = ((1.- 0.0)/ (200. - 0.));
//        double b = 0.0 -  a * 0.;
//        double h = a * value + b;            
//       // h +=0.5;
//        //h *=-1;
//        double r = Math.sin(Math.PI * (h+0.1) );
//        double g = Math.sin(Math.PI * (h+0.2));
//        double bl = Math.sin(Math.PI * (h +0.4));       
//        int[] result = new int[3];
//        result[0] = (int) (255 * r*r);
//        result[1] = (int) (255 * g*g);
//        result[2] = (int)(255 * bl*bl);
//        return new Color(result[0], result[1], result[2]);
        
        
//        double a = ((0.83- 0.15)/ (200. - 0.));
//        double b = 0.15 -  a * 0.;
//        double h = a * value + b;            
//       // h +=0.5;
//        //h *=-1;
//        double r = Math.sin(Math.PI * (h) );
//        double g = Math.sin(Math.PI * (h+0.1));
//        double bl = Math.sin(Math.PI * (h +0.45));       
//        int[] result = new int[3];
//        result[0] = (int) (255 * r*r);
//        result[1] = (int) (255 * g*g);
//        result[2] = (int)(255 * bl*bl);
//        return new Color(result[0], result[1], result[2]);
    
    
        double a = ((0.77- 0.1)/ (200. - 0.));
        double b = 0.1 -  a * 0.;
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
    

    
}
