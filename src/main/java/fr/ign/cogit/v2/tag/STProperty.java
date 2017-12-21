package fr.ign.cogit.v2.tag;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Propriété temporellement-dépendante d'un STEntity
 * @author bcostes
 *
 */
public class STProperty<T> implements Serializable {
    
    public static enum PROPERTIE_FINAL_TYPES{
        TimeSerie, Weight, Indicator, Geometry, Attribute, Transformation;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(STProperty.class);
    /**
     * Nom de la propriété
     */
    protected String name;
    /**
     * Valeur de la propriété pour chaque date
     */
    protected Map<FuzzyTemporalInterval, T> values;
    
    private void initProperty(PROPERTIE_FINAL_TYPES propertyType, String name){
        switch(propertyType){
        case TimeSerie:
            this.name = "TimeSerie";
            break;
        case Weight:
            this.name = "Weight";
            break;
        case Geometry:
            this.name = "Geometry";
            break;
        case Transformation:
          this.name = "Transformation";
          break;
        case Indicator:
            if(name == null || name.equals("")){
                if(logger.isInfoEnabled()){
                    logger.info("STProperty name missing");
                }
            }
            this.name = name;
            break;
        case Attribute:
            if(name == null || name.equals("")){
                if(logger.isInfoEnabled()){
                    logger.info("STProperty name missing");
                }
            }
            this.name = name;
            break;
        }
    }

    public STProperty(PROPERTIE_FINAL_TYPES propertyType, String name) {
        initProperty(propertyType, name);
        this.values = new HashMap<FuzzyTemporalInterval, T>();
    }


    public STProperty(PROPERTIE_FINAL_TYPES propertyType,String name, List<FuzzyTemporalInterval> times) {
        initProperty(propertyType, name);
        this.values = new HashMap<FuzzyTemporalInterval, T>();
        for (FuzzyTemporalInterval t : times) {
            this.values.put(t, null);
        }
    }


    public STProperty(STProperty<T> tt) {
        this.name = new String(tt.name);
        this.values = new HashMap<FuzzyTemporalInterval, T>();
        for (FuzzyTemporalInterval t : tt.getValues().keySet()) {
            this.values.put(t, tt.getValueAt(t));
        }
    }

    public STProperty<T> copy() {
        STProperty<T> s = new  STProperty<T>(this);
        Map<FuzzyTemporalInterval, T> ts = new HashMap<FuzzyTemporalInterval, T>();
        for (FuzzyTemporalInterval t : this.values.keySet()) {
            ts.put(t, this.values.get(t));
        }
        s.setValues(ts);
        return s;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setValues(Map<FuzzyTemporalInterval, T> values) {
        this.values = values;
    }

    public Map<FuzzyTemporalInterval, T> getValues() {
        return values;
    }

    public T getValueAt(FuzzyTemporalInterval t) {
        if (this.values.containsKey(t)) {
            return this.values.get(t);
        }
        return null;
    }

    public void setValueAt(FuzzyTemporalInterval t, T value) {
        this.values.put(t, value);
    }

    @Override
    public String toString(){
        String s = "PROPERTY NAME : "+ this.getName()+"\n" + this.values.toString();
        return s;
    }


    public void updateFuzzyTemporalInterval(FuzzyTemporalInterval told, FuzzyTemporalInterval tnew) {
        this.values.put(tnew, this.values.get(told));
        this.values.remove(told);

    }





}
