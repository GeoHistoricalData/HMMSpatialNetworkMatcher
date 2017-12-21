package fr.ign.cogit.v2.patterns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STProperty;

public class STPattern {

  protected Pattern pattern;
  
  public static enum PATTERN_TYPE{
      APPEARANCE, REINCARNATION, LIFE, DEATH, CONTINUITY;
  }
  
  
 protected STPattern.PATTERN_TYPE type;

  public STPattern.PATTERN_TYPE getType() {
    return type;
}

public void setType(STPattern.PATTERN_TYPE type) {
    this.type = type;
}

public Pattern getPattern() {
    return Pattern.compile(this.pattern.pattern());
  }

  public String getRegex() {
    return new String(this.pattern.pattern());
  }

  protected boolean find(String sequence) {
    Matcher m = this.pattern.matcher(sequence);
    return m.find();
  }

  public boolean find(STProperty<Boolean>  ts) {
    return this.find(this.getSequence(ts));
  }
  
  public boolean find(STProperty<Boolean>  ts,  Map<Integer, FuzzyTemporalInterval> indexes) {
      if(indexes == null){
          indexes = new HashMap<Integer, FuzzyTemporalInterval>();
      }
      List<FuzzyTemporalInterval> l = new ArrayList<FuzzyTemporalInterval>();
      l.addAll(ts.getValues().keySet());
      l.remove(null);
      Collections.sort(l);
      String sequence = "";
      for (int i=0; i< l.size(); i++) {
        sequence += (ts.getValues().get(l.get(i)) ? "1" : "0");
        indexes.put(i, l.get(i));
      }
      return this.find(sequence);
    }
  
  public String getSequence(STProperty<Boolean>  ts, Map<Integer, FuzzyTemporalInterval> indexes){
      if(indexes == null){
          indexes = new HashMap<Integer, FuzzyTemporalInterval>();
      }
      List<FuzzyTemporalInterval> l = new ArrayList<FuzzyTemporalInterval>();
      l.addAll(ts.getValues().keySet());
      l.remove(null);
      Collections.sort(l);
      String sequence = "";
      for (int i=0; i< l.size(); i++) {
        sequence += (ts.getValues().get(l.get(i)) ? "1" : "0");
        indexes.put(i, l.get(i));
      }
      return sequence;
  }
  
  public String getSequence(STProperty<Boolean>  ts){
      List<FuzzyTemporalInterval> l = new ArrayList<FuzzyTemporalInterval>();
      l.addAll(ts.getValues().keySet());
      l.remove(null);
      Collections.sort(l);
      String sequence = "";
      for (int i=0; i< l.size(); i++) {
        sequence += (ts.getValues().get(l.get(i)) ? "1" : "0");
      }
      return sequence;
  }
}
