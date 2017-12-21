package fr.ign.cogit.v2.patterns;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STProperty;

public class DeathSTPattern extends StableSTPattern {
  public DeathSTPattern() {
    this.pattern = Pattern.compile("^1+0+$");
    this.type = STPattern.PATTERN_TYPE.DEATH;

  }

  @Override
  public FuzzyTemporalInterval findEvent(STProperty<Boolean> ts) {
    if (!this.find(ts)) {
      return null;
    }
    Pattern plife = Pattern.compile("0+$");
    Map<Integer, FuzzyTemporalInterval> indexes = new HashMap<Integer, FuzzyTemporalInterval>();
    Matcher m = plife.matcher(this.getSequence(ts, indexes));
    m.find();
    int regex = m.start();
    return indexes.get(regex);
  }
}
