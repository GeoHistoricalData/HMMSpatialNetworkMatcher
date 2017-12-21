package fr.ign.cogit.v2.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STProperty;

public class TimeUtils {
    
    /**
     * Trie d'un time série par FuzzyTemporalInterval croissants
     * @param ts
     * @return
     */
    public static STProperty<Boolean> sort(STProperty<Boolean> ts){
        Map<FuzzyTemporalInterval, Boolean> map = new HashMap<FuzzyTemporalInterval, Boolean>();
        List<FuzzyTemporalInterval> times =  new ArrayList<FuzzyTemporalInterval>(ts.getValues().keySet());
        Collections.sort(times);
        for(FuzzyTemporalInterval t :times){
            map.put(t, ts.getValueAt(t));
        }
        STProperty<Boolean> result = new STProperty<Boolean>(STProperty.PROPERTIE_FINAL_TYPES.TimeSerie, null);
        result.setValues(map);
        return result;
    }

}
