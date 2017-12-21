package fr.ign.cogit.v2.patterns;

import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STProperty;

public abstract class StableSTPattern extends STPattern {

  public abstract FuzzyTemporalInterval findEvent(STProperty<Boolean> ts);
}
