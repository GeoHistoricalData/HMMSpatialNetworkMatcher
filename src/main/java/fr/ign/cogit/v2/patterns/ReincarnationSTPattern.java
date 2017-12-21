package fr.ign.cogit.v2.patterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STProperty;

public class ReincarnationSTPattern extends UnstableSTPattern {
  public ReincarnationSTPattern() {
    this.pattern = Pattern.compile("10+1");
    this.type = STPattern.PATTERN_TYPE.REINCARNATION;
  }

  @Override
  public List<FuzzyTemporalInterval> findEvent(STProperty<Boolean> ts) {
    if (!this.find(ts)) {
      return null;
    }
    List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>();
    Pattern p = Pattern.compile("10+1");
    Map<Integer, FuzzyTemporalInterval> indexes = new HashMap<Integer, FuzzyTemporalInterval>();
    Matcher m = p.matcher(this.getSequence(ts, indexes));
    int regex = 0;
    while (m.find(regex)) {
      regex = m.start() + 1;
      times.add(indexes.get(regex));
      regex = m.end() - 1;
      times.add(indexes.get(regex));
    }

    return times;
  }
}
