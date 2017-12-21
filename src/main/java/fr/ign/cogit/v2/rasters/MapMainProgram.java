package fr.ign.cogit.v2.rasters;

public class MapMainProgram {

    public static void main(String[] args) {
      
        String inputFile ="/home/bcostes/Bureau/tmp/betweenness.shp";
        String outputImg ="/home/bcostes/Bureau//betweenness_values_new.png";
        String weightAttribute ="betweennes";
        
        AbstractSHP2Raster rasterMaker = new SHP2RasterQuantilClassification(inputFile, weightAttribute, 
                outputImg, AbstractSHP2Raster.FORMAT.PNG, 250., 1500);
        
       // AbstractSHP2Raster rasterMaker = new SHP2RasterValuesClassification(inputFile, weightAttribute, 
       //                outputImg, AbstractSHP2Raster.FORMAT.PNG, 250., 10000);
        
//        AbstractSHP2Raster rasterMaker = new SHP2RasterMeanInterpolation(inputFile, weightAttribute, 
//                outputImg, AbstractSHP2Raster.FORMAT.PNG, 250., 1000);
        
        rasterMaker.convert();

    }

}
