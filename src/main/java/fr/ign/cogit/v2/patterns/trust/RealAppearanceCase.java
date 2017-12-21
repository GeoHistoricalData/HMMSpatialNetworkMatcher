package fr.ign.cogit.v2.patterns.trust;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.patterns.AppearanceSTPattern;
import fr.ign.cogit.v2.patterns.STPattern.PATTERN_TYPE;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;

public class RealAppearanceCase extends InconsistencyCriteria{
    
    private final double buffer = 300;

    @Override
    public void evaluate(STGraph stgraph, STEntity edge, PATTERN_TYPE type) {
        if(type != PATTERN_TYPE.APPEARANCE){
            return;
        }
        //date de destruction
        Map<Integer, FuzzyTemporalInterval> indexes = new HashMap<Integer, FuzzyTemporalInterval>();
        List<FuzzyTemporalInterval> l = new ArrayList<FuzzyTemporalInterval>();
        l.addAll(edge.getTimeSerie().getValues().keySet());
        l.remove(null);
        Collections.sort(l);
        String sequence = "";
        for (int i=0; i< l.size(); i++) {
            sequence += (edge.getTimeSerie().getValues().get(l.get(i)) ? "1" : "0");
            indexes.put(i, l.get(i));
        }
        int indexC = sequence.indexOf("01")+1;
        int indexD =sequence.indexOf("10")+1;
        FuzzyTemporalInterval tconstruction = indexes.get(indexC);
        FuzzyTemporalInterval tdestruction = indexes.get(indexD);
        // proportion d'arcs construits
        List<STEntity> edgesAtTConstruction = stgraph.getEdgesAt(tconstruction);
        double edgesCreation = 0;
        double edgesClose =0.;
        IGeometry edgeGeom = edge.getGeometry().toGeoxGeometry();
        for(STEntity e: edgesAtTConstruction){
            if(e.getGeometry().toGeoxGeometry().distance(edgeGeom)< this.buffer){
                edgesClose += 1.;
                if(!e.existsAt(indexes.get(indexC -1))){
                    edgesCreation += 1.;
                }
            }
        }
        edgesCreation /= edgesClose;
        // proportion d'arcs détruits
        List<STEntity> edgesAtTDestruction = stgraph.getEdgesAt(tdestruction);
        double edgesDestruction = 0;
        double edgesClose2 =0.;
        for(STEntity e: edgesAtTDestruction){
            if(e.getGeometry().toGeoxGeometry().distance(edgeGeom)< this.buffer){
                edgesClose2 += 1.;
                if(!e.existsAt(indexes.get(indexD-1))){
                    edgesDestruction += 1.;
                }
            }
        }
        edgesDestruction /= edgesClose2;
        this.trust = edgesCreation * edgesDestruction;        
    }

    public static void main(String[] args) {
        STGraph stg = TAGIoManager.deserialize("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v5/tag.tag");
        AppearanceSTPattern app = new AppearanceSTPattern();
        for(STEntity e : stg.getEdges()){
            if(app.find(e.getTimeSerie())){
                RealAppearanceCase criteria = new RealAppearanceCase();
                criteria.evaluate(stg, e, PATTERN_TYPE.APPEARANCE);
            }
        }

    }

}
