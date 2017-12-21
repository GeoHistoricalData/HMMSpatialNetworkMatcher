package fr.ign.cogit.v2.patterns.trust;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;

public class Classification {

    public static void main(String args []){

        String file ="/home/bcostes/Bureau/acp.txt";
        String tagfile ="/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v5/tag.tag";

        STGraph stg = TAGIoManager.deserialize(tagfile);


        FileReader fr;
        try {
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line ="";
            Map<Integer, Integer> data = new HashMap<Integer, Integer>();
            Map<Integer, List<Double>> values = new HashMap<Integer, List<Double>>();

            while((line = br.readLine()) !=null){
                StringTokenizer tokenizer = new StringTokenizer(line, ";");
                int cpt=0;
                int id=0;
                int classe =0;
                List<Double> coord = new ArrayList<Double>();
                while(tokenizer.hasMoreTokens()){
                    if(cpt == 0){
                        id =  Integer.parseInt(tokenizer.nextToken());
                    }
                    else if(cpt >=6 ){
                        classe =  Integer.parseInt(tokenizer.nextToken());
                    }
                    else{
                        coord.add(Double.parseDouble(tokenizer.nextToken()));
                    }
                    cpt++;

                }
                data.put(id, classe);
                values.put(id, coord);
            }
            
            IPopulation<IFeature> out = new Population<IFeature>();
            for(STEntity e: stg.getEdges()){
                if(data.containsKey(e.getId())){
                    IFeature f =new DefaultFeature(e.getGeometry().toGeoxGeometry());
                    AttributeManager.addAttribute(f, "classe", data.get(e.getId()), "Integer");
                    int cpt=1;
                    for(Double d: values.get(e.getId())){
                        AttributeManager.addAttribute(f, "c"+cpt, d, "Double");
                        cpt++;
                    }
                    out.add(f);
                }
            }
            ShapefileWriter.write(out, "/home/bcostes/Bureau/test_acp.shp");

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
