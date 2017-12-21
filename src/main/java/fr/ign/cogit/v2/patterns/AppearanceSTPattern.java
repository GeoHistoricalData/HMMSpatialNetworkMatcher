package fr.ign.cogit.v2.patterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STProperty;

public class AppearanceSTPattern extends UnstableSTPattern {

  public AppearanceSTPattern() {
    this.pattern = Pattern.compile("^0+1+0+$");
    this.type = STPattern.PATTERN_TYPE.APPEARANCE;
  }

  @Override
  public List<FuzzyTemporalInterval> findEvent(STProperty<Boolean> ts) {
    if (!this.find(ts)) {
      return null;
    }
    List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>();
    Pattern p = Pattern.compile("1+");
    Map<Integer, FuzzyTemporalInterval> indexes = new HashMap<Integer, FuzzyTemporalInterval>();
    Matcher m = p.matcher(this.getSequence(ts, indexes));
    m.find();
    int regex = m.start();
    times.add(indexes.get(regex));
    regex = m.end();
    times.add(indexes.get(regex));
    return times;
  }
}
