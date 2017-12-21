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

import org.apache.log4j.Logger;
import org.jdesktop.swingx.image.GaussianBlurFilter;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IEnvelope;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public abstract class AbstractSHP2Raster {


    private double XMAX;
    private double XMIN;
    private double YMAX;
    private double YMIN;
    private double minValue;
    private double maxValue;
    private int width = 0;
    private int height;
    private String srcOutputImg;
    private String inputShp;
    private String weightAttribute;
    private double bufSize;
    private double alpha;
    private int objectSize;
    private Map<IFeature, Double> mappingValuesFeatures ;
    private IPopulation<IFeature> features;
    private int gaussianBlurRadius = -1;
    private AbstractSHP2Raster.FORMAT format;

    private static final Logger logger = Logger.getLogger(AbstractSHP2Raster.class);

    public static enum FORMAT{
        TIF,
        PNG;
    }

    //*********************************************************************************
    //***************************** Constructeurs *************************************
    //*********************************************************************************

    public AbstractSHP2Raster(String inputShp, String weightAttribute, String outputImgSrc, AbstractSHP2Raster.FORMAT format, double bufSize){
        this.inputShp = inputShp;
        this.weightAttribute = weightAttribute;
        this.srcOutputImg = outputImgSrc;
        this.mappingValuesFeatures = new HashMap<IFeature, Double>();
        this.bufSize = bufSize;
        this.alpha = 1.5;
        this.objectSize = 4;
        this.gaussianBlurRadius = -1;

        this.init();
    }
    
    public AbstractSHP2Raster(String inputShp, String weightAttribute, String outputImgSrc, AbstractSHP2Raster.FORMAT format, int width, double bufSize){
        this.inputShp = inputShp;
        this.width = width;
        this.weightAttribute = weightAttribute;
        this.srcOutputImg = outputImgSrc;
        this.mappingValuesFeatures = new HashMap<IFeature, Double>();
        this.bufSize = bufSize;
        this.alpha = 1.5;
        this.objectSize = 4;
        this.gaussianBlurRadius = -1;

        this.init();
    }

    public AbstractSHP2Raster(String inputShp, String weightAttribute, String outputImgSrc, AbstractSHP2Raster.FORMAT format, double bufSize, int width, double alpha){
        this.inputShp = inputShp;
        this.weightAttribute = weightAttribute;
        this.srcOutputImg = outputImgSrc;
        this.mappingValuesFeatures = new HashMap<IFeature, Double>();
        this.bufSize = bufSize;
        this.width = width;
        this.alpha = alpha;
        this.objectSize = 4;
        this.gaussianBlurRadius = -1;

        this.init();
    }

    public AbstractSHP2Raster(String inputShp, String weightAttribute, String outputImgSrc, AbstractSHP2Raster.FORMAT format, double bufSize, int objectSizeInPixel){
        this.inputShp = inputShp;
        this.weightAttribute = weightAttribute;
        this.srcOutputImg = outputImgSrc;
        this.mappingValuesFeatures = new HashMap<IFeature, Double>();
        this.bufSize = bufSize;
        this.alpha = 1.5;
        this.objectSize = objectSizeInPixel;
        this.gaussianBlurRadius = -1;

        this.init();
    }

    public AbstractSHP2Raster(String inputShp, String weightAttribute, String outputImgSrc, AbstractSHP2Raster.FORMAT format, double bufSize, double alpha, int objectSize, int gaussianBlurRadius){
        this.inputShp = inputShp;
        this.weightAttribute = weightAttribute;
        this.srcOutputImg = outputImgSrc;
        this.mappingValuesFeatures = new HashMap<IFeature, Double>();
        this.bufSize = bufSize;
        this.alpha = alpha;
        this.objectSize = objectSize;
        this.gaussianBlurRadius = gaussianBlurRadius;

        this.init();
    }

    //*********************************************************************************
    //************************ Méthodes abstraites ************************************
    //*********************************************************************************

    protected abstract Color value2Color(double value);
    protected abstract Color getOutlyingPixelsColor(Collection<IFeature> candidates, IGeometry geom);

    //*********************************************************************************
    //***************************** Méthodes ******************************************
    //*********************************************************************************

    protected void init(){
        this.features = ShapefileReader.read(this.getInputShp());
        this.getFeatures().initSpatialIndex(Tiling.class, false);
        IEnvelope env = this.getFeatures().getEnvelope();
        this.XMIN = env.getLowerCorner().getX();
        this.YMIN = env.getLowerCorner().getY();
        this.XMAX = env.getUpperCorner().getX();
        this.YMAX = env.getUpperCorner().getY();
        
        //si on a pas renseigné la largeur en pixel on lui donne une valeur par défaut
        // 1px = 5m
        if(this.getWidth() == 0){
            this.width = (int)((this.getXMAX() - this.getXMIN()) / 5);
        }

        for (IFeature f :  this.getFeatures()) {
            if(f.getAttribute(this.getWeightAttribute()) == null){
                logger.warn(this.getWeightAttribute() + " is not a valid feature attribute. Process aborted.");
                return;
            }
            this.getMappingValuesFeatures().put(f,
                    Double.parseDouble(f.getAttribute(this.getWeightAttribute()).toString()));
        }
        List<Double> l = new ArrayList<Double>();
        l.addAll(mappingValuesFeatures.values());
        Collections.sort(l);
        this.minValue = l.get(0);
        this.maxValue =l.get(l.size()-1);
        l.clear();
        l = null;
        this.height = (int) (((double) (width)) * (YMAX - YMIN) / (XMAX - XMIN)) + 1;
    }

    public void convert(){
        long t0= System.currentTimeMillis();

        // transfo global => local
        double ax = (this.getXMAX() - this.getXMIN()) / ((double) this.getWidth());
        double bx = this.getXMIN();
        double ay = (this.getYMIN() - this.getYMAX()) / ((double) this.getHeight());
        double by = this.getYMAX();

        Iterator<?> writers = ImageIO.getImageWritersByFormatName(this.getStringFormat());
        ImageWriter writer = (ImageWriter)writers.next();

        File f = new File(this.getSrcOutputImg());
        ImageOutputStream ios;
        try {

            ios = ImageIO.createImageOutputStream(f);
            writer.setOutput(ios);
            BufferedImage bi = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
            // Auxiliary counter.
            int cpt=0;
            int size  = this.getHeight() * this.getWidth() / 100;
            for (int h = 0; h < this.getHeight(); h++)
                // Fill the array with a degradé pattern.
                for (int w = 0; w < this.getWidth(); w++)
                {
                    IDirectPosition pt = new DirectPosition(ax * w + bx, ay * h + by);
                    Color rgba = this.kneighbor(pt);
                    bi.setRGB(w, h, rgba.getRGB());
                    cpt++;
                    if(cpt%size == 0){
                        if(logger.isInfoEnabled()){
                            logger.info(cpt / size  + "%");
                        }
                    }
                }
            // filtre gaussien de deux pixels
            if(this.getGaussianBlurRadius() != -1){
                GaussianBlurFilter gfilter = new GaussianBlurFilter(this.getGaussianBlurRadius());
                BufferedImage bfiltered = gfilter.filter(bi, null);
                BufferedImage bfinal = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
                bfinal.setData(bfiltered.getRaster());
                writer.write(bfinal);
            }
            else{
                writer.write(bi);
            }
            this.generate_fileW();
            long t= System.currentTimeMillis();
            if(logger.isInfoEnabled()){
                logger.info("Process terminated in " + (t-t0) + " ms");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected Color kneighbor(IDirectPosition pt) {
        IGeometry geom = new GM_Point(pt);
        Collection<IFeature> candidates = this.getFeatures().select(geom, this.getObjectSize());
        if(!candidates.isEmpty()){
            candidates = this.getFeatures().select(geom, 4 * this.getObjectSize());
            //on regarde si on a qu'un seul candidat proche
            if(candidates.size() == 1){
                Color r= this.value2Color(this.getMappingValuesFeatures().get(candidates.iterator().next()));
                return r;
            }
            //plusieurs candidats => on va interpoler
            double result = 0.;
            double somme = 0.;
            for (IFeature ptf : candidates){
                double distance = ptf.getGeom().distance(geom);
                double weight = this.getMappingValuesFeatures().get(ptf);
                distance = 1. / (Math.pow(distance, this.getAlpha()));
                somme += distance;
                result += weight * distance;
            }
            result = result / somme;
            Color r= value2Color(this.getMappingValuesFeatures().get(candidates.iterator().next()));
            return r;
        }
        candidates = this.getFeatures().select(geom, this.getBufSize());
        if(!candidates.isEmpty()){
            return this.getOutlyingPixelsColor(candidates, geom);
        }
        return  new Color(0,0,0);
    }


    /**
     * Génère un fichier pngw ou tfw de géoérérférencement
     */
    protected void generate_fileW(){
        String out = this.getSrcOutputImg()+ "w";
        String s ="";
        double pixelSizeX = (this.getXMAX() - this.getXMIN()) / ((double)this.getWidth());
        double pixelSizeY = (this.getYMAX() - this.getYMIN()) / ((double)this.getHeight());
        s+=pixelSizeX  + "\n";
        s+="0\n";
        s+="0\n";
        s+= "-" + pixelSizeY + "\n";
        s+= this.getXMIN() + "\n";
        s+= this.getYMAX();
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

    //*********************************************************************************
    //***************************** Accesseurs ****************************************
    //*********************************************************************************

    public double getXMAX() {
        return XMAX;
    }

    public double getXMIN() {
        return XMIN;
    }

    public double getYMAX() {
        return YMAX;
    }

    public double getYMIN() {
        return YMIN;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getSrcOutputImg() {
        return srcOutputImg;
    }

    public double getBufSize() {
        return bufSize;
    }

    public double getAlpha() {
        return alpha;
    }

    public int getObjectSize() {
        return objectSize;
    }

    public Map<IFeature, Double> getMappingValuesFeatures() {
        return mappingValuesFeatures;
    }

    public IPopulation<IFeature> getFeatures() {
        return features;
    }

    public int getGaussianBlurRadius() {
        return gaussianBlurRadius;
    }

    public String getInputShp() {
        return inputShp;
    }

    public String getWeightAttribute() {
        return weightAttribute;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public AbstractSHP2Raster.FORMAT getFormat() {
        return format;
    }

    public String getStringFormat() {
        if(this.getFormat() == AbstractSHP2Raster.FORMAT.PNG){
            return "png";
        }
        else{
            return "tif";
        }
    }
}
