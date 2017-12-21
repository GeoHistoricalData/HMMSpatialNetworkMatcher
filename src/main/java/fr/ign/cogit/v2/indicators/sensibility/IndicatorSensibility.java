package fr.ign.cogit.v2.indicators.sensibility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class IndicatorSensibility {

    public static Map<IFeature, Double> relative(Map<IFeature, double[]> values ){
        Map<IFeature, Double> result = new HashMap<IFeature, Double>();

        for(IFeature f: values.keySet()){
            double c1 = values.get(f)[0];
            double c2 = values.get(f)[1];
            if(c2 == 0){
                if(c1>0){
                    result.put(f, Double.POSITIVE_INFINITY);
                }
                else if (c1<0){
                    result.put(f, Double.NEGATIVE_INFINITY);
                }
                else{
                    result.put(f, 0.);
                }
            }
            double c = 100.*(c1 -c2)/c2;

            result.put(f, c);
        }
        return result;
    }

    public static Map<IFeature, Double> absolute(Map<IFeature, double[]> values ){
        Map<IFeature, Double> result = new HashMap<IFeature, Double>();
        for(IFeature f: values.keySet()){
            double c1 = values.get(f)[0];
            double c2 = values.get(f)[1];
            double c = c1 -c2;
            result.put(f, c);
        }
        return result;
    }

    public static void main(String[] args) {
        //shp le plus détaillé
        String shp1 ="/home/bcostes/Bureau/bdtopo/ind/ROUTE_grph_sansbois_simpli.shp";
        // String shp2 ="/home/bcostes/Bureau/bdtopo/ind/ROUTE_grph_sansbois.shp";
        String shp2 ="/home/bcostes/Bureau/subsetBetw.shp";

        String centrality ="betw";
        IPopulation<IFeature> pop1 = ShapefileReader.read(shp1);
        pop1.initSpatialIndex(Tiling.class, false);
        IPopulation<IFeature> pop2 = ShapefileReader.read(shp2);
        pop2.initSpatialIndex(Tiling.class, false);


        System.out.println(pop1.size()+" "+ pop2.size());
        Map<IFeature, double[]> values = new HashMap<IFeature, double[]>();
        for(IFeature f: pop1){
            Collection<IFeature> candidates = pop2.select(f.getGeom(),0.);
            if(!candidates.isEmpty()){
                for(IFeature c: candidates){
                    if(c.getGeom().equals(f.getGeom())){
                        double centrality1 = Double.parseDouble(f.getAttribute(f.getFeatureType()
                                .getFeatureAttributeByName(centrality)).toString());
                        double centrality2 = Double.parseDouble(c.getAttribute(f.getFeatureType()
                                .getFeatureAttributeByName(centrality)).toString());

                        double[] d = {centrality1, centrality2};
                        values.put(f, d);                               
                    }

                }
            }
        }

        Map<IFeature, Double> mapAbsolute = IndicatorSensibility.absolute(values);
        Map<IFeature, Double> mapRelative = IndicatorSensibility.relative(values);

        IPopulation<IFeature> out = new Population<IFeature>();
        String s ="id;c1;c2;absolute;relative\n";
        for(IFeature f: mapAbsolute.keySet()){
            IFeature newf = new DefaultFeature(f.getGeom());
            int id = Integer.parseInt(f.getAttribute("ID").toString());
            AttributeManager.addAttribute(newf, "ID", id, "Integer");
            AttributeManager.addAttribute(newf, "c1", values.get(f)[0], "Double");
            AttributeManager.addAttribute(newf, "c2", values.get(f)[1], "Double");
            AttributeManager.addAttribute(newf, "absolute", mapAbsolute.get(f), "Double");
            AttributeManager.addAttribute(newf, "relative", mapRelative.get(f), "Double");
            s+=id +";"+values.get(f)[0]+";"+values.get(f)[1]+";"+mapAbsolute.get(f)+";"+mapRelative.get(f)+"\n";
            out.add(newf);
        }

        ShapefileWriter.write(out, "/home/bcostes/Bureau/test_betw.shp");

        try {
            FileWriter fr = new FileWriter("/home/bcostes/Bureau/test_betw.csv");
            BufferedWriter br = new BufferedWriter(fr);
            br.write(s);
            br.flush();
            br.close();
            fr.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }




        //        Map<Double, Integer> mapReverse1 = new HashMap<Double, Integer>();
        //        Map<Integer, Double> mapNotReverse2 = new HashMap<Integer, Double>();
        //        for(IFeature f: values.keySet()){
        //            int id = Integer.parseInt(f.getAttribute("ID").toString());
        //            double c1 = values.get(f)[0];
        //            double c2 = values.get(f)[1];
        //            mapReverse1.put(c1, id);
        //            mapNotReverse2.put(id, c2);
        //        }
        //
        //
        //        List<Double> c1 = new ArrayList<Double>(mapReverse1.keySet());
        //        List<Double> c2 = new ArrayList<Double>(mapNotReverse2.values());
        //        Collections.sort(c1,Collections.reverseOrder());
        //        Collections.sort(c2, Collections.reverseOrder());
        //        List<Double> c1Bis = new ArrayList<Double>(mapReverse1.keySet());
        //        List<Double> c2Bis = new ArrayList<Double>(mapNotReverse2.values());
        //        Collections.sort(c1Bis);
        //        Collections.sort(c2Bis);
        //        s="edges_percentage;more_central;less_central\n";
        //        int cpt=0;
        //        int thresold = c1.size()/100;
        //        for(int i=thresold; i< c1.size(); i+=thresold){
        //            int nbMore=0;
        //            int nbLess=0;
        //            cpt++;
        //            double taille = (double) cpt * thresold;
        //            if(cpt==100){
        //                taille = c1.size();
        //            }
        //            for( int j=0;j<i;j++){
        //                double c = c1.get(j);
        //                int id =  mapReverse1.get(c);
        //                int index = c2.indexOf(mapNotReverse2.get(id));
        //                if(index < taille){
        //                    nbMore++;
        //                }
        //                c = c1Bis.get(j);
        //                id =  mapReverse1.get(c);
        //                index = c2Bis.indexOf(mapNotReverse2.get(id));
        //                if(index < taille){
        //                    nbLess++;
        //                }
        //            }
        //            double pMore = ((double)nbMore)/(taille);
        //            double pLess = ((double)nbLess)/(taille);
        //            s+=cpt+";"+pMore+";"+pLess+"\n";
        //        }
        //        try {
        //            FileWriter fr = new FileWriter("/home/bcostes/Bureau/betw_courbes.csv");
        //            BufferedWriter br = new BufferedWriter(fr);
        //            br.write(s);
        //            br.flush();
        //            br.close();
        //            fr.close();
        //        } catch (IOException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }


        Map<Double, Integer> mapReverse1 = new HashMap<Double, Integer>();
        Map<Integer, Double> mapNotReverse2 = new HashMap<Integer, Double>();
        for(IFeature f: values.keySet()){
            int id = Integer.parseInt(f.getAttribute("ID").toString());
            double c1 = values.get(f)[0];
            double c2 = values.get(f)[1];
            mapReverse1.put(c1, id);
            mapNotReverse2.put(id, c2);
        }


        List<Double> c1 = new ArrayList<Double>(mapReverse1.keySet());
        List<Double> c2 = new ArrayList<Double>(mapNotReverse2.values());
        Collections.sort(c1,Collections.reverseOrder());
        Collections.sort(c2, Collections.reverseOrder());

        int cpt=0;
        int thresold = c1.size()/100;

        IPopulation<IFeature> variation = new Population<IFeature>();
        for( int j=0;j<thresold * 10;j++){
            double c = c1.get(j);
            int id =  mapReverse1.get(c);
            int index = c2.indexOf(mapNotReverse2.get(id));
            if(index > thresold * 10){
                IFeature f =null;
                for(IFeature ff: values.keySet()){
                    int id2 = Integer.parseInt(ff.getAttribute("ID").toString());
                    if(id2 == id){
                        f = ff;
                        break;
                    }
                }
                IFeature newf = new DefaultFeature(f.getGeom());
                AttributeManager.addAttribute(newf, "ID", id, "Integer");
                AttributeManager.addAttribute(newf, "c1", values.get(f)[0], "Double");
                AttributeManager.addAttribute(newf, "c2", values.get(f)[1], "Double");
                AttributeManager.addAttribute(newf, "absolute", mapAbsolute.get(f), "Double");
                AttributeManager.addAttribute(newf, "relative", mapRelative.get(f), "Double");
                variation.add(newf);
            }

        }
        ShapefileWriter.write(variation, "/home/bcostes/Bureau/variation.shp");





    }

}
