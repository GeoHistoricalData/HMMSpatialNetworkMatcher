package fr.ign.cogit.morphogenesis.exploring_tool.utils.interpolation;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;

import org.apache.log4j.Logger;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IEnvelope;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IPoint;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class HeatMap {


    private static double RATIO;
    private static int WIDTH;
    private static boolean UNIT = false;
    private static final double XMAX = 656578.;
    private static final double XMIN = 647298;

    private static final double YMAX = 6865429;
    private static final double YMIN = 6858728;


    private static final Logger logger = Logger.getLogger(HeatMap.class);

    /**
     * @param args
     */
//    public static void main(String[] args) {
//        // Create an instance of DisplayJAI.
//
//        final BufferedImage image = HeatMap.getBufferedImageInterpolation(
//                "/home/bcostes/Bureau/tmp/edges/DESTROY_[1785.0,1790.0,1793.0,1795.0:1.0]_[1808.0,1810.0,1836.0,1839.0:1.0].shp", "w_length", 350, 2.);
//        // final int width = image.getWidth() * 4;
//        // final int height = image.getHeight() * 4;
//
//        JFrame frame = new JFrame("Test");
//        frame.setLayout(new GridLayout(1, 2));
//        final int height = image.getHeight();
//        final int width = image.getWidth();
//
//        frame.setContentPane(new JComponent() {
//            private static final long serialVersionUID = 1L;
//
//            public void paintComponent(Graphics g) {
//                Graphics2D g2d = (Graphics2D) g;
//                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
//                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//                g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
//                        RenderingHints.VALUE_RENDER_QUALITY);
//                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//                        RenderingHints.VALUE_ANTIALIAS_ON);
//
//                g2d.drawImage(image, 0, 0, 500,
//                        (int) (((double) (height) / (double) width) * 500), null);
//
//                String str = "1789";
//                g2d.setPaint(Color.red);
//                g2d.drawString(str, 500 / 2,
//                        (int) (((double) (height) / (double) width) * 500));
//            }
//        });
//
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setPreferredSize(new Dimension(850, 850));
//        frame.pack();
//        frame.setVisible(true);
//
//    }

    public static PlanarImage getRasterInterpolation(Collection<Point2D> points,
            Map<Point2D, Double> mappingFeaturesValues, IEnvelope enveloppe,
            int width, double ratio) {
        // ******************** Chargement / instanciation********************
        WIDTH = width;
        RATIO = ratio;

        // ******************** caractéristiques du raster ********************
//        if(XMAX == 0){
//            XMIN = enveloppe.minX();
//            YMIN = enveloppe.minY();
//            XMAX = enveloppe.maxX();
//            YMAX = enveloppe.maxY();
//        }
        int HEIGHT = (int) (((double) (WIDTH)) * (YMAX - YMIN) / (XMAX - XMIN)) + 1;

        // transfo global => local
        double ax = (XMAX - XMIN) / ((double) WIDTH);
        double bx = XMIN;
        double ay = (YMIN - YMAX) / ((double) HEIGHT);
        double by = YMAX;

        // ************************* Interpolation ***************************

        float[] imageData = new float[WIDTH * HEIGHT];
        // Image data array.
        int count = 0;
        // Auxiliary counter.
        for (int h = 0; h < HEIGHT; h++)
            // Fill the array with a degradé pattern.

            for (int w = 0; w < WIDTH; w++)

            {
                Point2D pt = new Point2D.Double(ax * w + bx, ay * h + by);
                imageData[count++] = (float) HeatMap.interpole_point(pt,
                        points, mappingFeaturesValues);
            }

        // **************************** création du raster ********************
        // Create a DataBuffer from the values on the image array.
        javax.media.jai.DataBufferFloat dbuffer = new javax.media.jai.DataBufferFloat(
                imageData, WIDTH * HEIGHT);
        // Create a float data sample model.
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(
                DataBuffer.TYPE_FLOAT, WIDTH, HEIGHT, 1);
        // Create a compatible ColorModel.
        ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
        // Create a WritableRaster.
        Raster raster = RasterFactory.createWritableRaster(sampleModel, dbuffer,
                new Point(0, 0));
        // Create a TiledImage using the float SampleModel.
        TiledImage tiledImage = new TiledImage(0, 0, WIDTH, HEIGHT, 0, 0,
                sampleModel, colorModel);
        // Set the data of the tiled image to be the raster.
        tiledImage.setData(raster);
        return tiledImage;
    }

    public static BufferedImage getBufferedImageInterpolation(
            Collection<Point2D> points, Map<Point2D, Double> mappingFeaturesValues,
            IEnvelope enveloppe, int width, double alpha) {
        float[] imageData = getDataInterpolation(points, mappingFeaturesValues,
                enveloppe, width, alpha);
        List<Float> l = new ArrayList<Float>();
        for (float f : imageData) {
            l.add(f);
        }
        Collections.sort(l);
        float min = l.get(0);
        float max = l.get(l.size() - 1);
        float a = 1f / (max - min);
        float b = -a * min;
        float[] newImageData = new float[imageData.length];
        int cpt = 0;
        for (Float f : imageData) {
            newImageData[cpt] = a * f + b;
            cpt++;
        }
        javax.media.jai.DataBufferFloat dbuffer = new javax.media.jai.DataBufferFloat(
                newImageData, newImageData.length);
        // Create a float data sample model.
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(
                DataBuffer.TYPE_FLOAT, 350, newImageData.length / WIDTH, 1);
        // Create a compatible ColorModel.
        ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
        // Create a WritableRaster.
        Raster raster = RasterFactory.createWritableRaster(sampleModel, dbuffer,
                new Point(0, 0));
        // Create a TiledImage using the float SampleModel.
        TiledImage imageP = new TiledImage(0, 0, 350, newImageData.length / WIDTH,
                0, 0, sampleModel, colorModel);
        // Set the data of the tiled image to be the raster.
        imageP.setData(raster);
        final BufferedImage image = imageP.getAsBufferedImage();
        return image;
    }

    public static float[] getDataInterpolation(Collection<Point2D> points,
            Map<Point2D, Double> mappingFeaturesValues, IEnvelope enveloppe,
            int width, double ratio) {
        // ******************** Chargement / instanciation********************
        WIDTH = width;
        RATIO = ratio;

        // ******************** caractéristiques du raster ********************
//        double xmin = enveloppe.minX();
//        double ymin = enveloppe.minY();
//        double xmax = enveloppe.maxX();
//        double ymax = enveloppe.maxY();
        int HEIGHT = (int) (((double) (WIDTH)) * (YMAX - YMIN) / (XMAX - XMIN)) + 1;

        // transfo global => local
        double ax = (XMAX - XMIN) / ((double) WIDTH);
        double bx = XMIN;
        double ay = (YMIN - YMAX) / ((double) HEIGHT);
        double by = YMAX;


        // ************************* Interpolation ***************************

        float[] imageData = new float[WIDTH * HEIGHT];
        // Image data array.
        int count = 0;
        // Auxiliary counter.
        for (int h = 0; h < HEIGHT; h++)
            // Fill the array with a degradé pattern.

            for (int w = 0; w < WIDTH; w++)

            {
                Point2D pt = new Point2D.Double(ax * w + bx, ay * h + by);
                imageData[count++] = (float) HeatMap.interpole_point(pt,
                        points, mappingFeaturesValues);
            }

        return imageData;
    }

    public static BufferedImage getBufferedImageInterpolation(String shp,
            String weightAttribute, int width, double alpha) {
        if(weightAttribute == null){
            UNIT = true;
        }
        else{
            UNIT = false;
        }
        float[] imageData = getDataInterpolation(shp, weightAttribute, width, alpha);
        List<Float> l = new ArrayList<Float>();
        for (float f : imageData) {
            l.add(f);
        }
        Collections.sort(l);
        float min = l.get(0);
        float max = l.get(l.size() - 1);
        float a = 1f / (max - min);
        float b = -a * min;
        float[] newImageData = new float[imageData.length];
        int cpt = 0;
        for (Float f : imageData) {
            newImageData[cpt] = a * f + b;
            cpt++;
        }
        javax.media.jai.DataBufferFloat dbuffer = new javax.media.jai.DataBufferFloat(
                newImageData, newImageData.length);
        // Create a float data sample model.
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(
                DataBuffer.TYPE_FLOAT, 350, newImageData.length / WIDTH, 1);
        // Create a compatible ColorModel.
        ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
        // Create a WritableRaster.
        Raster raster = RasterFactory.createWritableRaster(sampleModel, dbuffer,
                new Point(0, 0));
        // Create a TiledImage using the float SampleModel.
        TiledImage imageP = new TiledImage(0, 0, 350, newImageData.length / WIDTH,
                0, 0, sampleModel, colorModel);
        // Set the data of the tiled image to be the raster.
        imageP.setData(raster);
        final BufferedImage image = imageP.getAsBufferedImage();
        return image;
    }

    public static PlanarImage getRasterInterpolation(String shp,
            String weightAttribute, int width, double ratio) {
        // ******************** Chargement / instanciation********************
        WIDTH = width;
        RATIO = ratio;
        IPopulation<IFeature> features = ShapefileReader.read(shp);
        features.initSpatialIndex(Tiling.class, false);

        // ******************** Vérification ********************

        for (IFeature f : features) {
            if (f.getAttribute(weightAttribute) == null) {
                logger.error("Pas d'attribut " + weightAttribute
                        + " pour ce feature : " + f.getGeom().toString());
                System.exit(-1000);
            }
            try {
                @SuppressWarnings("unused")
                double d = Double.parseDouble(f.getAttribute(weightAttribute)
                        .toString());
            } catch (NumberFormatException e) {
                logger.error("Conversion de " + f.getAttribute(weightAttribute)
                        + " en Double impossible");
                e.printStackTrace();
                System.exit(-1000);
            }
        }

        // ******************** caractéristiques du raster ********************
//        IEnvelope env = features.getEnvelope();
//        double xmin = env.minX();
//        double ymin = env.minY();
//        double xmax = env.maxX();
//        double ymax = env.maxY();
        int HEIGHT = (int) (((double) (WIDTH)) * (YMAX - YMIN) / (XMAX - XMIN)) + 1;

        // transfo global => local
        double ax = (XMAX - XMIN) / ((double) WIDTH);
        double bx = XMIN;
        double ay = (YMIN - YMAX) / ((double) HEIGHT);
        double by = YMAX;

        // ************************* Pré traitement ***************************
        Map<IFeature, Double> mappingValuesFeatures = new HashMap<IFeature, Double>();
        for (IFeature f : features) {
            mappingValuesFeatures.put(f,
                    Double.parseDouble(f.getAttribute(weightAttribute).toString()));
        }

        // ************************* Interpolation ***************************

        float[] imageData = new float[WIDTH * HEIGHT];
        // Image data array.
        int count = 0;
        // Auxiliary counter.
        for (int h = 0; h < HEIGHT; h++)
            // Fill the array with a degradé pattern.

            for (int w = 0; w < WIDTH; w++)

            {
                Point2D pt = new Point2D.Double(ax * w + bx, ay * h + by);
                imageData[count++] = (float) HeatMap.interpole_point(pt,
                        features, mappingValuesFeatures);
            }

        // **************************** création du raster ********************
        // Create a DataBuffer from the values on the image array.
        javax.media.jai.DataBufferFloat dbuffer = new javax.media.jai.DataBufferFloat(
                imageData, WIDTH * HEIGHT);
        // Create a float data sample model.
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(
                DataBuffer.TYPE_FLOAT, WIDTH, HEIGHT, 1);
        // Create a compatible ColorModel.
        ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
        // Create a WritableRaster.
        Raster raster = RasterFactory.createWritableRaster(sampleModel, dbuffer,
                new Point(0, 0));
        // Create a TiledImage using the float SampleModel.
        TiledImage tiledImage = new TiledImage(0, 0, WIDTH, HEIGHT, 0, 0,
                sampleModel, colorModel);
        // Set the data of the tiled image to be the raster.
        tiledImage.setData(raster);
        return tiledImage;
    }

    public static float[] getDataInterpolation(String shp,
            String weightAttribute, int width, double ratio) {
        // ******************** Chargement / instanciation********************
        WIDTH = width;
        RATIO = ratio;
        IPopulation<IFeature> features = ShapefileReader.read(shp);
        features.initSpatialIndex(Tiling.class, false);

        // ******************** Vérification ********************

        for (IFeature f : features) {
            if (f.getAttribute(weightAttribute) == null) {
                logger.error("Pas d'attribut " + weightAttribute
                        + " pour ce feature : " + f.getGeom().toString());
                System.exit(-1000);
            }
            try {
                @SuppressWarnings("unused")
                double d = Double.parseDouble(f.getAttribute(weightAttribute)
                        .toString());
            } catch (NumberFormatException e) {
                logger.error("Conversion de " + f.getAttribute(weightAttribute)
                        + " en Double impossible");
                e.printStackTrace();
                System.exit(-1000);
            }
        }

        // ******************** caractéristiques du raster ********************
//        IEnvelope env = features.getEnvelope();
//        double xmin = env.minX();
//        double ymin = env.minY();
//        double xmax = env.maxX();
//        double ymax = env.maxY();
        int HEIGHT = (int) (((double) (WIDTH)) * (YMAX - YMIN) / (XMAX - XMIN)) + 1;

        // transfo global => local
        double ax = (XMAX - XMIN) / ((double) WIDTH);
        double bx = XMIN;
        double ay = (YMIN - YMAX) / ((double) HEIGHT);
        double by = YMAX;


        // ************************* Pré traitement ***************************
        Map<IFeature, Double> mappingValuesFeatures = new HashMap<IFeature, Double>();
        for (IFeature f : features) {
            mappingValuesFeatures.put(f,
                    Double.parseDouble(f.getAttribute(weightAttribute).toString()));
        }

        // ************************* Interpolation ***************************

        float[] imageData = new float[WIDTH * HEIGHT];
        // Image data array.
        int count = 0;
        // Auxiliary counter.
        for (int h = 0; h < HEIGHT; h++)
            // Fill the array with a degradé pattern.

            for (int w = 0; w < WIDTH; w++)

            {
                Point2D pt = new Point2D.Double(ax * w + bx, ay * h + by);
                imageData[count++] = (float) HeatMap.interpole_point(pt,
                        features, mappingValuesFeatures);
            }

        return imageData;
    }

    public static void writeRasterInterpolation(String shp, String output,
            String weightAttribute, int width, double ratio) {
        if(weightAttribute == null){
            UNIT =true;
        }
        else{
            UNIT = false;
        }
        System.out.println(UNIT);
        // ******************** Chargement / instanciation********************
        WIDTH = width;
        RATIO = ratio;
        IPopulation<IFeature> features = ShapefileReader.read(shp);
        features.initSpatialIndex(Tiling.class, false);

        // ******************** Vérification ********************

        if(!UNIT){
        for (IFeature f : features) {
            if (f.getAttribute(weightAttribute) == null) {
                logger.error("Pas d'attribut " + weightAttribute
                        + " pour ce feature : " + f.getGeom().toString());
                System.exit(-1000);
            }
            try {
                @SuppressWarnings("unused")
                double d = Double.parseDouble(f.getAttribute(weightAttribute)
                        .toString());
            } catch (NumberFormatException e) {
                logger.error("Conversion de " + f.getAttribute(weightAttribute)
                        + " en Double impossible");
                e.printStackTrace();
                System.exit(-1000);
            }
        }
        }

        // ******************** caractéristiques du raster ********************
//        IEnvelope env = features.getEnvelope();
//        double xmin = env.minX();
//        double ymin = env.minY();
//        double xmax = env.maxX();
//        double ymax = env.maxY();
        int HEIGHT = (int) (((double) (WIDTH)) * (YMAX - YMIN) / (XMAX - XMIN)) + 1;
        
        // transfo global => local
        double ax = (XMAX - XMIN) / ((double) WIDTH);
        double bx = XMIN;
        double ay = (YMIN - YMAX) / ((double) HEIGHT);
        double by = YMAX;
        
        // ************************* Pré traitement ***************************
        Map<IFeature, Double> mappingValuesFeatures = new HashMap<IFeature, Double>();
        if(!UNIT){
            for (IFeature f : features) {
                mappingValuesFeatures.put(f,
                        Double.parseDouble(f.getAttribute(weightAttribute).toString()));
            }
        }

        // ************************* Interpolation ***************************

        float[] imageData = new float[WIDTH * HEIGHT];
        // Image data array.
        int count = 0;
        // Auxiliary counter.
        for (int h = 0; h < HEIGHT; h++){
            // Fill the array with a degradé pattern.
            for (int w = 0; w < WIDTH; w++)

            {
                Point2D pt = new Point2D.Double(ax * w + bx, ay * h + by);
                imageData[count] = (float) HeatMap.interpole_point(pt,
                        features, mappingValuesFeatures);
                count++;
            }
        }

        // **************************** création du raster ********************
        // Create a DataBuffer from the values on the image array.
        javax.media.jai.DataBufferFloat dbuffer = new javax.media.jai.DataBufferFloat(
                imageData, WIDTH * HEIGHT);
        // Create a float data sample model.
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(
                DataBuffer.TYPE_FLOAT, WIDTH, HEIGHT, 1);
        // Create a compatible ColorModel.
        ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
        // Create a WritableRaster.
        Raster raster = RasterFactory.createWritableRaster(sampleModel, dbuffer,
                new Point(0, 0));
        // Create a TiledImage using the float SampleModel.
        TiledImage tiledImage = new TiledImage(0, 0, WIDTH, HEIGHT, 0, 0,
                sampleModel, colorModel);
        // Set the data of the tiled image to be the raster.
        tiledImage.setData(raster);
        // Save the image on a file.
        JAI.create("filestore", tiledImage, output, "TIFF");
    }

    public static double interpole_point(Point2D pt,
            IPopulation<IFeature> features,
            Map<IFeature, Double> mappingValuesFeatures) {
        double result = 0;
        for (IFeature f : features) {
            if(f.getGeom() instanceof IPoint){
                IDirectPosition ptf = f.getGeom().coord().get(0);
                double distance = Math.sqrt((pt.getX() - ptf.getX())
                        * (pt.getX() - ptf.getX()) + (pt.getY() - ptf.getY())
                        * (pt.getY() - ptf.getY()));
                if (distance < RATIO) {
                    double tmp = distance / RATIO;
                    //application du noyau : bi-pondération (quartil)
                    tmp = (15.0/16.0)*(1.-tmp*tmp)*(1-tmp*tmp);
                    if(!UNIT){
                    double weight = mappingValuesFeatures.get(f);
                    tmp *= weight; 
                    }
                    result += tmp;
                } else {
                    continue;
                }
            }
            else if(f.getGeom() instanceof ILineString || (f.getGeom() instanceof IMultiCurve<?>)){
                double distance  = f.getGeom().distance(new GM_Point(new DirectPosition(pt.getX(), pt.getY())));
                if (distance < RATIO) {
                    double tmp = distance / RATIO;
                    //application du noyau : bi-pondération (quartil)
                    tmp = (15.0/16.0)*(1.-tmp*tmp)*(1-tmp*tmp);
                    if(!UNIT){
                    double weight = mappingValuesFeatures.get(f);
                    tmp *= weight; 
                    }
                    result += tmp;
                } else {
                    continue;
                }
            }
        }
        return result;
    }

    public static double interpole_point(Point2D pt,
            Collection<Point2D> features, Map<Point2D, Double> mappingValuesFeatures) {
        double somme = 0;
        double result = 0;
        for (Point2D f : mappingValuesFeatures.keySet()) {
            double distance = Math.sqrt((pt.getX() - f.getX())
                    * (pt.getX() - f.getX()) + (pt.getY() - f.getY())
                    * (pt.getY() - f.getY()));
            if (distance < RATIO) {
                double tmp = distance / RATIO;
                //application du noyau : bi-pondération (quartil)
                tmp = (15.0/16.0)*(1.-tmp*tmp)*(1-tmp*tmp);
                double weight = mappingValuesFeatures.get(f);
                tmp *= weight; 
                result += tmp;
            } else {
                continue;
            }

        }

        result = result / somme;
        return result;
    }

}
