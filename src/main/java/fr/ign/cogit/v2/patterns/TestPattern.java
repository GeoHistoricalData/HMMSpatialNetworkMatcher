package fr.ign.cogit.v2.patterns;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.feature.Population;

public class TestPattern {

    public static void main(String args[]) {


        IPopulation<IFeature> pop = new Population<IFeature>();

        // STGraph stg = TAGIoManager.deserialize("/home/bcostes/Bureau/TAG/etape4/tag_new.tag");
        //
        //
        //        ReincarnationSTPattern pattern = new ReincarnationSTPattern();       
        //        for(STEntity edge : stg.getEdges()){
        //            TimeSerie ts = edge.getTimeSerie();
        //            ts.asList().remove(new TemporalInterval(1884, 1885, 1888, 1889));
        //            if(pattern.find(ts)){
        //                // System.out.println("Réincarnation ! ");
        //                List<TemporalInterval> times = pattern.findEvent(edge.getTimeSerie());
        //                DefaultFeature f = new DefaultFeature(edge.getGeometry().toGeoxGeometry());
        //                AttributeManager.addAttribute(f, "death", times.get(0).toString(), "String");
        //                AttributeManager.addAttribute(f, "re-live", times.get(1).toString(), "String");
        //                pop.add(f);
        //            }
        //        }
        //
        //        ShapefileWriter.write(pop, "/home/bcostes/Bureau/TAG/patterns/reincarnations.shp");
        //        pop.clear();
        //        
        //        
        //        AppearanceSTPattern pattern2 = new AppearanceSTPattern();       
        //        for(STEntity edge : stg.getEdges()){
        //            TimeSerie ts = edge.getTimeSerie();
        //            ts.asList().remove(new TemporalInterval(1884, 1885, 1888, 1889));
        //            if(pattern2.find(ts)){
        //                List<TemporalInterval> times = pattern2.findEvent(ts);
        //                DefaultFeature f = new DefaultFeature(edge.getGeometry().toGeoxGeometry());
        //                AttributeManager.addAttribute(f, "live", times.get(0).toString(), "String");
        //                AttributeManager.addAttribute(f, "death", times.get(1).toString(), "String");
        //                pop.add(f);
        //            }        
        //        }      
        //        ShapefileWriter.write(pop, "/home/bcostes/Bureau/TAG/patterns/appearances.shp");
        //        pop.clear();
        //
        //
        //
        //        LifeSTPattern patternLife = new LifeSTPattern();       
        //        for(STEntity edge : stg.getEdges()){
        //            TimeSerie ts = edge.getTimeSerie();
        //            ts.asList().remove(new TemporalInterval(1884, 1885, 1888, 1889));
        //            if(patternLife.find(ts)){
        //                // System.out.println("Réincarnation ! ");
        //                TemporalInterval time = patternLife.findEvent(edge.getTimeSerie());
        //                DefaultFeature f = new DefaultFeature(edge.getGeometry().toGeoxGeometry());
        //                AttributeManager.addAttribute(f, "life", time.toString(), "String");
        //                pop.add(f);
        //
        //            }
        //        }
        //        ShapefileWriter.write(pop, "/home/bcostes/Bureau/TAG/patterns/life.shp");
        //        pop.clear();
        //        
        //        DeathSTPattern patternDeath = new DeathSTPattern();       
        //        for(STEntity edge : stg.getEdges()){
        //            TimeSerie ts = edge.getTimeSerie();
        //            ts.asList().remove(new TemporalInterval(1884, 1885, 1888, 1889));
        //            if(patternDeath.find(ts)){
        //                TemporalInterval time = patternDeath.findEvent(ts);
        //                DefaultFeature f = new DefaultFeature(edge.getGeometry().toGeoxGeometry());
        //                AttributeManager.addAttribute(f, "death", time.toString(), "String");
        //                pop.add(f);
        //
        //            }
        //        }
        //        ShapefileWriter.write(pop, "/home/bcostes/Bureau/TAG/patterns/death.shp");
        //        pop.clear();
        //        
        //        
        //        FullStabilitySTPattern patternStablity = new FullStabilitySTPattern();       
        //        for(STEntity edge : stg.getEdges()){
        //            TimeSerie ts = edge.getTimeSerie();
        //            ts.asList().remove(new TemporalInterval(1884, 1885, 1888, 1889));
        //            if(patternStablity.find(ts)){
        //                DefaultFeature f = new DefaultFeature(edge.getGeometry().toGeoxGeometry());
        //                pop.add(f);
        //
        //            }
        //        }
        //        ShapefileWriter.write(pop, "/home/bcostes/Bureau/TAG/patterns/stability.shp");

        //        List<TemporalInterval> times = new ArrayList<TemporalInterval>();
        //        times.add(new TemporalInterval(1884, 1885, 1888, 1889));
        //        
        //                List<Pair<STEntity>> patternsClose = PatternManager.lookForSemiClosePattern(stg, new ReincarnationSTPattern(), new AppearanceSTPattern(), 1, 80., times, STEntity.EDGE);
        //                patternsClose.addAll(PatternManager.lookForSemiClosePattern(stg, new ReincarnationSTPattern(), new LifeSTPattern(), 1, 80.,times, STEntity.EDGE));
        //                patternsClose.addAll(PatternManager.lookForSemiClosePattern(stg, new ReincarnationSTPattern(), new DeathSTPattern(), 1, 80.,times, STEntity.EDGE));
        //                patternsClose.addAll(PatternManager.lookForSemiClosePattern(stg, new AppearanceSTPattern(), new LifeSTPattern(), 1,80., times, STEntity.EDGE));
        //                patternsClose.addAll(PatternManager.lookForSemiClosePattern(stg, new AppearanceSTPattern(), new DeathSTPattern(), 1, 80.,times, STEntity.EDGE));
        //                patternsClose.addAll(PatternManager.lookForSemiClosePattern(stg, new LifeSTPattern(), new DeathSTPattern(), 1, 80.,times, STEntity.EDGE));
        //
        //
        //
        //        PatternManager.closePattern2Shp(patternsClose, "/home/bcostes/Bureau/tmp/semi_close.shp");




        //        List<TemporalInterval>times = stg.getTemporalDomain().asList();
        //        Collections.sort(times);
        //        times.remove(new TemporalInterval(1884, 1885, 1888, 1889));
        //        
        //        for(int i=0; i< times.size()-1; i++){
        //            System.out.println("-----------");
        //            System.out.println(times.get(i));
        //            System.out.println(times.get(i+1));
        //            IPopulation<IFeature>outNew = new Population<IFeature>();
        //            IPopulation<IFeature>outDestroy = new Population<IFeature>();
        //            IPopulation<IFeature>outStable = new Population<IFeature>();
        //
        //            int newE = 0, destroy = 0, stable = 0;
        //            for(STEntity e : stg.getEdges()){
        //                if(e.existsAt(times.get(i)) && !e.existsAt(times.get(i+1)) ){
        //                    IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
        //                    AttributeManager.addAttribute(f, "w_unit", 1., "Double");
        //                    AttributeManager.addAttribute(f, "w_length", f.getGeom().length(), "Double");
        //                    AttributeManager.addAttribute(f, "w_degree", stg.getIncidentCount(e), "Double");
        //
        //                    outDestroy.add(f);
        //                    destroy++;
        //                }
        //                else if(!e.existsAt(times.get(i)) && e.existsAt(times.get(i+1)) ){
        //                    IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
        //                    AttributeManager.addAttribute(f, "w_unit", 1., "Double");
        //                    AttributeManager.addAttribute(f, "w_length", f.getGeom().length(), "Double");
        //                    AttributeManager.addAttribute(f, "w_degree", stg.getIncidentCount(e), "Double");
        //
        //                    outNew.add(f);
        //                    newE++;
        //                }
        //                else if(e.existsAt(times.get(i)) && e.existsAt(times.get(i+1))){
        //                    IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
        //                    AttributeManager.addAttribute(f, "w_unit", 1., "Double");
        //                    AttributeManager.addAttribute(f, "w_length", f.getGeom().length(), "Double");
        //                    AttributeManager.addAttribute(f, "w_degree", stg.getIncidentCount(e), "Double");
        //
        //                    outStable.add(f);
        //                    stable++;
        //                }
        //            }
        //            System.out.println(newE+ " "+destroy+" " +stable);
        //            File f = new File("/home/bcostes/Bureau/tmp/edges");
        //            if(!f.exists()){
        //                f.mkdir();
        //            }
        //            ShapefileWriter.write(outNew, f.toString()+"/NEW_" + times.get(i).toString()+"_"+times.get(i+1).toString()+".shp");
        //            ShapefileWriter.write(outDestroy, f.toString()+"/DESTROY_" + times.get(i).toString()+"_"+times.get(i+1).toString()+".shp");
        //            ShapefileWriter.write(outStable, f.toString()+"/STABLE_" + times.get(i).toString()+"_"+times.get(i+1).toString()+".shp");
        //
        //        }
        //        
        //        
        //        for(int i=0; i< times.size()-1; i++){
        //            System.out.println("-----------");
        //            System.out.println(times.get(i));
        //            System.out.println(times.get(i+1));
        //            IPopulation<IFeature>outNew = new Population<IFeature>();
        //            IPopulation<IFeature>outDestroy = new Population<IFeature>();
        //            IPopulation<IFeature>outStable = new Population<IFeature>();
        //
        //            int newE = 0, destroy = 0, stable = 0;
        //            for(STEntity e : stg.getVertices()){
        //                if(e.existsAt(times.get(i)) && !e.existsAt(times.get(i+1)) ){
        //                    IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
        //                    AttributeManager.addAttribute(f, "w_unit", 1., "Double");
        //                    AttributeManager.addAttribute(f, "w_degree", stg.getNeighborCount(e), "Double");
        //
        //                    outDestroy.add(f);
        //                    destroy++;
        //                }
        //                else if(!e.existsAt(times.get(i)) && e.existsAt(times.get(i+1)) ){
        //                    IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
        //                    AttributeManager.addAttribute(f, "w_unit", 1., "Double");
        //                    AttributeManager.addAttribute(f, "w_degree", stg.getNeighborCount(e), "Double");
        //
        //                    outNew.add(f);                 
        //                    newE++;
        //                }
        //                else if(e.existsAt(times.get(i)) && e.existsAt(times.get(i+1))){
        //                    IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
        //                    AttributeManager.addAttribute(f, "w_unit", 1., "Double");
        //                    AttributeManager.addAttribute(f, "w_degree", stg.getNeighborCount(e), "Double");
        //
        //                    outStable.add(f);               
        //                    stable++;
        //                }
        //            }
        //            System.out.println(newE+ " "+destroy+" " +stable);
        //            File f = new File("/home/bcostes/Bureau/tmp/vertices");
        //            if(!f.exists()){
        //                f.mkdir();
        //            }
        //            ShapefileWriter.write(outNew, f.toString()+"/NEW_" + times.get(i).toString()+"_"+times.get(i+1).toString()+".shp");
        //            ShapefileWriter.write(outDestroy, f.toString()+"/DESTROY_" + times.get(i).toString()+"_"+times.get(i+1).toString()+".shp");
        //            ShapefileWriter.write(outStable, f.toString()+"/STABLE_" + times.get(i).toString()+"_"+times.get(i+1).toString()+".shp");
        //
        //        }

        //         HeatMap.writeRasterInterpolation( "/home/bcostes/Bureau/tmp/edges/DESTROY_[1785.0,1790"
        //                 + ".0,1793.0,1795.0:1.0]_[1808.0,1810.0,1836.0,1839.0:1.0].shp","/home/bcostes/Bureau/tmp/edges/img/death/1785_1810_unit.tiff", null, 500, 200.);
        //         HeatMap.writeRasterInterpolation( "/home/bcostes/Bureau/tmp/edges/DESTROY_[1808.0,1810.0,1836.0,1839.0:1.0]_[1826.0,1827.0,1838.0,1839.0:1.0].shp","/home/bcostes/Bureau/tmp/edges/img/death/1810_1839_unit.tiff", null, 500, 200.);
        //         HeatMap.writeRasterInterpolation( "/home/bcostes/Bureau/tmp/edges/DESTROY_[1826.0,1827.0,1838.0,1839.0:1.0]_[1849.0,1850.0,1851.0,1852.0:1.0].shp","/home/bcostes/Bureau/tmp/edges/img/death/1839_1849_unit.tiff", null, 500, 200.);
        //         HeatMap.writeRasterInterpolation( "/home/bcostes/Bureau/tmp/edges/DESTROY_[1849.0,1850.0,1851.0,1852.0:1.0]_[1870.0,1871.0,1871.0,1872.0:1.0].shp","/home/bcostes/Bureau/tmp/edges/img/death/1849_1871_unit.tiff", null, 500, 200.);
        //
        //         HeatMap.writeRasterInterpolation( "/home/bcostes/Bureau/tmp/edges/DESTROY_[1785.0,1790"
        //                 + ".0,1793.0,1795.0:1.0]_[1808.0,1810.0,1836.0,1839.0:1.0].shp","/home/bcostes/Bureau/tmp/edges/img/death/1785_1810_length.tiff", "w_length", 500, 200.);
        //         HeatMap.writeRasterInterpolation( "/home/bcostes/Bureau/tmp/edges/DESTROY_[1808.0,1810.0,1836.0,1839.0:1.0]_[1826.0,1827.0,1838.0,1839.0:1.0].shp","/home/bcostes/Bureau/tmp/edges/img/death/1810_1839_length.tiff", "w_length", 500, 200.);
        //         HeatMap.writeRasterInterpolation( "/home/bcostes/Bureau/tmp/edges/DESTROY_[1826.0,1827.0,1838.0,1839.0:1.0]_[1849.0,1850.0,1851.0,1852.0:1.0].shp","/home/bcostes/Bureau/tmp/edges/img/death/1839_1849_length.tiff", "w_length", 500, 200.);
        //         HeatMap.writeRasterInterpolation( "/home/bcostes/Bureau/tmp/edges/DESTROY_[1849.0,1850.0,1851.0,1852.0:1.0]_[1870.0,1871.0,1871.0,1872.0:1.0].shp","/home/bcostes/Bureau/tmp/edges/img/death/1849_1871_length.tiff", "w_length", 500, 200.);
        //
        //         
        //         HeatMap.writeRasterInterpolation( "/home/bcostes/Bureau/tmp/edges/NEW_[1785.0,1790.0,1793.0,1795.0:1.0]_[1808.0,1810.0,1836.0,1839.0:1.0].shp","/home/bcostes/Bureau/tmp/edges/img/life/1785_1810_unit.tiff", null, 500, 200.);
        //         HeatMap.writeRasterInterpolation( "/home/bcostes/Bureau/tmp/edges/NEW_[1808.0,1810.0,1836.0,1839.0:1.0]_[1826.0,1827.0,1838.0,1839.0:1.0].shp","/home/bcostes/Bureau/tmp/edges/img/life/1810_1839_unit.tiff", null, 500, 200.);
        //         HeatMap.writeRasterInterpolation( "/home/bcostes/Bureau/tmp/edges/NEW_[1826.0,1827.0,1838.0,1839.0:1.0]_[1849.0,1850.0,1851.0,1852.0:1.0].shp","/home/bcostes/Bureau/tmp/edges/img/life/1839_1849_unit.tiff", null, 500, 200.);
        //         HeatMap.writeRasterInterpolation( "/home/bcostes/Bureau/tmp/edges/NEW_[1849.0,1850.0,1851.0,1852.0:1.0]_[1870.0,1871.0,1871.0,1872.0:1.0].shp","/home/bcostes/Bureau/tmp/edges/img/life/1849_1871_unit.tiff", null, 500, 200.);
        //
        //         HeatMap.writeRasterInterpolation( "/home/bcostes/Bureau/tmp/edges/NEW_[1785.0,1790.0,1793.0,1795.0:1.0]_[1808.0,1810.0,1836.0,1839.0:1.0].shp","/home/bcostes/Bureau/tmp/edges/img/life/1785_1810_length.tiff", "w_length", 500, 200.);
        //         HeatMap.writeRasterInterpolation( "/home/bcostes/Bureau/tmp/edges/NEW_[1808.0,1810.0,1836.0,1839.0:1.0]_[1826.0,1827.0,1838.0,1839.0:1.0].shp","/home/bcostes/Bureau/tmp/edges/img/life/1810_1839_length.tiff", "w_length", 500, 200.);
        //         HeatMap.writeRasterInterpolation( "/home/bcostes/Bureau/tmp/edges/NEW_[1826.0,1827.0,1838.0,1839.0:1.0]_[1849.0,1850.0,1851.0,1852.0:1.0].shp","/home/bcostes/Bureau/tmp/edges/img/life/1839_1849_length.tiff", "w_length", 500, 200.);
        //         HeatMap.writeRasterInterpolation( "/home/bcostes/Bureau/tmp/edges/NEW_[1849.0,1850.0,1851.0,1852.0:1.0]_[1870.0,1871.0,1871.0,1872.0:1.0].shp","/home/bcostes/Bureau/tmp/edges/img/life/1849_1871_length.tiff", "w_length", 500, 200.);
        //

        //        HeatMap.writeRasterInterpolation("/home/bcostes/Bureau/tmp/betweenness.shp", "/home/bcostes/Bureau/tmp/bet.tif", "betweennes", 1500,50);
        //        HeatMap.writeRasterInterpolation("/home/bcostes/Bureau/tmp/betweenness.shp", "/home/bcostes/Bureau/tmp/bet2.tif", "betweennes", 1500,100);
        //        HeatMap.writeRasterInterpolation("/home/bcostes/Bureau/tmp/betweenness.shp", "/home/bcostes/Bureau/tmp/bet3.tif", "betweennes", 1500,150);
        //        HeatMap.writeRasterInterpolation("/home/bcostes/Bureau/tmp/betweenness.shp", "/home/bcostes/Bureau/tmp/bet4.tif", "betweennes", 1500,200);


        Iterator writers = ImageIO.getImageWritersByFormatName("png");
        ImageWriter writer = (ImageWriter)writers.next();


        File f = new File("/home/bcostes/Bureau/tmp/test.png");
        ImageOutputStream ios;
        try {
            ios = ImageIO.createImageOutputStream(f);
            writer.setOutput(ios);
            BufferedImage bi = new BufferedImage(254, 254, BufferedImage.TYPE_INT_ARGB);
            for(int h=0;h<254; h++){
                for(int w=0;w<254; w++){
                    Color rgba = new Color(h,w,0,80);
                    bi.setRGB(w, h, rgba.getRGB());
                }
            }
            writer.write(bi);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }
}
