package fr.ign.cogit.v2.indicators.dynamics;

import java.util.Map;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;

public class VoronoiTest {

    public static void main(String[] args) {

        STGraph stg = TAGIoManager.deserialize("/home/bcostes/Bureau/TAG/etape4/tag_new.tag");

        for(int i=0; i< stg.getTemporalDomain().asList().size()-2; i++){
            IPopulation<IFeature>out = new Population<IFeature>();
            FuzzyTemporalInterval t1 = stg.getTemporalDomain().asList().get(i);
            FuzzyTemporalInterval t2 = stg.getTemporalDomain().asList().get(i+1);
            Map<GraphEntity, Double> pastTransfo = PastDestruction.calculateNodeIndicator(stg, t1, t2, false);
            for(GraphEntity n : pastTransfo.keySet()){
                IFeature f = new DefaultFeature(n.getGeometry().toGeoxGeometry());
                AttributeManager.addAttribute(f, "pastTransfo", pastTransfo.get(n), "Double");
                out.add(f);
            }
            ShapefileWriter.write(out, "/home/bcostes/Bureau/tmp/dynamics/destruction/" + t1.toString()+"_"+ t2.toString() + ".shp");
           // break;
        }

    }

}
