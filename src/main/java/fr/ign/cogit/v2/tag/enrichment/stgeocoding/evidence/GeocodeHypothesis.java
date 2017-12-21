package fr.ign.cogit.v2.tag.enrichment.stgeocoding.evidence;

import fr.ign.cogit.geoxygene.matching.dst.evidence.Hypothesis;
import fr.ign.cogit.v2.tag.enrichment.stgeocoding.GeocodedCandidate;

public class GeocodeHypothesis implements Hypothesis {
  public GeocodedCandidate getDecoratedFeature() {
    return decoratedFeature;
  }
  public void setDecoratedFeature(GeocodedCandidate decoratedFeature) {
    this.decoratedFeature = decoratedFeature;
  }
  /**
   * The decorated feature.
   */
  private GeocodedCandidate decoratedFeature;

  /**
   * Constructor with a decorated feature.
   * @param feature the decorated feature.
   */
  public GeocodeHypothesis(GeocodedCandidate feature) {
    this.decoratedFeature = feature;
  }
  /**
   * Default contructor.
   */
  public GeocodeHypothesis() {
    this.decoratedFeature = null;
  }
  @Override
  public String toString() {
    return this.decoratedFeature.toString();
  }
  @Override
  public boolean equals(Object o) {
    if (!GeocodeHypothesis.class.isAssignableFrom(o.getClass())) {
      return false;
    }
    GeocodeHypothesis h = (GeocodeHypothesis) o;
    return this.decoratedFeature.equals(h.decoratedFeature);
  }
  
}
